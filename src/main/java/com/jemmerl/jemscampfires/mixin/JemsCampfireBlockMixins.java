package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;

@Mixin(value = CampfireBlock.class, priority = 0)
public abstract class JemsCampfireBlockMixins extends ContainerBlock {
    protected JemsCampfireBlockMixins(Properties builder) {
        super(builder);
    }

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/tileentity/CampfireTileEntity.dropAllItems()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD,
            method = "extinguish(Lnet/minecraft/world/IWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V")
    private static void extinguishEternal(IWorld world, BlockPos pos, BlockState state, CallbackInfo ci, TileEntity tileentity) {
        if (!world.isRemote()) {
            ((IFueledCampfire)tileentity).doExtinguished();
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if(!worldIn.isRemote()) {
            IFueledCampfire cfTileEntity = Util.getCFTE(worldIn, pos);
            if (cfTileEntity != null) {
                boolean isSoul = (state.getBlock().getRegistryName().toString().contains("soul"));
                boolean isEternalNow = isSoul ?
                        ServerConfig.PLACE_SOUL_CAMPFIRE_ETERNAL.get() : ServerConfig.PLACE_CAMPFIRE_ETERNAL.get();

                // Override world-genned eternal status when placed by a player
                cfTileEntity.setEternal(isEternalNow);

                // Runs only if placed lit
                if (isSoul ? ServerConfig.PLACE_SOUL_CAMPFIRE_LIT.get() : ServerConfig.PLACE_CAMPFIRE_LIT.get()) {
                    if (!ServerConfig.FUEL_BASED_LIGHTING.get() || (isEternalNow && !ServerConfig.FUEL_BASED_LIGHTING_ETERNAL.get())) {
                        cfTileEntity.updateLighting();
                        return;
                    }
                    cfTileEntity.dynamicLightLevelUpdate();
                }
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "getStateForPlacement(Lnet/minecraft/item/BlockItemUseContext;)Lnet/minecraft/block/BlockState;", cancellable = true)
    private void getStateForPlacement(BlockItemUseContext context, CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(cir.getReturnValue()
                .with(CampfireBlock.LIT, ((this.getBlock().getRegistryName().toString().contains("soul")) ?
                        ServerConfig.PLACE_SOUL_CAMPFIRE_LIT.get() : ServerConfig.PLACE_CAMPFIRE_LIT.get())));
    }

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/entity/player/PlayerEntity.getHeldItem(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true,
                method = "onBlockActivated(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/math/BlockRayTraceResult;)Lnet/minecraft/util/ActionResultType;")
    public void onBlockActivated(BlockState arg0, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult arg5, CallbackInfoReturnable<ActionResultType> cir, TileEntity tileentity, CampfireTileEntity campfiretileentity, ItemStack itemstack) {
        if (!ServerConfig.NEED_FIRE_POKER.get() && player.isCrouching() && itemstack.isEmpty() && (campfiretileentity instanceof IFueledCampfire)) {
            Util.dispatchCampfireInfo(worldIn, pos, arg0, player, (IFueledCampfire)campfiretileentity);
            cir.setReturnValue(ActionResultType.SUCCESS);
        }
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader level, BlockPos pos) {
        if (!state.get(BlockStateProperties.LIT) || !ServerConfig.FUEL_BASED_LIGHTING.get())
            return super.getLightValue(state, level, pos);

        // I am not entirely sure if this is needed, because the light source isn't position dependant, but it can't hurt to keep.
        if (pos == BlockPos.ZERO) return 1;

        IFueledCampfire cfTileEntity = Util.getCFTE(level, pos);
        if ((cfTileEntity == null) || (cfTileEntity.getEternal() && !ServerConfig.FUEL_BASED_LIGHTING_ETERNAL.get()))
            return super.getLightValue(state, level, pos);
        int light = cfTileEntity.getFuelLightLevel();

        // Note: Thought this was helping the initial lighting flicker, but doesn't seem to be true.
        //  Also, it completely fries Starlight.
//        cfTileEntity.updateLighting();
        return (light < 1) ? super.getLightValue(state, level, pos) : light;
    }

}
