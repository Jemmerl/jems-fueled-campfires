package com.jemmerl.jemscampfires.network;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;

// Used to trick Java into letting me process a lot of data before calling the constructor
public class CFIntermedPkt {
    final boolean lit;
    final boolean waterlogged;
    final boolean bonfire;
    final boolean eternal;
    final ChatFormatting timeColor;
    final int fuelTicks;

    CFIntermedPkt(FriendlyByteBuf byteBuf) {
        byte stateBitSet = byteBuf.readByte();
        ChatFormatting color = ChatFormatting.getById(byteBuf.readByte());
        if (color == null) color = ChatFormatting.WHITE;

        this.lit = ((stateBitSet & 1) == 1);
        this.waterlogged = (((stateBitSet >> 1) & 1) == 1);
        this.bonfire = (((stateBitSet >> 2) & 1) == 1);
        this.eternal = (((stateBitSet >> 3) & 1) == 1);
        this.timeColor = color;
        this.fuelTicks = byteBuf.readInt();
    }
}