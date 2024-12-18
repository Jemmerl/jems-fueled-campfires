package com.jemmerl.jemscampfires.util;

import com.jemmerl.jemscampfires.init.ClientConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class Util {

    public static int mod(int a, int b) {
        return (a % b + b) % b;
    }

    public static IFueledCampfire getCFTE(BlockGetter worldIn, BlockPos posIn) {
        BlockEntity tileentity = worldIn.getBlockEntity(posIn);
        if (tileentity instanceof IFueledCampfire) {
            return (IFueledCampfire) tileentity;
        }
        return null;
    }

    public static void displayCampfireInfo(Level level, BlockPos pos, BlockState state, Player player, IFueledCampfire cfTileEntity) {
        if (state.getValue(CampfireBlock.LIT)) {
            if(level.isClientSide) {
                if (!ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString().contains("soul")) {
                    RandomSource randomSource = level.getRandom();
                    int n = randomSource.nextInt(4) + 1;
                    for (int i = 0; i < n; i++) {
                        level.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D,
                                (randomSource.nextFloat() / 2.0F), 3.0E-5D, (randomSource.nextFloat() / 2.0F));
                    }
                }
            } else {
                MutableComponent msg;
                if (cfTileEntity.getEternal()) {
                    if (cfTileEntity.getBonfire()) {
                        msg = Component.translatable("info.jemscampfires.eternalbonfire", (cfTileEntity.getFuelTicks() / 20));
                    } else {
                        msg = Component.translatable( "info.jemscampfires.eternalcozy", (cfTileEntity.getFuelTicks() / 20));
                    }
                } else {
                    if (cfTileEntity.getBonfire()) {
                        msg = Component.translatable( "info.jemscampfires.regularbonfire", (cfTileEntity.getFuelTicks() / 20));
                    } else {
                        msg = Component.translatable("info.jemscampfires.regularcozy", (cfTileEntity.getFuelTicks() / 20));
                    }

                    if (ClientConfig.SHOW_TICKS_REMAINING.get()) {
                        msg = msg.append(Component.translatable("info.jemscampfires.ticks", cfTileEntity.getFuelTicks()));
                    } else {
                        msg = msg.append(".");
                    }

                }
                player.sendSystemMessage(msg);
            }
        } else {
            if(!level.isClientSide) {
                Component msg;
                if (state.getValue(CampfireBlock.WATERLOGGED)) {
                    msg = Component.translatable("info.jemscampfires.waterlogged");
                } else if (cfTileEntity.getEternal()) {
                    if (cfTileEntity.getFuelTicks() <= 0) {
                        msg = Component.translatable("info.jemscampfires.unliteternalnofuel");
                    } else {
                        msg = Component.translatable("info.jemscampfires.unliteternalfuel");
                    }
                } else {
                    if (cfTileEntity.getFuelTicks() <= 0) {
                        msg = Component.translatable("info.jemscampfires.unlitnofuel");
                    } else {
                        msg = Component.translatable("info.jemscampfires.unlitfuel");
                    }
                }
                player.sendSystemMessage(msg);
            }
        }
    }

}
