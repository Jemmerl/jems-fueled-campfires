package com.jemmerl.jemscampfires.util;

public interface IFueledCampfire {
    int getFuelTicks();
    void setFuelTicks(int setTicks);

    boolean getEternal();
    void setEternal(boolean eternal);

    boolean getBonfire();
    void setBonfire(boolean bonfire);

    void doExtinguished();
}
