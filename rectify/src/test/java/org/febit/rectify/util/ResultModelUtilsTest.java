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

import org.febit.rectify.GenericStruct;
import org.febit.rectify.Schema;
import org.febit.rectify.TestSchemas;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        GenericStruct record;

        record = (GenericStruct) ResultModelUtils.convert(schema, namedMap(
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
                "unused", "unused field"
        ), GenericStruct.model());

        // id
        assertEquals(1234, record.get(0));
        // name
        assertEquals("Mr.R", record.get(1));
        // ints
        assertEquals(new ArrayList<>(), record.get(2));
        // float
        assertEquals(11111F, record.get(3));
        // double
        assertEquals(1.23D, record.get(4));
        // strings
        assertEquals(asList("1", "2.0", "345", "a"), record.get(5));
        // longMap
        assertEquals(namedMap("1", 1L, "2", 2L, "NULL", 0L), record.get(6));
        // stringMap
        assertEquals(namedMap(), record.get(7));
        // optionalStringMap
        assertEquals(namedMap("1", "1", "2", "2", "NULL", ""), record.get(8));

        // session struct<id:string,launch:bigint,du:long,date:int>
        assertTrue(record.get(9) instanceof GenericStruct);
        GenericStruct session = (GenericStruct) record.get(9);
        assertEquals("1", session.get(0));
        assertEquals(0L, session.get(1));
        assertEquals(2L, session.get(2));
        assertEquals(0, session.get(3));

        // events array<struct< du: bigint, name : string  ,  ts:optional<bigint>, attrs:map<String> , struct:struct<xx:String>, flag:boolean>  >
        assertTrue(record.get(10) instanceof List);
        @SuppressWarnings("unchecked")
        List<GenericStruct> events = (List<GenericStruct>) record.get(10);
        assertEquals(3, events.size());
        GenericStruct event;
        event = events.get(0);
        // event.du
        assertEquals(2L, event.get(0));
        // event.name
        assertEquals("1", event.get(1));
        // event.ts
        assertEquals(3L, event.get(2));
        // event.attrs
        assertEquals(namedMap(), event.get(3));
        // event.struct
        assertTrue(event.get(4) instanceof GenericStruct);
        assertEquals("", ((GenericStruct) event.get(4)).get(0));
        // event.flag
        assertEquals(true, event.get(5));

        event = events.get(1);
        // event.du
        assertEquals(2L, event.get(0));
        // event.name
        assertEquals("2", event.get(1));
        // event.ts
        assertNull(event.get(2));
        // event.attrs
        assertEquals(namedMap("1", "1", "2", "2", "NULL", ""), event.get(3));
        // event.struct
        assertTrue(event.get(4) instanceof GenericStruct);
        assertEquals("", ((GenericStruct) event.get(4)).get(0));
        // event.flag
        assertEquals(false, event.get(5));

        event = events.get(2);
        // event.du
        assertEquals(0L, event.get(0));
        // event.name
        assertEquals("", event.get(1));
        // event.ts
        assertNull(event.get(2));
        // event.attrs
        assertEquals(namedMap(), event.get(3));
        // event.struct
        assertTrue(event.get(4) instanceof GenericStruct);
        assertEquals("yy", ((GenericStruct) event.get(4)).get(0));
        // event.flag
        assertEquals(false, event.get(5));

    }
}
