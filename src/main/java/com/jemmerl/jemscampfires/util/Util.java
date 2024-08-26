package com.jemmerl.jemscampfires.util;

public class Util {

    public static int mod(int a, int b) {
        return (a % b + b) % b;
    }

    //int mod = Util.mod(pos.getX(), 2) + ((Util.mod(pos.getZ(), 2) + 1) * 2) - 2;
    //                    if ((worldIn.getGameTime() % 4) != mod) {
    //                        System.out.println("canceled: " + mod);
    //                        return;
    //                    }
}
