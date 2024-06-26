package com.jemmerl.jemscampfires.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

public class ServerConfig {

    public static ForgeConfigSpec SERVER_SPEC;

    // Default values
    private static final boolean place_cf_lit = false; // Are campfires placed by players initially lit - Default: false
    private static final boolean place_soul_cf_lit = false; // Are soul campfires placed by players initially lit - Default: false
    private static final int cf_max_fuel_ticks = 2400; // Maximum campfire fuel in ticks - Default 2400 ticks (2 minutes)
    private static final int soul_cf_max_fuel_ticks = 1200; // Maximum soul campfire fuel in ticks - Default 2400 ticks (2 minutes)
    private static final int cf_initial_fuel_ticks = 200; // Maximum campfire initial fuel in ticks (can't be more than config max) - Default 200 ticks (10 seconds)
    private static final int soul_cf_initial_fuel_ticks = 100; // Maximum soul campfire initial fuel in ticks (can't be more than config max) - Default 200 ticks (10 seconds)
    private static final double cf_fuel_multiplier = 1.0; // Multiplies the burn time of fuel added to campfires - Default 1
    private static final double soul_cf_fuel_multiplier = 1.0; // Multiplies the burn time of fuel added to soul campfires - Default 1
    private static final boolean cf_break_unlit = false; // Will campfires break when they run out of fuel - Default false
    private static final boolean soul_cf_break_unlit = false; // Will soul campfires break when they run out of fuel - Default false
    private static final boolean place_cf_eternal = false; // Campfires placed by players don't burn fuel (eternal) when NOT cooking - Default: false
    private static final boolean place_soul_cf_eternal = false; // Soul campfires placed by players don't burn fuel (eternal) when NOT cooking - Default: false

    private static final boolean cf_fuel_based_light = true; // Campfire light-level is based on its remaining fuel percent from max - Default true
    private static final boolean soul_cf_fuel_based_light = true; // Soul campfire light-level is based on its remaining fuel percent from max - Default true
    private static final int cf_rain_fuel_tick_loss = 5; // Campfire fuel lost per tick from rain (-1 for instant burnout without loss) - Default 10 ticks
    private static final int soul_cf_rain_fuel_tick_loss = 5; // Soul campfire fuel lost per tick from rain (-1 for instant burnout without loss) - Default 2 ticks
    private static final boolean cf_burn_when_sleep = false; // Campfires lose the appropriate fuel when you sleep - Default false
    private static final boolean soul_cf_burn_when_sleep = false; // Soul campfires lose the appropriate fuel when you sleep - Default false

    private static final boolean spawn_cf_eternal = true; // Campfires spawned in structures don't burn fuel (eternal) when NOT cooking - Default: true
    private static final boolean spawn_soul_cf_eternal = true; // Soul campfires spawned in structures don't burn fuel (eternal) when NOT cooking - Default: true
    private static final boolean cf_allow_eternal_burn_items = true; // Allow items (defined by a tag) to make campfires burn without using fuel when NOT cooking - Default true
    private static final boolean soul_cf_allow_eternal_burn_items = true; // Remove eternal status if the campfire cooks (prevent abuse of natural-spawned eternal fires) - Default true
    private static final boolean cf_lose_eternal_when_cooking = true; // Allow items (defined by a tag) to make soul campfires burn without using fuel when NOT cooking - Default true
    private static final boolean soul_cf_lose_eternal_when_cooking = true; // Remove eternal status if the soul campfire cooks (prevent abuse of natural-spawned eternal fires) - Default true
    private static final boolean cf_rain_affect_eternal = false; // Will rain affect eternal-burning campfires (lose fuel, get put out) - Default false
    private static final boolean soul_cf_rain_affect_eternal = false; // Will rain affect eternal-burning soul campfires (lose fuel, get put out) - Default false

