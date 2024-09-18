package com.jemmerl.jemscampfires.init;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

public class ServerConfig {
    public static ForgeConfigSpec SERVER_SPEC;

    // Default values
    // General
    private static final boolean place_cf_lit = false; // Are regular campfires placed by players initially lit - Default: false
    private static final boolean place_soul_cf_lit = false; // Are soul campfires placed by players initially lit - Default: false
    private static final int cf_max_fuel_ticks = 4800; // Maximum regular campfire fuel in ticks - Default 4800 ticks (4 minutes; 20 ticks/second)
    private static final int soul_cf_max_fuel_ticks = 4800; // Maximum soul campfire fuel in ticks - Default 4800 ticks (4 minutes; 20 ticks/second)
    private static final int cf_initial_fuel_ticks = 200; // Initial regular campfire fuel in ticks (can't be more than the configured max) - Default 200 ticks (10 seconds)
    private static final int soul_cf_initial_fuel_ticks = 200; // Initial soul campfire fuel in ticks (can't be more than the configured max) - Default 200 ticks (10 seconds)
    private static final double cf_fuel_multiplier = 1.0; // Multiplies the burn time of fuel added to regular campfires - Default 1.0
    private static final double soul_cf_fuel_multiplier = 1.0; // Multiplies the burn time of fuel added to soul campfires - Default 1.0
    private static final boolean cf_burn_fuel_items_when_full = true; // Will lit regular campfires consume (destroy) dropped fuel items even when fully fueled - Default true
    private static final boolean soul_cf_burn_fuel_items_when_full = true; // Will lit soul campfires consume (destroy) dropped fuel items even when fully fueled - Default true
    private static final boolean cf_break_unlit = false; // Will regular campfires break when they run out of fuel - Default false
    private static final boolean soul_cf_break_unlit = false; // Will soul campfires break when they run out of fuel - Default false
    private static final boolean cf_firespread = false; // Will regular campfires spread fire to adjacent flammable blocks - Default false
    private static final boolean soul_cf_firespread = false; // Will soul campfires spread fire to adjacent flammable blocks - Default false
    private static final int cf_rain_fuel_tick_loss = 10; // Regular campfire fuel lost per tick from rain (-1 for instant burnout without loss, 0 to disable, 0 to disable) - Default 10 ticks of fuel
    private static final int soul_cf_rain_fuel_tick_loss = 5; // Soul campfire fuel lost per tick from rain (-1 for instant burnout without loss, 0 to disable) - Default 5 ticks of fuel

    // Postponed
    //private static final boolean cf_fuel_based_light = true; // Campfire light-level is based on its remaining fuel percent from max - Default true
    //private static final boolean soul_cf_fuel_based_light = false; // Soul campfire light-level is based on its remaining fuel percent from max - Default true
    //private static final boolean cf_burn_when_sleep = false; // Campfires lose the appropriate fuel when you sleep - Default false
    //private static final boolean soul_cf_burn_when_sleep = false; // Soul campfires lose the appropriate fuel when you sleep - Default false

