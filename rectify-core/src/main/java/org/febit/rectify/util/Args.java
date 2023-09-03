/*
 * Copyright 2018-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.rectify.util;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Args {

    @Nullable
    public static Integer int0(Object[] args) {
        return asInt(arg0(args));
    }

    @Nullable
    public static Integer int1(Object[] args) {
        return asInt(arg1(args));
    }

    @Nullable
    public static Integer int2(Object[] args) {
        return asInt(arg2(args));
    }

    @Nullable
    public static Integer int3(Object[] args) {
        return asInt(arg3(args));
    }

    @Nullable
    public static String string0(Object[] args) {
        return asString(arg0(args));
    }

    @Nullable
    public static String string1(Object[] args) {
        return asString(arg1(args));
    }

    @Nullable
    public static String string2(Object[] args) {
        return asString(arg2(args));
    }

    @Nullable
    public static String string3(Object[] args) {
        return asString(arg3(args));
    }

    @Nullable
    public static Object arg0(Object[] args) {
        return argX(args, 0, null);
    }

    @Nullable
    public static Object arg0(Object[] args, @Nullable Object def) {
        return argX(args, 0, def);
    }

    @Nullable
    public static Object arg1(Object[] args) {
        return argX(args, 1, null);
    }

    @Nullable
    public static Object arg1(Object[] args, @Nullable Object def) {
        return argX(args, 1, def);
    }

    @Nullable
    public static Object arg2(Object[] args) {
        return argX(args, 2, null);
    }

    @Nullable
    public static Object arg2(Object[] args, @Nullable Object def) {
        return argX(args, 2, def);
    }

    @Nullable
    public static Object arg3(Object[] args) {
        return argX(args, 3, null);
    }

    @Nullable
    public static Object arg3(Object[] args, @Nullable Object def) {
        return argX(args, 3, def);
    }

    @Nullable
    public static Object argX(Object[] args, int x) {
        return argX(args, x, null);
    }

    @Nullable
    public static Object argX(@Nullable Object[] args, int x, @Nullable Object def) {
        if (args != null && x >= 0 && x < args.length) {
            return args[x];
        }
        return def;
    }

    @Nullable
    public static String asString(@Nullable Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    @Nullable
    public static Integer asInt(@Nullable Object value) {
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