    private static final boolean cf_can_bonfire = false; // Will regular campfires become a bonfire if over-fueled - Default false
    private static final boolean soul_cf_can_bonfire = false; // Will soul campfires become a bonfire if over-fueled - Default false
    private static final boolean cf_bonfire_extra_particles = true; // Will campfire bonfires make more particles - Default true
    private static final boolean soul_cf_bonfire_extra_particles = true; // Will soul campfire bonfires make more particles - Default true
    private static final int cf_bonfire_extra_max_fuel_ticks = 600; // Campfire bonfire fuel over normal max - Default 600 ticks
    private static final int soul_cf_bonfire_extra_max_fuel_ticks = 300; // Soul campfire bonfire fuel over normal max - Default 600 ticks
    private static final int cf_bonfire_burn_mult = 2; // Campfire bonfire fuel use multiplier - Default 2
    private static final int soul_cf_bonfire_burn_mult = 2; // Soul campfire bonfire fuel use multiplier - Default 2
    private static final int cf_bonfire_cooking_mult = 2; // Campfire bonfire cooking speed multiplier - Default 2
    private static final int soul_cf_bonfire_cooking_mult = 2; // Soul campfire bonfire cooking speed multiplier - Default 2
    private static final boolean cf_bonfire_firespread = true; // Will campfire bonfires spread fire - Default true
    private static final boolean soul_cf_bonfire_firespread = true; // Will soul campfire bonfires spread fire - Default true
    private static final boolean soul_cf_bright_bonfire = false; // Will soul campfires get brighter (from light-level 10 to 12) when a bonfire (needs soul fuel-based light-level enabled) - Default false

    // Basic
    public static ForgeConfigSpec.BooleanValue PLACE_CAMPFIRE_LIT; //done
    public static ForgeConfigSpec.BooleanValue PLACE_SOUL_CAMPFIRE_LIT; //done
    public static ForgeConfigSpec.IntValue CAMPFIRE_MAX_FUEL_TICKS;
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_MAX_FUEL_TICKS;
    public static ForgeConfigSpec.IntValue CAMPFIRE_INITIAL_FUEL_TICKS; //done
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_INITIAL_FUEL_TICKS; //done
    public static ForgeConfigSpec.DoubleValue CAMPFIRE_FUEL_MULT;
    public static ForgeConfigSpec.DoubleValue SOUL_CAMPFIRE_FUEL_MULT;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_BREAK_UNLIT; //done
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_BREAK_UNLIT; //done
    public static ForgeConfigSpec.BooleanValue PLACE_CAMPFIRE_ETERNAL; //done
    public static ForgeConfigSpec.BooleanValue PLACE_SOUL_CAMPFIRE_ETERNAL; //done

    // Advanced
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_FUEL_BASED_LIGHT;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_FUEL_BASED_LIGHT;
    public static ForgeConfigSpec.IntValue CAMPFIRE_RAIN_FUEL_TICK_LOSS; //done
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_RAIN_FUEL_TICK_LOSS; //done
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_BURN_WHEN_SLEEP;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_BURN_WHEN_SLEEP;

    // Decorative
    public static ForgeConfigSpec.BooleanValue SPAWN_CAMPFIRE_ETERNAL; //done
    public static ForgeConfigSpec.BooleanValue SPAWN_SOUL_CAMPFIRE_ETERNAL; //done
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_ALLOW_ETERNAL_BURN_ITEMS;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_ALLOW_ETERNAL_BURN_ITEMS;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_RAIN_AFFECT_ETERNAL; //done
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_RAIN_AFFECT_ETERNAL; //done

