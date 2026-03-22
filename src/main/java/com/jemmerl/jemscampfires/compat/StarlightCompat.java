package com.jemmerl.jemscampfires.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.ModList;

// Based from XFactHD's Framed Blocks
// https://github.com/XFactHD/FramedBlocks
public class StarlightCompat {

    private static boolean loaded = false;

    public static void init() {
        loaded = ModList.get().isLoaded("starlight");
    }

    // "Using IForgeBlockGetter#getExistingBlockEntity() with Starlight causes chunk-loading deadlocks"
    public static BlockEntity getBlockEntitySafely(BlockGetter world, BlockPos pos) {
        return loaded ? world.getBlockEntity(pos) : world.getExistingBlockEntity(pos);
    }
}

// Other XFactHD Framed Blocks BE getting stuff. I'll deal with understanding this if issues ever crop up/get reported.
/*
    @Nullable
    private static BlockEntity getExistingBlockEntity0(BlockGetter blockGetter, BlockPos pos)
    {
        if (blockGetter instanceof Level level)
        {
            int chunkX = SectionPos.blockToSectionCoord(pos.getX());
            int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
            ChunkSource chunkSource;
            try
            {
                chunkSource = level.getChunkSource();
            }
            catch (Throwable t)
            {
                // Some mods' fake levels think they are funny for throwing in getChunkSource()
                return null;
            }
            LightChunk chunk = chunkSource.getChunkForLighting(chunkX, chunkZ);
            return chunk != null ? getExistingBlockEntity0(chunk, pos) : null;
        }
        else if (blockGetter instanceof LevelChunk chunk)
        {
            return chunk.getBlockEntities().get(pos);
        }
        else if (blockGetter instanceof ImposterProtoChunk chunk)
        {
            return getExistingBlockEntity0(chunk.getWrapped(), pos);
        }
        return blockGetter.getBlockEntity(pos);
    }

        //    public static BlockEntity getBlockEntitySafe(BlockGetter blockGetter, BlockPos pos)
    //    {
    //        if (blockGetter instanceof RenderChunkRegion renderChunk)
    //        {
    //            return renderChunk.getBlockEntity(pos);
    //        }
    //        return null;
    //    }
 */