package com.jemmerl.jemscampfires.init.fueloverrides;

import com.jemmerl.jemscampfires.JemsCampfires;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FuelOverrideEntry {

    private final int fuelticks;
    private Either<ResourceLocation, List<ResourceLocation>> rawValues;
    private Either<Tags.IOptionalNamedTag<Item>, List<RegistryKey<Item>>> values = null;

    public static Codec<FuelOverrideEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.intRange(1, 1000000000).optionalFieldOf("fuel_ticks", 1).forGetter(entry -> entry.fuelticks),
                    Codec.either(Codec.STRING.xmap(s -> s.startsWith("#") ? new ResourceLocation(s.substring(1)) : null,
                                            ResourceLocation::toString),
                                    Codec.STRING.xmap(ResourceLocation::new, ResourceLocation::toString).listOf())
                            .fieldOf("input").forGetter(entry -> entry.rawValues))
            .apply(instance, FuelOverrideEntry::new));

    public FuelOverrideEntry(int fuelticks, Either<ResourceLocation, List<ResourceLocation>> rawValues) {
        this.fuelticks = fuelticks;
        this.rawValues = rawValues;
    }

    public boolean contains(Item item, RegistryKey<Item> itemRegistryKey) {
        return values.map((tag -> tag.contains(item)), (list -> list.contains(itemRegistryKey)));
    }

    public Either<Tags.IOptionalNamedTag<Item>, List<RegistryKey<Item>>> values() {
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
                        Tags.IOptionalNamedTag<Item> itemTag = ItemTags.createOptional(rl);
                        if (itemTag.isDefaulted()) {
                            JemsCampfires.LOGGER.error("Tag {} not found for fuel override with fuel value {}.", rl, fuelticks);
                        }
                        values = Either.left(itemTag);
                    })
                    .ifRight(resourceLocationList -> values = Either.right(new ArrayList<>(resourceLocationList.stream()
                            .map(resourceLocation -> {
                                Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
                                if (item == null || item == Items.AIR) {
                                    JemsCampfires.LOGGER.error("Item {} not found for fuel override with fuel value {}.", resourceLocation, fuelticks);
                                    return null;
                                }
                                return RegistryKey.getOrCreateKey(Registry.ITEM_KEY, resourceLocation);
                            }).filter(Objects::nonNull).collect(Collectors.toList()))));
            rawValues = Either.right(new ArrayList<>());
        }
    }

}