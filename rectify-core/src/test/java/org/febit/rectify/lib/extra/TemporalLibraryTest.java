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

import java.util.List;

import static org.febit.rectify.lib.LibraryTestSupport.export;
import static org.febit.rectify.lib.LibraryTestSupport.namespace;
import static org.junit.jupiter.api.Assertions.*;

class TemporalLibraryTest {

    private final Lib dates = namespace(TemporalLibrary.class, "Dates");

    @Test
    void alias() {
        var lib = export(TemporalLibrary.class);
        assertTrue(lib.has("Dates"));
        assertFalse(lib.has("DATES"));
    }

    @Test
    void basic() {
        List.of(
                        "now",
                        "zone",
                        "parse",
                        "format",
                        "toMillis",
                        "toInstant",
                        "addDays",
                        "firstDayOfMonth",
                        "lastDayOfMonth",
                        "firstDayOfNextMonth"
                )
                .forEach(name -> assertDoesNotThrow(() -> dates.get(name)));
    }

    @Test
    void parseFormatAndAdjust() {
        var parse = dates.function("parse");
        var format = dates.function("format");
        var addDays = dates.function("addDays");
        var firstDayOfMonth = dates.function("firstDayOfMonth");
        var lastDayOfMonth = dates.function("lastDayOfMonth");
        var firstDayOfNextMonth = dates.function("firstDayOfNextMonth");

        var dateTime = parse.apply("2024-01-20 03:04:05", "yyyy-MM-dd HH:mm:ss");
        var date = parse.apply("2024-01-20", "yyyy-MM-dd");

        assertNotNull(date);
        assertNotNull(dateTime);

        assertNull(format.apply());
        assertNull(parse.apply());
        assertNull(firstDayOfMonth.apply());

        assertEquals("2024-01-20T03:04:05Z", format.apply(dateTime));
        assertEquals("2024-01-20 03:04:05", format.apply(dateTime, "yyyy-MM-dd HH:mm:ss"));

        assertEquals(
                dateTime,
                parse.apply(format.apply(dateTime))
        );

        assertEquals("2024-01-22", format.apply(addDays.apply(date, 2), "yyyy-MM-dd"));
        assertEquals("2024-01-20", format.apply(addDays.apply(date, null), "yyyy-MM-dd"));

        assertEquals("2024-01-01", format.apply(firstDayOfMonth.apply(date), "yyyy-MM-dd"));

        assertEquals("2024-01-31", format.apply(lastDayOfMonth.apply(date), "yyyy-MM-dd"));
        assertEquals("2024-02-01", format.apply(firstDayOfNextMonth.apply(date), "yyyy-MM-dd"));

        assertNull(parse.apply(null, "yyyy-MM-dd"));
        assertNull(format.apply(null, "yyyy-MM-dd"));
        assertNull(addDays.apply(null, 2));
        assertEquals(date.toString(), format.apply(date, null));
    }
}

