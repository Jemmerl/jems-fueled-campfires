package com.jemmerl.jemscampfires.mixin.compat.farmersdelight;

import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
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

    @Inject(at = @At(value = "HEAD"), cancellable = true, method = "useItemOn", require = 0)
    public void useItemOn(ItemStack arg0, BlockState state, Level arg2, BlockPos arg3, Player arg4, InteractionHand arg5, BlockHitResult arg6, CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (!ServerConfig.FARMERS_DELIGHT_STOVE_COMPAT.get()) return;
        IFueledCampfire campfireEntity = Util.getCFTE(arg2, arg3);
        if (campfireEntity == null) return;

        ItemStack heldStack = arg4.getItemInHand(arg5);
        if (!ServerConfig.NEED_FIRE_POKER.get() && arg4.isCrouching() && heldStack.isEmpty()) {
            Util.dispatchCampfireInfo(arg2, arg3, state, arg4, campfireEntity);
            cir.setReturnValue(ItemInteractionResult.SUCCESS);
        }
    }
}