    // Decorative
    private static final boolean place_cf_eternal = false; // Regular campfires placed by players don't burn fuel (eternal) - Default: false
    private static final boolean place_soul_cf_eternal = false; // Soul campfires placed by players don't burn fuel (eternal) - Default: false
    private static final boolean spawn_cf_eternal = true; // Regular campfires spawned in structures don't burn fuel (eternal) - Default: true
    private static final boolean spawn_soul_cf_eternal = true; // Soul campfires spawned in structures don't burn fuel (eternal) - Default: true
    private static final boolean cf_allow_eternal_items = true; // Allow items (defined by a tag) to make regular campfires burn without using fuel - Default true
    private static final boolean soul_cf_allow_eternal_items = true; // Allow items (defined by a tag) to make soul campfires burn without using fuel - Default true
    private static final boolean cf_lose_eternal_when_cooking = true; // Remove eternal status if a regular campfire cooks (prevents abuse of natural-spawned eternal campfires) - Default true
    private static final boolean soul_cf_lose_eternal_when_cooking = true; // Remove eternal status if a soul campfire cooks (prevents abuse of natural-spawned eternal campfires) - Default true
    private static final boolean cf_lose_eternal_extinguish = true; // Will regular campfires lose eternal status if extinguished or put out - Default true
    private static final boolean soul_cf_lose_eternal_extinguish = true; // Will soul campfires lose eternal status if extinguished or put out - Default true
    private static final boolean cf_rain_affect_eternal = false; // Will rain affect eternal regular campfires (lose fuel or get put-out) - Default false
    private static final boolean soul_cf_rain_affect_eternal = false; // Will rain affect eternal soul campfires (lose fuel or get put-out) - Default false
    private static final boolean cf_eternal_bonfire = false; // Can eternal regular campfires do bonfire behavior - Default false
    private static final boolean soul_cf_eternal_bonfire = false; // Can eternal soul campfires do bonfire behavior - Default false
    private static final boolean allow_client_packets = true; // Will allow the server to send packets for client-side bonfire particles - Default true

    // Bonfire
    private static final boolean cf_can_bonfire = false; // Will regular campfires become a bonfire if over-fueled - Default false
    private static final boolean soul_cf_can_bonfire = false; // Will soul campfires become a bonfire if over-fueled - Default false
    private static final int cf_bonfire_fuel_ticks = 800; // Regular bonfire fuel capacity, this gets added onto the normal max fuel - Default 800 ticks (40 seconds)
    private static final int soul_cf_bonfire_fuel_ticks = 800; // Soul bonfire fuel capacity, this gets added onto the normal max fuel - Default 800 ticks (40 seconds)
    private static final boolean cf_bonfire_lose_fuel = true; // Will regular bonfires lose their extra bonfire fuel when extinguished (returns to a normal fire) - Default true
    private static final boolean soul_cf_bonfire_lose_fuel = true; // Will soul bonfires lose their extra bonfire fuel when extinguished (returns to a normal fire) - Default true
    private static final int cf_bonfire_burn_mult = 2; // Regular bonfire fuel use multiplier - Default 2
    private static final int soul_cf_bonfire_burn_mult = 2; // Soul bonfire fuel use multiplier - Default 2
    private static final int cf_bonfire_cooking_mult = 2; // Regular bonfire cooking speed multiplier - Default 2
    private static final int soul_cf_bonfire_cooking_mult = 2; // Soul bonfire cooking speed multiplier - Default 2
    private static final boolean cf_bonfire_firespread = true; // Will regular bonfires spread fire (up to 2 blocks away) - Default true
    private static final boolean soul_cf_bonfire_firespread = true; // Will soul bonfires spread fire (up to 2 blocks away) - Default true
    //private static final boolean soul_cf_bright_bonfire = false; // Will soul campfires get brighter (from light-level 10 to 12) when a bonfire (needs soul fuel-based light-level enabled) - Default false

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // General
    public static ForgeConfigSpec.BooleanValue PLACE_CAMPFIRE_LIT;
    public static ForgeConfigSpec.BooleanValue PLACE_SOUL_CAMPFIRE_LIT;
    public static ForgeConfigSpec.IntValue CAMPFIRE_MAX_FUEL_TICKS;
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_MAX_FUEL_TICKS;
    public static ForgeConfigSpec.IntValue CAMPFIRE_INITIAL_FUEL_TICKS;
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_INITIAL_FUEL_TICKS;
    public static ForgeConfigSpec.DoubleValue CAMPFIRE_FUEL_MULT;
    public static ForgeConfigSpec.DoubleValue SOUL_CAMPFIRE_FUEL_MULT;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_BREAK_UNLIT;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_BREAK_UNLIT;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_FIRESPREAD;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_FIRESPREAD;
    public static ForgeConfigSpec.IntValue CAMPFIRE_RAIN_FUEL_TICK_LOSS;
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_RAIN_FUEL_TICK_LOSS;

