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
package org.febit.rectify.sqlline;

import org.febit.rectify.format.AccessLogSourceFormat;
import org.febit.rectify.format.JsonSourceFormat;
import org.febit.rectify.support.MappedArray;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SourceFormatFactoryTest {

    @Test
    void createJson() {
        var source = TableSettings.Source.builder()
                .format(JsonSourceFormat.NAME)
                .build();

        var format = SourceFormatFactory.create(source);
        assertInstanceOf(JsonSourceFormat.class, format);

        var sinkValue = new AtomicReference<Object>();
        format.process("""
                {"id":1,"status":2,"content":"ok"}
                """, sinkValue::set);

        var values = assertInstanceOf(Map.class, sinkValue.get());
        assertEquals(1, values.get("id"));
        assertEquals(2, values.get("status"));
        assertEquals("ok", values.get("content"));
    }

    @Test
    void createAccess() {
        var source = TableSettings.Source.builder()
                .format(AccessLogSourceFormat.NAME)
                .option("columns", List.of("ip", "status", "path"))
                .build();

        var format = SourceFormatFactory.create(source);
        assertInstanceOf(AccessLogSourceFormat.class, format);

        var sinkValue = new AtomicReference<Object>();
        format.process("127.0.0.1 200 \"/demo path\"", sinkValue::set);

        var values = assertInstanceOf(MappedArray.class, sinkValue.get());
        assertEquals(3, values.size());
        assertEquals("127.0.0.1", values.get("ip"));
        assertEquals("200", values.get("status"));
        assertEquals("/demo path", values.get("path"));
    }

    @Test
    void createAccessInvalid() {
        var source = TableSettings.Source.builder()
                .format(AccessLogSourceFormat.NAME)
                .build();

        assertThrows(IllegalArgumentException.class, () -> SourceFormatFactory.create(source));
    }

    @Test
    void createUnsupported() {
        var source = TableSettings.Source.builder()
                .format("csv")
                .build();

        assertThrows(IllegalArgumentException.class, () -> SourceFormatFactory.create(source));
    }
}
