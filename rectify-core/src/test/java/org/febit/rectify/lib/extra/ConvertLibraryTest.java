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
package org.febit.rectify.lib.extra;

import org.febit.rectify.lib.Lib;
import org.febit.rectify.lib.LibraryTestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConvertLibraryTest {

    private final Lib lib = LibraryTestSupport.export(ConvertLibrary.class);

    @Test
    void alias() {
        assertSame(lib.get("stringify"), lib.get("toString"));
    }

    @Test
    void basicNumbers() {
        List.of(
                        "toNumber",
                        "toByte",
                        "toShort",
                        "toInteger",
                        "toLong",
                        "toBigDecimal"
                )
                .forEach(name -> assertDoesNotThrow(() -> lib.function(name)));

        var number = (Number) lib.call("toNumber", "12");
        assertNotNull(number);
        assertEquals(12, number.intValue());

        assertEquals((byte) 1, lib.call("toByte", 1));
        assertEquals((short) 2, lib.call("toShort", 2));
        assertEquals(123, lib.call("toInteger", "123"));
        assertEquals(123L, lib.call("toLong", "123"));
    }

    @Test
    void basicScalars() {
        List.of(
                        "toFloat",
                        "toDouble",
                        "toBoolean",
                        "stringify",
                        "toString"
                )
                .forEach(name -> assertDoesNotThrow(() -> lib.function(name)));

        assertEquals(1.5f, lib.call("toFloat", "1.5"));
        assertEquals(2.5d, lib.call("toDouble", "2.5"));
        assertEquals(Boolean.TRUE, lib.call("toBoolean", "true"));
        assertEquals("123", lib.call("stringify", 123));
        assertEquals("123", lib.call("toString", 123));

        assertNull(lib.call("toInteger", (Object) null));
        assertNull(lib.call("stringify", (Object) null));
    }

}

