package com.jemmerl.jemscampfires.init.fueloverrides;

import com.jemmerl.jemscampfires.JemsCampfires;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FuelOverrideEntry {

    private final int fuelticks;
    private Either<ResourceLocation, List<ResourceLocation>> rawValues;
    private HolderSet<Item> values = null;

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

    public boolean contains(Item item) {
        Optional<Holder<Item>> holderSupplier = ForgeRegistries.ITEMS.getHolder(item);
        if (holderSupplier.isPresent()) return values().contains(holderSupplier.get());
        return false;
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
                        TagKey<Item> itemTagKey = TagKey.create(ForgeRegistries.Keys.ITEMS, rl);
                        if (ForgeRegistries.ITEMS.tags().isKnownTagName(itemTagKey)) {
                            this.values = Registry.ITEM.getOrCreateTag(itemTagKey);
                        } else {
                            JemsCampfires.LOGGER.error("Tag {} not found for fuel override with fuel value {}.", itemTagKey, fuelticks);
                            this.values = HolderSet.direct();
                        }
                    })
                    .ifRight(resourceLocationList -> this.values = HolderSet.direct(resourceLocationList.stream()
                            .map(resourceLocation -> {
                                Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
                                if (item == null || item == Items.AIR) {
                                    JemsCampfires.LOGGER.error("Item {} not found for fuel override with fuel value {}.", resourceLocation, fuelticks);
                                    return null;
                                }
                                Optional<Holder<Item>> holderSupplier = ForgeRegistries.ITEMS.getHolder(item);
                                if (holderSupplier.isPresent()) return holderSupplier.get();
                                return null;
                            }).filter(Objects::nonNull).collect(Collectors.toList())));
            rawValues = Either.right(new ArrayList<>());
        }
    }

}
