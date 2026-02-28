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

import org.febit.lang.Tuple2;
import org.febit.lang.modeler.Schema;
import org.febit.lang.util.JacksonUtils;
import org.febit.rectify.format.JsonSourceFormat;
import org.febit.rectify.wit.FilterBreakpoint;
import org.febit.rectify.wit.ScriptBuilder;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({
        "squid:S1192" // String literals should not be duplicated
})
class RectifierTest {

    final RectifierSettings settings = RectifierSettings.builder()
            // Named your schema
            .name("Demo")
            // Global code
            .setup("""
                    var isTruly = obj -> {
                       return obj == true
                                  || obj == "on" || obj == "true"
                                  || obj == 1;
                    };
                    """)
            // Global filters:
            //    Notice: only a Boolean.FALSE or a non-null String (reason) can ban current row, others pass.
            .filter("$.status > 0")
            //    Recommend: give a reason if falsely, `||` is logic OR (just what it means to in JS, feel free!).
            .filter("$.status < 100 || \"status should <100\"")
            // Global code and filters, Will be executed in defined order.
            .setup("var isEven = $.status % 2 == 0 ")
            .setup("var statusCopy = $.status")
            .filter("isEven || \"status is not even\"")
            // Columns

            .column()
            .name("id")
            .type("long")
            .expression("$.id")
            .commit()

            .column()
            .name("enable")
            .comment("The enable column, should be true or truthy")
            .type("boolean")
            // If expression is not specified, the default value is `$.{columnName}`
            //   So here the default expression is `.expression("$.enable")`
            .validation("$$ || \"enable is falsely\"")
            .commit()

            .column()
            .type("string")
            .name("content")
            .expression("\"prefix:\" + $.content")
            .commit()

            .column("int", "status", null)
            .column("boolean", "isEven", "isEven")
            .column("boolean", "call_isTruly", "isTruly($.isTrulyArg)")
            .build();

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
    void hints() {
        var rectifier = settings.create();
        List<String> hints = rectifier.hints();

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
    void testBaseInfo() {
        var rectifier = settings.create()
                .with(new JsonSourceFormat());

        Schema schema = rectifier.schema();
        assertTrue(schema.isStructType());
        assertEquals(6, schema.fieldsSize());
        assertEquals(0, schema.field("id").pos());
        assertEquals(1, schema.field("enable").pos());
        assertEquals(3, schema.field("status").pos());
    }

    @Test
    void process() {
        var rectifier = settings.create()
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
    void testFilter() {

        var rectifier = settings.create()
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
    void processInDebugMode() {
        var breakpoints = new ArrayList<Tuple2<FilterBreakpoint, Object>>();
        var rectifier = settings.toBuilder()
                .breakpointHandler((label, context, statement, val) -> {
                    if (label instanceof FilterBreakpoint breakpoint) {
                        breakpoints.add(Tuple2.ofNullable(breakpoint, val));
                        if ("enable".equals(breakpoint.field())) {
                            assertEquals(1, context.variables().get(ScriptBuilder.VAR_CURR_FIELD_INDEX));
                        }
                    }
                })
                .build()
                .create()
                .with(new JsonSourceFormat());

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
        assertEquals(0, breakpoint.v1().index());
        assertNull(breakpoint.v1().field());
        assertEquals("$.status > 0", breakpoint.v1().expr());
        assertEquals(true, breakpoint.v2());
        breakpoint = breakpoints.get(1);
        assertEquals(1, breakpoint.v1().index());
        assertNull(breakpoint.v1().field());
        assertEquals("$.status < 100 || \"status should <100\"", breakpoint.v1().expr());
        assertEquals(true, breakpoint.v2());
        breakpoint = breakpoints.get(2);
        assertEquals(2, breakpoint.v1().index());
        assertNull(breakpoint.v1().field());
        assertEquals("isEven || \"status is not even\"", breakpoint.v1().expr());
        assertEquals(true, breakpoint.v2());
        breakpoint = breakpoints.get(3);
        assertEquals(1, breakpoint.v1().index());
        assertEquals("enable", breakpoint.v1().field());
        assertEquals("$$ || \"enable is falsely\"", breakpoint.v1().expr());
        assertEquals(true, breakpoint.v2());

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
        assertEquals("status is not even", breakpoint.v2());
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
