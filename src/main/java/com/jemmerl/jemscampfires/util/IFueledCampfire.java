package com.jemmerl.jemscampfires.util;

public interface IFueledCampfire {
    int getFuelTicks();
    void setFuelTicks(int setTicks);

    boolean getEternal();
    void setEternal(boolean eternal);

    boolean getBonfire();
    void setBonfire(boolean bonfire);
    int getBonfireLimit();

    boolean isSoul();
    int[] fetchCookingVariable();

    void getFuel();
    void normalStuff();
    void bonfireStuff();
    void doExtinguishDrops();

    void clearChanged();
    boolean getChanged();

    void setFuelLightLevel(int lightLevel);
    int getFuelLightLevel();

    void updateLighting();
}
