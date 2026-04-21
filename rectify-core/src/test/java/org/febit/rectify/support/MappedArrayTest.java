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
package org.febit.rectify.support;

import org.febit.lang.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class MappedArrayTest {

    @Test
    void test() {
        var keys = List.of("key1", "key2", "key3");
        var array = MappedArray.of(Indexer.of(keys), new Object[3]);

        assertEquals(keys, Lists.collect(array.keys()));

        assertNull(array.get(null));
        assertNull(array.get("key1"));
        assertNull(array.get("key2"));
        assertNull(array.get("key3"));
        assertNull(array.get("key4"));

        array.set("key1", "value1");
        array.set("key2", "value2");
        array.set("key3", "value3");

        assertNull(array.get(null));
        assertNull(array.get("key4"));
        assertEquals("value1", array.get("key1"));
        assertEquals("value2", array.get("key2"));
        assertEquals("value3", array.get("key3"));

        assertThrows(NoSuchElementException.class, () -> array.set("key4", "value4"));
        assertThrows(NoSuchElementException.class, () -> array.set(3, "value4"));
    }

}
