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

import java.util.List;
import java.util.Map;

import static org.febit.rectify.lib.LibraryTestSupport.export;
import static org.febit.rectify.lib.LibraryTestSupport.namespace;
import static org.junit.jupiter.api.Assertions.*;

class ObjectLibraryTest {

    private final Lib library = export(ObjectLibrary.class);
    private final Lib object = namespace(ObjectLibrary.class, "Object");

    @Test
    void alias() {
        assertNotNull(library.get("Object"));
    }

    @Test
    void noop() {
        assertNull(object.call("noop"));
    }

    @Test
    void seq() {
        var first = (Long) object.call("seq");
        var second = (Long) object.call("seq");
        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.longValue() + 1L, second.longValue());
    }

    @Test
    void sizeOf() {
        assertEquals(0, object.call("sizeOf", (Object) null));
        assertEquals(3, object.call("sizeOf", "abc"));
        assertEquals(2, object.call("sizeOf", List.of(1, 2)));
        assertEquals(1, object.call("sizeOf", Map.of("a", 1)));
        assertEquals(3, object.call("sizeOf", (Object) new int[]{1, 2, 3}));
    }

    @Test
    void nullAndEquals() {
        assertEquals(true, object.call("isNull", (Object) null));
        assertEquals(false, object.call("isNull", "a"));
        assertEquals(true, object.call("nonNull", "a"));
        assertEquals(false, object.call("nonNull", (Object) null));

        assertEquals(true, object.call("isEquals", "a", "a"));
        assertEquals(false, object.call("isEquals", "a", "b"));
        assertEquals(true, object.call("isEquals", null, null));
    }
}


