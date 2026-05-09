package com.jemmerl.jemscampfires.util;

public interface IFueledCampfire {
    int jems_fueled_campfires$getFuelTicks();
    void jems_fueled_campfires$setFuelTicks(int setTicks);

    boolean jems_fueled_campfires$getEternal();
    void jems_fueled_campfires$setEternal(boolean eternal);
    void jems_fueled_campfires$setPlayerPlaced();

    boolean jems_fueled_campfires$getBonfire();
    void jems_fueled_campfires$setBonfire(boolean bonfire);
    int jems_fueled_campfires$getBonfireLimit();

    boolean jems_fueled_campfires$isSoul();
    int[] jems_fueled_campfires$fetchCookingVariable();

    void jems_fueled_campfires$getFuel();
    void jems_fueled_campfires$normalStuff();
    void jems_fueled_campfires$bonfireStuff();
    void jems_fueled_campfires$doExtinguishDrops();

    void jems_fueled_campfires$clearChanged();
    boolean jems_fueled_campfires$getChanged();

    void jems_fueled_campfires$setFuelLightLevel(int lightLevel);
    int jems_fueled_campfires$getFuelLightLevel();

    void jems_fueled_campfires$updateLighting();
}
