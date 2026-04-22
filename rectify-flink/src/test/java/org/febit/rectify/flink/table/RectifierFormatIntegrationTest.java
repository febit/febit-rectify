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

import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.catalog.ObjectIdentifier;
import org.apache.flink.table.factories.FactoryUtil;
import org.febit.rectify.flink.table.TestingBytesTableSourceFactory.TestingBytesTableSource;
import org.febit.rectify.flink.table.TestingBytesTableSourceFactory.TestingScanRuntimeProvider;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.febit.rectify.flink.table.TableTestSupport.assertRow;
import static org.junit.jupiter.api.Assertions.*;

class RectifierFormatIntegrationTest {

    @Test
    void createDynamicTableSourceDiscoversFormatAndDecodesRows() throws Exception {
        var source = FactoryUtil.createDynamicTableSource(
                null,
                ObjectIdentifier.of("default_catalog", "default_database", TableTestData.TABLE_NAME),
                TableTestData.catalogTable(),
                Map.of(),
                new Configuration(),
                getClass().getClassLoader(),
                true
        );
        var testingSource = assertInstanceOf(TestingBytesTableSource.class, source);

        var runtimeProvider = testingSource.getScanRuntimeProvider(new TableTestSupport.TestDynamicTableSourceContext());
        var scanRuntimeProvider = assertInstanceOf(TestingScanRuntimeProvider.class, runtimeProvider);
        var rows = TableTestSupport.collectRows(scanRuntimeProvider);
        var expectedRows = TableTestData.expectedRows();
        assertEquals(expectedRows.size(), rows.size());
        for (int i = 0; i < expectedRows.size(); i++) {
            assertRow(rows.get(i), expectedRows.get(i));
        }
    }
}


