package com.jemmerl.jemscampfires.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

public class ServerConfig {

    public static ForgeConfigSpec SERVER_SPEC;

    // Default values
    public static final boolean cf_place_unlit = true; // Player-placed campfires are not pre-lit - Default true
    public static final boolean soul_cf_place_unlit = true; // Player-placed soul campfires are not pre-lit - Default true
    public static final int cf_max_fuel_ticks = 2400; // Maximum campfire fuel in ticks - Default 2400 ticks (2 minutes)
    public static final int soul_cf_max_fuel_ticks = 2400; // Maximum soul campfire fuel in ticks - Default 2400 ticks (2 minutes)
    public static final int cf_initial_fuel_ticks = 200; // Maximum campfire initial fuel in ticks (can't be more than config max) - Default 200 ticks (10 seconds)
    public static final int soul_cf_initial_fuel_ticks = 200; // Maximum soul campfire initial fuel in ticks (can't be more than config max) - Default 200 ticks (10 seconds)

    public static final boolean cf_fuel_based_light = true; // Campfire light-level is based on its remaining fuel percent from max - Default true
    public static final boolean soul_cf_fuel_based_light = true; // Soul campfire light-level is based on its remaining fuel percent from max - Default true
    public static final int cf_rain_fuel_tick_loss = 200; // Campfire fuel lost per tick from rain (-1 for instant burnout without loss) - Default 200 ticks
    public static final int soul_cf_rain_fuel_tick_loss = 200; // Soul campfire fuel lost per tick from rain (-1 for instant burnout without loss) - Default 200 ticks
    public static final boolean cf_burn_when_sleep = false; // Campfires lose the appropriate fuel when you sleep - Default false
    public static final boolean soul_cf_burn_when_sleep = false; // Soul campfires lose the appropriate fuel when you sleep - Default false

    public static final boolean spawn_CF_unlit = false; // Regular campfires in generated structures are spawned unlit - Default: false
    public static final boolean spawn_soul_CF_unlit = false; // Soul campfires in generated structures are spawned unlit - Default: false
    public static final boolean spawn_CF_eternal = true; // Regular campfires spawned in structures don't burn fuel (eternal) when NOT cooking - Default: true
    public static final boolean spawn_soul_CF_eternal = true; // Soul campfires spawned in structures don't burn fuel (eternal) when NOT cooking - Default: true
    public static final boolean cf_allow_eternal_burn_items = true; // Allow items (defined by a tag) to make campfires burn without using fuel when NOT cooking - Default true
    public static final boolean soul_cf_allow_eternal_burn_items = true; // Remove eternal status if the campfire cooks (prevent abuse of natural-spawned eternal fires) - Default true
    public static final boolean cf_lose_eternal_when_cooking = true; // Allow items (defined by a tag) to make soul campfires burn without using fuel when NOT cooking - Default true
    public static final boolean soul_cf_lose_eternal_when_cooking = true; // Remove eternal status if the soul campfire cooks (prevent abuse of natural-spawned eternal fires) - Default true

    public static final boolean cf_can_bonfire = false; // Will regular campfires become a bonfire if over-fueled - Default false
    public static final boolean soul_cf_can_bonfire = false; // Will soul campfires become a bonfire if over-fueled - Default false
    public static final boolean cf_bonfire_extra_particles = true; // Will campfire bonfires make more particles - Default true
    public static final boolean soul_cf_bonfire_extra_particles = true; // Will soul campfire bonfires make more particles - Default true
    public static final int cf_bonfire_extra_max_fuel_ticks = 600; // Campfire bonfire fuel over normal max - Default 600 ticks
    public static final int soul_cf_bonfire_extra_max_fuel_ticks = 600; // Soul campfire bonfire fuel over normal max - Default 600 ticks
    public static final int cf_bonfire_burn_mult = 2; // Campfire bonfire fuel use multiplier - Default 2
    public static final int soul_cf_bonfire_burn_mult = 2; // Soul campfire bonfire fuel use multiplier - Default 2
    public static final int cf_bonfire_cooking_mult = 2; // Campfire bonfire cooking speed multiplier - Default 2
    public static final int soul_cf_bonfire_cooking_mult = 2; // Soul campfire bonfire cooking speed multiplier - Default 2
    public static final boolean cf_bonfire_firespread = true; // Will campfire bonfires spread fire - Default true
    public static final boolean soul_cf_bonfire_firespread = true; // Will soul campfire bonfires spread fire - Default true
    public static final boolean soul_cf_bright_bonfire = false; // Will soul campfires get brighter (from light-level 10 to 12) when a bonfire (needs soul fuel-based light-level enabled) - Default false


