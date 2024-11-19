package com.jemmerl.jemscampfires.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IFueledCampfire {
    int getFuelTicks();
    void setFuelTicks(int setTicks);

    boolean getEternal();
    void setEternal(boolean eternal);

    boolean getBonfire();
    void setBonfire(boolean bonfire);

    boolean isSoul();
    int[] fetchCookingVariable();

    void getFuel();
    void normalStuff();
    void bonfireStuff();
    void doExtinguished();
}
