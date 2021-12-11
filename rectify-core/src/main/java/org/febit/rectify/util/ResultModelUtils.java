/**
 * Copyright 2018-present febit.org (support@febit.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.rectify.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.febit.rectify.ResultModel;
import org.febit.rectify.Schema;
import org.febit.util.CollectionUtil;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

/**
 * @author zqq90
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResultModelUtils {

    public static Object convert(Schema schema, Object value, ResultModel model) {
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
            case BIGINT:
                return toNumber(value, Number::longValue, 0L);
            case STRING:
                return value.toString();
            case MAP:
                if (value instanceof Map) {
                    return convertToMap(schema, (Map<?, ?>) value, model);
                }
                throw new IllegalArgumentException("Unsupported map type: " + value.getClass());
            case STRUCT:
                if (value instanceof Map) {
                    return convertToStruct(schema, (Map<?, ?>) value, model);
                }
                throw new IllegalArgumentException("Unsupported record type: " + value.getClass());
            case ARRAY:
                return convertToArray(schema, value, model);
            case BYTES:
                // TODO need support bytes
            default:
                throw new IllegalArgumentException("Unsupported type: " + schema.getType());
        }
    }

    private static Boolean toBoolean(Object raw) {
        if (raw instanceof Boolean) {
            return (Boolean) raw;
        }
        if (raw == null) {
            return false;
        }
        if (raw instanceof Number) {
            return raw.equals(1);
        }
        String str = raw.toString().trim().toLowerCase();
        return str.equals("true") || str.equals("on");
    }

    private static <T extends Number> T toNumber(Object raw, Function<Number, T> converter, T defaultValue) {
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

    private static Object getDefaultValue(Schema schema, ResultModel model) {
        switch (schema.getType()) {
            case BOOLEAN:
                return Boolean.FALSE;
            case INT:
                return 0;
            case BIGINT:
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
                return convertToStruct(schema, Collections.emptyMap(), model);
            case OPTIONAL:
                return null;
            default:
                return null;
        }
    }

    private static List<Object> convertToArray(Schema schema, Object value, ResultModel model) {
        Iterator iter = CollectionUtil.toIterator(value);
        List<Object> list = new ArrayList<>();
        Schema valueType = schema.valueType();
        while (iter.hasNext()) {
            list.add(convert(valueType, iter.next(), model));
        }
        return list;
    }

    private static Map<String, Object> convertToMap(Schema schema, Map<?, ?> value, ResultModel model) {
        if (value == null) {
            return null;
        }
        Map<String, Object> distMap = new HashMap<>(value.size() * 4 / 3 + 1);
        Schema valueType = schema.valueType();
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            Object key = entry.getKey();
            //Note: ignore null-key
            if (key == null) {
                continue;
            }
            Object val = convert(valueType, entry.getValue(), model);
            distMap.put(key.toString(), val);
        }
        return distMap;
    }

    @SuppressWarnings("unchecked")
    private static Object convertToStruct(Schema schema, Map<?, ?> value, ResultModel model) {
        Object struct = model.newStruct(schema);
        for (Schema.Field field : schema.fields()) {
            Object val = convert(field.schema(), value.get(field.name()), model);
            model.setField(struct, field, val);
        }
        return struct;
    }
}