    public static ForgeConfigSpec.BooleanValue CAMPFIRE_PLACE_UNLIT;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_PLACE_UNLIT;
    public static ForgeConfigSpec.IntValue CAMPFIRE_MAX_FUEL_TICKS;
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_MAX_FUEL_TICKS;
    public static ForgeConfigSpec.IntValue CAMPFIRE_INITIAL_FUEL_TICKS;
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_INITIAL_FUEL_TICKS;

    public static ForgeConfigSpec.BooleanValue CAMPFIRE_FUEL_BASED_LIGHT;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_FUEL_BASED_LIGHT;
    public static ForgeConfigSpec.IntValue CAMPFIRE_RAIN_FUEL_TICK_LOSS;
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_RAIN_FUEL_TICK_LOSS;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_BURN_WHEN_SLEEP;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_BURN_WHEN_SLEEP;

    public static ForgeConfigSpec.BooleanValue SPAWN_CAMPFIRE_UNLIT;
    public static ForgeConfigSpec.BooleanValue SPAWN_SOUL_CAMPFIRE_UNLIT;
    public static ForgeConfigSpec.BooleanValue SPAWN_CAMPFIRE_ETERNAL;
    public static ForgeConfigSpec.BooleanValue SPAWN_SOUL_CAMPFIRE_ETERNAL;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_ALLOW_ETERNAL_BURN_ITEMS;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_ALLOW_ETERNAL_BURN_ITEMS;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING;

    public static ForgeConfigSpec.BooleanValue CAMPFIRE_CAN_BONFIRE;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_CAN_BONFIRE;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_BONFIRE_EXTRA_PARTICLES;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_BONFIRE_EXTRA_PARTICLES;
    public static ForgeConfigSpec.IntValue CAMPFIRE_BONFIRE_EXTRA_MAX_FUEL_TICKS;
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_BONFIRE_EXTRA_MAX_FUEL_TICKS;
    public static ForgeConfigSpec.IntValue CAMPFIRE_BONFIRE_BURN_MULT;
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_BONFIRE_BURN_MULT;
    public static ForgeConfigSpec.IntValue CAMPFIRE_BONFIRE_COOKING_MULT;
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_BONFIRE_COOKING_MULT;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_BONFIRE_FIRESPREAD;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_BONFIRE_FIRESPREAD;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_BRIGHT_BONFIRE;


    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Basic");
        builder.push("Regular Campfires");
        CAMPFIRE_PLACE_UNLIT = builder
                .comment("Player-placed campfires are not pre-lit - Default true")
                .define("placeCampfiresUnlit", cf_place_unlit);
        CAMPFIRE_MAX_FUEL_TICKS = builder
                .comment("Maximum campfire fuel in ticks - Default 2400 ticks (2 minutes)")
                .defineInRange("campfireMaxFuelTicks", cf_max_fuel_ticks, 0, 12000);
        CAMPFIRE_INITIAL_FUEL_TICKS = builder
                .comment("Maximum campfire initial fuel in ticks (can't be more than config max) - Default 200 ticks (10 seconds)")
                .defineInRange("campfireInitialFuelTicks", cf_initial_fuel_ticks, 0, 12000);
        builder.pop();
        builder.push("Soul Campfires");
        SOUL_CAMPFIRE_PLACE_UNLIT = builder
                .comment("Player-placed soul campfires are not pre-lit - Default true")
                .define("placeSoulCampfiresUnlit", soul_cf_place_unlit);
        SOUL_CAMPFIRE_MAX_FUEL_TICKS = builder
                .comment("Maximum soul campfire fuel in ticks - Default 2400 ticks (2 minutes)")
                .defineInRange("soulCampfireMaxFuelTicks", soul_cf_max_fuel_ticks, 0, 12000);
        SOUL_CAMPFIRE_INITIAL_FUEL_TICKS = builder
                .comment("Maximum soul campfire initial fuel in ticks (can't be more than config max) - Default 200 ticks (10 seconds)")
                .defineInRange("soulCampfireInitialFuelTicks", soul_cf_initial_fuel_ticks, 0, 12000);
        builder.pop();
        builder.pop();

