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

import org.febit.rectify.util.FuncFunctionDeclare;
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
        assertTrue((boolean) ((FuncFunctionDeclare) proto.get("isEmpty")).apply(""));
        assertFalse((boolean) ((FuncFunctionDeclare) proto.get("isNotEmpty")).apply(""));
        assertTrue((boolean) ((FuncFunctionDeclare) proto.get("isBlank")).apply(""));
        assertFalse((boolean) ((FuncFunctionDeclare) proto.get("isNotBlank")).apply(""));
        assertTrue((boolean) ((FuncFunctionDeclare) proto.get("isNumeric")).apply("123"));
        assertTrue((boolean) ((FuncFunctionDeclare) proto.get("isAlpha")).apply("abc"));
        assertTrue((boolean) ((FuncFunctionDeclare) proto.get("isAlphaSpace")).apply("abc "));
        assertTrue((boolean) ((FuncFunctionDeclare) proto.get("isAlphaNumeric")).apply("abc1"));
        assertTrue((boolean) ((FuncFunctionDeclare) proto.get("isAlphaNumericSpace")).apply("abc1 "));

        assertEquals("abc", ((FuncFunctionDeclare) proto.get("trim")).apply(" abc "));
        assertEquals("abc", ((FuncFunctionDeclare) proto.get("lower")).apply("ABC"));
        assertEquals("ABC", ((FuncFunctionDeclare) proto.get("upper")).apply("abc"));
        assertEquals("abc", ((FuncFunctionDeclare) proto.get("toLowerCase")).apply("ABC"));
        assertEquals("ABC", ((FuncFunctionDeclare) proto.get("toUpperCase")).apply("abc"));

        assertEquals("bc", ((FuncFunctionDeclare) proto.get("sub")).apply("abc", 1, 3));
        assertEquals("bc", ((FuncFunctionDeclare) proto.get("sub")).apply("abc", 1, null));
        assertEquals("abc", ((FuncFunctionDeclare) proto.get("sub")).apply("abc", null, null));
        assertEquals("abc", ((FuncFunctionDeclare) proto.get("sub")).apply("abc", null, 3));

        assertEquals("bc", ((FuncFunctionDeclare) proto.get("removeStart")).apply("abc", "a"));
        assertEquals("abc", ((FuncFunctionDeclare) proto.get("removeStart")).apply("abc", "b"));
        assertEquals("abc", ((FuncFunctionDeclare) proto.get("removeStart")).apply("abc", "c"));
        assertEquals("abc", ((FuncFunctionDeclare) proto.get("removeStart")).apply("abc", "d"));

        assertEquals("ab", ((FuncFunctionDeclare) proto.get("removeEnd")).apply("abc", "c"));
        assertEquals("abc", ((FuncFunctionDeclare) proto.get("removeEnd")).apply("abc", "b"));
        assertEquals("abc", ((FuncFunctionDeclare) proto.get("removeEnd")).apply("abc", "a"));
        assertEquals("abc", ((FuncFunctionDeclare) proto.get("removeEnd")).apply("abc", "d"));

        assertEquals("a", ((FuncFunctionDeclare) proto.get("before")).apply("abc", "b"));
        assertEquals("ab", ((FuncFunctionDeclare) proto.get("before")).apply("abc", "c"));

        assertEquals("bc", ((FuncFunctionDeclare) proto.get("after")).apply("abc", "a"));
        assertEquals("c", ((FuncFunctionDeclare) proto.get("after")).apply("abc", "b"));

        assertArrayEquals(
                new String[]{"a", "b", "c"},
                (String[]) ((FuncFunctionDeclare) proto.get("split")).apply("a,b,c", ",")
        );
        assertEquals("adc", ((FuncFunctionDeclare) proto.get("replace")).apply("abc", "b", "d"));
    }

}
