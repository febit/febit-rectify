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

import org.febit.rectify.ResultModel;
import org.febit.rectify.Schema;
import org.febit.rectify.TestSchemas;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultModelUtilsTest {

    private static List<Object> asList(Object... items) {
        return Arrays.asList(items);
    }

    private static List<Object> asArray(Object... items) {
        return Arrays.asList(items);
    }

    private static Map<Object, Object> namedMap(Object... items) {
        Map<Object, Object> map = new HashMap<>();
        int i = 0;
        while (i < items.length - 1) {
            map.put(items[i++], items[i++]);
        }
        return map;
    }

    @Test
    void testConvertComplex() {
        Schema schema = TestSchemas.COMPLEX;

        ZonedDateTime time = ZonedDateTime.parse("2022-01-23T02:03:56+07:00");

        @SuppressWarnings("unchecked")
        List<Object> record = (List<Object>) ResultModelUtils.convert(schema, namedMap(
                "id", 1234L,
                "name", "Mr.R",
                "float", 11111D,
                "double", "1.23",
                "strings", asArray(1, 2D, "345", 'a'),
                "longMap", namedMap(1, 1, "2", 2L, "NULL", null),
                "optionalStringMap", namedMap(1, 1, "2", 2L, "NULL", null),
                "session", namedMap("id", 1, "du", 2L),
                "events", asList(
                        namedMap("name", 1, "du", 2L, "ts", 3, "flag", true),
                        namedMap("name", "2", "du", 2L, "attrs", namedMap(1, 1, "2", 2L, "NULL", null)),
                        namedMap("struct", namedMap("xx", "yy"))
                ),
                "times", asList(
                        namedMap(),
                        namedMap(
                                "time", time.toLocalTime().toString(),
                                "date", time.toLocalDate(),
                                "dt", time.toLocalDateTime(),
                                "instant", time,
                                "dtz", time.toString()
                        )
                ),
                "unused", "unused field"
        ), new ListResultModel());

        assertEquals(asArray(
                // 0 id
                1234,
                // 1 name
                "Mr.R",
                // 2 ints
                new ArrayList<>(),
                // 3 float
                11111F,
                // 4 double
                1.23D,
                // 5 strings
                asList("1", "2.0", "345", "a"),
                // 6 longMap
                namedMap("1", 1L, "2", 2L, "NULL", 0L),
                // 7 stringMap
                namedMap(),
                // 8 optionalStringMap
                namedMap("1", "1", "2", "2", "NULL", ""),
                // 9 session struct<id:string,launch:bigint,du:long,date:int>
                asArray("1", 0L, 2L, 0),
                // 10 events array<struct< du: bigint, name : string  ,  ts:optional<bigint>, attrs:map<String> , struct:struct<xx:String>, flag:boolean>  >
                asArray(asArray(
                        // event.du
                        2L,
                        // event.name
                        "1",
                        // event.ts
                        3L,
                        // event.attrs
                        namedMap(),
                        // event.struct
                        asArray(""),
                        // event.flag
                        true
                        ), Arrays.asList(
                        // event.du
                        2L,
                        // event.name
                        "2",
                        // event.ts
                        null,
                        // event.attrs
                        namedMap("1", "1", "2", "2", "NULL", ""),
                        // event.struct
                        asArray(""),
                        // event.flag
                        false
                        ), Arrays.asList(
                        // event.du
                        0L,
                        // event.name
                        "",
                        // event.ts
                        null,
                        // event.attrs
                        namedMap(),
                        // event.struct
                        asArray("yy"),
                        // event.flag
                        false
                        )
                ),
                asArray(
                        asList(TimeUtils.TIME_DEFAULT, TimeUtils.DATE_DEFAULT, TimeUtils.DATETIME_DEFAULT, TimeUtils.ZONED_DATETIME_DEFAULT, TimeUtils.INSTANT_DEFAULT),
                        asList(time.toLocalTime(), time.toLocalDate(), time.toLocalDateTime(), time, time.toInstant())
                )
        ), record);
    }


    private static class ListResultModel implements ResultModel<List<Object>> {

        @Override
        public List<Object> newStruct(Schema schema) {
            return new ArrayList<>(Arrays.asList(new Object[schema.fieldSize()]));
        }

        @Override
        public void setField(List<Object> record, Schema.Field field, Object val) {
            record.set(field.pos(), val);
        }

        @Override
        public Object getField(List<Object> record, Schema.Field field) {
            return record.get(field.pos());
        }
    }
}
