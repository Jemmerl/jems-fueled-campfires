package com.jemmerl.jemscampfires.init.fueloverrides;

import com.jemmerl.jemscampfires.util.MergeableCodecDataManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.*;

public class FuelOverrideDataManager {
    // load and merge high level files, then merge individual entries with the same fuel ticks

    // TODO add The HU, Bii Biyelgee to playlist

//    private final TagLoader<CommandFunction> tagsLoader = new TagLoader<>(this::getFunction, "tags/functions");

    // TODO replace Map<Int, FuelOvrEntry> with Map<Int, Holder or Whatvr> due to redundancy

    private static MergeableCodecDataManager<FuelOverrides, List<FuelOverrideEntry>> DATA_LOADER = null;

    public static void buildDataLoader(RegistryAccess registryAccess) {
        if (DATA_LOADER != null) return;
        DATA_LOADER = new MergeableCodecDataManager<>(
                registryAccess, "fuel_overrides",
                FuelOverrides.CODEC, FuelOverrideDataManager::processOverrides);
    }

    public static MergeableCodecDataManager<FuelOverrides, List<FuelOverrideEntry>> getDataLoader() {
        return DATA_LOADER;
    }



    public static List<FuelOverrideEntry> getData() {
        System.out.println("data get");
        return DATA_LOADER.getData().get(new ResourceLocation("jemscampfires", "fuel_overrides"));
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
        System.out.println(entries.size());
        return mergeEntries(raw.getReplace()? new ArrayList<>() : entries, raw.getEntries());
    }

    public static List<FuelOverrideEntry> mergeEntries(final List<FuelOverrideEntry> currEntries, final List<FuelOverrideEntry> rawEntries) {
        List<FuelOverrideEntry> list = new ArrayList<>();
        list.addAll(currEntries);
        list.addAll(rawEntries);
        return list;
    }












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
//            map.merge(entry.getFuelticks(), entry, FuelOverrideDataManager::mergeEntries);
//        }
//        return map;
//    }
//
//    // Assumes both entries have the same fuel ticks.
//    private static FuelOverrideEntry mergeEntries(FuelOverrideEntry entry1, FuelOverrideEntry entry2) {
//        Set<ResourceLocation> mergeItemSet = new HashSet<>(entry1.getItems());
//        mergeItemSet.addAll(entry2.getItems());
//        return new FuelOverrideEntry(entry1.getFuelticks(), new ArrayList<>(mergeItemSet));
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
            map.merge(entry.getFuelticks(), entry, FuelOverrideDataManager::mergeEntries);
        }
        return map;
    }

    // Assumes both entries have the same fuel ticks.
    private static FuelOverrideEntry mergeEntries(FuelOverrideEntry entry1, FuelOverrideEntry entry2) {
        Set<ResourceLocation> mergeItemSet = new HashSet<>(entry1.getItems());
        mergeItemSet.addAll(entry2.getItems());
        return new FuelOverrideEntry(entry1.getFuelticks(), new ArrayList<>(mergeItemSet));
    }
     */

}




