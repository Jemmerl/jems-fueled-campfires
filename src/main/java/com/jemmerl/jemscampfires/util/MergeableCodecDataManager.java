/*
// Yeah, I know my project is already MIT License, but since I am largely copy-pasting (some editing by me) this class,
// it feels right to put this front-and-foremost on it to best apply the wishes of its author! -Jem

The MIT License (MIT)

Copyright (c) 2020 Joseph Bettendorff a.k.a. "Commoble"

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.jemmerl.jemscampfires.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Reader;
import java.util.*;
import java.util.function.Function;

// Credit to Commoble's Data Buddy
// https://github.com/Commoble/databuddy
// Modified to pass a RegistryAccess upon creation. May no longer be needed.

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Joseph Bettendorff aka "Commoble"
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 */

/**
 * Generic data loader for Codec-parsable data.
 * This works best if initialized during your mod's construction.
 * After creating the manager, subscribeAsSyncable can optionally be called on it to subscribe the manager
 * to the forge events necessary for syncing datapack data to clients.
 * @param <RAW> The type of the objects that the codec is parsing jsons as
 * @param <FINE> The type of the object we get after merging the parsed objects. Can be the same as RAW
 */
public class MergeableCodecDataManager<RAW, FINE> extends SimplePreparableReloadListener<Map<ResourceLocation, FINE>>
{
    private static final Logger LOGGER = LogManager.getLogger();

    protected static final String JSON_EXTENSION = ".json";
    protected static final int JSON_EXTENSION_LENGTH = JSON_EXTENSION.length();

    /** the loaded data **/
    protected Map<ResourceLocation, FINE> data = new HashMap<>();

    private final RegistryAccess registryAccess;

    private final String folderName;
    private final Codec<RAW> codec;
    private final Function<List<RAW>, FINE> merger;

    /**
     * Initialize a data manager with the given folder name, codec, and merger
     * @param folderName The name of the folder to load data from,
     * e.g. "cheeses" would load data from "data/modid/cheeses" for all modids.
     * Can include subfolders, e.g. "cheeses/sharp"
     * @param codec A codec that will be used to parse jsons. See drullkus's codec primer for help on creating these:
     * https://gist.github.com/Drullkus/1bca3f2d7f048b1fe03be97c28f87910
     * @param merger A merging function that uses a list of java-objects-that-were-parsed-from-json to create a final object.
     * The list contains all successfully-parsed objects with the same ID from all mods and datapacks.
     * (for a json located at "data/modid/folderName/name.json", the object's ID is "modid:name")
     * As an example, consider vanilla's Tags: mods or datapacks can define tags with the same modid:name id,
     * and then all tag jsons defined with the same ID are merged additively into a single set of items, etc
     */
    public MergeableCodecDataManager(RegistryAccess registryAccess, final String folderName, Codec<RAW> codec, final Function<List<RAW>, FINE> merger) {
        this.registryAccess = registryAccess;

        this.folderName = folderName;
        this.codec = codec;
        this.merger = merger;
    }

    /**
     * @return The immutable map of data entries
     */
    public Map<ResourceLocation, FINE> getData() {
        return this.data;
    }

    /** Off-thread processing (can include reading files from hard drive) **/
    @Override
    protected Map<ResourceLocation, FINE> prepare(final ResourceManager resourceManager, final ProfilerFiller profiler) {
        LOGGER.info("Beginning loading of data for data loader: {}", this.folderName);
        final Map<ResourceLocation, FINE> map = new HashMap<>();

        Map<ResourceLocation,List<Resource>> resourceStacks = resourceManager.listResourceStacks(this.folderName, id -> id.getPath().endsWith(JSON_EXTENSION));
        for (var entry : resourceStacks.entrySet()) {
            List<RAW> raws = new ArrayList<>();
            ResourceLocation fullId = entry.getKey();
            String fullPath = fullId.getPath(); // includes folderName/ and .json
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    fullId.getNamespace(),
                    fullPath.substring(this.folderName.length() + 1, fullPath.length() - JSON_EXTENSION_LENGTH));

            for (Resource resource : entry.getValue()) {
                try(Reader reader = resource.openAsReader()) {
                    JsonElement jsonElement = JsonParser.parseReader(reader);
                    this.codec.parse(RegistryOps.create(JsonOps.INSTANCE, registryAccess), jsonElement)
                            .resultOrPartial(errorMsg -> LOGGER.error("Error deserializing json {} in folder {} from pack {}: {}", id, this.folderName, resource.sourcePackId(), errorMsg))
                            .ifPresent(raws::add);
                }
                catch(Exception e) {
                    LOGGER.error(String.format(Locale.ENGLISH, "Error reading resource %s in folder %s from pack %s: ", id, this.folderName, resource.sourcePackId()), e);
                }
            }
            map.put(id, merger.apply(raws));
        }

        LOGGER.info("Data loader for {} loaded {} finalized objects", this.folderName, this.data.size());
        return Map.copyOf(map);
    }

    /** Main-thread processing, runs after prepare concludes **/
    @Override
    protected void apply(final Map<ResourceLocation, FINE> processedData, final ResourceManager resourceManager, final ProfilerFiller profiler) {
        // now that we're on the main thread, we can finalize the data
        this.data = processedData;
    }

    // I don't use this, so why port it? Hehe.
//    /**
//     * This should be called at most once, during construction of your mod
//     * Calling this method automatically subscribes a packet-sender to {@link OnDatapackSyncEvent}.
//     * @param <PACKET> the packet type that will be sent on the given channel
//     * @param channel The networking channel of your mod
//     * @param packetFactory  A packet constructor or factory method that converts the given map to a packet object to send on the given channel
//     * @return this manager object
//     */
//    public <PACKET> MergeableCodecDataManager<RAW, FINE> subscribeAsSyncable(final SimpleChannel channel,
//                                                                             final Function<Map<ResourceLocation, FINE>, PACKET> packetFactory) {
//        NeoForge.EVENT_BUS.addListener(this.getDatapackSyncListener(channel, packetFactory));
//        return this;
//    }
//
//    /** Generate an event listener function for the on-datapack-sync event **/
//    private <PACKET> Consumer<OnDatapackSyncEvent> getDatapackSyncListener(final SimpleChannel channel,
//                                                                           final Function<Map<ResourceLocation, FINE>, PACKET> packetFactory) {
//        return event -> {
//            ServerPlayer player = event.getPlayer();
//            PACKET packet = packetFactory.apply(this.data);
//            PacketTarget target = player == null
//                    ? PacketDistributor.ALL.noArg()
//                    : PacketDistributor.PLAYER.with(() -> player);
//            channel.send(target, packet);
//        };
//    }
}
