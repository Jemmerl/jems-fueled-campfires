package com.jemmerl.jemscampfires.init.fueloverrides;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;

import java.util.List;

public class FuelOverrides {

    public static final Codec<FuelOverrides> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(FuelOverrides::getReplace),
            FuelOverrideEntry.CODEC.listOf().fieldOf("fuel_overrides").forGetter(FuelOverrides::getEntries)
            /*ResourceLocation.CODEC.listOf().fieldOf("entries").forGetter(FuelOverrideEntries::getEntries)*/
    ).apply(instance, FuelOverrides::new));

    //TagEntry.CODEC.listOf().fieldOf("values").forGetter(TagFile::entries)

    private final boolean replace;
    private final List<FuelOverrideEntry> entries;

    public FuelOverrides(final boolean replace, final List<FuelOverrideEntry> entries) {
        System.out.println("created file");
        this.replace = replace;
        this.entries = entries;
    }

    public boolean getReplace() {
        return replace;
    }
    public List<FuelOverrideEntry> getEntries() {
        return entries;
    }


}
