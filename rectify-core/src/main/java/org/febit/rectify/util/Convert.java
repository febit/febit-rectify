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
import org.apache.commons.collections4.IteratorUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

@UtilityClass
public class Convert {

    @Nullable
    public static String toString(@Nullable Object obj) {
        return Args.asString(obj);
    }

    public static Boolean toBoolean(@Nullable Object raw) {
        if (raw instanceof Boolean) {
            return (Boolean) raw;
        }
        if (raw == null) {
            return false;
        }
        if (raw instanceof Number) {
            return raw.equals(1);
        }
        if (raw instanceof String && isTrue((String) raw)) {
            return true;
        }
        var text = raw.toString().trim().toLowerCase();
        return isTrue(text);
    }

    private static Boolean isTrue(String text) {
        switch (text) {
            case "true":
            case "True":
            case "TRUE":
            case "on":
            case "ON":
            case "1":
                return true;
            default:
                return false;
        }
    }

    @Nullable
    public static Long toLong(@Nullable Object obj) {
        return Convert.toNumber(obj, Number::longValue);
    }

    @Nullable
    public static Integer toInteger(@Nullable Object obj) {
        return Convert.toNumber(obj, Number::intValue);
    }

    @Nullable
    public static Byte toByte(@Nullable Object obj) {
        return Convert.toNumber(obj, Number::byteValue);
    }

    @Nullable
    public static Double toDouble(@Nullable Object obj) {
        return Convert.toNumber(obj, Number::doubleValue);
    }

    @Nullable
    public static Float toFloat(@Nullable Object obj) {
        return Convert.toNumber(obj, Number::floatValue);
    }

    @Nullable
    public static Short toShort(@Nullable Object obj) {
        return Convert.toNumber(obj, Number::shortValue);
    }

    @Nullable
    public static <T extends Number> T toNumber(@Nullable Object obj, Function<Number, T> converter) {
        return toNumber(obj, converter, null);
    }

    @Nullable
    public static Number toNumber(@Nullable Object obj) {
        return Convert.toNumber(obj, Function.identity());
    }

    @Nullable
    public static <T extends Number> T toNumber(
            @Nullable Object raw,
            Function<Number, T> converter,
            @Nullable T defaultValue
    ) {
        if (raw instanceof Number) {
            return converter.apply((Number) raw);
        }
        if (raw == null) {
            return defaultValue;
        }
        if (raw instanceof Character) {
            return converter.apply((int) ((Character) raw));
        }
        var str = raw.toString().trim();
        if (str.isEmpty()) {
            return defaultValue;
        }
        var decimal = new BigDecimal(str);
        return converter.apply(decimal);
    }

    @Nullable
    public static BigDecimal toBigDecimal(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            if (obj instanceof BigDecimal) {
                return (BigDecimal) obj;
            }
            var type = obj.getClass();
            if (type == Integer.class
                    || type == Long.class
                    || type == Short.class
                    || type == Byte.class) {
                return BigDecimal.valueOf(((Number) obj).longValue());
            }
            if (obj instanceof Double) {
                return BigDecimal.valueOf(((Number) obj).doubleValue());
            }
            if (obj instanceof BigInteger) {
                return new BigDecimal((BigInteger) obj);
            }
        }
        if (obj instanceof Character) {
            return new BigDecimal((int) ((Character) obj));
        }
        // Float or else ...
        return new BigDecimal(obj.toString().trim());
    }

    @SuppressWarnings({"unchecked"})
    public static Iterator<Object> toIterator(@Nullable final Object o1) {
        if (o1 == null) {
            return Collections.emptyIterator();
        }
        if (o1 instanceof Iterator) {
            return (Iterator<Object>) o1;
        }
        if (o1 instanceof Iterable) {
            return ((Iterable<Object>) o1).iterator();
        }
        if (o1 instanceof Stream) {
            return ((Stream<Object>) o1).iterator();
        }
        if (o1 instanceof Object[]) {
            return IteratorUtils.arrayIterator((Object[]) o1);
        }
        if (o1 instanceof Enumeration) {
            return IteratorUtils.asIterator((Enumeration<Object>) o1);
        }
        if (o1.getClass().isArray()) {
            return IteratorUtils.arrayIterator(o1);
        }
        throw new RuntimeException("Can't convert to iter: " + o1.getClass());
    }
}
