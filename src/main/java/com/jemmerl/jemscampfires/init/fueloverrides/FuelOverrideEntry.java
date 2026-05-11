package com.jemmerl.jemscampfires.init.fueloverrides;

import com.jemmerl.jemscampfires.JemsCampfires;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

public class FuelOverrideEntry {

    private final int fuelticks;
    private Either<ResourceLocation, HolderSet<Item>> rawValues;
    private HolderSet<Item> values = null;

    public static Codec<FuelOverrideEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ExtraCodecs.intRange(1, 1000000000).optionalFieldOf("fuel_ticks", 1).forGetter(entry -> entry.fuelticks),
                    Codec.either(Codec.STRING.xmap(s -> s.startsWith("#") ? ResourceLocation.parse(s.substring(1)) : null,
                                           ResourceLocation::toString),
                    HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false))
                            .fieldOf("input").forGetter(entry -> entry.rawValues))
            .apply(instance, FuelOverrideEntry::new));

    public FuelOverrideEntry(int fuelticks, Either<ResourceLocation, HolderSet<Item>> rawValues) {
        this.fuelticks = fuelticks;
        this.rawValues = rawValues;
    }

    public boolean contains(Item item) {
        return values().contains(BuiltInRegistries.ITEM.wrapAsHolder(item));
    }

    public HolderSet<Item> values() {
        processValuesIfNeeded();
        return values;
    }

    public int fuelTicks() {
        return fuelticks;
    }

    public void processValuesIfNeeded() {
        if (values == null) {
            rawValues
                    .ifLeft(rl -> {
                        TagKey<Item> itemTagKey = TagKey.create(Registries.ITEM, rl);
                        if (BuiltInRegistries.ITEM.getTag(itemTagKey).isPresent()) {
                            this.values = BuiltInRegistries.ITEM.getOrCreateTag(itemTagKey);
                        } else {
                            JemsCampfires.LOGGER.error("Tag {} not found for fuel override with fuel value {}.", itemTagKey, fuelticks);
                            this.values = HolderSet.direct();
                        }
                    })
                    .ifRight(holderSet -> values = holderSet);
            rawValues = Either.right(HolderSet.direct());
        }
    }

}
