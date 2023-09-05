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
import org.febit.lang.util.TimeUtils;
import org.febit.rectify.OutputModel;
import org.febit.rectify.Schema;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.febit.rectify.util.Convert.toBoolean;
import static org.febit.rectify.util.Convert.toNumber;
import static org.febit.rectify.util.TimeConvert.toDate;
import static org.febit.rectify.util.TimeConvert.toDateTime;
import static org.febit.rectify.util.TimeConvert.toInstant;
import static org.febit.rectify.util.TimeConvert.toTime;
import static org.febit.rectify.util.TimeConvert.toZonedDateTime;

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
                return toDate(value);
            case TIME:
                return toTime(value);
            case DATETIME:
                return toDateTime(value);
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
        var iter = Convert.toIterator(value);
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

}
