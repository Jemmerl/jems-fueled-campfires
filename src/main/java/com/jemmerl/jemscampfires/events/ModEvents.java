package com.jemmerl.jemscampfires.events;

import com.jemmerl.jemscampfires.JemsCampfires;
import com.jemmerl.jemscampfires.items.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JemsCampfires.MOD_ID)
public class ModEvents {

    //@SubscribeEvent
    public static void buildContents(final CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.FIRE_POKER);
        }
    }

}
