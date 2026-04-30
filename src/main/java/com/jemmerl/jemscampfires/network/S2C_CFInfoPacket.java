package com.jemmerl.jemscampfires.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_CFInfoPacket {
    boolean lit;
    boolean waterlogged;
    boolean bonfire;
    boolean eternal;
    final TextFormatting timeColor;
    final int fuelTicks;

    public S2C_CFInfoPacket(boolean lit, boolean waterlogged, boolean bonfire, boolean eternal, TextFormatting timeColor, int fuelTicks) {
        this.lit = lit;
        this.waterlogged = waterlogged;
        this.bonfire = bonfire;
        this.eternal = eternal;
        this.timeColor = timeColor;
        this.fuelTicks = fuelTicks;
    }

    public void encoder(PacketBuffer buffer) {
        int stateBitSet = 0;
        if (lit) stateBitSet = stateBitSet ^ (1);
        if (waterlogged) stateBitSet = stateBitSet ^ (1 << 1);
        if (bonfire) stateBitSet = stateBitSet ^ (1 << 2);
        if (eternal) stateBitSet = stateBitSet ^ (1 << 3);
        buffer.writeByte((byte)stateBitSet);

        int colorId = Math.max(timeColor.getColorIndex(), 0);
        buffer.writeByte((byte)((colorId > 15) ? TextFormatting.WHITE.getColorIndex() : colorId));
        buffer.writeInt(fuelTicks);
    }

    public static S2C_CFInfoPacket decoder(PacketBuffer buffer) {
        byte stateBitSet = buffer.readByte();
        TextFormatting color = TextFormatting.fromColorIndex(buffer.readByte());
        if (color == null) color = TextFormatting.WHITE;

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