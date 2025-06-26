package com.jemmerl.jemscampfires.mixin.compat;

import com.jemmerl.jemscampfires.JemsCampfires;
import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import vectorwing.farmersdelight.common.block.StoveBlock;

import javax.annotation.Nullable;

// Compat mixin done with permission from vectorwing with condition of configurability! :)
@SuppressWarnings("target")
@Mixin(value = StoveBlock.class, priority = 0)
public abstract class FarmersDelightStoveBlockMixins extends BaseEntityBlock {

    protected FarmersDelightStoveBlockMixins(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if(!worldIn.isClientSide()) {
            IFueledCampfire cfTileEntity = Util.getCFTE(worldIn, pos);
            if (cfTileEntity != null) {
                // Override world-genned eternal status when placed by a player
                cfTileEntity.setEternal(ServerConfig.PLACE_CAMPFIRE_ETERNAL.get());
            }
        }
        super.setPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Inject(at = @At("RETURN"), method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            cancellable = true)
    private void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        JemsCampfires.LOGGER.warn("TRY");
        if (!ServerConfig.FARMERS_DELIGHT_STOVE_COMPAT.get()) return;
        JemsCampfires.LOGGER.warn("RAN");
        cir.setReturnValue(cir.getReturnValue()
                .setValue(BlockStateProperties.LIT, ServerConfig.PLACE_CAMPFIRE_LIT.get()));
    }

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/world/level/Level.getBlockEntity (Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true,
            method = "use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;")
    public void use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult arg5, CallbackInfoReturnable<InteractionResult> cir, ItemStack heldStack, Item heldItem, BlockEntity tileEntity) {
        if (!ServerConfig.FARMERS_DELIGHT_STOVE_COMPAT.get()) return;
        if (!ServerConfig.NEED_FIRE_POKER.get() && player.isCrouching() && heldStack.isEmpty() && (tileEntity instanceof IFueledCampfire)) {
            Util.displayCampfireInfo(level, pos, state, player, (IFueledCampfire)tileEntity);
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
