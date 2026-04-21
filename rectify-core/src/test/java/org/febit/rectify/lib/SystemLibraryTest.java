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
package org.febit.rectify.lib;

import org.febit.rectify.wit.ExitException;
import org.febit.rectify.wit.FilterBreakpoint;
import org.febit.rectify.wit.ScriptBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SystemLibraryTest {

    private final Lib lib = LibraryTestSupport.export(SystemLibrary.class);

    @Test
    void bindingNames() {
        assertTrue(lib.has(ScriptBuilder.VAR_EXIT));
        assertTrue(lib.has(ScriptBuilder.VAR_FILTER_VERIFY));
        assertTrue(lib.has(ScriptBuilder.VAR_CREATE_FILTER_BREAKPOINT));

        assertFalse(lib.has("EXIT"));
        assertFalse(lib.has("VERIFY_FILTER"));
        assertFalse(lib.has("CREATE_FILTER_BREAKPOINT"));
    }

    @Test
    void exit() {
        var exit = lib.function(ScriptBuilder.VAR_EXIT);

        var noReason = assertThrows(ExitException.class, () -> exit.apply((Object) null));
        assertNull(noReason.getReason());

        var withReason = assertThrows(ExitException.class, () -> exit.apply("bad-data"));
        assertEquals("bad-data", withReason.getReason());
    }

    @Test
    void verifyFilter() {
        var verifyFilter = lib.function(ScriptBuilder.VAR_FILTER_VERIFY);

        assertNull(verifyFilter.apply((Object) null));
        assertNull(verifyFilter.apply(true));
        assertNull(verifyFilter.apply(123));

        var noReason = assertThrows(ExitException.class, () -> verifyFilter.apply(false));
        assertNull(noReason.getReason());

        var withReason = assertThrows(ExitException.class, () -> verifyFilter.apply("rejected"));
        assertEquals("rejected", withReason.getReason());
    }

    @Test
    void createFilterBreakpoint() {
        var create = lib.function(ScriptBuilder.VAR_CREATE_FILTER_BREAKPOINT);

        assertEquals(
                new FilterBreakpoint(3, "field", "expr"),
                create.apply(3, "field", "expr")
        );
        assertEquals(
                new FilterBreakpoint(0, null, "expr"),
                create.apply(null, null, "expr")
        );
    }

}

