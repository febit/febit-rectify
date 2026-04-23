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
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.types.Row;
import org.febit.lang.util.JacksonUtils;
import org.febit.lang.util.Lists;
import org.febit.rectify.flink.table.factory.RectifierFormatFactory;
import org.febit.rectify.format.JsonSourceFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
final class AllTypesTableData {

    static final String TABLE = "all_types";

    static final List<AllTypes> ALL = List.of(
            AllTypes.builder()
                    .id(1)
                    .bool(true)
                    .tinyInt((byte) 2)
                    .shortNum((short) 3)
                    .intNum(42)
                    .longNum(123456789L)
                    .floatNum(3.14F)
                    .doubleNum(2.71828)
                    .decimalNum(BigDecimal.valueOf(12345.6789))
                    .text("Hello")
                    .date(LocalDate.of(2024, 6, 1))
                    .time(LocalTime.of(12, 34, 56))
                    .datetime(LocalDateTime.of(2024, 6, 1, 12, 34, 56))
                    .instant(Instant.parse("2024-06-01T12:34:56Z"))
                    .datatimetz(ZonedDateTime.parse("2024-06-01T12:34:56+08:00"))
                    .list(List.of(1, 2, 3))
                    .bytes(new byte[]{0x01, 0x02, 0x03})
                    .map(Map.of("key1", 1, "key2", 42))
                    .structFoo(new StructFoo(9L, true, 90, "999"))
                    .arrayBool(new Boolean[]{true, false, true})
                    .arrayByte(new Byte[]{1, 2, 3})
                    .arrayShort(new Short[]{4, 5, 6})
                    .arrayInt(new Integer[]{7, 8, 9})
                    .arrayLong(new Long[]{10L, 11L, 12L})
                    .arrayFloat(new Float[]{1.25F, 2.5F, 5.0F})
                    .arrayDouble(new Double[]{1.75, 3.5, 7.0})
                    .arrayDecimal(new BigDecimal[]{
                            BigDecimal.valueOf(10.125),
                            BigDecimal.valueOf(20.5),
                            BigDecimal.valueOf(30)
                    })
                    .arrayString(new String[]{"a", "b", "c"})
                    .arrayFoo(new StructFoo[]{
                            new StructFoo(101L, true, 11, "alpha"),
                            new StructFoo(102L, false, 22, "beta")
                    })
                    .build()
    );

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

    @SuppressWarnings("unchecked")
    static <T> List<T> listColumn(String... paths) throws Exception {
        var escapedPaths = Stream.of(paths)
                .map("`%s`"::formatted)
                .collect(Collectors.joining("."));
        try (var results = EnvHolder.ENV.executeSql("""
                SELECT %s
                FROM `%s`
                ORDER BY id
                """.formatted(escapedPaths, TABLE)
        ).collect()) {
            return Lists.collect(results, r -> (T) Objects.requireNonNull(r.getField(0)));
        }
    }

    static <T> List<T> mapAll(Function<AllTypes, T> mapper) {
        return ALL.stream()
                .map(mapper)
                .toList();
    }

    record StructFoo(
            long id,
            boolean enable,
            int status,
            String content
    ) {
        Row toRow() {
            return Row.of(id, enable, status, content);
        }
    }

    static Map<String, String> tableOptions() {
        var options = new LinkedHashMap<String, String>();
        options.put("connector", TestingBytesTableSourceFactory.IDENTIFIER);
        options.put("format", RectifierFormatFactory.IDENTIFIER);
        options.put("febit-rectifier.source.format", JsonSourceFormat.NAME);
        options.put("febit-rectifier.name", "DemoAllTypeSupported");
        options.put("data", RAW_LINES);
        return options;
    }

    static String createTable() {
        return """
                CREATE TEMPORARY TABLE `%s` (
                  `id` INT,
                  `bool` BOOLEAN,
                  `tinyInt` TINYINT,
                  `shortNum` SMALLINT,
                  `intNum` INT,
                  `longNum` BIGINT,
                  `floatNum` FLOAT,
                  `doubleNum` DOUBLE,
                  `decimalNum` DECIMAL(10, 2),
                  `text` STRING,
                  `date` DATE,
                  `time` TIME,
                  `datetime` TIMESTAMP,
                  `instant` TIMESTAMP_LTZ,
                  `datatimetz` TIMESTAMP_LTZ, -- TIMESTAMP WITH TIME ZONE, -- https://issues.apache.org/jira/browse/FLINK-14084
                  `list` ARRAY<INT>,
                  `bytes` BYTES,
                  `map` MAP<STRING, INT>,
                  `structFoo` ROW<id BIGINT, enable BOOLEAN, status INT, content STRING>,
                  `arrayBool` ARRAY<BOOLEAN>,
                  `arrayByte` ARRAY<TINYINT>,
                  `arrayShort` ARRAY<SMALLINT>,
                  `arrayInt` ARRAY<INT>,
                  `arrayLong` ARRAY<BIGINT>,
                  `arrayFloat` ARRAY<FLOAT>,
                  `arrayDouble` ARRAY<DOUBLE>,
                  `arrayDecimal` ARRAY<DECIMAL(10, 2)>,
                  `arrayString` ARRAY<STRING>,
                  `arrayFoo` ARRAY<ROW<id BIGINT, enable BOOLEAN, status INT, content STRING>>
                ) WITH (
                %s
                )
                """.formatted(TABLE, TableTestSupport.sqlOptions(tableOptions()));
    }

}

