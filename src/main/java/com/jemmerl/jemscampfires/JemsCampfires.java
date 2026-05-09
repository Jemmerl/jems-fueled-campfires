package com.jemmerl.jemscampfires;

import com.jemmerl.jemscampfires.init.ClientConfig;
import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.init.fueloverrides.FuelOverrideDataManager;
import com.jemmerl.jemscampfires.items.ModItems;
import com.jemmerl.jemscampfires.network.ClientPacketHandler;
import com.jemmerl.jemscampfires.network.S2C_CFInfoPkt;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(JemsCampfires.MOD_ID)
public class JemsCampfires
{
    public static final String MOD_ID = "jemscampfires";
    public static final Logger LOGGER = LogManager.getLogger();

    // TODO Future
    //  Run profiling somehow. Never done that before.

    // This bug happened a while ago. Haven't seen it since. Race condition with checkBlock?
    /*
    java.lang.NullPointerException: Cannot invoke "it.unimi.dsi.fastutil.longs.LongArrayList.getLong(int)" because "this.wrapped" is null
	at it.unimi.dsi.fastutil.longs.LongOpenHashSet$SetIterator.nextLong(LongOpenHashSet.java:545) ~[fastutil-8.5.9.jar:?]
	at net.minecraft.world.level.lighting.LightEngine.runLightUpdates(LightEngine.java:143) ~[forge-1.20.1-47.3.12_mapped_parchment_2023.09.03-1.20.1-recomp.jar:?]
	at net.minecraft.world.level.lighting.LevelLightEngine.runLightUpdates(LevelLightEngine.java:48) ~[forge-1.20.1-47.3.12_mapped_parchment_2023.09.03-1.20.1-recomp.jar:?]
     */


    public JemsCampfires(final IEventBus modEventBus, final ModContainer modContainer) {

        modEventBus.addListener(this::buildContents);

        ModItems.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
//        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SERVER_SPEC);
//        ServerConfig.loadConfig(ServerConfig.SERVER_SPEC, FMLPaths.GAMEDIR.get()
//                .resolve(FMLConfig.defaultConfigPath()).resolve(MOD_ID + "-server.toml"));

//        StarlightCompat.init();
    }

//    private void setup(final FMLCommonSetupEvent event) {
//        event.enqueueWork(() -> {
//            JCPacketHandler.register();
//        });
//    }

//    private void doClientStuff(final FMLClientSetupEvent event) {}

    public void buildContents(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.FIRE_POKER);
        }
    }

    //Fixed it by passing in registry access to my reload listener during AddReloadListenerEvent since it provides registry access
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        FuelOverrideDataManager.buildDataLoader(event.getRegistryAccess());
        event.addListener(FuelOverrideDataManager.getDataLoader());
    }
}