    // Postponed
    //public static ForgeConfigSpec.BooleanValue CAMPFIRE_FUEL_BASED_LIGHT; //postponed
    //public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_FUEL_BASED_LIGHT; //postponed
    //public static ForgeConfigSpec.BooleanValue CAMPFIRE_BURN_WHEN_SLEEP; //postponed
    //public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_BURN_WHEN_SLEEP; //postponed

    // Decorative
    public static ForgeConfigSpec.BooleanValue PLACE_CAMPFIRE_ETERNAL;
    public static ForgeConfigSpec.BooleanValue PLACE_SOUL_CAMPFIRE_ETERNAL;
    public static ForgeConfigSpec.BooleanValue SPAWN_CAMPFIRE_ETERNAL;
    public static ForgeConfigSpec.BooleanValue SPAWN_SOUL_CAMPFIRE_ETERNAL;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_ALLOW_ETERNAL_ITEMS;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_ALLOW_ETERNAL_ITEMS;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_LOSE_ETERNAL_WHEN_EXTINGUISH;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_LOSE_ETERNAL_WHEN_EXTINGUISH;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_RAIN_AFFECT_ETERNAL;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_RAIN_AFFECT_ETERNAL;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_ETERNAL_BONFIRE;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_ETERNAL_BONFIRE;
    public static ForgeConfigSpec.BooleanValue ALLOW_CLIENT_PACKETS;

