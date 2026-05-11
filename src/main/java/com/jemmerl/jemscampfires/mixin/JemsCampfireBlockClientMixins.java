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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CampfireBlock.class, priority = 0)
public abstract class JemsCampfireBlockClientMixins extends BaseEntityBlock {
    protected JemsCampfireBlockClientMixins(Properties builder) {
        super(builder);
    }

    @Shadow
    private boolean spawnParticles;

    @Inject(at = @At("HEAD"),
            method = "animateTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V")
    private void animateTickanimateTick(BlockState state, Level arg1, BlockPos arg2, RandomSource arg3, CallbackInfo ci) {
        if (this.spawnParticles && ClientConfig.BONFIRE_EXTRA_PARTICLES.get() && checkBonfire(arg1, arg2)) {
            arg1.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true,
                    (double)arg2.getX() + 0.5D + arg3.nextDouble() / 3.0D * (double)(arg3.nextBoolean() ? 1 : -1),
                    (double)arg2.getY() + arg3.nextDouble() + arg3.nextDouble(),
                    (double)arg2.getZ() + 0.5D + arg3.nextDouble() / 3.0D * (double)(arg3.nextBoolean() ? 1 : -1),
                    (arg3.nextFloat()*0.02D-0.01D), 0.07D, (arg3.nextFloat()*0.02D-0.01D));
            arg1.addParticle(ParticleTypes.LAVA, (double)arg2.getX() + 0.5D, (double)arg2.getY() + 0.5D, (double)arg2.getZ() + 0.5D, arg3.nextFloat(), 10.0E-5D, arg3.nextFloat());
            arg1.addParticle(ParticleTypes.LAVA, (double)arg2.getX() + 0.5D, (double)arg2.getY() + 0.5D, (double)arg2.getZ() + 0.5D, (arg3.nextFloat() / 1.5F), 8.0E-5D, (arg3.nextFloat() / 1.5F));
            arg1.addParticle(ParticleTypes.LAVA, (double)arg2.getX() + 0.5D, (double)arg2.getY() + 0.5D, (double)arg2.getZ() + 0.5D, (arg3.nextFloat() / 2.0F), 5.0E-5D, (arg3.nextFloat() / 2.0F));
        }
    }

    private boolean checkBonfire(Level worldIn, BlockPos posIn) {
        IFueledCampfire cfTileEntity = Util.getCFTE(worldIn, posIn);
        return ((cfTileEntity != null) && (cfTileEntity.jems_fueled_campfires$getBonfire()));
    }
}
