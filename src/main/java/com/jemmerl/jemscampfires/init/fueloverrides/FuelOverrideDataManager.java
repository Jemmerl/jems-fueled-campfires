package com.jemmerl.jemscampfires.init.fueloverrides;

import com.jemmerl.jemscampfires.JemsCampfires;
import com.jemmerl.jemscampfires.util.MergeableCodecDataManager;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EventBusSubscriber(modid = JemsCampfires.MOD_ID)
public class FuelOverrideDataManager {

    // load and merge high level files, then merge individual entries with the same fuel ticks
    private static final ObjectOpenHashSet<FuelOverrideEntry> fuelOverrides = new ObjectOpenHashSet<>();
    private static boolean reloadCache = true;

    // Caching
    private static final int CACHE_SIZE = 64; // TODO: I am sure an optimal value of this can be found.
    private static Reference2IntOpenHashMap<Item> unrolledMap = null;
    private static final Reference2IntLinkedOpenHashMap<Item> cache = initCache();

    private static Reference2IntLinkedOpenHashMap<Item> initCache() {
        Reference2IntLinkedOpenHashMap<Item> c = new Reference2IntLinkedOpenHashMap<>(CACHE_SIZE);
        c.defaultReturnValue(-1);
        return c;
    }

    @SubscribeEvent
    public static void reloadFuelOverrides(TagsUpdatedEvent event) {
        if (event.shouldUpdateStaticData()) {
            fuelOverrides.clear();
            invalidateCache();
            fuelOverrides.addAll(DATA_LOADER.getData()
                    .getOrDefault(ResourceLocation.fromNamespaceAndPath(JemsCampfires.MOD_ID, "fuel_overrides"),
                            Collections.emptyList()));
        }
    }

    public static int getCustomFuelVal(Item item) {
        if (reloadCache) reloadCacheMap();

        if (unrolledMap == null) {
            int fuelVal = cache.getAndMoveToFirst(item);
            if (fuelVal > -1) return fuelVal; // Returns -1 for nothing in cache, returns 0 for in cache but no custom
        } else {
            return unrolledMap.getInt(item); // If the map is populated, return the result or default 0 (no custom).
        }

        // Should only ever be reached if the cache is being used
        for (FuelOverrideEntry entry : fuelOverrides) {
            if (entry.contains(item)) {
                int fuelVal = entry.fuelTicks();
                put(item, fuelVal);
                return fuelVal;
            }
        }
        put(item, 0);
        return 0;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                Cache Stuff                                                  //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void put(Item item, int fuelVal) {
        if (unrolledMap == null) {
            if (cache.size() >= CACHE_SIZE) {
                cache.removeLastInt();
            }
            cache.putAndMoveToFirst(item, fuelVal);
        }
    }

    private static void invalidateCache() {
        unrolledMap = null;
        cache.clear(); // Always the same size, can reuse object :)
        reloadCache = true;
    }

    private static void reloadCacheMap() {
        unrolledMap = unroll(); // Will be null if the unrolled size is > CACHE_SIZE*4;
        reloadCache = false;
    }

    static Reference2IntOpenHashMap<Item> unroll() {
        Reference2IntOpenHashMap<Item> unrolled = new Reference2IntOpenHashMap<>();
        for (FuelOverrideEntry entry : fuelOverrides) {
            entry.values().unwrap()
                    .ifLeft(itemTagKey -> BuiltInRegistries.ITEM.getTagOrEmpty(itemTagKey)
                            .forEach(itemHolder -> processItemHolder(itemHolder, entry.fuelTicks(), unrolled)))
                    .ifRight(holderList -> holderList
                            .forEach(itemHolder -> processItemHolder(itemHolder, entry.fuelTicks(), unrolled)));
            if (unrolled.size() > (CACHE_SIZE * 4)) return null; // Check during unrolling to catch a huge unroll early
        }
        return unrolled;
    }

    private static void processItemHolder(Holder<Item> itemHolder, int fuelVal, Reference2IntOpenHashMap<Item> unrolled) {
        int prev = unrolled.put(itemHolder.value(), fuelVal);
        if (prev == 0) return; // No value previously there, all is good.
        if (prev == fuelVal) JemsCampfires.LOGGER.info("Noticed a duplicate fuel entry with the same value: {}. There may be more.", itemHolder.value()); // Overwrote the exact same value. No noticable difference.
        else JemsCampfires.LOGGER.warn("Noticed an overwritten fuel entry: {}, {} -> {}. There may be more.", itemHolder.value(), prev, fuelVal); // Replaced a different value. Potentially unintended and unpredictable.
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static MergeableCodecDataManager<FuelOverrides, List<FuelOverrideEntry>> DATA_LOADER = null;

    public static void buildDataLoader(RegistryAccess registryAccess) {
        if (DATA_LOADER != null) return;
        DATA_LOADER = new MergeableCodecDataManager<>(registryAccess, "fuel_overrides",
                FuelOverrides.CODEC, FuelOverrideDataManager::processOverrides);
    }

    public static MergeableCodecDataManager<FuelOverrides, List<FuelOverrideEntry>> getDataLoader() {
        return DATA_LOADER;
    }


    // Takes in all the fuel overrides of the same id (jemscamp:file_name) from every mod, then reduces to a map.
//    public static List<FuelOverrideEntry> combineAllOverrides(final List<FuelOverrides> raws) {
//        return raws.stream().reduce(new ArrayList<>(), FuelOverrideDataManager::processOverrides, List::addAll);
//    }

    public static List<FuelOverrideEntry> processOverrides(final List<FuelOverrides> raws) {
        return raws.stream().reduce(new ArrayList<>(), FuelOverrideDataManager::mergeOverrides, (a,b) -> {
            List<FuelOverrideEntry> list = new ArrayList<>();
            list.addAll(a);
            list.addAll(b);
            return list;
        });
    }

    public static List<FuelOverrideEntry> mergeOverrides(final List<FuelOverrideEntry> entries, final FuelOverrides raw) {
        return mergeEntries(raw.replace() ? new ArrayList<>() : entries, raw.entries());
    }

    public static List<FuelOverrideEntry> mergeEntries(final List<FuelOverrideEntry> currEntries, final List<FuelOverrideEntry> rawEntries) {
        List<FuelOverrideEntry> list = new ArrayList<>();
        list.addAll(currEntries);
        list.addAll(rawEntries);
        return list;
    }
}




