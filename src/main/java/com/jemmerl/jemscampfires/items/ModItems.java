package com.jemmerl.jemscampfires.items;

import com.jemmerl.jemscampfires.JemsCampfires;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, JemsCampfires.MOD_ID);

    public static final RegistryObject<Item> FIRE_POKER = ITEMS.register("fire_poker",
            () -> new FirePoker(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).isImmuneToFire()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
