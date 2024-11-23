package com.jemmerl.jemscampfires.init;

import com.jemmerl.jemscampfires.JemsCampfires;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

public class JCTags {
    public static final TagKey<Item> JC_ETERNAL = createTag("jc_eternal_fuels");

    private static TagKey<Item> createTag(String name) {
        return ItemTags.create(new ResourceLocation(JemsCampfires.MOD_ID, name));
    }
}
