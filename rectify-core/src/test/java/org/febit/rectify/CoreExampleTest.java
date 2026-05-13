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

import org.febit.rectify.format.JsonSourceFormat;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CoreExampleTest {

    @Test
    void quickStart() {
        var settings = RectifierSettings.builder()
                .name("QuickDemo")
                .filter("$.status > 0")
                .property("long", "id", "$.id")
                .property("int", "status", "$.status")
                .property("string", "content", "\"prefix:\" + $.content")
                .build();

        var rectifier = settings.create()
                .with(new JsonSourceFormat());

        rectifier.process("""
                        {"id":1,"status":10,"content":"hello"}""",
                System.out::println,
                reason -> fail("Processing failed: " + reason)
        );

        var result = process(
                rectifier,
                """
                        {"id":1,"status":10,"content":"hello"}
                        """
        );

        assertNotNull(result);
        assertEquals(1L, result.get("id"));
        assertEquals(10, result.get("status"));
        assertEquals("prefix:hello", result.get("content"));
    }

    @Test
    void advanced() {
        var settings = RectifierSettings.builder()
                .name("Demo")
                .preinstall("""
                        var isTruly = obj -> {
                           return obj == true
                                      || obj == "on" || obj == "true"
                                      || obj == 1;
                        };
                        """)
                .filter("$.status > 0")
                .filter("$.status < 100 || \"status should <100\"")
                .preinstall("var isEven = $.status % 2 == 0 ")
                .preinstall("var statusCopy = $.status")
                .filter("isEven || \"status is not even\"")
                .property()
                .name("id")
                .type("long")
                .expression("$.id")
                .commit()
                .property()
                .name("enable")
                .comment("The enable property, should be true or truthy")
                .type("boolean")
                .validation("$$ || \"enable is falsely\"")
                .commit()
                .property()
                .type("string")
                .name("content")
                .expression("\"prefix:\" + $.content")
                .commit()
                .property("int", "status", null)
                .property("boolean", "isEven", "isEven")
                .property("boolean", "call_isTruly", "isTruly($.isTrulyArg)")
                .build();

        var rectifier = settings.create()
                .with(new JsonSourceFormat());

        var result = process(
                rectifier,
                """
                        {"id":"123","enable":true,"status":12,"isTrulyArg":"on","content":"hello"}
                        """
        );

        assertNotNull(result);
        assertEquals(123L, result.get("id"));
        assertEquals(true, result.get("enable"));
        assertEquals("prefix:hello", result.get("content"));
        assertEquals(12, result.get("status"));
        assertEquals(true, result.get("isEven"));
        assertEquals(true, result.get("call_isTruly"));
    }

    private static Map<String, Object> process(
            Rectifier<String, Map<String, Object>> rectifier,
            String input
    ) {
        var result = new AtomicReference<Map<String, Object>>();
        var reason = new AtomicReference<String>();

        rectifier.process(input, result::set, reason::set);

        assertNull(reason.get());
        return result.get();
    }
}

