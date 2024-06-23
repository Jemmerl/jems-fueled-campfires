package com.jemmerl.jemscampfires.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ContainerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CampfireBlock.class, priority = 0)
public abstract class JemsCampfireBlockMixins extends ContainerBlock {
    protected JemsCampfireBlockMixins(Properties builder) {
        super(builder);
    }

//    @Shadow
//    public static boolean isLit(BlockState state) {
//        return false;
//    }

    // Detect fuel dropped



}
