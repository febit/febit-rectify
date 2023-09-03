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
import org.febit.lang.util.TimeUtils;
import org.febit.rectify.OutputModel;
import org.febit.rectify.Schema;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@UtilityClass
public class OutputModelUtils {

    @Nullable
    public static Object convert(Schema schema, @Nullable Object value, OutputModel<?> model) {
        if (value == null) {
            return getDefaultValue(schema, model);
        }
        switch (schema.getType()) {
            case OPTIONAL:
                return convert(schema.valueType(), value, model);
            case FLOAT:
                return toNumber(value, Number::floatValue, 0F);
            case DOUBLE:
                return toNumber(value, Number::doubleValue, 0D);
            case INT:
                return toNumber(value, Number::intValue, 0);
            case BOOLEAN:
                return toBoolean(value);
            case INT64:
                return toNumber(value, Number::longValue, 0L);
            case STRING:
                return value.toString();
            case DATE:
                return toLocalDate(value);
            case TIME:
                return toLocalTime(value);
            case DATETIME:
                return toLocalDateTime(value);
            case DATETIME_WITH_TIMEZONE:
                return toZonedDateTime(value);
            case INSTANT:
                return toInstant(value);
            case MAP:
                if (value instanceof Map) {
                    return constructMap(schema, (Map<?, ?>) value, model);
                }
                throw new IllegalArgumentException("Unsupported map type: " + value.getClass());
            case STRUCT:
                if (value instanceof Map) {
                    return constructStruct(schema, (Map<?, ?>) value, model);
                }
                throw new IllegalArgumentException("Unsupported record type: " + value.getClass());
            case ARRAY:
                return constructArray(schema, value, model);
            case BYTES:
                // TODO need support bytes
            default:
                throw new IllegalArgumentException("Unsupported type: " + schema.getType());
        }
    }

    @Nullable
    private static Instant toInstant(Object obj) {
        if (obj instanceof Temporal) {
            return TimeUtils.instant((Temporal) obj);
        }
        return TimeUtils.parseInstant(obj.toString());
    }

    @Nullable
    private static ZonedDateTime toZonedDateTime(Object obj) {
        if (obj instanceof Temporal) {
            return TimeUtils.zonedDateTime((Temporal) obj);
        }
        return TimeUtils.parseZonedDateTime(obj.toString());
    }

    @Nullable
    private static LocalDateTime toLocalDateTime(Object obj) {
        if (obj instanceof TemporalAccessor) {
            return TimeUtils.localDateTime((TemporalAccessor) obj);
        }
        return TimeUtils.parseDateTime(obj.toString());
    }

    @Nullable
    private static LocalDate toLocalDate(Object obj) {
        if (obj instanceof TemporalAccessor) {
            return TimeUtils.localDate((TemporalAccessor) obj);
        }
        return TimeUtils.parseDate(obj.toString());
    }

    @Nullable
    private static LocalTime toLocalTime(Object obj) {
        if (obj instanceof TemporalAccessor) {
            return TimeUtils.localTime((TemporalAccessor) obj);
        }
        return TimeUtils.parseTime(obj.toString());
    }

    private static Boolean toBoolean(@Nullable Object raw) {
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
    private static <T extends Number> T toNumber(
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
        String str = raw.toString().trim();
        if (str.isEmpty()) {
            return defaultValue;
        }
        BigDecimal number = new BigDecimal(str);
        return converter.apply(number);
    }

    @Nullable
    private static Object getDefaultValue(Schema schema, OutputModel<?> model) {
        switch (schema.getType()) {
            case BOOLEAN:
                return Boolean.FALSE;
            case INT:
                return 0;
            case INT64:
                return 0L;
            case FLOAT:
                return 0F;
            case DOUBLE:
                return 0D;
            case ARRAY:
                return new ArrayList<>(0);
            case STRING:
                return "";
            case MAP:
                return new HashMap<>(0);
            case BYTES:
                return ByteBuffer.wrap(new byte[0]);
            case STRUCT:
                return constructStruct(schema, Map.of(), model);
            case OPTIONAL:
                return null;
            case INSTANT:
                return TimeUtils.INSTANT_DEFAULT;
            case DATE:
                return TimeUtils.DATE_DEFAULT;
            case TIME:
                return TimeUtils.TIME_DEFAULT;
            case DATETIME:
                return TimeUtils.DATETIME_DEFAULT;
            case DATETIME_WITH_TIMEZONE:
                return TimeUtils.ZONED_DATETIME_DEFAULT;
            default:
                throw new IllegalArgumentException("Unsupported type: " + schema.getType());
        }
    }

    private static List<Object> constructArray(Schema schema, @Nullable Object value, OutputModel<?> model) {
        var iter = toIterator(value);
        var list = new ArrayList<>();
        var valueType = schema.valueType();
        while (iter.hasNext()) {
            list.add(convert(valueType, iter.next(), model));
        }
        return list;
    }

    private static Map<String, Object> constructMap(Schema schema, Map<?, ?> value, OutputModel<?> model) {
        var distMap = new HashMap<String, Object>(value.size() * 4 / 3 + 1);
        var valueType = schema.valueType();
        for (var entry : value.entrySet()) {
            var key = entry.getKey();
            //Note: ignore null-key
            if (key == null) {
                continue;
            }
            var v = convert(valueType, entry.getValue(), model);
            distMap.put(key.toString(), v);
        }
        return distMap;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object constructStruct(Schema schema, @Nullable Map<?, ?> value, OutputModel model) {
        var struct = model.newStruct(schema);
        if (value == null) {
            value = Map.of();
        }
        for (var field : schema.fields()) {
            var v = convert(field.schema(), value.get(field.name()), model);
            model.setField(struct, field, v);
        }
        return struct;
    }

    @SuppressWarnings({"unchecked"})
    private static Iterator<Object> toIterator(@Nullable final Object o1) {
        if (o1 == null) {
            return Collections.emptyIterator();
        }
        if (o1 instanceof Iterator) {
            return (Iterator<Object>) o1;
        }
        if (o1 instanceof Iterable) {
            return ((Iterable<Object>) o1).iterator();
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