    // Bonfire
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_CAN_BONFIRE;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_CAN_BONFIRE;
    public static ForgeConfigSpec.IntValue CAMPFIRE_BONFIRE_FUEL_TICKS;
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_BONFIRE_FUEL_TICKS;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_BONFIRE_LOSE_FUEL_EXTINGUISH;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_BONFIRE_LOSE_FUEL_EXTINGUISH;
    public static ForgeConfigSpec.IntValue CAMPFIRE_BONFIRE_BURN_MULT;
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_BONFIRE_BURN_MULT;
    public static ForgeConfigSpec.IntValue CAMPFIRE_BONFIRE_COOKING_MULT;
    public static ForgeConfigSpec.IntValue SOUL_CAMPFIRE_BONFIRE_COOKING_MULT;
    public static ForgeConfigSpec.BooleanValue CAMPFIRE_BONFIRE_FIRESPREAD;
    public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_BONFIRE_FIRESPREAD;
    //public static ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_BRIGHT_BONFIRE;


    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("General Options");
        builder.push("Regular Campfires");
        PLACE_CAMPFIRE_LIT = builder
                .comment("Are regular campfires placed by players initially lit - Default: false")
                .define("placedCampfiresAreLit", place_cf_lit);
        CAMPFIRE_MAX_FUEL_TICKS = builder
                .comment("Maximum regular campfire fuel in ticks - Default 4800 ticks (4 minutes; 20 ticks/second)")
                .defineInRange("campfireMaxFuelTicks", cf_max_fuel_ticks, 100, 12000);
        CAMPFIRE_INITIAL_FUEL_TICKS = builder
                .comment("Initial regular campfire fuel in ticks (can't be more than the configured max) - Default 200 ticks (10 seconds)")
                .defineInRange("campfireInitialFuelTicks", cf_initial_fuel_ticks, 0, 12000);
        CAMPFIRE_FUEL_MULT = builder
                .comment("Multiplies the burn time of fuel added to regular campfires - Default 1.0")
                .defineInRange("campfireBurnTimeMultiplier", cf_fuel_multiplier, 0.01, 10.0);
        CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS = builder
                .comment("Will lit regular campfires consume (destroy) dropped fuel items even when fully fueled - Default: true")
                .define("campfiresBurnItemsWhenFull", cf_burn_fuel_items_when_full);
        CAMPFIRE_BREAK_UNLIT = builder
                .comment("Will regular campfires break when they run out of fuel - Default false")
                .define("breakCampfiresWhenOut", cf_break_unlit);
        CAMPFIRE_FIRESPREAD = builder
                .comment("Will regular campfires spread fire to adjacent flammable blocks - Default false")
                .define("campfiresSpreadFire", cf_firespread);
        CAMPFIRE_RAIN_FUEL_TICK_LOSS = builder
                .comment("Regular campfire fuel lost per tick from rain (-1 for instant burnout without loss, 0 to disable) - Default 200 ticks")
                .defineInRange("campfireRainFuelLoss", cf_rain_fuel_tick_loss, -1, 24000);
        builder.pop();
        builder.push("Soul Campfires");
        PLACE_SOUL_CAMPFIRE_LIT = builder
                .comment("Are soul campfires placed by players initially lit - Default: false")
                .define("placedSoulCampfiresAreLit", place_soul_cf_lit);
        SOUL_CAMPFIRE_MAX_FUEL_TICKS = builder
                .comment("Maximum soul campfire fuel in ticks - Default 4800 ticks (4 minutes; 20 ticks/second)")
                .defineInRange("soulCampfireMaxFuelTicks", soul_cf_max_fuel_ticks, 100, 12000);
        SOUL_CAMPFIRE_INITIAL_FUEL_TICKS = builder
                .comment("Initial soul campfire fuel in ticks (can't be more than the configured max) - Default 200 ticks (10 seconds)")
                .defineInRange("soulCampfireInitialFuelTicks", soul_cf_initial_fuel_ticks, 0, 12000);
        SOUL_CAMPFIRE_FUEL_MULT = builder
                .comment("Multiplies the burn time of fuel added to soul campfires - Default 1.0")
                .defineInRange("soulCampfireBurnTimeMultiplier", soul_cf_fuel_multiplier, 0.01, 10.0);
        SOUL_CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS = builder
                .comment("Will lit soul campfires consume (destroy) dropped fuel items even when fully fueled - Default: true")
                .define("soulCampfiresBurnItemsWhenFull", soul_cf_burn_fuel_items_when_full);
        SOUL_CAMPFIRE_BREAK_UNLIT = builder
                .comment("Will soul campfires break when they run out of fuel - Default false")
                .define("breakSoulCampfiresWhenOut", soul_cf_break_unlit);
        SOUL_CAMPFIRE_FIRESPREAD = builder
                .comment("Will soul campfires spread fire to adjacent flammable blocks - Default false")
                .define("soulCampfiresSpreadFire", soul_cf_firespread);
        SOUL_CAMPFIRE_RAIN_FUEL_TICK_LOSS = builder
                .comment("Soul campfire fuel lost per tick from rain (-1 for instant burnout without loss, 0 to disable) - Default 200 ticks")
                .defineInRange("soulCampfireRainFuelLoss", soul_cf_rain_fuel_tick_loss, -1, 24000);
        builder.pop();
        builder.pop();

//        builder.push("Advanced Options");
////        builder.push("Regular Campfires");
//////        CAMPFIRE_FUEL_BASED_LIGHT = builder
//////                .comment("Campfire light-level is based on its remaining fuel percent from max - Default true")
//////                .define("campfireFuelBasedLight", cf_fuel_based_light);
////
//////        CAMPFIRE_BURN_WHEN_SLEEP = builder
//////                .comment("Campfires lose the appropriate fuel when you sleep - Default false")
//////                .define("campfiresBurnFuelWhenSleep", cf_burn_when_sleep);
////        builder.pop();
////        builder.push("Soul Campfires");
//////        SOUL_CAMPFIRE_FUEL_BASED_LIGHT = builder
//////                .comment("Soul Campfire light-level is based on its remaining fuel percent from max - Default true")
//////                .define("soulCampfireFuelBasedLight", soul_cf_fuel_based_light);
////
//////        SOUL_CAMPFIRE_BURN_WHEN_SLEEP = builder
//////                .comment("Soul campfires lose the appropriate fuel when you sleep - Default false")
//////                .define("soulCampfiresBurnFuelWhenSleep", soul_cf_burn_when_sleep);
////        builder.pop();
//        builder.pop();

