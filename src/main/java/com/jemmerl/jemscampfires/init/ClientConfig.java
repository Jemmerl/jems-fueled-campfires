package com.jemmerl.jemscampfires.init;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

public class ClientConfig {
    public static ForgeConfigSpec CLIENT_SPEC;

    private static final boolean bonfire_extra_particles = true; // Will campfire bonfires make more particles - Default true

    public static ForgeConfigSpec.BooleanValue BONFIRE_EXTRA_PARTICLES; //done

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Client Options");
        BONFIRE_EXTRA_PARTICLES = builder
                .comment("Will bonfires make more particles - Default true")
                .define("bonfireExtraParticles", bonfire_extra_particles);
        builder.pop();

        CLIENT_SPEC = builder.build();
    }

}
