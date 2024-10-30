package com.jemmerl.jemscampfires.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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
                ITextComponent msg;
                if (cfTileEntity.getEternal()) {
                    if (cfTileEntity.getBonfire()) {
                        msg = new TranslationTextComponent( "info.jemscampfires.eternalbonfire", (cfTileEntity.getFuelTicks() / 20), cfTileEntity.getFuelTicks());
                    } else {
                        msg = new TranslationTextComponent( "info.jemscampfires.eternalcozy", (cfTileEntity.getFuelTicks() / 20), cfTileEntity.getFuelTicks());
                    }
                } else {
                    if (cfTileEntity.getBonfire()) {
                        msg = new TranslationTextComponent( "info.jemscampfires.regularbonfire", (cfTileEntity.getFuelTicks() / 20), cfTileEntity.getFuelTicks());
                    } else {
                        msg = new TranslationTextComponent("info.jemscampfires.regularcozy", (cfTileEntity.getFuelTicks() / 20), cfTileEntity.getFuelTicks());
                    }
                }
                player.sendMessage(msg, PlayerEntity.getUUID(player.getGameProfile()));
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
                player.sendMessage(msg, PlayerEntity.getUUID(player.getGameProfile()));
            }
        }
    }


}
