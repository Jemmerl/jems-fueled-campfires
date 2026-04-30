package com.jemmerl.jemscampfires.network;

import com.jemmerl.jemscampfires.JemsCampfires;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class JCPacketHandler {
    private static int networkID = 0;
    private static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder.named(
                    new ResourceLocation(JemsCampfires.MOD_ID, "main"))
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void sendToClient(Object msg, ServerPlayerEntity player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void register() {
        INSTANCE.messageBuilder(S2C_CFInfoPacket.class, networkID++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2C_CFInfoPacket::encoder)
                .decoder(S2C_CFInfoPacket::decoder)
                .consumer(S2C_CFInfoPacket::messageConsumer)
                .add();
    }
}