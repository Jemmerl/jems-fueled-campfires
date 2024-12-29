package com.jemmerl.jemscampfires.init;

import com.jemmerl.jemscampfires.JemsCampfires;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static final TagKey<Item> JC_ETERNAL = createTag("jc_eternal_fuels");
    public static final TagKey<Item> JC_FUEL_BLACKLIST = createTag("jc_blacklist_fuels");

    private static TagKey<Item> createTag(String name) {
        return ItemTags.create(new ResourceLocation(JemsCampfires.MOD_ID, name));
    }
}
