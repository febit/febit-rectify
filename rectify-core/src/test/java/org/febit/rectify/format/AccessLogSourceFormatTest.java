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

import org.febit.rectify.support.MappedArray;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.febit.rectify.format.AccessLogSourceFormat.create;
import static org.febit.rectify.format.AccessLogSourceFormat.parse;
import static org.junit.jupiter.api.Assertions.*;

class AccessLogSourceFormatTest {

    @Test
    void parseMixedTokens() {
        String log = "100.001.002.003 - - [2/Aug/2018:01:02:03 +0800] \"PUT /path/to/action HTTP/1.1\" 200 111 \"-\" \"HttpClient\" 0.011"
                + " \"test.febit.org\" [-some thing very bad \"\" {{}{}}\"\"]";
        String[] values = parse(log);

        assertEquals(12, values.length);
        assertEquals("100.001.002.003", values[0]);
        assertNull(values[1]);
        assertNull(values[2]);
        assertEquals("2/Aug/2018:01:02:03 +0800", values[3]);
        assertEquals("PUT /path/to/action HTTP/1.1", values[4]);
        assertEquals("200", values[5]);
        assertEquals("111", values[6]);
        assertNull(values[7]);
        assertEquals("HttpClient", values[8]);
        assertEquals("0.011", values[9]);
        assertEquals("test.febit.org", values[10]);
        assertEquals("-some thing very bad \"\" {{}{}}\"\"", values[11]);
    }

    @Test
    void parseReturnsEmptyArrayForEmptyOrBlankInput() {
        assertArrayEquals(new String[0], parse(""));
        assertArrayEquals(new String[0], parse("   "));
    }

    @Test
    void parseConvertsDashTokensToNull() {
        var values = parse(" - \"-\" [-] value ");

        assertEquals(4, values.length);
        assertNull(values[0]);
        assertNull(values[1]);
        assertNull(values[2]);
        assertEquals("value", values[3]);
    }

    @Test
    void createRejectsEmptyColumns() {
        var options = AccessLogSourceFormat.Options.builder().build();
        var ex = assertThrows(IllegalArgumentException.class,
                () -> create(options));

        assertEquals("Columns is required to create a AccessLogSourceFormat", ex.getMessage());
    }

    @Test
    void processIgnoresEmptyInputs() {
        var format = create(AccessLogSourceFormat.Options.builder()
                .column("ip")
                .build());
        var calls = new AtomicInteger();

        format.process(null, ignored -> calls.incrementAndGet());
        format.process("", ignored -> calls.incrementAndGet());
        format.process("   ", ignored -> calls.incrementAndGet());

        assertEquals(0, calls.get());
    }

    @Test
    void processMapsValuesByConfiguredColumns() {
        var format = create(AccessLogSourceFormat.Options.builder()
                .column("remoteAddr")
                .column("identity")
                .column("userid")
                .column("time")
                .column("request")
                .build());
        var sinkValue = new AtomicReference<>();

        format.process("100.001.002.003 - - [2/Aug/2018:01:02:03 +0800] \"PUT /path/to/action HTTP/1.1\"",
                sinkValue::set);

        assertNotNull(sinkValue.get());
        assertInstanceOf(MappedArray.class, sinkValue.get());
        var mapped = (MappedArray) sinkValue.get();
        assertEquals(5, mapped.size());
        assertEquals("100.001.002.003", mapped.get("remoteAddr"));
        assertEquals("100.001.002.003", mapped.get(0));
        assertNull(mapped.get("identity"));
        assertNull(mapped.get("userid"));
        assertEquals("2/Aug/2018:01:02:03 +0800", mapped.get("time"));
        assertEquals("PUT /path/to/action HTTP/1.1", mapped.get("request"));
        assertNull(mapped.get("missing"));
    }

}
