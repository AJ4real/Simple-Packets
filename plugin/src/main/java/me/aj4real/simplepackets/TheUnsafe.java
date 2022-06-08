/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.simplepackets;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class TheUnsafe {

    private static final Unsafe unsafe;

    static {
        Unsafe finalUnsafe;
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            finalUnsafe = (Unsafe) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            finalUnsafe = null;
        }
        unsafe = finalUnsafe;
    }
    public static Unsafe get() {
        return unsafe;
    }
}
