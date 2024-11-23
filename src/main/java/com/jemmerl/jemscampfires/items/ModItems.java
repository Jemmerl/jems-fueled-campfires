package com.jemmerl.jemscampfires.items;

import com.jemmerl.jemscampfires.JemsCampfires;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, JemsCampfires.MOD_ID);

    public static final RegistryObject<Item> FIRE_POKER = ITEMS.register("fire_poker",
            () -> new FirePoker(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS).stacksTo(1).fireResistant()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
