package com.jemmerl.jemscampfires.init.fueloverrides;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class FuelOverrides {

    private final boolean replace;
    private final List<FuelOverrideEntry> entries;

    public static final Codec<FuelOverrides> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(FuelOverrides::getReplace),
            FuelOverrideEntry.CODEC.listOf().fieldOf("fuel_overrides").forGetter(FuelOverrides::getEntries)
    ).apply(instance, FuelOverrides::new));

    public FuelOverrides(boolean replace, List<FuelOverrideEntry> entries) {
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