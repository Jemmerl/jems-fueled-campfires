package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.init.ClientConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

// ...NEVER never mind. Would have to update the fuel value a lot, and this code runs frequently.
// Maybe will revisit again later. Maybe.

    ///tp Dev 195 73 -168 220 30

    // TODO sync server config to client so this can be short circuited to speed up rendering?
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (!state.getValue(BlockStateProperties.LIT)) return super.getLightEmission(state, level, pos);

        // "Ensure chunk sections with this block are correctly considered as containing a light source"
        // From XFactHD -> FramedBlocks. Unknown if needed.
        if (pos == BlockPos.ZERO) return 15;

        IFueledCampfire cfTileEntity = Util.getCFTE(level, pos);
        if (cfTileEntity == null) return super.getLightEmission(state, level, pos);

        int light = cfTileEntity.getFuelLightLevel();
        // TODO desperate solution attempt. Increasing the number would probably work, but a horribly way to go.
        if (cfTileEntity.getFuelTicks() < 0) {
            cfTileEntity.setFuelTicks(cfTileEntity.getFuelTicks()+1);
            cfTileEntity.updateLighting();
        };

        return (light < 1) ? super.getLightEmission(state, level, pos) : light;
    }

    @Override
    public int getLightBlock(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return super.getLightBlock(pState, pLevel, pPos);
    }

    //XfactHD

    //I found another slight issue which doesn't seem to be new either though: BEs have not been populated yet when
    // lights are collected in the chunk loading process, which leads to the lighting relying on weird persistence*
    // or the chunk being saved with "my lights are up-to-date" (which appears to be the case for all fully generated
    // chunks, with the only likely case of it not being up-to-date coming from a chunk having its lights modified right before saving)
    //
    //* If I force the chunk with my test block to save and load with "my lights need updating",
    // the light is still correct after loading, even though it goes through a light source collection
    // that doesn't collect my test block due to it having no access to its BE

    //BE-based light values work fine in my application
}
