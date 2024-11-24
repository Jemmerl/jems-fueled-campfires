package com.jemmerl.jemscampfires.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

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
                if (!ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString().contains("soul")) {
                    Random random = world.getRandom();
                    int n = random.nextInt(4) + 1;
                    for (int i = 0; i < n; i++) {
                        world.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D,
                                (random.nextFloat() / 2.0F), 3.0E-5D, (random.nextFloat() / 2.0F));
                    }
                }
            } else {
                Component msg;
                if (cfTileEntity.getEternal()) {
                    if (cfTileEntity.getBonfire()) {
                        msg = new TranslatableComponent( "info.jemscampfires.eternalbonfire", (cfTileEntity.getFuelTicks() / 20), cfTileEntity.getFuelTicks());
                    } else {
                        msg = new TranslatableComponent( "info.jemscampfires.eternalcozy", (cfTileEntity.getFuelTicks() / 20), cfTileEntity.getFuelTicks());
                    }
                } else {
                    if (cfTileEntity.getBonfire()) {
                        msg = new TranslatableComponent( "info.jemscampfires.regularbonfire", (cfTileEntity.getFuelTicks() / 20), cfTileEntity.getFuelTicks());
                    } else {
                        msg = new TranslatableComponent("info.jemscampfires.regularcozy", (cfTileEntity.getFuelTicks() / 20), cfTileEntity.getFuelTicks());
                    }
                }
                player.sendMessage(msg, Player.createPlayerUUID(player.getGameProfile()));
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
                player.sendMessage(msg, Player.createPlayerUUID(player.getGameProfile()));
            }
        }
    }


}
