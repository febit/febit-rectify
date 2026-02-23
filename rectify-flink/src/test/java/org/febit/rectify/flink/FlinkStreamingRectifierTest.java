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
import org.apache.flink.types.Row;
import org.febit.lang.util.Lists;
import org.febit.rectify.flink.streaming.FlinkStreamingRectifier;
import org.febit.rectify.format.JsonSourceFormat;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlinkStreamingRectifierTest {

    @Test
    void processDataStream() throws Exception {
        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        var rawStream = env.fromData(FlinkRectifierTests.SOURCE, BasicTypeInfo.STRING_TYPE_INFO);
        var rowStream = FlinkStreamingRectifier.operator(
                rawStream, new JsonSourceFormat(), FlinkRectifierTests.CONF);
        try (var iter = rowStream.executeAndCollect()) {
            List<Row> rows = Lists.collect(iter);
            rows.sort(Comparator.comparingLong(r -> (Long) r.getField(0)));

            assertEquals(4, rows.size());
            Row row;
            row = rows.get(0);
            assertEquals(2L, row.getField(0));
            assertEquals(20, row.getField(2));
            assertEquals("prefix:Second", row.getField(3));
            row = rows.get(1);
            assertEquals(4L, row.getField(0));
            assertEquals(40, row.getField(2));
            assertEquals("prefix:fourth", row.getField(3));
            row = rows.get(2);
            assertEquals(5L, row.getField(0));
            assertEquals(50, row.getField(2));
            assertEquals("prefix:5555", row.getField(3));
            row = rows.get(3);
            assertEquals(6L, row.getField(0));
            assertEquals(6, row.getField(2));
            assertEquals("prefix:666", row.getField(3));
        }
    }
}
