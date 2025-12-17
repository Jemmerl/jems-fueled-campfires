package com.jemmerl.jemscampfires.network;

import com.jemmerl.jemscampfires.JemsCampfires;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

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
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                JemsCampfires.LOGGER.warn("Player was somehow null for client campfire info message packet!");
                return;
            }
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHelper.campfireMessage(player, lit, waterlogged, bonfire, eternal, timeColor, fuelTicks));
        });
        ctx.get().setPacketHandled(true);
    }

    /*
    public void handle(CustomPayloadEvent.Context ctx){
        ctx.enqueueWork(() -> {
            Minecraft client = Minecraft.getInstance();
            ClientLevel level = client.level;
            if (level == null) return;

            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> client.execute(() -> ChargedEnderPearlEntity.handlePearlImpact(this.pos)));
        });
        ctx.setPacketHandled(true);
    }
     */
}
