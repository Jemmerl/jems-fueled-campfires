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
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = JemsCampfires.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FuelOverrideDataManager {

    // load and merge high level files, then merge individual entries with the same fuel ticks
    private static final ObjectOpenHashSet<FuelOverrideEntry> fuelOverrides = new ObjectOpenHashSet<>();
    private static boolean reloadCache = true;

    // Caching
    private static final int CACHE_SIZE = 64; // TODO: I am sure an optimal value of this can be found.
    private static Reference2IntOpenHashMap<Item> unrolledMap = null;
    private static final Reference2IntLinkedOpenHashMap<Item> cache = new Reference2IntLinkedOpenHashMap<>(CACHE_SIZE);

    @SubscribeEvent
    public static void reloadFuelOverrides(TagsUpdatedEvent event) {
        if (event.shouldUpdateStaticData()) {
            fuelOverrides.clear();
            invalidateCache();
            fuelOverrides.addAll(DATA_LOADER.getData()
                    .get(new ResourceLocation("jemscampfires", "fuel_overrides")));
        }
    }



    public static int getCustomFuelVal(Item item) {
        if (reloadCache) reloadCacheMap();

        if (unrolledMap == null) {
            int fuelVal = cache.getAndMoveToFirst(item);
            if (fuelVal > 0) return fuelVal; // If the cache is used, check if present- if not, check all entries.
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
        System.out.println("use cache? : " + (unrolledMap == null));
        System.out.println(unrolledMap.toString());
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

    public static void buildDataLoader(ReloadableServerResources reloadableServerResources, RegistryAccess registryAccess) {
        if (DATA_LOADER != null) return;
        DATA_LOADER = new MergeableCodecDataManager<>(reloadableServerResources, registryAccess, "fuel_overrides",
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////







//    public static List<FuelOverrideEntry> combinePartialMaps(List<FuelOverrideEntry> map1, List<FuelOverrideEntry> map2) {
//        List<FuelOverrideEntry> mergedMap = new HashMap<>(map1);
//        mergedMap.putAll(map2);
//        return mergedMap;
//    }
//
//    // Takes in the current group of merged objects, as well as the next to be merged
//    public static List<FuelOverrideEntry> 1processOverrides(final List<FuelOverrideEntry> map, final FuelOverrides raw) {
//        return mergeOverrideEntries(raw.getReplace() ? new HashMap<>() : map, raw.getEntries());
//    }
//
//    //
//    public static List<FuelOverrideEntry> mergeOverrideEntries(final List<FuelOverrideEntry> map, final List<FuelOverrideEntry> entries) {
//        for (FuelOverrideEntry entry : entries) {
//            map.merge(entry.getfuelTicks(), entry, FuelOverrideDataManager::mergeEntries);
//        }
//        return map;
//    }
//
//    // Assumes both entries have the same fuel ticks.
//    private static FuelOverrideEntry mergeEntries(FuelOverrideEntry entry1, FuelOverrideEntry entry2) {
//        Set<ResourceLocation> mergeItemSet = new HashSet<>(entry1.getItems());
//        mergeItemSet.addAll(entry2.getItems());
//        return new FuelOverrideEntry(entry1.getfuelTicks(), new ArrayList<>(mergeItemSet));
//    }

    /*
        public static final MergeableCodecDataManager<FuelOverrides, Map<Integer, FuelOverrideEntry>> DATA_LOADER = new MergeableCodecDataManager<>(
            "fuel_overrides",
            FuelOverrides.CODEC,
            FuelOverrideDataManager::combineAllOverrides);

    public static Map<Integer, FuelOverrideEntry> getData() {
        System.out.println("data get");
        return DATA_LOADER.getData().get(new ResourceLocation("jemscampfires", "fuel_overrides"));
    }



    // Takes in all the fuel overrides of the same id (jemscamp:file_name) from every mod, then reduces to a map.
    public static Map<Integer, FuelOverrideEntry> combineAllOverrides(final List<FuelOverrides> raws) {
        return raws.stream().reduce(new HashMap<>(), FuelOverrideDataManager::processOverrides, FuelOverrideDataManager::combinePartialMaps);
    }

    public static HashMap<Integer, FuelOverrideEntry> combinePartialMaps(HashMap<Integer, FuelOverrideEntry> map1, HashMap<Integer, FuelOverrideEntry> map2) {
        HashMap<Integer, FuelOverrideEntry> mergedMap = new HashMap<>(map1);
        mergedMap.putAll(map2);
        return mergedMap;
    }

    // Takes in the current group of merged objects, as well as the next to be merged
    public static HashMap<Integer, FuelOverrideEntry> processOverrides(final HashMap<Integer, FuelOverrideEntry> map, final FuelOverrides raw) {
        return mergeOverrideEntries(raw.getReplace() ? new HashMap<>() : map, raw.getEntries());
    }

    //
    public static HashMap<Integer, FuelOverrideEntry> mergeOverrideEntries(final HashMap<Integer, FuelOverrideEntry> map, final List<FuelOverrideEntry> entries) {
        for (FuelOverrideEntry entry : entries) {
            map.merge(entry.getfuelTicks(), entry, FuelOverrideDataManager::mergeEntries);
        }
        return map;
    }

    // Assumes both entries have the same fuel ticks.
    private static FuelOverrideEntry mergeEntries(FuelOverrideEntry entry1, FuelOverrideEntry entry2) {
        Set<ResourceLocation> mergeItemSet = new HashSet<>(entry1.getItems());
        mergeItemSet.addAll(entry2.getItems());
        return new FuelOverrideEntry(entry1.getfuelTicks(), new ArrayList<>(mergeItemSet));
    }
     */

}




