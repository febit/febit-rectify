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
package org.febit.rectify;

import jakarta.annotation.Nullable;
import org.febit.lang.Tuple2;
import org.febit.lang.modeler.Schema;
import org.febit.lang.util.JacksonUtils;
import org.febit.rectify.engine.FilterBreakpoint;
import org.febit.rectify.engine.ScriptBuilder;
import org.febit.rectify.format.JsonSourceFormat;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({
        "squid:S1192" // String literals should not be duplicated
})
public class RectifierTest {

    final RectifierConf conf = RectifierConf.create()
            // Named your schema
            .name("Demo")
            // Global code
            .frontSegment("\n"
                    + "var isTruly = obj -> {\n"
                    + "   return obj == true \n"
                    + "              || obj == \"on\" || obj == \"true\"\n"
                    + "              || obj == 1;\n"
                    + "};")
            // Global filters:
            //    Notice: only a Boolean.FALSE or a non-null String (reason) can ban current row, others pass.
            .frontFilter("$.status > 0")
            //    Recommend: give a reason if falsely, `||` is logic OR (just what it means to in JS, feel free!).
            .frontFilter("$.status < 100 || \"status should <100\"")
            // Global code and filters, Will be executed in defined order.
            .frontSegment("var isEven = $.status % 2 == 0 ")
            .frontSegment("var statusCopy = $.status")
            .frontFilter("isEven || \"status is not even\"")
            // Columns
            .column("long", "id", "$.id")
            // column with check expression
            .column("boolean", "enable", "", "$$ || \"enable is falsely\"")
            .column("int", "status", "$.status")
            .column("boolean", "isEven", "isEven")
            .column("boolean", "call_isTruly", "isTruly($.isTrulyArg)")
            .column("string", "content", "\"prefix:\"+$.content");

    private static String buildInput(
            Object id,
            Object enable,
            Object status,
            Object isTrulyArg,
            @Nullable Object content) {
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
        var rectifier = conf.build();
        List<String> hints = rectifier.getHints();

        assertFalse(hints.isEmpty());
        assertFalse(hints.contains("SomeoneAbsent"));
        assertFalse(hints.contains("checkAccept"));

        assertTrue(hints.contains(ScriptBuilder.VAR_EXIT));
        assertTrue(hints.contains(ScriptBuilder.VAR_CHECK_FILTER));
        assertTrue(hints.contains(ScriptBuilder.VAR_NEW_FILTER_BREAKPOINT));

        assertTrue(hints.contains(ScriptBuilder.VAR_INPUT));
        assertTrue(hints.contains(ScriptBuilder.VAR_CURR_FIELD));

        assertFalse(hints.contains(ScriptBuilder.VAR_RESULT));
        assertFalse(hints.contains(ScriptBuilder.VAR_SCHEMA_NAME));
        assertFalse(hints.contains(ScriptBuilder.VAR_CURR_FIELD_INDEX));
    }

    @Test
    public void testBaseInfo() {
        var rectifier = conf.build()
                .with(new JsonSourceFormat());

        Schema schema = rectifier.schema();
        assertTrue(schema.isStructType());
        assertEquals(6, schema.fieldSize());
        assertEquals(0, schema.field("id").pos());
        assertEquals(1, schema.field("enable").pos());
        assertEquals(2, schema.field("status").pos());
    }

