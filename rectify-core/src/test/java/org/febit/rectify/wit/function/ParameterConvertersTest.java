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

import org.febit.lang.util.JacksonUtils;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JavaType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.febit.rectify.wit.function.ParameterConverters.resolve;
import static org.junit.jupiter.api.Assertions.*;

class ParameterConvertersTest {

    private static JavaType type(Class<?> type) {
        return JacksonUtils.TYPES.constructType(type);
    }

    private static JavaType[] types(Class<?>... types) {
        var javaTypes = new JavaType[types.length];
        for (int i = 0; i < types.length; i++) {
            javaTypes[i] = type(types[i]);
        }
        return javaTypes;
    }

    @Test
    void resolveConvertsKnownTypes() {
        var converters = resolve(types(
                Boolean.class,
                Integer.class,
                BigDecimal.class,
                LocalDate.class,
                Instant.class,
                ZoneOffset.class,
                Object.class
        ), 0, 7);

        assertEquals(7, converters.length);
        assertEquals(Boolean.TRUE, converters[0].convert("true"));
        assertEquals(12, converters[1].convert("12"));
        assertEquals(new BigDecimal("12.50"), converters[2].convert("12.50"));
        assertEquals(LocalDate.parse("2024-01-20"), converters[3].convert("2024-01-20"));
        assertEquals(Instant.parse("2024-01-20T03:04:05Z"), converters[4].convert("2024-01-20T03:04:05Z"));
        assertEquals(ZoneOffset.ofHours(8), converters[5].convert("+08:00"));
        assertSame(this, converters[6].convert(this));
    }

    @Test
    void resolveSupportsSubRange() {
        var converters = resolve(types(
                String.class,
                Integer.class,
                Boolean.class
        ), 1, 3);

        assertEquals(2, converters.length);
        assertEquals(123, converters[0].convert("123"));
        assertEquals(Boolean.TRUE, converters[1].convert("true"));
    }

    @Test
    void resolveFillsMissingTypesWithAbsentConverters() {
        var converters = resolve(types(String.class), 0, 3);

        assertEquals(3, converters.length);
        assertEquals("123", converters[0].convert(123));
        assertNull(converters[1].convert("ignored"));
        assertNull(converters[2].convert(null));
    }

    @Test
    void resolveReturnsEmptyArrayForEmptyRange() {
        assertEquals(0, resolve(types(String.class), 1, 1).length);
    }

    @Test
    void resolveRejectsInvalidRange() {
        var negativeFrom = assertThrows(IllegalArgumentException.class,
                () -> resolve(new JavaType[0], -1, 0));
        assertEquals("from < 0", negativeFrom.getMessage());

        var reversedRange = assertThrows(IllegalArgumentException.class,
                () -> resolve(new JavaType[0], 1, 0));
        assertEquals("to < from", reversedRange.getMessage());
    }

    @Test
    void resolveRejectsUnsupportedType() {
        var unsupportedTypes = types(List.class);
        var ex = assertThrows(IllegalArgumentException.class,
                () -> resolve(unsupportedTypes, 0, 1));
        assertTrue(ex.getMessage().startsWith("Unsupported type: "));
    }

}
