package com.jemmerl.jemscampfires.network;

import com.jemmerl.jemscampfires.JemsCampfires;
import com.jemmerl.jemscampfires.init.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.text.DecimalFormat;

public class ClientPacketHandler {
    public static void campfireMessage(boolean lit, boolean waterlogged, boolean bonfire, boolean eternal, TextFormatting timeColor, int fuelTicks) {
        ClientWorld clientLevel = Minecraft.getInstance().world;
        if ((clientLevel == null) || (!clientLevel.isRemote)) return;

        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null) {
            JemsCampfires.LOGGER.warn("Player was somehow null for client campfire info message packet!");
            return;
        }

        if (lit) {
            IFormattableTextComponent msg;
            IFormattableTextComponent timeRemaining = convertTime(fuelTicks).mergeStyle(timeColor);

            if (eternal) {
                if (bonfire) {
                    msg = new TranslationTextComponent("info.jemscampfires.eternalbonfire", timeRemaining);
                } else {
                    msg = new TranslationTextComponent( "info.jemscampfires.eternalcozy", timeRemaining);
                }
            } else {
                if (bonfire) {
                    msg = new TranslationTextComponent( "info.jemscampfires.regularbonfire", timeRemaining);
                } else {
                    msg = new TranslationTextComponent("info.jemscampfires.regularcozy", timeRemaining);
                }
            }

            if (ClientConfig.DEBUG_TICKS_REMAINING.get()) {
                msg = msg.appendSibling(new TranslationTextComponent("info.jemscampfires.ticks", fuelTicks));
            } else {
                msg = msg.appendString(".");
            }
            player.sendStatusMessage(msg, !ClientConfig.CF_INFO_IN_CHAT.get());
        } else {
            TextComponent msg;
            if (waterlogged) {
                msg = new TranslationTextComponent("info.jemscampfires.waterlogged");
            } else if (eternal) {
                if (fuelTicks <= 0) {
                    msg = new TranslationTextComponent("info.jemscampfires.unliteternalnofuel");
                } else {
                    msg = new TranslationTextComponent("info.jemscampfires.unliteternalfuel");
                }
            } else {
                if (fuelTicks <= 0) {
                    msg = new TranslationTextComponent("info.jemscampfires.unlitnofuel");
                } else {
                    msg = new TranslationTextComponent("info.jemscampfires.unlitfuel");
                }
            }
            player.sendStatusMessage(msg, !ClientConfig.CF_INFO_IN_CHAT.get());
        }
    }

    private static TextComponent convertTime(int fuelTicks) {
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