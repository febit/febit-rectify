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

import org.febit.rectify.lib.Lib;
import org.junit.jupiter.api.Test;

import static org.febit.rectify.lib.LibraryTestSupport.export;
import static org.febit.rectify.lib.LibraryTestSupport.namespace;
import static org.junit.jupiter.api.Assertions.*;

class StringLibraryTest {

    final Lib str = namespace(StringLibrary.class, "Str");

    @Test
    void alias() {
        var lib = export(StringLibrary.class);
        assertSame(lib.get("Str"), lib.get("Strings"));
    }

    @Test
    void is() {
        var isEmpty = str.function("isEmpty");
        var isNotEmpty = str.function("isNotEmpty");
        var isBlank = str.function("isBlank");
        var isNotBlank = str.function("isNotBlank");
        var isNumeric = str.function("isNumeric");
        var isAlpha = str.function("isAlpha");
        var isAlphaSpace = str.function("isAlphaSpace");
        var isAlphaNumeric = str.function("isAlphaNumeric");
        var isAlphaNumericSpace = str.function("isAlphaNumericSpace");

        assertEquals(true, isEmpty.apply());
        assertEquals(true, isEmpty.apply(""));
        assertEquals(false, isNotEmpty.apply());
        assertEquals(false, isNotEmpty.apply(""));

        assertEquals(true, isBlank.apply());
        assertEquals(true, isBlank.apply(""));
        assertEquals(false, isNotBlank.apply());
        assertEquals(false, isNotBlank.apply(""));

        assertEquals(false, isNumeric.apply());
        assertEquals(true, isNumeric.apply("123"));

        assertEquals(false, isAlpha.apply());
        assertEquals(true, isAlpha.apply("abc"));

        assertEquals(false, isAlphaSpace.apply());
        assertEquals(true, isAlphaSpace.apply("abc "));

        assertEquals(false, isAlphaNumeric.apply());
        assertEquals(true, isAlphaNumeric.apply("abc1"));

        assertEquals(false, isAlphaNumericSpace.apply());
        assertEquals(true, isAlphaNumericSpace.apply("abc1 "));
    }

    @Test
    void fix() {
        var lower = str.function("lower");
        var upper = str.function("upper");
        var toLowerCase = str.function("toLowerCase");
        var toUpperCase = str.function("toUpperCase");

        assertEquals("abc", lower.apply("ABC"));
        assertEquals("ABC", upper.apply("abc"));
        assertEquals("abc", toLowerCase.apply("ABC"));
        assertEquals("ABC", toUpperCase.apply("abc"));
    }

    @Test
    void sub() {
        var trim = str.function("trim");
        var sub = str.function("sub");
        var removeStart = str.function("removeStart");
        var removeEnd = str.function("removeEnd");
        var before = str.function("before");
        var after = str.function("after");
        var split = str.function("split");
        var replace = str.function("replace");

        assertNull(trim.apply());
        assertNull(sub.apply());
        assertNull(removeStart.apply());
        assertNull(removeEnd.apply());
        assertNull(before.apply());
        assertNull(after.apply());
        assertNull(split.apply());
        assertNull(replace.apply());

        assertEquals("abc", trim.apply(" abc "));
        assertEquals("bc", sub.apply("abc", 1, 3));
        assertEquals("bc", sub.apply("abc", 1, null));
        assertEquals("abc", sub.apply("abc", null, null));
        assertEquals("abc", sub.apply("abc", null, 3));

        assertEquals("bc", removeStart.apply("abc", "a"));
        assertEquals("abc", removeStart.apply("abc", "b"));
        assertEquals("abc", removeStart.apply("abc", "c"));
        assertEquals("abc", removeStart.apply("abc", "d"));

        assertEquals("ab", removeEnd.apply("abc", "c"));
        assertEquals("abc", removeEnd.apply("abc", "b"));
        assertEquals("abc", removeEnd.apply("abc", "a"));
        assertEquals("abc", removeEnd.apply("abc", "d"));

        assertEquals("a", before.apply("abc", "b"));
        assertEquals("ab", before.apply("abc", "c"));

        assertEquals("bc", after.apply("abc", "a"));
        assertEquals("c", after.apply("abc", "b"));

        assertArrayEquals(
                new String[]{"a", "b", "c"},
                (String[]) split.apply("a,b,c", ",")
        );
        assertEquals("adc", replace.apply("abc", "b", "d"));
    }

}
