package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.init.ClientConfig;
import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.phys.BlockHitResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.Random;

@Mixin(value = CampfireBlock.class, priority = 0)
public abstract class JemsCampfireBlockMixins extends BaseEntityBlock {
    protected JemsCampfireBlockMixins(Properties builder) {
        super(builder);
    }

    @Shadow
    private boolean smokey;

    //public void animateTick( ,  , BlockPos , Random ) {
//    @Inject(at = @At(value = "JUMP", opcode = Opcodes.IFEQ, ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD,
//            method = "animateTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V")
//    private void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand, CallbackInfo ci) {
//        if (this.smokey && ClientConfig.BONFIRE_EXTRA_PARTICLES.get() && checkBonfire(pLevel, pPos)) {
//            pLevel.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true,
//                    (double)pPos.getX() + 0.5D + pRand.nextDouble() / 3.0D * (double)(pRand.nextBoolean() ? 1 : -1),
//                    (double)pPos.getY() + pRand.nextDouble() + pRand.nextDouble(),
//                    (double)pPos.getZ() + 0.5D + pRand.nextDouble() / 3.0D * (double)(pRand.nextBoolean() ? 1 : -1),
//                    (pRand.nextFloat()*0.02D-0.01D), 0.07D, (pRand.nextFloat()*0.02D-0.01D));
//            pLevel.addParticle(ParticleTypes.LAVA, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, pRand.nextFloat(), 10.0E-5D, pRand.nextFloat());
//            pLevel.addParticle(ParticleTypes.LAVA, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, (pRand.nextFloat() / 1.5F), 8.0E-5D, (pRand.nextFloat() / 1.5F));
//            pLevel.addParticle(ParticleTypes.LAVA, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, (pRand.nextFloat() / 2.0F), 5.0E-5D, (pRand.nextFloat() / 2.0F));
//        }
//    }
//
//    private boolean checkBonfire(Level worldIn, BlockPos posIn) {
//        IFueledCampfire cfTileEntity = Util.getCFTE(worldIn, posIn);
//        if (cfTileEntity != null) {
//            return cfTileEntity.getBonfire();
//        }
//        return false;
//    }
//
//    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/tileentity/CampfireTileEntity.dropAllItems()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD,
//            method = "dowse(Lnet/minecraft/world/IWorld;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V")
//    private static void extinguishEternal(LevelAccessor world, BlockPos pos, BlockState state, CallbackInfo ci, BlockEntity tileentity) {
//        if (!world.isClientSide()) {
//            ((IFueledCampfire)tileentity).doExtinguished();
//        }
//    }
//
//    @Override
//    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
//        if(!worldIn.isClientSide()) {
//            IFueledCampfire cfTileEntity = Util.getCFTE(worldIn, pos);
//            if (cfTileEntity != null) {
//                // Override world-genned eternal status when placed by a player
//                if (state.getBlock().getRegistryName().toString().contains("soul")) {
//                    cfTileEntity.setEternal(ServerConfig.PLACE_SOUL_CAMPFIRE_ETERNAL.get());
//                } else {
//                    cfTileEntity.setEternal(ServerConfig.PLACE_CAMPFIRE_ETERNAL.get());
//                }
//            }
//        }
//        super.setPlacedBy(worldIn, pos, state, placer, stack);
//    }
//
//    @Inject(at = @At("RETURN"), method = "getStateForPlacement(Lnet/minecraft/world/level/LevelAccessor;)Lnet/minecraft/world/level/block/state/BlockState;", cancellable = true)
//    private void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
//        cir.setReturnValue(cir.getReturnValue()
//                .setValue(CampfireBlock.LIT, ((this.asBlock().getRegistryName().toString().contains("soul")) ?
//                        ServerConfig.PLACE_SOUL_CAMPFIRE_LIT.get() : ServerConfig.PLACE_CAMPFIRE_LIT.get())));
//    }
//
//    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/entity/player/PlayerEntity.getHeldItem(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/item/ItemStack;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true,
//                method = "onBlockActivated(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;")
//    public void use(BlockState arg0, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult arg5, CallbackInfoReturnable<InteractionResult> cir, BlockEntity tileentity, CampfireBlockEntity campfiretileentity, ItemStack itemstack) {
//        if (!ServerConfig.NEED_FIRE_POKER.get() && player.isCrouching() && itemstack.isEmpty()) {
//            Util.displayCampfireInfo(worldIn, pos, arg0, player, (IFueledCampfire)campfiretileentity);
//            cir.setReturnValue(InteractionResult.SUCCESS);
//        }
//    }

// ...NEVER never mind. Would have to update the fuel value a lot, and this code runs frequently.
// Maybe will revisit again later. Maybe.
//    @Override
//    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
//        if (state.get(CampfireBlock.LIT)) {
//            boolean isSoul = this.getDefaultState().getBlock() == Blocks.SOUL_CAMPFIRE;
//            if (isSoul ? true : true) {
//                IFueledCampfire cfTileEntity = Util.getCFTE(world, pos);
//                if (cfTileEntity != null) {
//                    float fuel = cfTileEntity.getFuelTicks();
//                    float maxFuel = isSoul ? ServerConfig.SOUL_CAMPFIRE_MAX_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_MAX_FUEL_TICKS.get();
//                    float maxLight = isSoul ? 10f : 15f;
//                    if (fuel >= maxFuel) {
//                        if (isSoul && cfTileEntity.getBonfire() && true) {
//                            maxLight = 15f;
//                        }
//                        return (int)maxLight;
//                    }
//                    return (int)Math.ceil(maxLight * (fuel/maxFuel));
//                }
//            }
//        }
//        return super.getLightValue(state, world, pos);
//    }

}
