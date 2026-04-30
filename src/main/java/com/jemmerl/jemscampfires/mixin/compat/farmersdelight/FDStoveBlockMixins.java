package com.jemmerl.jemscampfires.mixin.compat.farmersdelight;

import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import vectorwing.farmersdelight.blocks.StoveBlock;
import vectorwing.farmersdelight.tile.StoveTileEntity;

import javax.annotation.Nullable;

// Compat mixin done with permission from vectorwing with condition of configurability! :)
@SuppressWarnings("target")
@Mixin(value = StoveBlock.class, priority = 0)
public abstract class FDStoveBlockMixins extends HorizontalBlock {

    protected FDStoveBlockMixins(Properties pProperties) {
        super(pProperties);
    }

    // To be honest, I would be shocked if anyone else happens to be needing this for the stove block.
    // I am just going to be lazy and only fix this if a compat issue does turn up, in which case I fully
    // permit whoever reports it to yell at me meanly. I deserve it. -Jem
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if(!worldIn.isRemote()) {
            IFueledCampfire cfTileEntity = Util.getCFTE(worldIn, pos);
            if (cfTileEntity != null) {
                // Override world-genned eternal status when placed by a player
                cfTileEntity.setEternal(ServerConfig.PLACE_CAMPFIRE_ETERNAL.get());
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "getStateForPlacement(Lnet/minecraft/item/BlockItemUseContext;)Lnet/minecraft/block/BlockState;", cancellable = true)
    private void getStateForPlacement(BlockItemUseContext context, CallbackInfoReturnable<BlockState> cir) {
        if (!ServerConfig.FARMERS_DELIGHT_STOVE_COMPAT.get()) return;
        cir.setReturnValue(cir.getReturnValue()
                .with(BlockStateProperties.LIT, ServerConfig.PLACE_CAMPFIRE_LIT.get()));
    }

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/entity/player/PlayerEntity.getHeldItem(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true,
            method = "onBlockActivated(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/math/BlockRayTraceResult;)Lnet/minecraft/util/ActionResultType;")
    public void onBlockActivated(BlockState arg0, World arg1, BlockPos arg2, PlayerEntity player, Hand handIn, BlockRayTraceResult arg5, CallbackInfoReturnable<ActionResultType> cir, ItemStack heldStack)    {
        if (!ServerConfig.FARMERS_DELIGHT_STOVE_COMPAT.get()) return;

        TileEntity tileEntity = arg1.getTileEntity(arg2);
        if (!(tileEntity instanceof StoveTileEntity)) return;

        if (!ServerConfig.NEED_FIRE_POKER.get() && player.isCrouching() && heldStack.isEmpty() && (tileEntity instanceof IFueledCampfire)) {
            Util.dispatchCampfireInfo(arg1, arg2, arg0, player, (IFueledCampfire)tileEntity);
            cir.setReturnValue(ActionResultType.SUCCESS);
        }
    }
}