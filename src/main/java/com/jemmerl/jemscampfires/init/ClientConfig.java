package com.jemmerl.jemscampfires.init;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static ForgeConfigSpec CLIENT_SPEC;

    private static final boolean bonfire_extra_particles = true; // Render additional bonfire embers and smoke - Default true
    private static final boolean show_ticks_remaining = true; // Fuel info messages also show the time remaining in ticks - Default true
    private static final boolean convert_time_units = true; // Fuel info messages convert seconds into minutes/hours when applicable - Default true

    public static ForgeConfigSpec.BooleanValue BONFIRE_EXTRA_PARTICLES;
    public static ForgeConfigSpec.BooleanValue SHOW_TICKS_REMAINING;
    public static ForgeConfigSpec.BooleanValue CONVERT_TIME_UNITS;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Client Options");

        //TODO add config option to show remaining ticks (default true)
        //  add config option to convert seconds into minutes/hours when applicble
        //  Jade compat

        BONFIRE_EXTRA_PARTICLES = builder
                .comment("Render additional bonfire embers and smoke - Default true")
                .define("showBonfireParticles", bonfire_extra_particles);
        SHOW_TICKS_REMAINING = builder
                .comment("Fuel info messages also show the time remaining in ticks - Default true")
                .define("showTicksRemaining", show_ticks_remaining);
        CONVERT_TIME_UNITS = builder
                .comment("Fuel info messages convert seconds into minutes/hours when applicable - Default true")
                .define("convertTimeUnits", convert_time_units);
        builder.pop();

        CLIENT_SPEC = builder.build();
    }

}
