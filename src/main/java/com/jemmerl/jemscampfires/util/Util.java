package com.jemmerl.jemscampfires.util;

import com.jemmerl.jemscampfires.init.ServerConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.text.DecimalFormat;
import java.util.Random;

public class Util {

    public static int mod(int a, int b) {
        return (a % b + b) % b;
    }

    public static IFueledCampfire getCFTE(IBlockReader worldIn, BlockPos posIn) {
        TileEntity tileentity = worldIn.getTileEntity(posIn);
        if (tileentity instanceof IFueledCampfire) {
            return (IFueledCampfire) tileentity;
        }
        return null;
    }

    public static void displayCampfireInfo(World world, BlockPos pos, BlockState state, PlayerEntity player, IFueledCampfire cfTileEntity) {
        if (state.get(CampfireBlock.LIT)) {
            if(world.isRemote) {
                Random random = world.getRandom();
                int n = random.nextInt(4) + 1;
                for (int i = 0; i < n; i++) {
                    world.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D,
                            (random.nextFloat() / 2.0F), 3.0E-5D, (random.nextFloat() / 2.0F));
                }
            } else {
                IFormattableTextComponent msg;
                TranslationTextComponent timeRemaining = convertTime(cfTileEntity.getFuelTicks());

                if (cfTileEntity.getEternal()) {
                    if (cfTileEntity.getBonfire()) {
                        msg = new TranslationTextComponent("info.jemscampfires.eternalbonfire", timeRemaining);
                    } else {
                        msg = new TranslationTextComponent( "info.jemscampfires.eternalcozy", timeRemaining);
                    }
                } else {
                    if (cfTileEntity.getBonfire()) {
                        msg = new TranslationTextComponent( "info.jemscampfires.regularbonfire", timeRemaining);
                    } else {
                        msg = new TranslationTextComponent("info.jemscampfires.regularcozy", timeRemaining);
                    }
                }

                if (ServerConfig.DEBUG_TICKS_REMAINING.get()) {
                    msg = msg.appendSibling(new TranslationTextComponent("info.jemscampfires.ticks", cfTileEntity.getFuelTicks()));
                } else {
                    msg = msg.appendString(".");
                }
                player.sendStatusMessage(msg, !ServerConfig.DEBUG_INFO_IN_CHAT.get());
            }
        } else {
            if(!world.isRemote) {
                ITextComponent msg;
                if (state.get(CampfireBlock.WATERLOGGED)) {
                    msg = new TranslationTextComponent("info.jemscampfires.waterlogged");
                } else if (cfTileEntity.getEternal()) {
                    if (cfTileEntity.getFuelTicks() <= 0) {
                        msg = new TranslationTextComponent("info.jemscampfires.unliteternalnofuel");
                    } else {
                        msg = new TranslationTextComponent("info.jemscampfires.unliteternalfuel");
                    }
                } else {
                    if (cfTileEntity.getFuelTicks() <= 0) {
                        msg = new TranslationTextComponent("info.jemscampfires.unlitnofuel");
                    } else {
                        msg = new TranslationTextComponent("info.jemscampfires.unlitfuel");
                    }
                }
                player.sendStatusMessage(msg, !ServerConfig.DEBUG_INFO_IN_CHAT.get());
            }
        }
    }

    private static TranslationTextComponent convertTime(int fuelTicks) {
        if (fuelTicks < 2400) {
            return new TranslationTextComponent("info.jemscampfires.seconds", (fuelTicks / 20));
        } else if (fuelTicks < 144000) {
            return new TranslationTextComponent("info.jemscampfires.minutes", formatTimeOutput(fuelTicks / 1200d));
        } else {
            return new TranslationTextComponent("info.jemscampfires.hours", formatTimeOutput(fuelTicks / 72000d));
        }
    }

    private static String formatTimeOutput(double doubleIn) {
        double doubleOut = Math.round(doubleIn * 10) / 10d;
        DecimalFormat formatter = new DecimalFormat("0.#####");
        return formatter.format(doubleOut);
    }

}
