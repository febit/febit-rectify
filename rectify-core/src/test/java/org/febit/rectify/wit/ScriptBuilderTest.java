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
package org.febit.rectify.wit;

import org.junit.jupiter.api.Test;

import static org.febit.rectify.wit.ScriptBuilder.escapeForString;
import static org.junit.jupiter.api.Assertions.*;

class ScriptBuilderTest {

    @Test
    void testEscapeForString() {
        assertEquals("null", escapeForString(null));
        assertEquals("\"\"", escapeForString(""));
        assertEquals("\"abc\"", escapeForString("abc"));
        assertEquals("\"a\\\"b\\\\c\"", escapeForString("a\"b\\c"));
        assertEquals("\"a\\nb\\rc\"", escapeForString("a\nb\rc"));

        assertEquals("\"a\\tbc\"", escapeForString("a\tb\b\fc"));

    }
}
