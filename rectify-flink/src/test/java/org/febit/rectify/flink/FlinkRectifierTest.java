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

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.types.Row;
import org.febit.lang.util.JacksonUtils;
import org.febit.rectify.RectifierConf;
import org.febit.rectify.format.JsonSourceFormat;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FlinkRectifierTest {

    static final RectifierConf conf = RectifierConf.create()
            .name("Demo")
            .frontFilter("$.status > 0")
            .frontFilter("$.status < 100 || \"status should <100\"")
            .column("long", "id", "$.id")
            .column("boolean", "enable", "", "$$ || \"enable is falsely\"")
            .column("int", "status", "$.status")
            .column("string", "content", "\"prefix:\"+$.content");

    static final List<String> source = Arrays.asList(
            buildInput(6, true, 6, "666"),
            buildInput(5, true, 50, "5555"),
            // not pass: status <= 0
            buildInput(1, true, 0, "First"),
            buildInput(2, true, 20, "Second"),
            // not pass: enable is false
            buildInput(3, false, 20, "third"),
            buildInput(4, true, 40, "fourth")
    );

    private static String buildInput(
            Object id,
            Object enable,
            Object status,
            Object content) {
        Map<String, Object> bean = new HashMap<>();
        bean.put("id", id);
        bean.put("enable", enable);
        bean.put("status", status);
        bean.put("content", content);
        return JacksonUtils.toJsonString(bean);
    }

    @Test
    public void processDataSet() throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        DataSet<String> rawSet = env.fromCollection(source);
        DataSet<Row> rowSet = FlinkRectifier.operator(rawSet, new JsonSourceFormat(), conf);

        List<Row> rows = new ArrayList<>(rowSet.collect());
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
