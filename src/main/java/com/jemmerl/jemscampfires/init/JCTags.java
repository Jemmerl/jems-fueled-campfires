package com.jemmerl.jemscampfires.init;

import com.jemmerl.jemscampfires.JemsCampfires;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class JCTags {
    public static final Tags.IOptionalNamedTag<Item> JC_ETERNAL = createTag("jc_eternal_fuels");

    private static Tags.IOptionalNamedTag<Item> createTag(String name) {
        return ItemTags.createOptional(new ResourceLocation(JemsCampfires.MOD_ID, name));
    }
}
