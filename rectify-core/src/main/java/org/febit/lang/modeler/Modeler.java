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
package org.febit.lang.modeler;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.febit.lang.util.Iterators;
import org.febit.lang.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.febit.lang.util.ConvertUtils.toBoolean;
import static org.febit.lang.util.ConvertUtils.toNumber;
import static org.febit.rectify.util.TimeConvert.toDate;
import static org.febit.rectify.util.TimeConvert.toDateTime;
import static org.febit.rectify.util.TimeConvert.toInstant;
import static org.febit.rectify.util.TimeConvert.toTime;
import static org.febit.rectify.util.TimeConvert.toZonedDateTime;

@UtilityClass
public class Modeler {

    @Nullable
    public static Object process(Schema schema, @Nullable Object value, StructSpec<?> model) {
        if (value == null) {
            return resolveDefaultValue(schema, model);
        }
        switch (schema.type()) {
            case OPTIONAL:
                return process(schema.valueType(), value, model);
            case FLOAT:
                return toNumber(value, Number::floatValue, 0F);
            case DOUBLE:
                return toNumber(value, Number::doubleValue, 0D);
            case INT:
                return toNumber(value, Number::intValue, 0);
            case LONG:
                return toNumber(value, Number::longValue, 0L);
            case BOOLEAN:
                return toBoolean(value);
            case STRING:
                return value.toString();
            case DATE:
                return toDate(value);
            case TIME:
                return toTime(value);
            case DATETIME:
                return toDateTime(value);
            case DATETIME_ZONED:
                return toZonedDateTime(value);
            case INSTANT:
                return toInstant(value);
            case ARRAY:
            case LIST:
                return constructArray(schema, value, model);
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
            case BYTES:
                // TODO need support bytes
            default:
                throw new IllegalArgumentException("Unsupported type: " + schema.type());
        }
    }

    @Nullable
    private static Object resolveDefaultValue(Schema schema, StructSpec<?> model) {
        switch (schema.type()) {
            case OPTIONAL:
                return null;
            case INT:
                return 0;
            case LONG:
                return 0L;
            case FLOAT:
                return 0F;
            case DOUBLE:
                return 0D;
            case STRING:
                return "";
            case BOOLEAN:
                return Boolean.FALSE;
            case BYTES:
                return new byte[0];
            case ARRAY:
                return new ArrayList<>(0);
            case MAP:
                return new HashMap<>(0);
            case STRUCT:
                return constructStruct(schema, Map.of(), model);
            case INSTANT:
                return TimeUtils.INSTANT_DEFAULT;
            case DATE:
                return TimeUtils.DATE_DEFAULT;
            case TIME:
                return TimeUtils.TIME_DEFAULT;
            case DATETIME:
                return TimeUtils.DATETIME_DEFAULT;
            case DATETIME_ZONED:
                return TimeUtils.ZONED_DATETIME_DEFAULT;
            default:
                throw new IllegalArgumentException("Unsupported type: " + schema.type());
        }
    }

    private static List<Object> constructArray(Schema schema, @Nullable Object value, StructSpec<?> model) {
        var iter = Iterators.forAny(value);
        var list = new ArrayList<>();
        var valueType = schema.valueType();
        while (iter.hasNext()) {
            list.add(process(valueType, iter.next(), model));
        }
        return list;
    }

    private static Map<Object, Object> constructMap(Schema schema, Map<?, ?> value, StructSpec<?> model) {
        var distMap = new HashMap<>(value.size() * 4 / 3 + 1);
        var keyType = schema.keyType();
        var valueType = schema.valueType();
        for (var entry : value.entrySet()) {
            var key = process(keyType, entry.getKey(), model);
            //Note: ignore null-key
            if (key == null) {
                continue;
            }
            var v = process(valueType, entry.getValue(), model);
            distMap.put(key.toString(), v);
        }
        return distMap;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object constructStruct(Schema schema, Map<?, ?> value, StructSpec model) {
        var struct = model.create(schema);
        for (var field : schema.fields()) {
            var v = process(field.schema(), value.get(field.name()), model);
            model.set(struct, field, v);
        }
        return struct;
    }

}
