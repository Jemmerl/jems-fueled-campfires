package com.jemmerl.jemscampfires.network;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_CFInfoPacket {
    boolean lit;
    boolean waterlogged;
    boolean bonfire;
    boolean eternal;
    final ChatFormatting timeColor;
    final int fuelTicks;

    public S2C_CFInfoPacket(boolean lit, boolean waterlogged, boolean bonfire, boolean eternal, ChatFormatting timeColor, int fuelTicks) {
        this.lit = lit;
        this.waterlogged = waterlogged;
        this.bonfire = bonfire;
        this.eternal = eternal;
        this.timeColor = timeColor;
        this.fuelTicks = fuelTicks;
    }

    public void encoder(FriendlyByteBuf buffer) {
        int stateBitSet = 0;
        if (lit) stateBitSet = stateBitSet ^ (1);
        if (waterlogged) stateBitSet = stateBitSet ^ (1 << 1);
        if (bonfire) stateBitSet = stateBitSet ^ (1 << 2);
        if (eternal) stateBitSet = stateBitSet ^ (1 << 3);
        buffer.writeByte((byte)stateBitSet);

        int colorId = Math.max(timeColor.getId(), 0);
        buffer.writeByte((byte)((colorId > 15) ? ChatFormatting.WHITE.getId() : colorId));
        buffer.writeInt(fuelTicks);
    }

    public static S2C_CFInfoPacket decoder(FriendlyByteBuf buffer) {
        byte stateBitSet = buffer.readByte();
        ChatFormatting color = ChatFormatting.getById(buffer.readByte());
        if (color == null) color = ChatFormatting.WHITE;

        return new S2C_CFInfoPacket(
                ((stateBitSet & 1) == 1),
                (((stateBitSet >> 1) & 1) == 1),
                (((stateBitSet >> 2) & 1) == 1),
                (((stateBitSet >> 3) & 1) == 1),
                color, buffer.readInt());
    }

    public void messageConsumer(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientPacketHandler.campfireMessage(lit, waterlogged, bonfire, eternal, timeColor, fuelTicks);
        });
        ctx.get().setPacketHandled(true);
    }
}