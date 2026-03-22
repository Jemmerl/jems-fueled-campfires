package com.jemmerl.jemscampfires.init.fueloverrides;

import com.google.gson.JsonPrimitive;
import com.jemmerl.jemscampfires.JemsCampfires;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

public class FuelOverrideEntry {

    private final int fuelticks;
    private Either<ResourceLocation, HolderSet<Item>> rawValues;
    private HolderSet<Item> values = null;

    public static Codec<FuelOverrideEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.flatXmap(checkMin(), checkMin()).fieldOf("fuel_ticks").forGetter(entry -> entry.fuelticks),
                    Codec.either(Codec.STRING.xmap(s -> s.startsWith("#") ? new ResourceLocation(s.substring(1)) : null,
                                           ResourceLocation::toString),
                    HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false))
                            .fieldOf("input").forGetter(entry -> entry.rawValues))
            .apply(instance, FuelOverrideEntry::new));

    //Either<TagKey<Item>, HolderSet<Item>> values

//    private static Supplier<HolderSet<Item>> buildTagSupplier(TagKey<?> tagKey) {
//        return () -> (HolderSet<Item>) BuiltInRegistries.ITEM.getTagOrEmpty((TagKey<Item>) tagKey);
//    }
//
//    private static Supplier<HolderSet<Item>> buildItemSupplier(HolderSet<Item> set) {
//        return () -> set;
//    }

//    private static Codec<String> RL_STRING_CODEC = new PrimitiveCodec<String>() {
//        @Override
//        public <T> DataResult<String> read(DynamicOps<T> ops, T input) {
//            if (input instanceof JsonPrimitive primitive) {
//                if (primitive.getAsJsonPrimitive().isString() || primitive.getAsJsonPrimitive().isNumber() && ops.compressMaps()) {
//                    String str = primitive.getAsString();
//                    if (str.startsWith("#")) return DataResult.success(str);
//                }
//            }
//            return DataResult.error(() -> "Not a string: " + input);
//        }
//
//        @Override
//        public <T> T write(DynamicOps<T> ops, String value) {
//            return ops.createString(value);
//        }
//    };

            //RegistryCodecs.homogeneousList(Registries.ITEM)

    //HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false)

            //   Codec.either(TagKey.hashedCodec(BuiltInRegistries.ITEM.key()),
    //                                    HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false))

    public FuelOverrideEntry(int fuelticks, Either<ResourceLocation, HolderSet<Item>> rawValues) {
        this.fuelticks = fuelticks;
        this.rawValues = rawValues;
    }


//    public static Codec<FuelOverrideEntry> CODEC = null;
//
//    public static void buildCodec(RegistryAccess registryAccess) {
//        if (CODEC != null) return;
//
//        CODEC = RecordCodecBuilder.create(instance -> instance.group(
//                        Codec.INT.flatXmap(checkMin(), checkMin()).fieldOf("fuel_ticks").forGetter(FuelOverrideEntry::fuelticks),
//                        Codec.either(
//                                        TagKey.hashedCodec(BuiltInRegistries.ITEM.key()),
//                                        HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false))
////                                .xmap(
////                                        either -> either.map(FuelOverrideEntry::buildTagSupplier, FuelOverrideEntry::buildItemSupplier),
////                                        supplier -> Either.right(supplier.get()))
//                                .fieldOf("input").forGetter(FuelOverrideEntry::values))
//                .apply(instance, FuelOverrideEntry::new));
//    }

    private static Function<Integer, DataResult<Integer>> checkMin() {
        return value -> {
            if (value.compareTo(0) >= 0) {
                return DataResult.success(value);
            }
            return DataResult.error(() -> "A custom fuel value " + value + "is negative.", value);
        };
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
                        TagKey<Item> itemTagKey = TagKey.create(ForgeRegistries.Keys.ITEMS, rl);
                        if (ForgeRegistries.ITEMS.tags().getTag(itemTagKey).isBound()) {
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

//      public boolean contains(ItemStack itemStack) {
//        return ((values.left().isPresent() && itemStack.is(values.left().get())) ||
//                (values.right().isPresent() &&
//                        values.right().get().contains(BuiltInRegistries.ITEM.wrapAsHolder(itemStack.getItem()))));
//    }


    //  "fuel_overrides": [
//        {
//          "fuel_ticks": 1000,
//          "input": [
//            "minecraft:iron_ingot",
//            "minecraft:diamond"
//          ]
//        },
//        {
//          "fuel_ticks": 2000,
//          "input": [
//            "minecraft:stone"
//          ]
//        },
//        {
//          "fuel_ticks": 4000,
//          "input": [
//            "minecraft:spruce_trapdoor",
//            "minecraft:oak_trapdoor"
//          ]
//        }
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



    /*

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


    // EITHER codec
    //Codec.either(
    //                                HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(),false),
    //                                TagKey.hashedCodec())
    //                                .xmap(either -> either.left().isPresent() ? either.left().get() : either.right().get(),
    //                                        from -> from.unwrap())
    //                                .fieldOf("input").forGetter(FuelOverrideEntry::getValues)

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

     */

}
