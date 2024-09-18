package com.jemmerl.jemscampfires.init;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static ForgeConfigSpec CLIENT_SPEC;

    private static final boolean bonfire_extra_particles = true; // Will campfire bonfires make more particles - Default true

    public static ForgeConfigSpec.BooleanValue BONFIRE_EXTRA_PARTICLES; //done

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Client Options");
        BONFIRE_EXTRA_PARTICLES = builder
                .comment("Render additional bonfire embers and smoke - Default true")
                .define("showBonfireParticles", bonfire_extra_particles);
        builder.pop();

        CLIENT_SPEC = builder.build();
    }

}
