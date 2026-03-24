package com.jemmerl.jemscampfires.init.fueloverrides;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record FuelOverrides(boolean replace, List<FuelOverrideEntry> entries) {

    public static final Codec<FuelOverrides> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(FuelOverrides::replace),
            FuelOverrideEntry.CODEC.listOf().fieldOf("fuel_overrides").forGetter(FuelOverrides::entries)
    ).apply(instance, FuelOverrides::new));

}
