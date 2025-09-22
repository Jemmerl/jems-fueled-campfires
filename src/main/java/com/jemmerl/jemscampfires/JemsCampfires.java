package com.jemmerl.jemscampfires;

import com.jemmerl.jemscampfires.init.ClientConfig;
import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.items.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("removal")
@Mod(JemsCampfires.MOD_ID)
public class JemsCampfires
{
    public static final String MOD_ID = "jemscampfires";
    public static final Logger LOGGER = LogManager.getLogger();

    public JemsCampfires() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
//        eventBus.addListener(this::setup);
//        eventBus.addListener(this::doClientStuff);
        eventBus.addListener(this::onConfigLoad);
//        eventBus.addListener(ModEvents::buildContents);

        ModItems.register(eventBus);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SERVER_SPEC);
        ServerConfig.loadConfig(ServerConfig.SERVER_SPEC, FMLPaths.GAMEDIR.get()
                .resolve(FMLConfig.defaultConfigPath()).resolve(MOD_ID + "-server.toml"));
    }

//    private void setup(final FMLCommonSetupEvent event) {}
//    private void doClientStuff(final FMLClientSetupEvent event) {}

    @SubscribeEvent
    public void onConfigLoad(ModConfigEvent event) {
        // Only run during loading and reloading event types
        if (event instanceof ModConfigEvent.Unloading) return;
        ServerConfig.updateCustomFuelList();
    }


}