        builder.push("Decorative Options");
        ALLOW_CLIENT_PACKETS = builder
                .comment("Will allow the server to send packets for client-side bonfire particles - Default true")
                .define("allowClientUpdatePackets", allow_client_packets);
        builder.push("Regular Campfires");
        PLACE_CAMPFIRE_ETERNAL = builder
                .comment("Regular campfires placed by players don't burn fuel (eternal) - Default: true")
                .define("placedCampfiresAreEternal", place_cf_eternal);
        SPAWN_CAMPFIRE_ETERNAL = builder
                .comment("Regular campfires spawned in structures don't burn fuel (eternal) - Default: true")
                .define("spawnedCampfiresAreEternal", spawn_cf_eternal);
        CAMPFIRE_ALLOW_ETERNAL_ITEMS = builder
                .comment("Allow items (defined by a tag) to make regular campfires burn without using fuel - Default true")
                .define("allowEternalCampfireItems", cf_allow_eternal_items);
        CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING = builder
                .comment("Remove eternal status if a regular campfire cooks (prevents abuse of natural-spawned eternal campfires) - Default true")
                .define("removeEternalCampfireIfCook", cf_lose_eternal_when_cooking);
        CAMPFIRE_LOSE_ETERNAL_WHEN_EXTINGUISH = builder
                .comment("Will regular campfires lose eternal status if extinguished or put out - Default true")
                .define("removeEternalCampfireIfOut", cf_lose_eternal_extinguish);
        CAMPFIRE_RAIN_AFFECT_ETERNAL = builder
                .comment("Will rain affect eternal regular campfires (lose fuel or get put-out) - Default false")
                .define("doesRainAffectEternalCampfire", cf_rain_affect_eternal);
        CAMPFIRE_ETERNAL_BONFIRE = builder
                .comment("Can eternal regular campfires do bonfire behavior - Default false")
                .define("eternalCampfireBonfire", cf_eternal_bonfire);
        builder.pop();
        builder.push("Soul Campfires");
        PLACE_SOUL_CAMPFIRE_ETERNAL = builder
                .comment("Soul campfires placed by players don't burn fuel (eternal) - Default: true")
                .define("placedCampfiresAreEternal", place_soul_cf_eternal);
        SPAWN_SOUL_CAMPFIRE_ETERNAL = builder
                .comment("Soul campfires spawned in structures don't burn fuel (eternal) - Default: true")
                .define("spawnedCampfiresAreEternal", spawn_soul_cf_eternal);
        SOUL_CAMPFIRE_ALLOW_ETERNAL_ITEMS = builder
                .comment("Allow items (defined by a tag) to make soul campfires burn without using fuel - Default true")
                .define("allowEternalSoulCampfireItems", soul_cf_allow_eternal_items);
        SOUL_CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING = builder
                .comment("Remove eternal status if a soul campfire cooks (prevents abuse of natural-spawned eternal campfires) - Default true")
                .define("removeEternalSoulCampfireIfCook", soul_cf_lose_eternal_when_cooking);
        SOUL_CAMPFIRE_LOSE_ETERNAL_WHEN_EXTINGUISH = builder
                .comment("Will soul campfires lose eternal status if extinguished or put out - Default true")
                .define("removeEternalSoulCampfireIfOut", soul_cf_lose_eternal_extinguish);
        SOUL_CAMPFIRE_RAIN_AFFECT_ETERNAL = builder
                .comment("Will rain affect eternal soul campfires (lose fuel or get put-out) - Default false")
                .define("doesRainAffectEternalSoulCampfire", soul_cf_rain_affect_eternal);
        SOUL_CAMPFIRE_ETERNAL_BONFIRE = builder
                .comment("Can eternal soul campfires do bonfire behavior - Default false")
                .define("canEternalSoulCampfireBonfire", soul_cf_eternal_bonfire);
        builder.pop();
        builder.pop();

