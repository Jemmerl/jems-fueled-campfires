package com.jemmerl.jemscampfires.items;

import com.jemmerl.jemscampfires.JemsCampfires;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(JemsCampfires.MOD_ID);

    public static final DeferredItem<Item> FIRE_POKER = ITEMS.register("fire_poker",
            () -> new FirePoker(new Item.Properties().stacksTo(1).fireResistant()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
