package com.jemmerl.jemscampfires.init.fueloverrides;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

import java.util.function.Function;
import java.util.function.Supplier;

public class FuelOverrideEntry {

    private static Function<Integer, DataResult<Integer>> checkMin() {
        return value -> {
            if (value.compareTo(0) >= 0) { return DataResult.success(value); }
            return DataResult.error(() -> "Value " + value + " outside of range [" + 1 + "]", value);
        };
    }

    private static Supplier<HolderSet<Item>> buildTagSupplier(TagKey<?> tagKey) {
        return () -> (HolderSet<Item>) BuiltInRegistries.ITEM.getTagOrEmpty((TagKey<Item>) tagKey);
    }

    private static Supplier<HolderSet<Item>> buildItemSupplier(HolderSet<Item> set) {
        return () -> set;
    }

    //(TagKey<Item>) tagKey

    //private static Supplier<HolderSet<Item>> buildSupplier(TagKey<?> tagKey, RegistryAccess registryAccess) {
    //        return () -> (HolderSet<Item>)registryAccess.registryOrThrow(Registries.ITEM).getTag(((TagKey<Item>) tagKey)).get();
    //    }

    public static Codec<FuelOverrideEntry> CODEC = null;

    public static void buildCodec(RegistryAccess registryAccess) {
        if (CODEC != null) return;

        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.INT.flatXmap(checkMin(), checkMin()).fieldOf("fuel_ticks").forGetter(FuelOverrideEntry::getFuelticks),
                        Codec.either(
                                TagKey.hashedCodec(BuiltInRegistries.ITEM.key()),
                                HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(),false))
                                .xmap(
                                        either -> either.map(FuelOverrideEntry::buildTagSupplier, FuelOverrideEntry::buildItemSupplier),
                                        supplier -> Either.right(supplier.get()))
                                .fieldOf("input").forGetter(FuelOverrideEntry::getValues))
                                .apply(instance, FuelOverrideEntry::new));
    }


    // valid item holder set codec
    //HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(),false)
    //                        .xmap(holders -> buildItemSupplier(holders, registryAccess),
    //                                Supplier::get)


    // valid tagkey holder set codec
    //TagKey.hashedCodec(BuiltInRegistries.ITEM.key())
    //                                .xmap(tagKey -> buildTagSupplier(tagKey, registryAccess),
    //                                        holderSetSupplier -> null)
    //

    //registryAccess.registries().filter(reg -> reg.key() == Registries.ITEM).findFirst()

    /*
    public record TagWithCount(TagKey<Item> tag, int count) {
    public static final Codec<TagWithCount> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagWithCount::tag),
            Codec.INT.fieldOf("count").forGetter(TagWithCount::count).validate((i) -> i >= 1 ? DataResult.success(i) : DataResult.error(() -> "Count must be >= 1."))
        ).apply(instance, TagWithCount::new)
    );
}

public record TaggableItemStack(Either<ItemStack, TagWithCount> either) {
    public static final Codec<TaggableItemStack> CODEC = Codec.either(ItemStack.CODEC, TagWithCount.CODEC).xmap(TaggableItemStack::new, taggable -> taggable.either);

    public ItemStack computeStack(RandomSource rand, HolderLookup.Provider lookupProvider) {
        // compute an item, either copy of stack or generated from tag
    }
}

public static final MapCodec<GrindingRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter((r) -> r.ingredient),
                TaggableItemStack.CODEC.fieldOf("output").forGetter((r) -> r.output),
                Codec.INT.fieldOf("ticks").validate((i) -> i >= 1 ? DataResult.success(i) : DataResult.error(() -> "Ticks must be >= 1.")).forGetter((r) -> r.ticks)
                ).apply(builder, GrindingRecipe::new));
     */


    // EITHER codec
    //Codec.either(
    //                                HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(),false),
    //                                TagKey.hashedCodec())
    //                                .xmap(either -> either.left().isPresent() ? either.left().get() : either.right().get(),
    //                                        from -> from.unwrap())
    //                                .fieldOf("input").forGetter(FuelOverrideEntry::getValues)

    /*
    //      {
//        "fuel_ticks": 1000,
//        "input": [
//          "minecraft:diamond"
//        ]
//      },
//      {
//        "fuel_ticks": 2000,
//        "input": [
//          "minecraft:stone",
//          "minecraft:granite",
//          "minecraft:andesite"
//        ]
//      },
     */

    //HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(),false)
    //                                        .fieldOf("items").forGetter(FuelOverrideEntry::getValues)

    //TagKey.hashedCodec(Registries.ITEM).flatComapMap(
    //                                       tagKey -> (HolderSet<Item>)registryAccess.registryOrThrow(Registries.ITEM).getTag(tagKey).get(),
    //                                       holders -> null
    //                                       ).fieldOf("tag")
    //                                        .forGetter(FuelOverrideEntry::getValues

    //registryAccess.registry(Registries.ITEM).get().getTag()

    //                        Codec.either(
    //                                HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(),false)
    //                                        .fieldOf("items").forGetter(FuelOverrideEntry::getValues),
    //                                RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("tag")
    //                                        .forGetter(FuelOverrideEntry::getValues))


    // VALID CODEC
    //        CODEC = RecordCodecBuilder.create(instance -> instance.group(
    //                        Codec.INT.flatXmap(checkMin(), checkMin()).fieldOf("fuel_ticks").forGetter(FuelOverrideEntry::getFuelticks),
    //                HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(),false)
    //                        .fieldOf("items").forGetter(FuelOverrideEntry::getValues)
    //                        ).apply(instance, FuelOverrideEntry::new));



    //TagEntry.CODEC.listOf().fieldOf("values").forGetter(TagFile::entries)

    private final int fuelticks;
    private final Supplier<HolderSet<Item>> values;

    public FuelOverrideEntry(final int fuelticks, final Supplier<HolderSet<Item>> values) {
        System.out.println("created entry");
        this.fuelticks = fuelticks;
        this.values = values;
    }

    public int getFuelticks() {
        return fuelticks;
    }
    public Supplier<HolderSet<Item>> getValues() {
        return values;
    }


    //  "fuel_overrides": [
    //    {
    //      "fuel_ticks": 1000,
    //      "input": [
    //        "minecraft:iron_ingot",
    //        "minecraft:diamond"
    //      ]
    //    },
    //    {
    //      "fuel_ticks": 2000,
    //      "input": [
    //        "minecraft:stone"
    //      ]
    //    },
    //    {
    //      "fuel_ticks": 4000,
    //      "input": [
    //        "minecraft:spruce_trapdoor",
    //        "minecraft:oak_trapdoor"
    //      ]
    //    }
    //  ]


    //"fuel_overrides": [
    //
    //      {
    //        "fuel_ticks": 2000,
    //        "input": "#minecraft:stone_buttons"
    //      },
    //      {
    //        "fuel_ticks": 4000,
    //        "input": "#minecraft:logs"
    //      }
    //    ]


}
