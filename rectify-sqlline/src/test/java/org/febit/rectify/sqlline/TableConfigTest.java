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

import org.febit.rectify.RectifierConf;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TableConfigTest {

    @Test
    public void fromYaml() {
        String yaml = "name: Demo\n" +
                "sourceFormat: json\n" +
                "globalCodes: \n" +
                "  - var isEven = $.status % 2 == 0\n" +
                "  - var statusCopy = $.status\n" +
                "columns:\n" +
                "  - name: id\n" +
                "    type: long\n" +
                "    expr: $.id\n" +
                "  - name: enable\n" +
                "    type: boolean\n" +
                "    expr: \n" +
                "    checkExpr: $$ || \"enable is falsely\"\n";

        var config = TableConfig.fromYaml(yaml);
        assertNotNull(config);

        assertEquals("Demo", config.getName());
        assertEquals("json", config.getSourceFormat());

        var codes = config.getGlobalCodes();
        var columns = config.getColumns();

        assertNotNull(codes);
        assertEquals(2, codes.size());
        assertEquals("var isEven = $.status % 2 == 0", codes.get(0));
        assertEquals("var statusCopy = $.status", codes.get(1));

        assertEquals(Arrays.asList(
                RectifierConf.Column.create("long", "id", "$.id", null, null),
                RectifierConf.Column.create("boolean", "enable", null, "$$ || \"enable is falsely\"", null)
        ), columns);

    }
}
