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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("UnnecessaryParentheses")
class CoreLibraryTest {

    private final Lib lib = LibraryTestSupport.export(CoreLibrary.class);

    @Test
    void basicFactories() {
        List.of(
                        "noop",
                        "seq",
                        "uuid",
                        "newList",
                        "newSet",
                        "newMap"
                )
                .forEach(name -> assertDoesNotThrow(() -> lib.function(name)));
    }

    @Test
    void basicPredicates() {
        List.of(
                        "size",
                        "isNull",
                        "nonNull",
                        "isEquals"
                )
                .forEach(name -> assertDoesNotThrow(() -> lib.function(name)));
    }

    @Test
    void values() {
        assertNull(lib.function("noop").apply());

        var first = (Long) lib.function("seq").apply();
        var second = (Long) lib.function("seq").apply();
        assertNotNull(first);
        assertNotNull(second);
        long firstValue = first;
        long secondValue = second;
        assertEquals(firstValue + 1, secondValue);

        var uuid1 = (UUID) lib.function("uuid").apply();
        var uuid2 = (UUID) lib.function("uuid").apply();
        assertNotNull(uuid1);
        assertNotNull(uuid2);
        assertNotEquals(uuid1, uuid2);

        assertInstanceOf(ArrayList.class, lib.function("newList").apply());
        assertInstanceOf(HashSet.class, lib.function("newSet").apply());
        assertInstanceOf(LinkedHashMap.class, lib.function("newMap").apply());
    }

    @Test
    void predicates() {
        assertEquals(3, lib.function("size").apply(List.of(1, 2, 3)));
        assertEquals(2, lib.function("size").apply(Map.of("a", 1, "b", 2)));

        assertEquals(true, lib.function("isNull").apply((Object) null));
        assertEquals(true, lib.function("nonNull").apply("x"));
        assertEquals(true, lib.function("isEquals").apply("same", "same"));
        assertEquals(false, lib.function("isEquals").apply("left", "right"));
    }

}

