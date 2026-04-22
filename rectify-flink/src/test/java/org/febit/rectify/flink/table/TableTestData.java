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

import lombok.experimental.UtilityClass;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.catalog.CatalogTable;
import org.apache.flink.table.catalog.ResolvedCatalogTable;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.types.Row;
import org.febit.rectify.flink.table.factory.RectifierFormatFactory;
import org.febit.rectify.flink.table.factory.RectifierFormatOptions;
import org.febit.rectify.format.JsonSourceFormat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
final class TableTestData {

    static final String TABLE_NAME = "rectifier_input";

    static final String QUERY_ALL = """
            SELECT id, enable, status, content
            FROM rectifier_input
            ORDER BY id DESC
            """;

    private static final List<ExpectedRow> EXPECTED_ROWS = List.of(
            new ExpectedRow(6L, 60, "666"),
            new ExpectedRow(4L, 40, "444")
    );

    record ExpectedRow(long id, int status, String content) {
        Row toRow() {
            return Row.of(id, true, status, content);
        }
    }

    static DataType physicalDataType() {
        return DataTypes.ROW(
                DataTypes.FIELD("id", DataTypes.BIGINT().notNull()),
                DataTypes.FIELD("enable", DataTypes.BOOLEAN().notNull()),
                DataTypes.FIELD("status", DataTypes.INT().notNull()),
                DataTypes.FIELD("content", DataTypes.STRING().notNull())
        );
    }

    static Configuration factoryOptions() {
        var options = new Configuration();
        options.set(RectifierFormatOptions.SOURCE_FORMAT, JsonSourceFormat.NAME);
        options.set(RectifierFormatOptions.NAME, "Demo");
        options.set(RectifierFormatOptions.PREINSTALLS, List.of("var statusCopy = $.status"));
        options.set(RectifierFormatOptions.FILTERS, List.of(
                "$.status > 0",
                "$.enable || \"enable is falsely\""
        ));
        options.set(RectifierFormatOptions.COLUMNS, Map.of(
                "id", "$.id",
                "status", "$.status",
                "content", "\"prefix:\" + $.content"
        ));
        return options;
    }

    static List<ExpectedRow> expectedRows() {
        return EXPECTED_ROWS;
    }

    static ResolvedCatalogTable catalogTable() {
        var physicalDataType = physicalDataType();
        var rowType = (RowType) physicalDataType.getLogicalType();
        var catalogTable = CatalogTable.newBuilder()
                .schema(Schema.newBuilder().fromRowDataType(physicalDataType).build())
                .options(options())
                .build();
        return new ResolvedCatalogTable(
                catalogTable,
                ResolvedSchema.physical(rowType.getFieldNames(), physicalDataType.getChildren())
        );
    }

    static Map<String, String> options() {
        return options(TestingBytesTableSourceFactory.IDENTIFIER);
    }

    static Map<String, String> options(String connectorIdentifier) {
        var options = new LinkedHashMap<String, String>();
        options.put("connector", connectorIdentifier);
        options.put("data", rawInput());
        options.put("format", RectifierFormatFactory.IDENTIFIER);
        options.put("febit-rectifier.source.format", JsonSourceFormat.NAME);
        options.put("febit-rectifier.name", "Demo");
        options.put("febit-rectifier.filters", "['$.status > 0', '$.enable']");
        options.put("febit-rectifier.columns", "{id: '$.id', status: '$.status * 10', content: '$.content'}");
        return options;
    }

    static String createTableSql() {
        return """
                CREATE TEMPORARY TABLE `%s` (
                  id BIGINT NOT NULL,
                  enable BOOLEAN NOT NULL,
                  status INT NOT NULL,
                  content STRING NOT NULL
                ) WITH (
                %s
                )
                """.formatted(TABLE_NAME, sqlOptions());
    }

    static String rawInput() {
        return """
                {"id":6,"enable":true,"status":6,"content":"666"}
                {"id":7,"enable":false,"status":7,"content":"777"}
                {"id":4,"enable":true,"status":4,"content":"444"}
                {"id":8,"enable":true,"status":0,"content":"888"}
                """.stripIndent();
    }

    private static String sqlOptions() {
        return options().entrySet().stream()
                .map(entry -> "  '%s' = '%s'".formatted(
                        entry.getKey(),
                        escapeSqlLiteral(entry.getValue()))
                )
                .collect(Collectors.joining(",\n"));
    }

    private static String escapeSqlLiteral(String value) {
        return value.replace("'", "''");
    }
}

