package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.init.ClientConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ContainerBlock;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;

@Mixin(value = CampfireBlock.class, priority = 0)
public abstract class JemsCampfireBlockClientMixins extends ContainerBlock {
    protected JemsCampfireBlockClientMixins(Properties builder) {
        super(builder);
    }

    @Shadow
    private boolean smokey;

    @Inject(at = @At(value = "JUMP", opcode = Opcodes.IFEQ, ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD, method = "animateTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V")
    private void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand, CallbackInfo ci) {
        if (worldIn == null) return;

        if (this.smokey && ClientConfig.BONFIRE_EXTRA_PARTICLES.get() && checkBonfire(worldIn, pos)) {
            worldIn.addOptionalParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true,
                    (double)pos.getX() + 0.5D + rand.nextDouble() / 3.0D * (double)(rand.nextBoolean() ? 1 : -1),
                    (double)pos.getY() + rand.nextDouble() + rand.nextDouble(),
                    (double)pos.getZ() + 0.5D + rand.nextDouble() / 3.0D * (double)(rand.nextBoolean() ? 1 : -1),
                    (rand.nextFloat()*0.02D-0.01D), 0.07D, (rand.nextFloat()*0.02D-0.01D));
            worldIn.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, rand.nextFloat(), 10.0E-5D, rand.nextFloat());
            worldIn.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, (rand.nextFloat() / 1.5F), 8.0E-5D, (rand.nextFloat() / 1.5F));
            worldIn.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, (rand.nextFloat() / 2.0F), 5.0E-5D, (rand.nextFloat() / 2.0F));
        }
    }

    private boolean checkBonfire(World worldIn, BlockPos posIn) {
        IFueledCampfire cfTileEntity = Util.getCFTE(worldIn, posIn);
        if (cfTileEntity != null) {
            return cfTileEntity.getBonfire();
        }
        return false;
    }

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