    // Bonfire
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
        PLACE_CAMPFIRE_LIT = builder
                .comment("Are campfires placed by players initially lit - Default: false")
                .define("placedCampfiresAreLit", place_cf_lit);
        CAMPFIRE_MAX_FUEL_TICKS = builder
                .comment("Maximum campfire fuel in ticks - Default 2400 ticks (2 minutes)")
                .defineInRange("campfireMaxFuelTicks", cf_max_fuel_ticks, 0, 12000);
        CAMPFIRE_INITIAL_FUEL_TICKS = builder
                .comment("Maximum campfire initial fuel in ticks (can't be more than config max) - Default 200 ticks (10 seconds)")
                .defineInRange("campfireInitialFuelTicks", cf_initial_fuel_ticks, 0, 12000);
        CAMPFIRE_FUEL_MULT = builder
                .comment("Multiplies the burn time of fuel added to campfires - Default 1.0")
                .defineInRange("campfireBurnTimeMultiplier", cf_fuel_multiplier, 0.01, 10.0);
        CAMPFIRE_BREAK_UNLIT = builder
                .comment("Will campfires break when they run out of fuel - Default false")
                .define("breakCampfiresUnlit", cf_break_unlit);
        PLACE_CAMPFIRE_ETERNAL = builder
                .comment("Regular campfires placed by players don't burn fuel (eternal) when NOT cooking - Default: true")
                .define("placedCampfiresAreEternal", place_cf_eternal);
        builder.pop();
        builder.push("Soul Campfires");
        PLACE_SOUL_CAMPFIRE_LIT = builder
                .comment("Are soul campfires placed by players initially lit - Default: false")
                .define("placedSoulCampfiresAreLit", place_soul_cf_lit);
        SOUL_CAMPFIRE_MAX_FUEL_TICKS = builder
                .comment("Maximum soul campfire fuel in ticks - Default 2400 ticks (2 minutes)")
                .defineInRange("soulCampfireMaxFuelTicks", soul_cf_max_fuel_ticks, 0, 12000);
        SOUL_CAMPFIRE_INITIAL_FUEL_TICKS = builder
                .comment("Maximum soul campfire initial fuel in ticks (can't be more than config max) - Default 200 ticks (10 seconds)")
                .defineInRange("soulCampfireInitialFuelTicks", soul_cf_initial_fuel_ticks, 0, 12000);
        SOUL_CAMPFIRE_FUEL_MULT = builder
                .comment("Multiplies the burn time of fuel added to soul campfires - Default 1.0")
                .defineInRange("soulCampfireBurnTimeMultiplier", soul_cf_fuel_multiplier, 0.01, 10.0);
        SOUL_CAMPFIRE_BREAK_UNLIT = builder
                .comment("Will soul campfires break when they run out of fuel - Default false")
                .define("breakSoulCampfiresUnlit", soul_cf_break_unlit);
        PLACE_SOUL_CAMPFIRE_ETERNAL = builder
                .comment("Soul campfires placed by players don't burn fuel (eternal) when NOT cooking - Default: true")
                .define("placedCampfiresAreEternal", place_soul_cf_eternal);
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
        SPAWN_CAMPFIRE_ETERNAL = builder
                .comment("Regular campfires spawned in structures don't burn fuel (eternal) when NOT cooking - Default: true")
                .define("spawnedCampfiresAreEternal", spawn_cf_eternal);
        CAMPFIRE_ALLOW_ETERNAL_BURN_ITEMS = builder
                .comment("Allow items (defined by a tag) to make campfires burn without using fuel when NOT cooking - Default true")
                .define("allowEternalCampfireItems", cf_allow_eternal_burn_items);
        CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING = builder
                .comment("Remove eternal status if the campfire cooks (prevent abuse of natural-spawned eternal fires) - Default true")
                .define("removeEternalCampfireIfCook", cf_lose_eternal_when_cooking);
        CAMPFIRE_RAIN_AFFECT_ETERNAL = builder
                .comment("Will rain affect eternal-burning campfires (lose fuel, get put out) - Default false")
                .define("doesRainAffectEternalCampfire", cf_rain_affect_eternal);
        builder.pop();
        builder.push("Soul Campfires");
        SPAWN_SOUL_CAMPFIRE_ETERNAL = builder
                .comment("Soul campfires spawned in structures don't burn fuel (eternal) when NOT cooking - Default: true")
                .define("spawnedCampfiresAreEternal", spawn_soul_cf_eternal);
        SOUL_CAMPFIRE_ALLOW_ETERNAL_BURN_ITEMS = builder
                .comment("Allow items (defined by a tag) to make soul campfires burn without using fuel when NOT cooking - Default true")
                .define("allowEternalSoulCampfireItems", soul_cf_allow_eternal_burn_items);
        SOUL_CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING = builder
                .comment("Remove eternal status if the soul campfire cooks (prevent abuse of natural-spawned eternal fires) - Default true")
                .define("removeEternalSoulCampfireIfCook", soul_cf_lose_eternal_when_cooking);
        SOUL_CAMPFIRE_RAIN_AFFECT_ETERNAL = builder
                .comment("Will rain affect eternal-burning soul campfires (lose fuel, get put out) - Default false")
                .define("doesRainAffectEternalSoulCampfire", soul_cf_rain_affect_eternal);
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
