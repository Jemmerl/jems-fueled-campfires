package com.jemmerl.jemscampfires.init;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static ForgeConfigSpec CLIENT_SPEC;



    private static final boolean bonfire_extra_particles = true; // Render additional bonfire embers and smoke - Default true
    private static final boolean cf_info_in_chat = false; // Campfire info messages are logged in the chat window instead of the action-bar - Default false

    // Debug
    private static final boolean debug_ticks_remaining = false; // Debug/Dev: Campfire info messages also show the time remaining in ticks - Default false

    public static ForgeConfigSpec.BooleanValue BONFIRE_EXTRA_PARTICLES;
    public static ForgeConfigSpec.BooleanValue CF_INFO_IN_CHAT;

    // Debug
    public static ForgeConfigSpec.BooleanValue DEBUG_TICKS_REMAINING;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Debug/Dev Options");
        DEBUG_TICKS_REMAINING = builder
                .comment("Debug/Dev: Campfire info messages also show the time remaining in ticks - Default false")
                .define("debugTicksRemaining", debug_ticks_remaining);
        builder.pop();

        builder.push("Client Options");
        BONFIRE_EXTRA_PARTICLES = builder
                .comment("Render additional bonfire embers and smoke - Default true")
                .define("showBonfireParticles", bonfire_extra_particles);
        CF_INFO_IN_CHAT = builder
                .comment("Campfire info messages are logged in the chat window instead of the action-bar - Default false")
                .define("debugInfoInChat", cf_info_in_chat);
        CLIENT_SPEC = builder.build();
    }

}
