package com.jemmerl.jemscampfires.network;

import com.jemmerl.jemscampfires.JemsCampfires;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = JemsCampfires.MOD_ID)
public class NetworkEvents {

    @SubscribeEvent
    public static void registerPacket(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                S2C_CFInfoPkt.TYPE,
                S2C_CFInfoPkt.STREAM_CODEC,
                ClientPacketHandler::handle
        );
    }
}
