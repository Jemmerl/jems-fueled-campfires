package com.jemmerl.jemscampfires.util;

import com.jemmerl.jemscampfires.init.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.text.DecimalFormat;
import java.util.Random;

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

    public static void displayCampfireInfo(Level world, BlockPos pos, BlockState state, Player player, IFueledCampfire cfTileEntity) {
        if (state.getValue(CampfireBlock.LIT)) {
            if(world.isClientSide) {
                if (!state.getBlock().getRegistryName().toString().contains("soul")) {
                    Random random = world.getRandom();
                    int n = random.nextInt(4) + 1;
                    for (int i = 0; i < n; i++) {
                        world.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D,
                                (random.nextFloat() / 2.0F), 3.0E-5D, (random.nextFloat() / 2.0F));
                    }
                }
            } else {
                MutableComponent msg;
                MutableComponent timeRemaining = convertTime(cfTileEntity.getFuelTicks());

                if (cfTileEntity.getEternal()) {
                    if (cfTileEntity.getBonfire()) {
                        msg = new TranslatableComponent("info.jemscampfires.eternalbonfire", timeRemaining);
                    } else {
                        msg = new TranslatableComponent( "info.jemscampfires.eternalcozy", timeRemaining);
                    }
                } else {
                    if (cfTileEntity.getBonfire()) {
                        msg = new TranslatableComponent( "info.jemscampfires.regularbonfire", timeRemaining);
                    } else {
                        msg = new TranslatableComponent("info.jemscampfires.regularcozy", timeRemaining);
                    }
                }

                if (ServerConfig.DEBUG_TICKS_REMAINING.get()) {
                    msg = msg.append(new TranslatableComponent("info.jemscampfires.ticks", cfTileEntity.getFuelTicks()));
                } else {
                    msg = msg.append(".");
                }
                player.displayClientMessage(msg, !ServerConfig.DEBUG_INFO_IN_CHAT.get());
            }
        } else {
            if(!world.isClientSide) {
                Component msg;
                if (state.getValue(CampfireBlock.WATERLOGGED)) {
                    msg = new TranslatableComponent("info.jemscampfires.waterlogged");
                } else if (cfTileEntity.getEternal()) {
                    if (cfTileEntity.getFuelTicks() <= 0) {
                        msg = new TranslatableComponent("info.jemscampfires.unliteternalnofuel");
                    } else {
                        msg = new TranslatableComponent("info.jemscampfires.unliteternalfuel");
                    }
                } else {
                    if (cfTileEntity.getFuelTicks() <= 0) {
                        msg = new TranslatableComponent("info.jemscampfires.unlitnofuel");
                    } else {
                        msg = new TranslatableComponent("info.jemscampfires.unlitfuel");
                    }
                }
                player.displayClientMessage(msg, !ServerConfig.DEBUG_INFO_IN_CHAT.get());
            }
        }
    }


    private static MutableComponent convertTime(int fuelTicks) {
        if (fuelTicks < 2400) {
            return new TranslatableComponent("info.jemscampfires.seconds", (fuelTicks / 20));
        } else if (fuelTicks < 144000) {
            return new TranslatableComponent("info.jemscampfires.minutes", formatTimeOutput(fuelTicks / 1200d));
        } else {
            return new TranslatableComponent("info.jemscampfires.hours", formatTimeOutput(fuelTicks / 72000d));
        }
    }

    private static String formatTimeOutput(double doubleIn) {
        double doubleOut = Math.round(doubleIn * 10) / 10d;
        DecimalFormat formatter = new DecimalFormat("0.#####");
        return formatter.format(doubleOut);
    }

}
