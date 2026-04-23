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
package org.febit.rectify.flink.table;

import org.apache.flink.types.Row;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.febit.rectify.flink.table.AllTypesTableData.listColumn;
import static org.febit.rectify.flink.table.AllTypesTableData.mapAll;
import static org.junit.jupiter.api.Assertions.*;

class RectifierSqlTypesSupportTest {

    @Test
    void id() throws Exception {
        assertColumn("id", AllTypes::id);
    }

    @Test
    void bool() throws Exception {
        assertColumn("bool", AllTypes::bool);
    }

    @Test
    void tinyInt() throws Exception {
        assertColumn("tinyInt", AllTypes::tinyInt);
    }

    @Test
    void shortNum() throws Exception {
        assertColumn("shortNum", AllTypes::shortNum);
    }

    @Test
    void intNum() throws Exception {
        assertColumn("intNum", AllTypes::intNum);
    }

    @Test
    void longNum() throws Exception {
        assertColumn("longNum", AllTypes::longNum);
    }

    @Test
    void floatNum() throws Exception {
        assertColumn("floatNum", AllTypes::floatNum);
    }

    @Test
    void doubleNum() throws Exception {
        assertColumn("doubleNum", AllTypes::doubleNum);
    }

    @Test
    void decimalNum() throws Exception {
        assertColumn("decimalNum", AllTypes::decimalNum);
    }

    @Test
    void text() throws Exception {
        assertColumn("text", AllTypes::text);
    }

    @Test
    void date() throws Exception {
        assertColumn("date", AllTypes::date);
    }

    @Test
    void time() throws Exception {
        assertColumn("time", AllTypes::time);
    }

    @Test
    void datetime() throws Exception {
        assertColumn("datetime", AllTypes::datetime);
    }

    @Test
    void instant() throws Exception {
        assertColumn("instant", AllTypes::instant);
    }

    @Test
    void datatimetz() throws Exception {
        assertColumn("datatimetz", AllTypes::datatimetz);
    }

    @Test
    void structFoo() throws Exception {
        assertColumn("structFoo", AllTypes::structFoo);
    }

    @Test
    void arrayBool() throws Exception {
        assertColumn("arrayBool", AllTypes::arrayBool);
    }

    @Test
    void arrayByte() throws Exception {
        assertColumn("arrayByte", AllTypes::arrayByte);
    }

    @Test
    void arrayShort() throws Exception {
        assertColumn("arrayShort", AllTypes::arrayShort);
    }

    @Test
    void arrayInt() throws Exception {
        assertColumn("arrayInt", AllTypes::arrayInt);
    }

    @Test
    void arrayLong() throws Exception {
        assertColumn("arrayLong", AllTypes::arrayLong);
    }

    @Test
    void arrayFloat() throws Exception {
        assertColumn("arrayFloat", AllTypes::arrayFloat);
    }

    @Test
    void arrayDouble() throws Exception {
        assertColumn("arrayDouble", AllTypes::arrayDouble);
    }

    @Test
    void arrayDecimal() throws Exception {
        assertColumn("arrayDecimal", AllTypes::arrayDecimal);
    }

    @Test
    void arrayString() throws Exception {
        assertColumn("arrayString", AllTypes::arrayString);
    }

    @Test
    void arrayFoo() throws Exception {
        assertColumn("arrayFoo", AllTypes::arrayFoo);
    }

    @Test
    void list() throws Exception {
        assertColumn("list", AllTypes::list);
    }

    @Test
    void bytes() throws Exception {
        var actual = normalizeValues(listColumn("bytes"));
        var expected = normalizeValues(mapAll(value -> Base64.getEncoder().encode(value.bytes())));
        assertEquals(expected, actual);
    }

    @Test
    void map() throws Exception {
        assertColumn("map", AllTypes::map);
    }

    @Test
    void fooStatus() throws Exception {
        var actual = normalizeValues(listColumn("structFoo", "status"));
        var expected = normalizeValues(mapAll(r -> r.structFoo().status()));
        assertEquals(expected, actual);
    }

    private static void assertColumn(String field, Function<AllTypes, ?> mapper) throws Exception {
        var actual = normalizeValues(listColumn(field));
        var expected = normalizeValues(mapAll(mapper));
        assertEquals(expected, actual);
    }

    private static List<Object> normalizeValues(List<?> values) {
        return values.stream()
                .map(RectifierSqlTypesSupportTest::normalizeValue)
                .toList();
    }

    private static Object normalizeValue(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal.setScale(2, RoundingMode.HALF_UP);
        }
        if (value instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.toInstant();
        }
        if (value instanceof AllTypesTableData.StructFoo structFoo) {
            return normalizeValue(structFoo.toRow());
        }
        if (value instanceof Row row) {
            return normalizeRow(row);
        }
        if (value instanceof Map<?, ?> map) {
            return normalizeMap(map);
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(RectifierSqlTypesSupportTest::normalizeValue)
                    .toList();
        }
        if (value != null && value.getClass().isArray()) {
            return normalizeArray(value);
        }
        return value;
    }

    private static List<Object> normalizeRow(Row row) {
        var result = new java.util.ArrayList<>(row.getArity());
        for (int i = 0; i < row.getArity(); i++) {
            result.add(normalizeValue(row.getField(i)));
        }
        return result;
    }

    private static List<Object> normalizeArray(Object array) {
        var length = Array.getLength(array);
        var result = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            result.add(normalizeValue(Array.get(array, i)));
        }
        return result;
    }

    private static Map<Object, Object> normalizeMap(Map<?, ?> map) {
        var result = new LinkedHashMap<>();
        for (var entry : map.entrySet()) {
            result.put(entry.getKey(), normalizeValue(entry.getValue()));
        }
        return result;
    }

}