    @Test
    public void process() {
        var rectifier = conf.build()
                .with(new JsonSourceFormat());

        SingleElementRectifierSink<Map<String, Object>> consumer;

        consumer = new SingleElementRectifierSink<>();
        rectifier.process(buildInput(
                "123",
                true,
                12,
                true,
                "tell something"
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.reason);
        assertNotNull(consumer.rawOutput);
        assertEquals(123L, consumer.out.get("id"));
        assertEquals(true, consumer.out.get("enable"));
        assertEquals(12, consumer.out.get("status"));
        assertEquals(true, consumer.out.get("isEven"));
        assertEquals(true, consumer.out.get("call_isTruly"));
        assertEquals("prefix:tell something", consumer.out.get("content"));

        consumer = new SingleElementRectifierSink<>();
        rectifier.process(buildInput(
                456,
                true,
                2,
                0,
                null
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.reason);
        assertNotNull(consumer.rawOutput);
        assertEquals(456L, consumer.out.get("id"));
        assertEquals(true, consumer.out.get("enable"));
        assertEquals(2, consumer.out.get("status"));
        assertEquals(true, consumer.out.get("isEven"));
        assertEquals(false, consumer.out.get("call_isTruly"));
        assertEquals("prefix:", consumer.out.get("content"));
    }

    @Test
    public void testFilter() {

        var rectifier = conf.build()
                .with(new JsonSourceFormat());
        SingleElementRectifierSink<Map<String, Object>> consumer;
        consumer = new SingleElementRectifierSink<>();
        rectifier.process(buildInput(
                123,
                true,
                0,
                0,
                null
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.out);
        assertNotNull(consumer.rawOutput);
        assertNull(consumer.reason);

        consumer = new SingleElementRectifierSink<>();
        rectifier.process(buildInput(
                123,
                true,
                101,
                0,
                null
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.out);
        assertNotNull(consumer.rawOutput);
        assertEquals("status should <100", consumer.reason);

        consumer = new SingleElementRectifierSink<>();
        rectifier.process(buildInput(
                123,
                false,
                99,
                0,
                null
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.out);
        assertNotNull(consumer.rawOutput);
        assertEquals("status is not even", consumer.reason);

        consumer = new SingleElementRectifierSink<>();
        rectifier.process(buildInput(
                123,
                false,
                88,
                0,
                null
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.out);
        assertNotNull(consumer.rawOutput);
        assertEquals("enable is falsely", consumer.reason);
    }

    @Test
    public void processInDebugMode() {

        var breakpoints = new ArrayList<Tuple2<FilterBreakpoint, Object>>();
        conf.breakpointListener((label, context, statement, val) -> {
            if (label instanceof FilterBreakpoint) {
                var breakpoint = (FilterBreakpoint) label;
                breakpoints.add(Tuple2.of(breakpoint, val));
                if ("enable".equals(breakpoint.getField())) {
                    assertEquals(1, context.get(ScriptBuilder.VAR_CURR_FIELD_INDEX));
                }
            }
        });

        var rectifier = conf.build().with(new JsonSourceFormat());
        conf.setBreakpointListener(null);

        SingleElementRectifierSink<Map<String, Object>> consumer;
        Tuple2<FilterBreakpoint, Object> breakpoint;

        breakpoints.clear();
        consumer = new SingleElementRectifierSink<>();
        rectifier.process(buildInput(
                "123",
                true,
                12,
                true,
                "tell something"
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.reason);
        assertNotNull(consumer.rawOutput);

        assertEquals(123L, consumer.out.get("id"));
        assertEquals(true, consumer.out.get("enable"));
        assertEquals(12, consumer.out.get("status"));
        assertEquals(true, consumer.out.get("isEven"));
        assertEquals(true, consumer.out.get("call_isTruly"));
        assertEquals("prefix:tell something", consumer.out.get("content"));

        assertEquals(4, breakpoints.size());
        breakpoint = breakpoints.get(0);
        assertEquals(0, breakpoint.v1.getIndex());
        assertNull(breakpoint.v1.getField());
        assertEquals("$.status > 0", breakpoint.v1.getExpr());
        assertEquals(true, breakpoint.v2);
        breakpoint = breakpoints.get(1);
        assertEquals(1, breakpoint.v1.getIndex());
        assertNull(breakpoint.v1.getField());
        assertEquals("$.status < 100 || \"status should <100\"", breakpoint.v1.getExpr());
        assertEquals(true, breakpoint.v2);
        breakpoint = breakpoints.get(2);
        assertEquals(2, breakpoint.v1.getIndex());
        assertNull(breakpoint.v1.getField());
        assertEquals("isEven || \"status is not even\"", breakpoint.v1.getExpr());
        assertEquals(true, breakpoint.v2);
        breakpoint = breakpoints.get(3);
        assertEquals(1, breakpoint.v1.getIndex());
        assertEquals("enable", breakpoint.v1.getField());
        assertEquals("$$ || \"enable is falsely\"", breakpoint.v1.getExpr());
        assertEquals(true, breakpoint.v2);

        breakpoints.clear();
        consumer = new SingleElementRectifierSink<>();
        rectifier.process(buildInput(
                123,
                false,
                99,
                0,
                null
        ), consumer);
        assertTrue(consumer.flag);
        assertNull(consumer.out);
        assertNotNull(consumer.rawOutput);
        assertEquals("status is not even", consumer.reason);

        assertEquals(3, breakpoints.size());
        breakpoint = breakpoints.get(2);
        assertEquals("status is not even", breakpoint.v2);
    }

    private static class SingleElementRectifierSink<O> implements RectifierSink<O> {

        boolean flag = false;
        O out;
        RawOutput rawOutput;
        String reason;

        @Override
        public void onCompleted(@Nullable O out, RawOutput raw, @Nullable String reason) {
            if (flag) {
                throw new AssertionError("Assert single element, but onCompleted called more than once.");
            }
            this.flag = true;
            this.out = out;
            this.rawOutput = raw;
            this.reason = reason;
        }
    }
}
