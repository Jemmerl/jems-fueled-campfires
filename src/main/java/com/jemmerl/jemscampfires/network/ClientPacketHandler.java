package com.jemmerl.jemscampfires.network;

import com.jemmerl.jemscampfires.JemsCampfires;
import com.jemmerl.jemscampfires.init.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.text.DecimalFormat;

public class ClientPacketHandler {

    public static void handle(S2C_CFInfoPkt pkt, IPayloadContext context) {
        byte stateBitSet = pkt.bitMap();
        ChatFormatting color = pkt.timeColor();
        if (color == null) color = ChatFormatting.WHITE;

        boolean lit = ((stateBitSet & 1) == 1);
        boolean waterlogged = (((stateBitSet >> 1) & 1) == 1);
        boolean bonfire = (((stateBitSet >> 2) & 1) == 1);
        boolean eternal = (((stateBitSet >> 3) & 1) == 1);
        ChatFormatting timeColor = color;
        int fuelTicks = pkt.fuelTicks();
        campfireMessage(lit, waterlogged, bonfire, eternal, timeColor, fuelTicks);
    }

    public static void campfireMessage(boolean lit, boolean waterlogged, boolean bonfire, boolean eternal, ChatFormatting timeColor, int fuelTicks) {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        if ((clientLevel == null) || (!clientLevel.isClientSide)) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            JemsCampfires.LOGGER.warn("Player was somehow null for client campfire info message packet!");
            return;
        }

        if (lit) {
            MutableComponent msg;
            MutableComponent timeRemaining = convertTime(fuelTicks).withStyle(timeColor);

            if (eternal) {
                if (bonfire) {
                    msg = Component.translatable("info.jemscampfires.eternalbonfire", timeRemaining);
                } else {
                    msg = Component.translatable( "info.jemscampfires.eternalcozy", timeRemaining);
                }
            } else {
                if (bonfire) {
                    msg = Component.translatable( "info.jemscampfires.regularbonfire", timeRemaining);
                } else {
                    msg = Component.translatable("info.jemscampfires.regularcozy", timeRemaining);
                }
            }

            if (ClientConfig.DEBUG_TICKS_REMAINING.get()) {
                msg = msg.append(Component.translatable("info.jemscampfires.ticks", fuelTicks));
            } else {
                msg = msg.append(".");
            }
            player.displayClientMessage(msg, !ClientConfig.CF_INFO_IN_CHAT.get());
        } else {
            Component msg;
            if (waterlogged) {
                msg = Component.translatable("info.jemscampfires.waterlogged");
            } else if (eternal) {
                if (fuelTicks <= 0) {
                    msg = Component.translatable("info.jemscampfires.unliteternalnofuel");
                } else {
                    msg = Component.translatable("info.jemscampfires.unliteternalfuel");
                }
            } else {
                if (fuelTicks <= 0) {
                    msg = Component.translatable("info.jemscampfires.unlitnofuel");
                } else {
                    msg = Component.translatable("info.jemscampfires.unlitfuel");
                }
            }
            player.displayClientMessage(msg, !ClientConfig.CF_INFO_IN_CHAT.get());
        }
    }

    private static MutableComponent convertTime(int fuelTicks) {
        if (fuelTicks < 2400) {
            return Component.translatable("info.jemscampfires.seconds", (fuelTicks / 20));
        } else if (fuelTicks < 144000) {
            return Component.translatable("info.jemscampfires.minutes", formatTimeOutput(fuelTicks / 1200d));
        } else {
            return Component.translatable("info.jemscampfires.hours", formatTimeOutput(fuelTicks / 72000d));
        }
    }

    private static String formatTimeOutput(double doubleIn) {
        double doubleOut = Math.round(doubleIn * 10) / 10d;
        DecimalFormat formatter = new DecimalFormat("0.#####");
        return formatter.format(doubleOut);
    }
}
