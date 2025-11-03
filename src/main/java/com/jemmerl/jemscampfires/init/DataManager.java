package com.jemmerl.jemscampfires.init;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jemmerl.jemscampfires.util.MergeableCodecDataManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class DataManager extends SimpleJsonResourceReloadListener {
    public static final MergeableCodecDataManager<FlavorTag, Set<ResourceLocation>> DATA_LOADER = new MergeableCodecDataManager<>(
            "flavors",
            FlavorTag.CODEC,
            raws -> processFlavorTags(raws));

    public DataManager(Gson pGson, String pDirectory) {
        super(pGson, pDirectory);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {

    }

    public class FlavorTag {
        public static final Codec<FlavorTag> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(FlavorTag::getReplace),
                ResourceLocation.CODEC.listOf().fieldOf("values").forGetter(FlavorTag::getValues)
        ).apply(instance, FlavorTag::new));

        private final boolean replace; public boolean getReplace() {return this.replace;}
        private final List<ResourceLocation> values; public List<ResourceLocation> getValues() {return this.values;}

        public FlavorTag(final boolean replace, final List<ResourceLocation> values) {
            this.replace = replace;
            this.values = values;
        }
    }
}