        builder.push("Advanced");
        builder.push("Regular Campfires");
        CAMPFIRE_FUEL_BASED_LIGHT = builder
                .comment("Campfire light-level is based on its remaining fuel percent from max - Default true")
                .define("campfireFuelBasedLight", cf_fuel_based_light);
        CAMPFIRE_RAIN_FUEL_TICK_LOSS = builder
                .comment("Campfire fuel lost per tick from rain (-1 for instant burnout without loss) - Default 200 ticks")
                .defineInRange("campfireRainFuelLoss", cf_rain_fuel_tick_loss, -1, 24000);
        CAMPFIRE_BURN_WHEN_SLEEP = builder
                .comment("Campfires lose the appropriate fuel when you sleep - Default false")
                .define("campfiresBurnFuelWhenSleep", cf_burn_when_sleep);
        builder.pop();
        builder.push("Soul Campfires");
        SOUL_CAMPFIRE_FUEL_BASED_LIGHT = builder
                .comment("Soul Campfire light-level is based on its remaining fuel percent from max - Default true")
                .define("soulCampfireFuelBasedLight", soul_cf_fuel_based_light);
        SOUL_CAMPFIRE_RAIN_FUEL_TICK_LOSS = builder
                .comment("Soul campfire fuel lost per tick from rain (-1 for instant burnout without loss) - Default 200 ticks")
                .defineInRange("soulCampfireRainFuelLoss", soul_cf_rain_fuel_tick_loss, -1, 24000);
        SOUL_CAMPFIRE_BURN_WHEN_SLEEP = builder
                .comment("Soul campfires lose the appropriate fuel when you sleep - Default false")
                .define("soulCampfiresBurnFuelWhenSleep", soul_cf_burn_when_sleep);
        builder.pop();
        builder.pop();

        builder.push("Decorative Options");
        builder.push("Regular Campfires");
        SPAWN_CAMPFIRE_UNLIT = builder
                .comment("Regular campfires in generated structures are spawned unlit - Default: false")
                .define("spawnedCampfireLit", spawn_CF_unlit);
        SPAWN_CAMPFIRE_ETERNAL = builder
                .comment("Regular campfires spawned in structures don't burn fuel (eternal) when NOT cooking - Default: true")
                .define("spawnedCampfiresAreEternal", spawn_CF_eternal);
        CAMPFIRE_ALLOW_ETERNAL_BURN_ITEMS = builder
                .comment("Allow items (defined by a tag) to make campfires burn without using fuel when NOT cooking - Default true")
                .define("allowEternalCampfireItems", cf_allow_eternal_burn_items);
        CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING = builder
                .comment("Remove eternal status if the campfire cooks (prevent abuse of natural-spawned eternal fires) - Default true")
                .define("removeEternalCampfireIfCook", cf_lose_eternal_when_cooking);
        builder.pop();
        builder.push("Soul Campfires");
        SPAWN_SOUL_CAMPFIRE_UNLIT = builder
                .comment("Soul campfires in generated structures are spawned unlit - Default: false")
                .define("spawnedSoulCampfireLit", spawn_soul_CF_unlit);
        SPAWN_SOUL_CAMPFIRE_ETERNAL = builder
                .comment("Soul campfires spawned in structures don't burn fuel (eternal) when NOT cooking - Default: true")
                .define("spawnedSoulCampfiresAreEternal", spawn_soul_CF_eternal);
        SOUL_CAMPFIRE_ALLOW_ETERNAL_BURN_ITEMS = builder
                .comment("Allow items (defined by a tag) to make soul campfires burn without using fuel when NOT cooking - Default true")
                .define("allowEternalSoulCampfireItems", soul_cf_allow_eternal_burn_items);
        SOUL_CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING = builder
                .comment("Remove eternal status if the soul campfire cooks (prevent abuse of natural-spawned eternal fires) - Default true")
                .define("removeEternalSoulCampfireIfCook", soul_cf_lose_eternal_when_cooking);
        builder.pop();
        builder.pop();

