package com.jemmerl.jemscampfires.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ContainerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CampfireBlock.class, priority = 0)
public abstract class JemsCampfireMixins extends ContainerBlock {
    protected JemsCampfireMixins(Properties builder) {
        super(builder);
    }




    // Detect fuel dropped



}
