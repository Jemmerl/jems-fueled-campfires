package com.jemmerl.jemscampfires.init;

import com.jemmerl.jemscampfires.JemsCampfires;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

public class ModTags {
    public static final Tags.IOptionalNamedTag<Item> JC_ETERNAL = createTag("jc_eternal_fuels");
    public static final Tags.IOptionalNamedTag<Item> JC_BLACKLIST = createTag("jc_blacklist_fuels");

    private static Tags.IOptionalNamedTag<Item> createTag(String name) {
        return ItemTags.createOptional(new ResourceLocation(JemsCampfires.MOD_ID, name));
    }
}
