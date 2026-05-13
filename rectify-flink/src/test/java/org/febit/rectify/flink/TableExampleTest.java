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
package org.febit.rectify.flink;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.febit.lang.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TableExampleTest {

    @TempDir
    Path tempDir;

    @Test
    void quickStart() throws Exception {
        var env = TableEnvironment.create(EnvironmentSettings.inBatchMode());
        var sourceFile = tempDir.resolve("input_events.log").toAbsolutePath();

        env.executeSql("""
                CREATE TEMPORARY TABLE input_events (
                  id BIGINT,
                  enable BOOLEAN,
                  status INT,
                  content STRING
                ) WITH (
                  -- Replace with your connector options
                  'connector' = 'filesystem',
                  'path' = '%s',

                  -- febit-rectify format
                  'format' = 'febit-rectifier',
                  'febit-rectifier.source.format' = 'json',
                  'febit-rectifier.name' = 'Demo',
                  'febit-rectifier.filters' = '[''$.status > 0'', ''$.enable'']',
                  'febit-rectifier.columns' = '{id: ''$.id'', status: ''$.status * 10'', content: ''"prefix:" + $.content''}'
                );
                """.formatted(sourceFile));

        Files.writeString(sourceFile, """
                {"id": 1, "enable": true, "status": 1, "content": "ok"}
                {"id": 2, "enable": false, "status": 2, "content": "skip"}
                {"id": 3, "enable": true, "status": 5, "content": "hello"}
                """);

        var result = env.executeSql("""
                SELECT id, status, content
                FROM input_events
                ORDER BY id DESC
                """);

        try (var iter = result.collect()) {
            var rows = Lists.collect(iter);
            assertEquals(2, rows.size());
            var row1 = rows.get(0);
            assertEquals(3L, row1.getField(0));
            assertEquals(50, row1.getField(1));
            assertEquals("prefix:hello", row1.getField(2));
            var row2 = rows.get(1);
            assertEquals(1L, row2.getField(0));
            assertEquals(10, row2.getField(1));
            assertEquals("prefix:ok", row2.getField(2));
        }
    }

}

