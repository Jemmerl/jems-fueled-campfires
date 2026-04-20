package com.jemmerl.jemscampfires.init;

import com.jemmerl.jemscampfires.JemsCampfires;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

public class ModTags {
    public static final Tags.IOptionalNamedTag<Item> JC_ETERNAL = createTag("jc_eternal_fuels");
    public static final Tags.IOptionalNamedTag<Item> CF_FILTERED_FUELS = createTag("cf_filtered_fuels");
    public static final Tags.IOptionalNamedTag<Item> SOUL_CF_FILTERED_FUELS = createTag("soul_cf_filtered_fuels");

    private static Tags.IOptionalNamedTag<Item> createTag(String name) {
        return ItemTags.createOptional(new ResourceLocation(JemsCampfires.MOD_ID, name), null);
    }
}
