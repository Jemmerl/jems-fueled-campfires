package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.init.ClientConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;

@Mixin(value = CampfireBlock.class, priority = 0)
public abstract class JemsCampfireBlockClientMixins extends BaseEntityBlock {
    protected JemsCampfireBlockClientMixins(Properties builder) {
        super(builder);
    }

    @Shadow
    private boolean spawnParticles;

    @Inject(at = @At(value = "JUMP", opcode = Opcodes.IFEQ, ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD,
            method = "animateTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V")
    private void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRand, CallbackInfo ci) {
        if (this.spawnParticles && ClientConfig.BONFIRE_EXTRA_PARTICLES.get() && checkBonfire(pLevel, pPos)) {
            pLevel.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true,
                    (double)pPos.getX() + 0.5D + pRand.nextDouble() / 3.0D * (double)(pRand.nextBoolean() ? 1 : -1),
                    (double)pPos.getY() + pRand.nextDouble() + pRand.nextDouble(),
                    (double)pPos.getZ() + 0.5D + pRand.nextDouble() / 3.0D * (double)(pRand.nextBoolean() ? 1 : -1),
                    (pRand.nextFloat()*0.02D-0.01D), 0.07D, (pRand.nextFloat()*0.02D-0.01D));
            pLevel.addParticle(ParticleTypes.LAVA, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, pRand.nextFloat(), 10.0E-5D, pRand.nextFloat());
            pLevel.addParticle(ParticleTypes.LAVA, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, (pRand.nextFloat() / 1.5F), 8.0E-5D, (pRand.nextFloat() / 1.5F));
            pLevel.addParticle(ParticleTypes.LAVA, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, (pRand.nextFloat() / 2.0F), 5.0E-5D, (pRand.nextFloat() / 2.0F));
        }
    }

    private boolean checkBonfire(Level worldIn, BlockPos posIn) {
        IFueledCampfire cfTileEntity = Util.getCFTE(worldIn, posIn);
        if (cfTileEntity != null) {
            return cfTileEntity.getBonfire();
        }
        return false;
    }

//// ...NEVER never mind. Would have to update the fuel value a lot, and this code runs frequently.
//// Maybe will revisit again later. Maybe.
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
