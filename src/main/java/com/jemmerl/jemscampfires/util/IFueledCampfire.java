package com.jemmerl.jemscampfires.util;

public interface IFueledCampfire {
    int getFuelTicks();
    void setFuelTicks(int setTicks);
    void addFuelTicks(int addTicks);

    boolean getEternal();
    void setEternal(boolean eternal);
}
