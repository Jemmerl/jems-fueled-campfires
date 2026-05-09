package com.jemmerl.jemscampfires.network;

import com.jemmerl.jemscampfires.JemsCampfires;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2C_CFInfoPkt(Byte bitMap, ChatFormatting timeColor, int fuelTicks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2C_CFInfoPkt> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JemsCampfires.MOD_ID, "cf_info_pkt"));

    public static final StreamCodec<FriendlyByteBuf, S2C_CFInfoPkt> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE,
            S2C_CFInfoPkt::bitMap,
            NeoForgeStreamCodecs.enumCodec(ChatFormatting.class),
            S2C_CFInfoPkt::timeColor,
            ByteBufCodecs.INT,
            S2C_CFInfoPkt::fuelTicks,
            S2C_CFInfoPkt::new
    );

    public static S2C_CFInfoPkt createPktFromData(boolean lit, boolean waterlogged, boolean bonfire, boolean eternal, ChatFormatting timeColor, int fuelTicks) {
        int stateBitSet = 0;
        if (lit) stateBitSet = stateBitSet ^ (1);
        if (waterlogged) stateBitSet = stateBitSet ^ (1 << 1);
        if (bonfire) stateBitSet = stateBitSet ^ (1 << 2);
        if (eternal) stateBitSet = stateBitSet ^ (1 << 3);

        int colorId = Math.max(timeColor.getId(), 0);
        ChatFormatting writeColor = ((colorId > 15) ? ChatFormatting.WHITE : timeColor);
        return new S2C_CFInfoPkt((byte)stateBitSet, writeColor, fuelTicks);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
