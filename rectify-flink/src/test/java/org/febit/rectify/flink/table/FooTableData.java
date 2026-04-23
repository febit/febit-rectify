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
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.catalog.CatalogTable;
import org.apache.flink.table.catalog.ResolvedCatalogTable;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.types.Row;
import org.febit.lang.util.JacksonUtils;
import org.febit.lang.util.Lists;
import org.febit.rectify.flink.table.factory.RectifierFormatFactory;
import org.febit.rectify.flink.table.factory.RectifierFormatOptions;
import org.febit.rectify.format.JsonSourceFormat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
final class FooTableData {

    static final String TABLE = "foo";

    static final List<Foo> ALL = List.of(
            new Foo(6L, true, 60, "666"),
            new Foo(7L, false, 70, "777"),
            new Foo(4L, true, 40, "444"),
            new Foo(8L, true, 0, "888")
    );

    static final List<Foo> EXPECTED = ALL.stream()
            .filter(Foo::enable)
            .filter(foo -> foo.status() > 0)
            .map(foo -> foo.withStatus(foo.status() * 10))
            .toList();

    static final String RAW_LINES = ALL.stream()
            .map(JacksonUtils::jsonify)
            .collect(Collectors.joining("\n"));

    private static class EnvHolder {
        static final TableEnvironment ENV;

        static {
            try {
                ENV = TableEnvironment.create(EnvironmentSettings.inBatchMode());
                ENV.executeSql(createTable());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    static List<Row> query(String sql) throws Exception {
        try (var results = EnvHolder.ENV.executeSql(sql).collect()) {
            return Lists.collect(results);
        }
    }

    static DataType dataType() {
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

    static ResolvedCatalogTable catalogTable() {
        var dataType = dataType();
        var rowType = (RowType) dataType.getLogicalType();
        var catalogTable = CatalogTable.newBuilder()
                .schema(Schema.newBuilder().fromRowDataType(dataType).build())
                .options(tableOptions())
                .build();
        return new ResolvedCatalogTable(
                catalogTable,
                ResolvedSchema.physical(rowType.getFieldNames(), dataType.getChildren())
        );
    }

    static Map<String, String> tableOptions() {
        var options = new LinkedHashMap<String, String>();
        options.put("connector", TestingBytesTableSourceFactory.IDENTIFIER);
        options.put("format", RectifierFormatFactory.IDENTIFIER);
        options.put("febit-rectifier.source.format", JsonSourceFormat.NAME);
        options.put("febit-rectifier.name", "Demo");
        options.put("febit-rectifier.filters", "['$.status > 0', '$.enable']");
        options.put("febit-rectifier.columns", "{id: '$.id', status: '$.status * 10', content: '$.content'}");
        options.put("data", RAW_LINES);
        return options;
    }

    static String createTable() {
        return """
                CREATE TEMPORARY TABLE `%s` (
                  id BIGINT NOT NULL,
                  enable BOOLEAN NOT NULL,
                  status INT NOT NULL,
                  content STRING NOT NULL
                ) WITH (
                %s
                )
                """.formatted(TABLE, TableTestSupport.sqlOptions(tableOptions()));
    }
}

