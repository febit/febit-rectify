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
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.febit.rectify.lib.LibraryTestSupport.export;
import static org.febit.rectify.lib.LibraryTestSupport.namespace;
import static org.junit.jupiter.api.Assertions.*;

class CollectionLibraryTest {

    private final Lib library = export(CollectionLibrary.class);
    private final Lib list = namespace(CollectionLibrary.class, "List");
    private final Lib set = namespace(CollectionLibrary.class, "Set");
    private final Lib map = namespace(CollectionLibrary.class, "Map");

    @Test
    void alias() {
        assertNotNull(library.get("List"));
        assertNotNull(library.get("Set"));
        assertNotNull(library.get("Map"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void list() {
        var created = list.call("create");
        assertInstanceOf(java.util.ArrayList.class, created);

        assertEquals(List.of("a", "b"), list.call("of", "a", "b"));
        assertThrows(NullPointerException.class, () -> list.call("of", "a", null));

        var nullable = list.call("ofNullable", "a", null, "b");
        assertEquals(Arrays.asList("a", null, "b"), nullable);
        assertThrows(UnsupportedOperationException.class, () -> ((List<Object>) nullable).add("c"));

        assertEquals(List.of(), list.call("empty"));
        assertEquals(List.of("a", "b"), list.call("copyOf", List.of("a", "b")));
        var listWithNull = Arrays.asList("a", null);
        assertThrows(NullPointerException.class, () -> list.call("copyOf", listWithNull));

        var copiedNullable = list.call("copyOfNullable", listWithNull);
        assertEquals(listWithNull, copiedNullable);
        assertThrows(UnsupportedOperationException.class, ((List<?>) copiedNullable)::clear);
    }

    @Test
    @SuppressWarnings("unchecked")
    void set() {
        var created = set.call("create");
        assertInstanceOf(java.util.HashSet.class, created);

        var result = set.call("of", "a", "b");
        assertEquals(Set.of("a", "b"), result);
        assertThrows(IllegalArgumentException.class, () -> set.call("of", "a", "a"));

        var nullable = set.call("ofNullable", "a", null, "a");
        var expected = setWithNullable("a");
        assertEquals(expected, nullable);
        assertThrows(UnsupportedOperationException.class, () -> ((Set<Object>) nullable).add("c"));

        assertEquals(Set.of(), set.call("empty"));
        assertEquals(Set.of("a", "b"), set.call("copyOf", List.of("a", "b")));
        var listWithNull = Arrays.asList("a", null);
        assertThrows(NullPointerException.class, () -> set.call("copyOf", listWithNull));

        var copiedNullable = set.call("copyOfNullable", listWithNull);
        assertEquals(expected, copiedNullable);
        assertThrows(UnsupportedOperationException.class, ((Set<?>) copiedNullable)::clear);
    }

    @Test
    @SuppressWarnings("unchecked")
    void map() {
        var created = map.call("create");
        assertInstanceOf(LinkedHashMap.class, created);

        assertEquals(Map.of(), map.call("empty"));

        var source = new LinkedHashMap<String, Integer>();
        source.put("a", 1);
        source.put("b", 2);

        var copied = map.call("copyOf", source);
        assertEquals(Map.of("a", 1, "b", 2), copied);
        assertThrows(UnsupportedOperationException.class, () -> ((Map<Object, Object>) copied).put("c", 3));

        var withNull = new LinkedHashMap<String, Integer>();
        withNull.put("a", 1);
        withNull.put(null, 2);
        assertThrows(NullPointerException.class, () -> map.call("copyOf", withNull));

        var copiedNullable = map.call("copyOfNullable", withNull);
        assertEquals(2, ((Map<?, ?>) copiedNullable).size());
        assertEquals(Arrays.asList("a", null), new java.util.ArrayList<>(((Map<?, ?>) copiedNullable).keySet()));
        assertThrows(UnsupportedOperationException.class, ((Map<?, ?>) copiedNullable)::clear);
    }

    private static Set<Object> setWithNullable(Object value) {
        var set = new java.util.HashSet<Object>();
        set.add(value);
        set.add(null);
        return set;
    }
}


