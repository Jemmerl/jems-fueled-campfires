package com.jemmerl.jemscampfires.items;

import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.Random;

public class FirePoker extends Item {
    public FirePoker(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World world = context.getWorld();
        BlockState state = world.getBlockState(context.getPos());
        if (state.getBlock() instanceof CampfireBlock) {
            IFueledCampfire cfTileEntity = Util.getCFTE(context.getWorld(), context.getPos());
            if (cfTileEntity != null) {
                PlayerEntity player = context.getPlayer();
                if ((player == null) || (player.getCooldownTracker().hasCooldown(this))) {
                    return super.onItemUseFirst(stack, context);
                }
                player.getCooldownTracker().setCooldown(this, 10);

                if (state.get(CampfireBlock.LIT)) {
                    if(world.isRemote) {
                        Random random = world.getRandom();
                        BlockPos pos = context.getPos();
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
                        player.sendMessage(msg, player.getUUID(player.getGameProfile()));
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
                        player.sendMessage(msg, player.getUUID(player.getGameProfile()));
                    }
                }
                return ActionResultType.func_233537_a_(world.isRemote());
            }
        }
        return super.onItemUseFirst(stack, context);
    }
}
