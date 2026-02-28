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
package org.febit.rectify.sqlline;

import org.febit.rectify.RectifierSettings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TableSettingsTest {

    @Test
    void source() {
        TableSettings.Source source;
        source = TableSettings.fromYaml("""
                name: x
                path: /path/to/file
                source: json
                """).source();

        assertEquals("json", source.format());
        assertTrue(source.properties().isEmpty());

        source = TableSettings.fromYaml("""
                name: x
                path: /path/to/file
                source:
                  format: access
                  properties:
                    columns: [a, b, c]
                """).source();

        assertEquals("access", source.format());
        assertThat(source.properties())
                .containsEntry("columns", List.of("a", "b", "c"));
    }

    @Test
    void fromYaml() {
        String yaml = """
                name: Demo
                source: json
                path: /path/to/file
                setups:
                  - var isEven = $.status % 2 == 0
                  - var statusCopy = $.status
                columns:
                  - name: id
                    type: long
                    expression: $.id
                  - name: enable
                    type: boolean
                    validation: $$ || "enable is falsely"
                """;

        var config = TableSettings.fromYaml(yaml);
        assertNotNull(config);

        assertEquals("Demo", config.name());
        assertEquals(
                TableSettings.Source.builder().format("json").build(),
                config.source()
        );

        var codes = config.setups();
        var columns = config.columns();

        assertNotNull(codes);
        assertEquals(2, codes.size());
        assertEquals("var isEven = $.status % 2 == 0", codes.get(0));
        assertEquals("var statusCopy = $.status", codes.get(1));

        assertEquals(List.of(
                RectifierSettings.Column.builder()
                        .name("id")
                        .type("long")
                        .expression("$.id")
                        .build(),
                RectifierSettings.Column.builder()
                        .name("enable")
                        .type("boolean")
                        .validation("$$ || \"enable is falsely\"")
                        .build()
        ), columns);

    }
}
