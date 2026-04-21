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
package org.febit.rectify.wit.function;

import lombok.experimental.UtilityClass;
import org.febit.lang.util.ConvertUtils;
import tools.jackson.databind.JavaType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Map;

import static java.util.Map.entry;

@UtilityClass
public class ParameterConverters {

    private static final ParameterConverter ABSENT_CONVERTER = a -> null;

    private static final Map<Class<?>, ParameterConverter> CONVERTERS = Map.ofEntries(
            entry(Boolean.class, ConvertUtils::toBoolean),
            entry(String.class, ConvertUtils::toString),
            entry(Byte.class, ConvertUtils::toByte),
            entry(Short.class, ConvertUtils::toShort),
            entry(Integer.class, ConvertUtils::toInteger),
            entry(Long.class, ConvertUtils::toLong),
            entry(Float.class, ConvertUtils::toFloat),
            entry(Double.class, ConvertUtils::toDouble),
            entry(BigDecimal.class, ConvertUtils::toBigDecimal),
            entry(Temporal.class, ConvertUtils::toTemporal),
            entry(LocalDate.class, ConvertUtils::toDate),
            entry(LocalTime.class, ConvertUtils::toTime),
            entry(LocalDateTime.class, ConvertUtils::toDateTime),
            entry(Instant.class, ConvertUtils::toInstant),
            entry(ZonedDateTime.class, ConvertUtils::toZonedDateTime),
            entry(ZoneOffset.class, ConvertUtils::toZone),
            entry(Object.class, a -> a)
    );

    @SuppressWarnings({"SameParameterValue"})
    public static ParameterConverter[] resolve(JavaType[] types, int from, int to) {
        if (from < 0) {
            throw new IllegalArgumentException("from < 0");
        }
        if (to < from) {
            throw new IllegalArgumentException("to < from");
        }
        var size = to - from;
        var converters = new ParameterConverter[size];
        int i = 0;
        for (int j = from, end = Math.min(size, types.length); i < end; i++, j++) {
            converters[i] = of(types[j]);
        }
        for (; i < size; i++) {
            converters[i] = ABSENT_CONVERTER;
        }
        return converters;
    }

    private static ParameterConverter of(JavaType type) {
        var cls = type.getRawClass();
        var converter = CONVERTERS.get(cls == null ? Object.class : cls);
        if (converter == null) {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
        return converter;
    }
}
