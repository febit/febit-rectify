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
package org.febit.rectify.flink.table;

import org.febit.rectify.flink.table.factory.RectifierFormatOptions;
import org.febit.rectify.format.AccessLogSourceFormat;
import org.febit.rectify.format.JsonSourceFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RectifierFormatOptionsTest {

    @Test
    void design() {
        assertEquals("source.format", RectifierFormatOptions.SOURCE_FORMAT.key());
        assertFalse(RectifierFormatOptions.SOURCE_FORMAT.hasDefaultValue());

        assertEquals("{}", RectifierFormatOptions.SOURCE_OPTIONS.defaultValue());
        assertEquals("Unnamed", RectifierFormatOptions.NAME.defaultValue());
        assertTrue(RectifierFormatOptions.PREINSTALLS.defaultValue().isEmpty());
        assertTrue(RectifierFormatOptions.FILTERS.defaultValue().isEmpty());
        assertTrue(RectifierFormatOptions.COLUMNS.defaultValue().isEmpty());

        assertEquals("json", JsonSourceFormat.NAME);
        assertEquals("access", AccessLogSourceFormat.NAME);
    }

}
