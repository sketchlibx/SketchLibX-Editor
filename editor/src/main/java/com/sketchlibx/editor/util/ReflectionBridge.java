package com.sketchlibx.editor.util;

import java.lang.reflect.Method;

/** Small compatibility bridge for optional Sora APIs across minor releases. */
public final class ReflectionBridge {
    private ReflectionBridge() { }

    public static boolean call(Object target, String method, Class<?>[] types, Object... args) {
        if (target == null) return false;
        try {
            Method m = target.getClass().getMethod(method, types);
            m.setAccessible(true);
            m.invoke(target, args);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static Object callForResult(Object target, String method, Class<?>[] types, Object... args) {
        if (target == null) return null;
        try {
            Method m = target.getClass().getMethod(method, types);
            m.setAccessible(true);
            return m.invoke(target, args);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
