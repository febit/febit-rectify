package org.febit.rectify.lib.extra;

import org.febit.rectify.util.FuncMethodDeclare;
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

        assertTrue(map.get("Str") instanceof Map);
        assertSame(map.get("Str"), map.get("Strings"));

        proto = (Map<Object, Object>) map.get("Str");
    }

    @Test
    void basic() {
        assertTrue((boolean) ((FuncMethodDeclare) proto.get("isEmpty")).apply(""));
        assertFalse((boolean) ((FuncMethodDeclare) proto.get("isNotEmpty")).apply(""));
        assertTrue((boolean) ((FuncMethodDeclare) proto.get("isBlank")).apply(""));
        assertFalse((boolean) ((FuncMethodDeclare) proto.get("isNotBlank")).apply(""));
        assertTrue((boolean) ((FuncMethodDeclare) proto.get("isNumeric")).apply("123"));
        assertTrue((boolean) ((FuncMethodDeclare) proto.get("isAlpha")).apply("abc"));
        assertTrue((boolean) ((FuncMethodDeclare) proto.get("isAlphaSpace")).apply("abc "));
        assertTrue((boolean) ((FuncMethodDeclare) proto.get("isAlphaNumeric")).apply("abc1"));
        assertTrue((boolean) ((FuncMethodDeclare) proto.get("isAlphaNumericSpace")).apply("abc1 "));

        assertEquals("abc", ((FuncMethodDeclare) proto.get("trim")).apply(" abc "));
        assertEquals("abc", ((FuncMethodDeclare) proto.get("lower")).apply("ABC"));
        assertEquals("ABC", ((FuncMethodDeclare) proto.get("upper")).apply("abc"));
        assertEquals("abc", ((FuncMethodDeclare) proto.get("toLowerCase")).apply("ABC"));
        assertEquals("ABC", ((FuncMethodDeclare) proto.get("toUpperCase")).apply("abc"));

        assertEquals("bc", ((FuncMethodDeclare) proto.get("sub")).apply("abc", 1, 3));
        assertEquals("bc", ((FuncMethodDeclare) proto.get("sub")).apply("abc", 1, null));
        assertEquals("abc", ((FuncMethodDeclare) proto.get("sub")).apply("abc", null, null));
        assertEquals("abc", ((FuncMethodDeclare) proto.get("sub")).apply("abc", null, 3));

        assertEquals("bc", ((FuncMethodDeclare) proto.get("removeStart")).apply("abc", "a"));
        assertEquals("abc", ((FuncMethodDeclare) proto.get("removeStart")).apply("abc", "b"));
        assertEquals("abc", ((FuncMethodDeclare) proto.get("removeStart")).apply("abc", "c"));
        assertEquals("abc", ((FuncMethodDeclare) proto.get("removeStart")).apply("abc", "d"));

        assertEquals("ab", ((FuncMethodDeclare) proto.get("removeEnd")).apply("abc", "c"));
        assertEquals("abc", ((FuncMethodDeclare) proto.get("removeEnd")).apply("abc", "b"));
        assertEquals("abc", ((FuncMethodDeclare) proto.get("removeEnd")).apply("abc", "a"));
        assertEquals("abc", ((FuncMethodDeclare) proto.get("removeEnd")).apply("abc", "d"));

        assertEquals("a", ((FuncMethodDeclare) proto.get("before")).apply("abc", "b"));
        assertEquals("ab", ((FuncMethodDeclare) proto.get("before")).apply("abc", "c"));

        assertEquals("bc", ((FuncMethodDeclare) proto.get("after")).apply("abc", "a"));
        assertEquals("c", ((FuncMethodDeclare) proto.get("after")).apply("abc", "b"));

        assertArrayEquals(
                new String[]{"a", "b", "c"},
                (String[]) ((FuncMethodDeclare) proto.get("split")).apply("a,b,c", ",")
        );
        assertEquals("adc", ((FuncMethodDeclare) proto.get("replace")).apply("abc", "b", "d"));
    }

}
