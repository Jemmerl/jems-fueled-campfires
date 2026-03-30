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