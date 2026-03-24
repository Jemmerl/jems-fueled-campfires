package com.jemmerl.jemscampfires;

import com.jemmerl.jemscampfires.compat.StarlightCompat;
import com.jemmerl.jemscampfires.init.ClientConfig;
import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.init.fueloverrides.FuelOverrideDataManager;
import com.jemmerl.jemscampfires.items.ModItems;
import com.jemmerl.jemscampfires.network.JCPacketHandler;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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

    // TODO Future
    //  Cache primitive of the config? Could speed up performance if its ever found an issue.
    //  Run profiling somehow. Never done that before.

    // This bug happened a while ago. Haven't seen it since. Race condition with checkBlock?
    /*
    java.lang.NullPointerException: Cannot invoke "it.unimi.dsi.fastutil.longs.LongArrayList.getLong(int)" because "this.wrapped" is null
	at it.unimi.dsi.fastutil.longs.LongOpenHashSet$SetIterator.nextLong(LongOpenHashSet.java:545) ~[fastutil-8.5.9.jar:?]
	at net.minecraft.world.level.lighting.LightEngine.runLightUpdates(LightEngine.java:143) ~[forge-1.20.1-47.3.12_mapped_parchment_2023.09.03-1.20.1-recomp.jar:?]
	at net.minecraft.world.level.lighting.LevelLightEngine.runLightUpdates(LevelLightEngine.java:48) ~[forge-1.20.1-47.3.12_mapped_parchment_2023.09.03-1.20.1-recomp.jar:?]
     */


    public JemsCampfires() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::setup);
//        eventBus.addListener(this::doClientStuff);
        eventBus.addListener(this::buildContents);

        ModItems.register(eventBus);

        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SERVER_SPEC);
        ServerConfig.loadConfig(ServerConfig.SERVER_SPEC, FMLPaths.GAMEDIR.get()
                .resolve(FMLConfig.defaultConfigPath()).resolve(MOD_ID + "-server.toml"));

        StarlightCompat.init();
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            JCPacketHandler.register();
        });
    }

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