        builder.push("Bonfire Mode Options");
        builder.push("Regular Campfires");
        CAMPFIRE_CAN_BONFIRE = builder
                .comment("Will regular campfires become a bonfire if over-fueled - Default false")
                .define("enableCampfiresBonfire", cf_can_bonfire);
        CAMPFIRE_BONFIRE_EXTRA_PARTICLES = builder
                .comment("Will campfire bonfires make more particles - Default true")
                .define("campfireBonfireExtraParticles", cf_bonfire_extra_particles);
        CAMPFIRE_BONFIRE_EXTRA_MAX_FUEL_TICKS = builder
                .comment("Campfire bonfire fuel over normal max - Default 600 ticks")
                .defineInRange("campfireBonfireFuelOverMax", cf_bonfire_extra_max_fuel_ticks, 300, 12000);
        CAMPFIRE_BONFIRE_BURN_MULT = builder
                .comment("Campfire bonfire fuel use multiplier - Default 2")
                .defineInRange("campfireBonfireFuelMultiplier", cf_bonfire_burn_mult, 1, 5);
        CAMPFIRE_BONFIRE_COOKING_MULT = builder
                .comment("Campfire bonfire cooking speed multiplier - Default 2")
                .defineInRange("campfireCookSpeedMultiplier", cf_bonfire_cooking_mult, 1, 5);
        CAMPFIRE_BONFIRE_FIRESPREAD = builder
                .comment("Will campfire bonfires spread fire - Default true")
                .define("campfireBonfireFirespread", cf_bonfire_firespread);
        builder.pop();
        builder.push("Soul Campfires");
        SOUL_CAMPFIRE_CAN_BONFIRE = builder
                .comment("Will soul campfires become a bonfire if over-fueled - Default false")
                .define("enableSoulCampfiresBonfire", soul_cf_can_bonfire);
        SOUL_CAMPFIRE_BONFIRE_EXTRA_PARTICLES = builder
                .comment("Will soul campfires make more particles when a bonfire - Default true")
                .define("soulCampfireBonfireExtraParticles", soul_cf_bonfire_extra_particles);
        SOUL_CAMPFIRE_BONFIRE_EXTRA_MAX_FUEL_TICKS = builder
                .comment("Soul campfire bonfire fuel over normal max - Default 600 ticks")
                .defineInRange("soulCampfireBonfireFuelOverMax", soul_cf_bonfire_extra_max_fuel_ticks, 300, 12000);
        SOUL_CAMPFIRE_BONFIRE_BURN_MULT = builder
                .comment("Soul campfire bonfire fuel use multiplier - Default 2")
                .defineInRange("soulCampfireBonfireFuelMultiplier", soul_cf_bonfire_burn_mult, 1, 5);
        SOUL_CAMPFIRE_BONFIRE_COOKING_MULT = builder
                .comment("Soul campfire bonfire cooking speed multiplier - Default 2")
                .defineInRange("soulCampfireCookSpeedMultiplier", soul_cf_bonfire_cooking_mult, 1, 5);
        SOUL_CAMPFIRE_BONFIRE_FIRESPREAD = builder
                .comment("Will soul campfire bonfires spread fire - Default true")
                .define("soulCampfireBonfireFirespread", soul_cf_bonfire_firespread);
        SOUL_CAMPFIRE_BRIGHT_BONFIRE = builder
                .comment("Will soul campfires get brighter (from light-level 10 to 12) when a bonfire (needs soul fuel-based light-level enabled) - Default false")
                .define("brighterSoulCampfireBonfire", soul_cf_bright_bonfire);
        builder.pop();
        builder.pop();

        SERVER_SPEC = builder.build();
    }


    public static void loadConfig(ForgeConfigSpec serverSpec, Path configPath) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(configPath).sync()
                .autosave().writingMode(WritingMode.REPLACE).build();
        configData.load();
        serverSpec.setConfig(configData);
    }
}
