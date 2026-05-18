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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

class SqllineExampleTest {

    @TempDir
    Path tempDir;

    @Test
    void quickStart() throws Exception {
        var tablesDir = Files.createDirectories(tempDir.resolve("tables"));

        Files.writeString(tablesDir.resolve("orders.log"), """
                {"id": 1, "status": 10, "content": "hello"}
                {"id": 2, "status": 20, "content": "world"}
                {"id": 3, "status": 15, "content": "skip"}
                """);
        Files.writeString(tablesDir.resolve("orders-log.rectify.yml"), """
                name: orders
                source:
                  path: orders.log
                  format: json
                setups:
                  - var isEven = $.status % 2 == 0
                filters:
                  - isEven || "status is not even"
                columns:
                  - name: id
                    type: long
                    expr: $.id
                  - name: status
                    type: int
                    expr: $.status
                  - name: content
                    type: string
                    expr: '"prefix:" + $.content'
                """);

        var model = tempDir.resolve("model.json");
        Files.writeString(model, """
                {
                  "version": "1.0",
                  "defaultSchema": "rectify",
                  "schemas": [
                    {
                      "name": "rectify",
                      "type": "custom",
                      "factory": "org.febit.rectify.sqlline.RectifySchemaFactory",
                      "operand": {
                        "directory": "tables"
                      }
                    }
                  ]
                }
                """);

        Class.forName("org.apache.calcite.jdbc.Driver");
        try (var connection = DriverManager.getConnection("jdbc:calcite:model=" + model.toAbsolutePath());
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("""
                     SELECT "id", "status", "content"
                     FROM "orders"
                     ORDER BY "id"
                     """)) {
            resultSet.next();
            assertEquals(1L, resultSet.getLong(1));
            assertEquals(10, resultSet.getInt(2));
            assertEquals("prefix:hello", resultSet.getString(3));

            resultSet.next();
            assertEquals(2L, resultSet.getLong(1));
            assertEquals(20, resultSet.getInt(2));
            assertEquals("prefix:world", resultSet.getString(3));
        }
    }
}