        builder.push("Bonfire Mode Options");
        builder.push("Regular Campfires");
        CAMPFIRE_CAN_BONFIRE = builder
                .comment("Will regular campfires become a bonfire if over-fueled - Default false")
                .define("enableRegularBonfire", cf_can_bonfire);
        CAMPFIRE_BONFIRE_FUEL_TICKS = builder
                .comment("Regular bonfire fuel capacity, this gets added onto the normal max fuel - Default 800 ticks (40 seconds)")
                .defineInRange("regularBonfireFuel", cf_bonfire_fuel_ticks, 100, 12000);
        CAMPFIRE_BONFIRE_LOSE_FUEL_EXTINGUISH = builder
                .comment("Will regular bonfires lose their extra bonfire fuel when extinguished (returns to a normal fire) - Default true")
                .define("regularBonfireLoseFuelWhenPutOut", cf_bonfire_lose_fuel);
        CAMPFIRE_BONFIRE_BURN_MULT = builder
                .comment("Regular bonfire fuel use multiplier - Default 2")
                .defineInRange("regularBonfireFuelUseMultiplier", cf_bonfire_burn_mult, 1, 5);
        CAMPFIRE_BONFIRE_COOKING_MULT = builder
                .comment("Regular bonfire cooking speed multiplier - Default 2")
                .defineInRange("regularBonfireCookSpeedMultiplier", cf_bonfire_cooking_mult, 1, 5);
        CAMPFIRE_BONFIRE_FIRESPREAD = builder
                .comment("Will regular bonfires spread fire (up to 2 blocks away) - Default true")
                .define("regularBonfireFirespread", cf_bonfire_firespread);
        builder.pop();
        builder.push("Soul Campfires");
        SOUL_CAMPFIRE_CAN_BONFIRE = builder
                .comment("Will soul campfires become a bonfire if over-fueled - Default false")
                .define("enableSoulBonfire", soul_cf_can_bonfire);
        SOUL_CAMPFIRE_BONFIRE_FUEL_TICKS = builder
                .comment("Soul bonfire fuel capacity, this gets added onto the normal max fuel - Default 800 ticks (40 seconds)")
                .defineInRange("soulBonfireFuel", soul_cf_bonfire_fuel_ticks, 100, 12000);
        SOUL_CAMPFIRE_BONFIRE_LOSE_FUEL_EXTINGUISH = builder
                .comment("Will soul bonfires lose their extra bonfire fuel when extinguished (returns to a regular fire) - Default true")
                .define("soulBonfireLoseFuelWhenPutOut", soul_cf_bonfire_lose_fuel);
        SOUL_CAMPFIRE_BONFIRE_BURN_MULT = builder
                .comment("Soul bonfire fuel use multiplier - Default 2")
                .defineInRange("soulBonfireFuelUseMultiplier", soul_cf_bonfire_burn_mult, 1, 5);
        SOUL_CAMPFIRE_BONFIRE_COOKING_MULT = builder
                .comment("Soul bonfire cooking speed multiplier - Default 2")
                .defineInRange("soulBonfireCookSpeedMultiplier", soul_cf_bonfire_cooking_mult, 1, 5);
        SOUL_CAMPFIRE_BONFIRE_FIRESPREAD = builder
                .comment("Will soul bonfires spread fire (up to 2 blocks away) - Default true")
                .define("soulBonfireFirespread", soul_cf_bonfire_firespread);
//        SOUL_CAMPFIRE_BRIGHT_BONFIRE = builder
//                .comment("Will soul campfires get brighter (from light-level 10 to 12) when a bonfire (needs soul fuel-based light-level enabled) - Default false")
//                .define("brighterSoulBonfire", soul_cf_bright_bonfire);
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
