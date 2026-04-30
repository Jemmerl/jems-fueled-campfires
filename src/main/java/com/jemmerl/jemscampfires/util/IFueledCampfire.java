package com.jemmerl.jemscampfires.util;

public interface IFueledCampfire {
    int getFuelTicks();
    void setFuelTicks(int setTicks);

    boolean getEternal();
    void setEternal(boolean eternal);

    boolean getBonfire();
    void setBonfire(boolean bonfire);

    void doExtinguished();

    int getBonfireLimit();

    default void setFuelLightLevel(int lightLevel) {
        setFuelLightLevel(lightLevel, true);
    }
    void setFuelLightLevel(int lightLevel, boolean lightingUpdate);
    int getFuelLightLevel();

    void updateLighting();
    default void dynamicLightLevelUpdate() {
        dynamicLightLevelUpdate(true);
    }
    void dynamicLightLevelUpdate(boolean lightingUpdate);


}