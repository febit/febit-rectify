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

import org.junit.jupiter.api.Test;

import static org.febit.rectify.flink.table.FooTableData.query;
import static org.junit.jupiter.api.Assertions.*;

class RectifierSqlIntegrationTest {

    @Test
    void queryId() throws Exception {
        var rows = query("""
                SELECT id
                FROM `%s`
                ORDER BY id DESC"""
                .formatted(FooTableData.TABLE)
        );
        var expectedRows = FooTableData.EXPECTED;
        assertEquals(expectedRows.size(), rows.size());
        for (int i = 0; i < expectedRows.size(); i++) {
            assertEquals(expectedRows.get(i).id(), rows.get(i).getField(0));
        }
    }

    @Test
    void queryEnable() throws Exception {
        var rows = query("""
                SELECT enable
                FROM `%s`
                ORDER BY id DESC"""
                .formatted(FooTableData.TABLE)
        );
        var expectedRows = FooTableData.EXPECTED;
        assertEquals(expectedRows.size(), rows.size());
        for (int i = 0; i < expectedRows.size(); i++) {
            assertEquals(true, rows.get(i).getField(0));
        }
    }

    @Test
    void queryAll() throws Exception {
        var rows = query("""
                SELECT id, enable, status, content
                FROM `%s`
                ORDER BY id DESC"""
                .formatted(FooTableData.TABLE)
        );
        var expectedRows = FooTableData.EXPECTED;
        assertEquals(expectedRows.size(), rows.size());
        for (int i = 0; i < expectedRows.size(); i++) {
            assertEquals(expectedRows.get(i).toRow(), rows.get(i));
        }
    }
}

