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
package org.febit.rectify.flink;

import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.febit.lang.util.Lists;
import org.febit.rectify.RectifierSettings;
import org.febit.rectify.flink.streaming.RectifierStreamingSupport;
import org.febit.rectify.format.JsonSourceFormat;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StreamingExampleTest {

    @Test
    void quickStart() throws Exception {
        var settings = RectifierSettings.builder()
                .name("Demo")
                .filter("$.status > 0")
                .field("long", "id", "$.id")
                .field("boolean", "enable", "", "$$ || \"enable is falsely\"")
                .field("int", "status", "$.status")
                .field("string", "content", "\"prefix:\" + $.content")
                .build();

        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        var source = env.fromData(
                List.of("""
                        {"id":1,"enable":true,"status":10,"content":"ok"}
                        {"id":2,"enable":false,"status":20,"content":"skip"}
                        """.split("\n")),
                BasicTypeInfo.STRING_TYPE_INFO
        );

        var rows = RectifierStreamingSupport.flatMap(source, settings, new JsonSourceFormat());

        rows.print();
        env.execute("rectify-streaming-demo");

        try (var iter = rows.executeAndCollect()) {
            var result = Lists.collect(iter);
            assertEquals(1, result.size());
            var row = result.getFirst();
            assertEquals(1L, row.getField(0));
            assertEquals(true, row.getField(1));
            assertEquals(10, row.getField(2));
            assertEquals("prefix:ok", row.getField(3));
        }

    }
}

