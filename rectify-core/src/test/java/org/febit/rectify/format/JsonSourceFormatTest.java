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
package org.febit.rectify.format;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class JsonSourceFormatTest {

    private final JsonSourceFormat format = new JsonSourceFormat();

    @Test
    void processIgnoresNullEmptyAndBlankInput() {
        var calls = new AtomicInteger();

        format.process(null, ignored -> calls.incrementAndGet());
        format.process("", ignored -> calls.incrementAndGet());
        format.process("   ", ignored -> calls.incrementAndGet());

        assertEquals(0, calls.get());
    }

    @Test
    void processIgnoresInvalidOrUnsupportedJson() {
        var calls = new AtomicInteger();

        format.process("{", ignored -> calls.incrementAndGet());
        format.process("[]", ignored -> calls.incrementAndGet());
        format.process("123", ignored -> calls.incrementAndGet());

        assertEquals(0, calls.get());
    }

    @Test
    void processIgnoresEmptyObject() {
        var calls = new AtomicInteger();

        format.process("{}", ignored -> calls.incrementAndGet());

        assertEquals(0, calls.get());
    }

    @Test
    void processEmitsFlatObject() {
        var sinkValue = new AtomicReference<>();

        format.process("{\"name\":\"Alice\",\"age\":18,\"active\":true}", sinkValue::set);

        assertNotNull(sinkValue.get());
        assertInstanceOf(Map.class, sinkValue.get());
        var values = (Map<?, ?>) sinkValue.get();
        assertEquals(3, values.size());
        assertEquals("Alice", values.get("name"));
        assertEquals(18, values.get("age"));
        assertEquals(Boolean.TRUE, values.get("active"));
    }

    @Test
    void processPreservesNestedObjectsAndArrays() {
        var sinkValue = new AtomicReference<>();

        format.process("{\"meta\":{\"source\":\"api\"},\"tags\":[\"a\",\"b\"],\"count\":2}", sinkValue::set);

        assertNotNull(sinkValue.get());
        assertInstanceOf(Map.class, sinkValue.get());
        var values = (Map<?, ?>) sinkValue.get();
        assertEquals(2, values.get("count"));
        assertInstanceOf(Map.class, values.get("meta"));
        assertInstanceOf(List.class, values.get("tags"));
        assertEquals("api", ((Map<?, ?>) values.get("meta")).get("source"));
        assertEquals(List.of("a", "b"), values.get("tags"));
    }

}
