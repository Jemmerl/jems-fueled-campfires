package com.jemmerl.jemscampfires.init.fueloverrides;

import com.jemmerl.jemscampfires.JemsCampfires;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FuelOverrideEntry {

    private final int fuelticks;
    private Either<ResourceLocation, List<ResourceLocation>> rawValues;
    private Either<Tags.IOptionalNamedTag<Item>, List<ResourceKey<Item>>> values = null;

    public static final Codec<Integer> INT_RANGE = ExtraCodecs.intRangeWithMessage(0, 1000000000, (p_184429_) -> {
        return "Value must be 1-1000000000: " + p_184429_;
    });

    public static Codec<FuelOverrideEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    INT_RANGE.optionalFieldOf("fuel_ticks", 1).forGetter(entry -> entry.fuelticks),
                    Codec.either(Codec.STRING.xmap(s -> s.startsWith("#") ? new ResourceLocation(s.substring(1)) : null,
                                            ResourceLocation::toString),
                                    Codec.STRING.xmap(ResourceLocation::new, ResourceLocation::toString).listOf())
                            .fieldOf("input").forGetter(entry -> entry.rawValues))
            .apply(instance, FuelOverrideEntry::new));

    public FuelOverrideEntry(int fuelticks, Either<ResourceLocation, List<ResourceLocation>> rawValues) {
        this.fuelticks = fuelticks;
        this.rawValues = rawValues;
    }

    public boolean contains(Item item, ResourceKey<Item> itemResourceKey) {
        return values.map((tag -> tag.contains(item)), (list -> list.contains(itemResourceKey)));
    }

    public Either<Tags.IOptionalNamedTag<Item>, List<ResourceKey<Item>>> values() {
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
                                return ResourceKey.create(Registry.ITEM_REGISTRY, resourceLocation);
                            }).filter(Objects::nonNull).collect(Collectors.toList()))));
            rawValues = Either.right(new ArrayList<>());
        }
    }

}