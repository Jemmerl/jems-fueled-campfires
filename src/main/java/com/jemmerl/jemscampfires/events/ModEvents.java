package com.jemmerl.jemscampfires.events;

import com.jemmerl.jemscampfires.JemsCampfires;
import com.jemmerl.jemscampfires.items.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JemsCampfires.MOD_ID)
public class ModEvents {

    //@SubscribeEvent
    public static void buildContents(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.FIRE_POKER);
        }
    }

}
