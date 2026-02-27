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
package org.febit.rectify.lib.extra;

import org.febit.rectify.util.AdaptFunction;
import org.febit.rectify.util.FuncUtils;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"unchecked", "DataFlowIssue"})
class StringFunctionsTest {

    final Map<Object, Object> proto;

    {
        var map = new HashMap<String, Object>();
        FuncUtils.scanConstFields(StringFunctions.class, map::put);

        assertInstanceOf(Map.class, map.get("Str"));
        assertSame(map.get("Str"), map.get("Strings"));

        proto = (Map<Object, Object>) map.get("Str");
    }

    @Test
    void basic() {
        assertTrue((boolean) ((AdaptFunction) proto.get("isEmpty")).apply(""));
        assertFalse((boolean) ((AdaptFunction) proto.get("isNotEmpty")).apply(""));
        assertTrue((boolean) ((AdaptFunction) proto.get("isBlank")).apply(""));
        assertFalse((boolean) ((AdaptFunction) proto.get("isNotBlank")).apply(""));
        assertTrue((boolean) ((AdaptFunction) proto.get("isNumeric")).apply("123"));
        assertTrue((boolean) ((AdaptFunction) proto.get("isAlpha")).apply("abc"));
        assertTrue((boolean) ((AdaptFunction) proto.get("isAlphaSpace")).apply("abc "));
        assertTrue((boolean) ((AdaptFunction) proto.get("isAlphaNumeric")).apply("abc1"));
        assertTrue((boolean) ((AdaptFunction) proto.get("isAlphaNumericSpace")).apply("abc1 "));

        assertEquals("abc", ((AdaptFunction) proto.get("trim")).apply(" abc "));
        assertEquals("abc", ((AdaptFunction) proto.get("lower")).apply("ABC"));
        assertEquals("ABC", ((AdaptFunction) proto.get("upper")).apply("abc"));
        assertEquals("abc", ((AdaptFunction) proto.get("toLowerCase")).apply("ABC"));
        assertEquals("ABC", ((AdaptFunction) proto.get("toUpperCase")).apply("abc"));

        assertEquals("bc", ((AdaptFunction) proto.get("sub")).apply("abc", 1, 3));
        assertEquals("bc", ((AdaptFunction) proto.get("sub")).apply("abc", 1, null));
        assertEquals("abc", ((AdaptFunction) proto.get("sub")).apply("abc", null, null));
        assertEquals("abc", ((AdaptFunction) proto.get("sub")).apply("abc", null, 3));

        assertEquals("bc", ((AdaptFunction) proto.get("removeStart")).apply("abc", "a"));
        assertEquals("abc", ((AdaptFunction) proto.get("removeStart")).apply("abc", "b"));
        assertEquals("abc", ((AdaptFunction) proto.get("removeStart")).apply("abc", "c"));
        assertEquals("abc", ((AdaptFunction) proto.get("removeStart")).apply("abc", "d"));

        assertEquals("ab", ((AdaptFunction) proto.get("removeEnd")).apply("abc", "c"));
        assertEquals("abc", ((AdaptFunction) proto.get("removeEnd")).apply("abc", "b"));
        assertEquals("abc", ((AdaptFunction) proto.get("removeEnd")).apply("abc", "a"));
        assertEquals("abc", ((AdaptFunction) proto.get("removeEnd")).apply("abc", "d"));

        assertEquals("a", ((AdaptFunction) proto.get("before")).apply("abc", "b"));
        assertEquals("ab", ((AdaptFunction) proto.get("before")).apply("abc", "c"));

        assertEquals("bc", ((AdaptFunction) proto.get("after")).apply("abc", "a"));
        assertEquals("c", ((AdaptFunction) proto.get("after")).apply("abc", "b"));

        assertArrayEquals(
                new String[]{"a", "b", "c"},
                (String[]) ((AdaptFunction) proto.get("split")).apply("a,b,c", ",")
        );
        assertEquals("adc", ((AdaptFunction) proto.get("replace")).apply("abc", "b", "d"));
    }

}
