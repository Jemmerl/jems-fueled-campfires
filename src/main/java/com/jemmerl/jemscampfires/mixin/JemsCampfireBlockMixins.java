package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;

@Mixin(value = CampfireBlock.class, priority = 0)
public abstract class JemsCampfireBlockMixins extends BaseEntityBlock {
    protected JemsCampfireBlockMixins(Properties builder) {
        super(builder);
    }
    
    @Shadow
    private boolean spawnParticles;

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/world/level/block/entity.CampfireBlockEntity.dowse()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD,
            method = "dowse(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V")
    private static void extinguishDropsAndEternal(Entity arg0, LevelAccessor pLevel, BlockPos pPos, BlockState pState, CallbackInfo ci, BlockEntity blockentity) {
        if (!pLevel.isClientSide()) {
            ((IFueledCampfire)blockentity).doExtinguishDrops();
        }
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        if(!worldIn.isClientSide()) {
            IFueledCampfire cfTileEntity = Util.getCFTE(worldIn, pos);
            if (cfTileEntity != null) {
                // Override world-genned eternal status when placed by a player
                cfTileEntity.setEternal(ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString().contains("soul") ?
                        ServerConfig.PLACE_SOUL_CAMPFIRE_ETERNAL.get() : ServerConfig.PLACE_CAMPFIRE_ETERNAL.get());
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;", cancellable = true)
    private void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(cir.getReturnValue()
                .setValue(BlockStateProperties.LIT, (ForgeRegistries.BLOCKS.getKey(this.asBlock()).toString().contains("soul")) ?
                        ServerConfig.PLACE_SOUL_CAMPFIRE_LIT.get() : ServerConfig.PLACE_CAMPFIRE_LIT.get()));
    }

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/world/entity/player/Player.getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true,
                method = "use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;")
    public void use(BlockState arg0, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult arg5, CallbackInfoReturnable<InteractionResult> cir, BlockEntity blockentity, CampfireBlockEntity campfireblockentity, ItemStack itemstack) {
        if (!ServerConfig.NEED_FIRE_POKER.get() && pPlayer.isCrouching() && itemstack.isEmpty() && (campfireblockentity instanceof IFueledCampfire)) {
            Util.dispatchCampfireInfo(pLevel, pPos, arg0, pPlayer, (IFueledCampfire)campfireblockentity);
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
