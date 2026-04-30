package com.jemmerl.jemscampfires.init.fueloverrides;

import com.jemmerl.jemscampfires.JemsCampfires;
import com.jemmerl.jemscampfires.util.MergeableCodecDataManager;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = JemsCampfires.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FuelOverrideDataManager {

    // load and merge high level files, then merge individual entries with the same fuel ticks
    private static final ObjectOpenHashSet<FuelOverrideEntry> fuelOverrides = new ObjectOpenHashSet<>();
    private static boolean reloadCache = true;

    // Caching
    private static final int CACHE_SIZE = 64; // TODO: I am sure an optimal value of this can be found.
    private static Reference2IntOpenHashMap<RegistryKey<Item>> unrolledMap = null;
    private static final Reference2IntLinkedOpenHashMap<RegistryKey<Item>> cache = initCache();

    private static Reference2IntLinkedOpenHashMap<RegistryKey<Item>> initCache() {
        Reference2IntLinkedOpenHashMap<RegistryKey<Item>> c = new Reference2IntLinkedOpenHashMap<>(CACHE_SIZE);
        c.defaultReturnValue(-1);
        return c;
    }

    @SubscribeEvent
    public static void reloadFuelOverrides(TagsUpdatedEvent event) {
        fuelOverrides.clear();
        invalidateCache();
        fuelOverrides.addAll(DATA_LOADER.getData()
                .getOrDefault(new ResourceLocation(JemsCampfires.MOD_ID, "fuel_overrides"),
                        Collections.emptyList()));
    }

    public static int getCustomFuelVal(Item item) {
        if (reloadCache) reloadCacheMap();
        RegistryKey<Item> itemRegistryKey = item2RegistryKey(item);

        if (unrolledMap == null) {
            int fuelVal = cache.getAndMoveToFirst(itemRegistryKey);
            if (fuelVal > -1) return fuelVal; // Returns -1 for nothing in cache, returns 0 for in cache but no custom
        } else {
            return unrolledMap.getInt(itemRegistryKey); // If the map is populated, return the result or default 0 (no custom).
        }

        // Should only ever be reached if the cache is being used
        for (FuelOverrideEntry entry : fuelOverrides) {
            if (entry.contains(item, itemRegistryKey)) {
                int fuelVal = entry.fuelTicks();
                put(itemRegistryKey, fuelVal);
                return fuelVal;
            }
        }
        put(itemRegistryKey, 0);
        return 0;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                Cache Stuff                                                  //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void put(RegistryKey<Item> itemRegistryKey, int fuelVal) {
        if (unrolledMap == null) {
            if (cache.size() >= CACHE_SIZE) {
                cache.removeLastInt();
            }
            cache.putAndMoveToFirst(itemRegistryKey, fuelVal);
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

    static Reference2IntOpenHashMap<RegistryKey<Item>> unroll() {
        Reference2IntOpenHashMap<RegistryKey<Item>> unrolled = new Reference2IntOpenHashMap<>();
        for (FuelOverrideEntry entry : fuelOverrides) {
            entry.values()
                    .ifLeft(itemOptionalTag -> itemOptionalTag.getAllElements()
                            .forEach(itemEntry -> processItemRegistryKey(item2RegistryKey(itemEntry), entry.fuelTicks(), unrolled)))
                    .ifRight(registryKeyList -> registryKeyList
                            .forEach(registryKey -> processItemRegistryKey(registryKey, entry.fuelTicks(), unrolled)));
            if (unrolled.size() > (CACHE_SIZE * 4)) return null; // Check during unrolling to catch a huge unroll early
        }
        return unrolled;
    }

    private static RegistryKey<Item> item2RegistryKey(Item item) {
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(item);
        if (rl == null || rl.getPath().equals("air")) {
            rl = ForgeRegistries.ITEMS.getDefaultKey();
            JemsCampfires.LOGGER.warn("Item {} was null or \"air\" when unrolling fuel override entries.", item);
        }
        return RegistryKey.getOrCreateKey(Registry.ITEM_KEY, rl);
    }

    private static void processItemRegistryKey(RegistryKey<Item> itemRegistryKey, int fuelVal, Reference2IntOpenHashMap<RegistryKey<Item>> unrolled) {
        int prev = unrolled.put(itemRegistryKey, fuelVal);
        if (prev == 0) return; // No value previously there, all is good.
        if (prev == fuelVal) JemsCampfires.LOGGER.info("Noticed a duplicate fuel entry with the same value: {}. There may be more.", itemRegistryKey); // Overwrote the exact same value. No noticable difference.
        else JemsCampfires.LOGGER.warn("Noticed an overwritten fuel entry: {}, {} -> {}. There may be more.", itemRegistryKey, prev, fuelVal); // Replaced a different value. Potentially unintended and unpredictable.
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final MergeableCodecDataManager<FuelOverrides, List<FuelOverrideEntry>> DATA_LOADER =
            new MergeableCodecDataManager<FuelOverrides, List<FuelOverrideEntry>>(
                    "fuel_overrides",
                    FuelOverrides.CODEC,
                    FuelOverrideDataManager::processOverrides);

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
        return mergeEntries(raw.getReplace() ? new ArrayList<>() : entries, raw.getEntries());
    }

    public static List<FuelOverrideEntry> mergeEntries(final List<FuelOverrideEntry> currEntries, final List<FuelOverrideEntry> rawEntries) {
        List<FuelOverrideEntry> list = new ArrayList<>();
        list.addAll(currEntries);
        list.addAll(rawEntries);
        return list;
    }
}