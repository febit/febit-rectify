package org.febit.rectify.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Args {

    public static Integer int0(Object[] args) {
        return asInt(arg0(args));
    }

    public static Integer int1(Object[] args) {
        return asInt(arg1(args));
    }

    public static Integer int2(Object[] args) {
        return asInt(arg2(args));
    }

    public static Integer int3(Object[] args) {
        return asInt(arg3(args));
    }

    public static String string0(Object[] args) {
        return asString(arg0(args));
    }

    public static String string1(Object[] args) {
        return asString(arg1(args));
    }

    public static String string2(Object[] args) {
        return asString(arg2(args));
    }

    public static String string3(Object[] args) {
        return asString(arg3(args));
    }

    public static Object arg0(Object[] args) {
        return argX(args, 0, null);
    }

    public static Object arg0(Object[] args, Object def) {
        return argX(args, 0, def);
    }

    public static Object arg1(Object[] args) {
        return argX(args, 1, null);
    }

    public static Object arg1(Object[] args, Object def) {
        return argX(args, 1, def);
    }

    public static Object arg2(Object[] args) {
        return argX(args, 2, null);
    }

    public static Object arg2(Object[] args, Object def) {
        return argX(args, 2, def);
    }

    public static Object arg3(Object[] args) {
        return argX(args, 3, null);
    }

    public static Object arg3(Object[] args, Object def) {
        return argX(args, 3, def);
    }

    public static Object argX(Object[] args, int x) {
        return argX(args, x, null);
    }

    public static Object argX(Object[] args, int x, Object def) {
        if (args != null && x >= 0 && x < args.length) {
            return args[x];
        }
        return def;
    }

    public static String asString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static Integer asInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof Character) {
            return (int) ((Character) value);
        }
        throw new ClassCastException("Not a number: " + value.getClass());
    }
}
