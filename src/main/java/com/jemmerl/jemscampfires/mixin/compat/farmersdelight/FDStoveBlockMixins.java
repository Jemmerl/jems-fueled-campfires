package com.jemmerl.jemscampfires.mixin.compat.farmersdelight;

import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.AbstractStoveBlock;

import javax.annotation.Nullable;

// Compat mixin done with permission from vectorwing with condition of configurability! :)
@SuppressWarnings("target")
@Mixin(value = AbstractStoveBlock.class, priority = 0)
public abstract class FDStoveBlockMixins extends BaseEntityBlock {

    protected FDStoveBlockMixins(Properties pProperties) {
        super(pProperties);
    }

    // To be honest, I would be shocked if anyone else happens to be needing this for the stove block.
    // I am just going to be lazy and only fix this if a compat issue does turn up, in which case I fully
    // permit whoever reports it to yell at me meanly. I deserve it. -Jem
    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        if(!worldIn.isClientSide()) {
            IFueledCampfire cfTileEntity = Util.getCFTE(worldIn, pos);
            if (cfTileEntity != null) {
                // Override world-genned eternal status when placed by a player
//                cfTileEntity.jems_fueled_campfires$setEternal(ServerConfig.PLACE_CAMPFIRE_ETERNAL.get());
                cfTileEntity.jems_fueled_campfires$setPlayerPlaced();
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            cancellable = true, require = 0)
    private void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        if (!ServerConfig.FARMERS_DELIGHT_STOVE_COMPAT.get()) return;
        cir.setReturnValue(cir.getReturnValue()
                .setValue(BlockStateProperties.LIT, ServerConfig.PLACE_CAMPFIRE_LIT.get()));
    }

//    @Inject(at = @At(value = "HEAD"),
//            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true,
//            method = "use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
//            require = 0)
//    public void use(BlockState state, Level arg1, BlockPos arg2, Player arg3, InteractionHand arg4, BlockHitResult arg5, CallbackInfoReturnable<InteractionResult> cir) {
//        if (!ServerConfig.FARMERS_DELIGHT_STOVE_COMPAT.get()) return;
//
//        ItemStack heldStack = arg3.getItemInHand(arg4);
//        BlockEntity tileEntity = arg1.getBlockEntity(arg2);
//        if (!ServerConfig.NEED_FIRE_POKER.get() && arg3.isCrouching() && heldStack.isEmpty() && (tileEntity instanceof IFueledCampfire campfireEntity)) {
//            Util.dispatchCampfireInfo(arg1, arg2, state, arg3, campfireEntity);
//            cir.setReturnValue(InteractionResult.SUCCESS);
//        }
//    }
}
