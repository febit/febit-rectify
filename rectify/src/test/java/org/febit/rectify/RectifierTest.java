/**
 * Copyright 2018-present febit.org (support@febit.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.rectify;

import org.febit.lang.TerConsumer;
import org.febit.lang.Tuple2;
import org.febit.rectify.engine.FilterBreakpoint;
import org.febit.rectify.engine.ScriptBuilder;
import org.febit.rectify.util.JacksonUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RectifierTest {

    final RectifierConf conf = RectifierConf.builder()
            // Named your schema
            .name("Demo")
            // Source format
            .sourceFormat("json")
            // Global code
            .globalCode(""
                    + "var isTruly = obj -> {\n"
                    + "   return obj == true \n"
                    + "              || obj == \"on\" || obj == \"true\"\n"
                    + "              || obj == 1;\n"
                    + "};")
            // Global filters:
            //    Notice: only a Boolean.FALSE or a non-null String (reason) can ban current row, others pass.
            .globalFilter("$.status > 0")
            //    Recommend: give a reason if falsely, `||` is logic OR (just what it means to in JS, feel free!).
            .globalFilter("$.status < 100 || \"status should <100\"")
            // Global code and filters, Will be executed in defined order.
            .globalCode("var isEven = $.status % 2 == 0 ")
            .globalCode("var statusCopy = $.status")
            .globalFilter("isEven || \"status is not even\"")
            // Columns
            .column("long", "id", "$.id")
            // column with check expression
            .column("boolean", "enable", "", "$$ || \"enable is falsely\"")
            .column("int", "status", "$.status")
            .column("boolean", "isEven", "isEven")
            .column("boolean", "call_isTruly", "isTruly($.isTrulyArg)")
            .column("string", "content", "\"prefix:\"+$.content")
            .build();

    private static String buildInput(
            Object id,
            Object enable,
            Object status,
            Object isTrulyArg,
            Object content) {
        Map<String, Object> bean = new HashMap<>();
        bean.put("id", id);
        bean.put("enable", enable);
        bean.put("status", status);
        bean.put("isTrulyArg", isTrulyArg);
        bean.put("content", content);
        return JacksonUtils.toJsonString(bean);
    }

    @Test
    public void getHints() {
        List<String> hints = Rectifier.getHints();

        assertFalse(hints.isEmpty());
        assertFalse(hints.contains("SomeoneAbsent"));
        assertFalse(hints.contains("checkAccept"));

        assertTrue(hints.contains(ScriptBuilder.VAR_EXIT));
        assertTrue(hints.contains(ScriptBuilder.VAR_CHECK_FILTER));
        assertTrue(hints.contains(ScriptBuilder.VAR_NEW_FILTER_BREAKPOINT));

        assertTrue(hints.contains(ScriptBuilder.VAR_INPUT));
        assertTrue(hints.contains(ScriptBuilder.VAR_CURR));

        assertFalse(hints.contains(ScriptBuilder.VAR_RESULT));
        assertFalse(hints.contains(ScriptBuilder.VAR_SCHEMA_NAME));
        assertFalse(hints.contains(ScriptBuilder.VAR_CURR_COLUMN));
    }

    @Test
    public void testBaseInfo() {
        Rectifier<String, GenericStruct> rectifier = Rectifier.create(conf);
        assertFalse(rectifier.isDebug());

        Schema schema = rectifier.schema();
        assertTrue(schema.isStructType());
        assertEquals(6, schema.fieldSize());
        assertEquals(0, schema.getField("id").pos());
        assertEquals(1, schema.getField("enable").pos());
        assertEquals(2, schema.getField("status").pos());
    }

    @Test
    public void process() {
        Rectifier<String, GenericStruct> rectifier = Rectifier.create(conf);
        AssertSingleTerConsumer<GenericStruct> consumer;

        consumer = new AssertSingleTerConsumer<>();
        rectifier.process(buildInput(
                "123",
                true,
                12,
                true,
                "tell something"
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.reason);
        assertNotNull(consumer.resultRaw);
        assertEquals(123L, consumer.out.get(0));
        assertEquals(true, consumer.out.get(1));
        assertEquals(12, consumer.out.get(2));
        assertEquals(true, consumer.out.get(3));
        assertEquals(true, consumer.out.get(4));
        assertEquals("prefix:tell something", consumer.out.get(5));

        consumer = new AssertSingleTerConsumer<>();
        rectifier.process(buildInput(
                456,
                true,
                2,
                0,
                null
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.reason);
        assertNotNull(consumer.resultRaw);
        assertEquals(456L, consumer.out.get(0));
        assertEquals(true, consumer.out.get(1));
        assertEquals(2, consumer.out.get(2));
        assertEquals(true, consumer.out.get(3));
        assertEquals(false, consumer.out.get(4));
        assertEquals("prefix:", consumer.out.get(5));
    }

    @Test
    public void testFilter() {

        Rectifier<String, GenericStruct> rectifier = Rectifier.create(conf);
        AssertSingleTerConsumer<GenericStruct> consumer;
        consumer = new AssertSingleTerConsumer<>();
        rectifier.process(buildInput(
                123,
                true,
                0,
                0,
                null
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.out);
        assertNull(consumer.resultRaw);
        assertNull(consumer.reason);

        consumer = new AssertSingleTerConsumer<>();
        rectifier.process(buildInput(
                123,
                true,
                101,
                0,
                null
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.out);
        assertNull(consumer.resultRaw);
        assertEquals("status should <100", consumer.reason);

        consumer = new AssertSingleTerConsumer<>();
        rectifier.process(buildInput(
                123,
                false,
                99,
                0,
                null
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.out);
        assertNull(consumer.resultRaw);
        assertEquals("status is not even", consumer.reason);

        consumer = new AssertSingleTerConsumer<>();
        rectifier.process(buildInput(
                123,
                false,
                88,
                0,
                null
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.out);
        assertNull(consumer.resultRaw);
        assertEquals("enable is falsely", consumer.reason);
    }

    @Test
    public void processInDebugMode() {
        List<Tuple2<FilterBreakpoint, Object>> breakpoints = new ArrayList<>();
        Rectifier<String, GenericStruct> rectifier = Rectifier.create(conf, (label, context, statement, val) -> {
            if (label instanceof FilterBreakpoint) {
                FilterBreakpoint breakpoint = (FilterBreakpoint) label;
                breakpoints.add(Tuple2.of(breakpoint, val));
                if ("enable".equals(breakpoint.getField())) {
                    assertEquals(1, context.get(ScriptBuilder.VAR_CURR_COLUMN));
                }
            }
        });
        AssertSingleTerConsumer<GenericStruct> consumer;
        Tuple2<FilterBreakpoint, Object> breakpoint;

        breakpoints.clear();
        consumer = new AssertSingleTerConsumer<>();
        rectifier.process(buildInput(
                "123",
                true,
                12,
                true,
                "tell something"
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.reason);
        assertNotNull(consumer.resultRaw);
        assertEquals(123L, consumer.out.get(0));
        assertEquals(true, consumer.out.get(1));
        assertEquals(12, consumer.out.get(2));
        assertEquals(true, consumer.out.get(3));
        assertEquals(true, consumer.out.get(4));
        assertEquals("prefix:tell something", consumer.out.get(5));

        assertEquals(4, breakpoints.size());
        breakpoint = breakpoints.get(0);
        assertEquals(0, breakpoint._1.getIndex());
        assertNull(breakpoint._1.getField());
        assertEquals("$.status > 0", breakpoint._1.getExpr());
        assertEquals(true, breakpoint._2);
        breakpoint = breakpoints.get(1);
        assertEquals(1, breakpoint._1.getIndex());
        assertNull(breakpoint._1.getField());
        assertEquals("$.status < 100 || \"status should <100\"", breakpoint._1.getExpr());
        assertEquals(true, breakpoint._2);
        breakpoint = breakpoints.get(2);
        assertEquals(2, breakpoint._1.getIndex());
        assertNull(breakpoint._1.getField());
        assertEquals("isEven || \"status is not even\"", breakpoint._1.getExpr());
        assertEquals(true, breakpoint._2);
        breakpoint = breakpoints.get(3);
        assertEquals(1, breakpoint._1.getIndex());
        assertEquals("enable", breakpoint._1.getField());
        assertEquals("$$ || \"enable is falsely\"", breakpoint._1.getExpr());
        assertEquals(true, breakpoint._2);

        breakpoints.clear();
        consumer = new AssertSingleTerConsumer<>();
        rectifier.process(buildInput(
                123,
                false,
                99,
                0,
                null
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.out);
        assertNull(consumer.resultRaw);
        assertEquals("status is not even", consumer.reason);

        assertEquals(3, breakpoints.size());
        breakpoint = breakpoints.get(2);
        assertEquals("status is not even", breakpoint._2);
    }

    private static class AssertSingleTerConsumer<OUT> implements TerConsumer<OUT, ResultRaw, String> {

        boolean flag = false;
        OUT out;
        ResultRaw resultRaw;
        String reason;

        @Override
        public void accept(OUT out, ResultRaw resultRaw, String reason) {
            if (flag) {
                throw new RuntimeException("Assert single item, but got more");
            }
            flag = true;
            this.out = out;
            this.resultRaw = resultRaw;
            this.reason = reason;
        }
    }
}
