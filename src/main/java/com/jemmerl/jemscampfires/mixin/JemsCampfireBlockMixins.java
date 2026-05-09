package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = CampfireBlock.class, priority = 0)
public abstract class JemsCampfireBlockMixins extends BaseEntityBlock {
    protected JemsCampfireBlockMixins(Properties builder) {
        super(builder);
    }
    
    @Final
    @Shadow
    private boolean spawnParticles;

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/world/level/block/entity.CampfireBlockEntity.dowse()V", shift = At.Shift.AFTER),
            method = "dowse(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V")
    private static void extinguishDropsAndEternal(Entity arg0, LevelAccessor pLevel, BlockPos pPos, BlockState pState, CallbackInfo ci, @Local BlockEntity blockentity) {
        if (!pLevel.isClientSide()) {
            ((IFueledCampfire)blockentity).jems_fueled_campfires$doExtinguishDrops();
        }
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        if(!worldIn.isClientSide()) {
            IFueledCampfire cfTileEntity = Util.getCFTE(worldIn, pos);
            if (cfTileEntity != null) {
                // Override world-genned eternal status when placed by a player
//                cfTileEntity.jems_fueled_campfires$setEternal(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().contains("soul") ?
//                        ServerConfig.PLACE_SOUL_CAMPFIRE_ETERNAL.get() : ServerConfig.PLACE_CAMPFIRE_ETERNAL.get());
                cfTileEntity.jems_fueled_campfires$setPlayerPlaced();
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;", cancellable = true)
    private void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(cir.getReturnValue()
                .setValue(BlockStateProperties.LIT, (BuiltInRegistries.BLOCK.getKey(this.asBlock()).toString().contains("soul")) ?
                        ServerConfig.PLACE_SOUL_CAMPFIRE_LIT.get() : ServerConfig.PLACE_CAMPFIRE_LIT.get()));
    }

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/world/entity/player/Player.getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;",
            shift = At.Shift.AFTER), cancellable = true, method = "useItemOn")
    public void use(ItemStack arg0, BlockState arg1, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult arg6, CallbackInfoReturnable<ItemInteractionResult> cir, @Local CampfireBlockEntity campfireblockentity, @Local(ordinal = 1) ItemStack itemstack) {
        if (!ServerConfig.NEED_FIRE_POKER.get() && player.isCrouching() && itemstack.isEmpty() && (campfireblockentity instanceof IFueledCampfire)) {
            Util.dispatchCampfireInfo(level, pos, arg1, player, (IFueledCampfire)campfireblockentity);
            cir.setReturnValue(ItemInteractionResult.SUCCESS);
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (!state.getValue(BlockStateProperties.LIT) || !ServerConfig.FUEL_BASED_LIGHTING.get()) return super.getLightEmission(state, level, pos);

        // I am not entirely sure if this is needed, because the light source isn't position dependant, but it can't hurt to keep.
        if (pos == BlockPos.ZERO) return 1;

        IFueledCampfire cfTileEntity = Util.getCFTE(level, pos);
        if ((cfTileEntity == null) || (cfTileEntity.jems_fueled_campfires$getEternal() && !ServerConfig.FUEL_BASED_LIGHTING_ETERNAL.get())) return super.getLightEmission(state, level, pos);
        int light = cfTileEntity.jems_fueled_campfires$getFuelLightLevel();

        // Note: Thought this was helping the initial lighting flicker, but doesn't seem to be true.
        //  Also, it completely fries Starlight.
//        cfTileEntity.updateLighting();
        return (light < 1) ? super.getLightEmission(state, level, pos) : light;
    }
}
