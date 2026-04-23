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

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.DataType;
import org.febit.rectify.flink.table.factory.RectifierFormatFactory;
import org.febit.rectify.flink.table.factory.RectifierFormatOptions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.febit.rectify.flink.table.factory.RectifierFormatFactory.OPTIONAL_OPTIONS;
import static org.febit.rectify.flink.table.factory.RectifierFormatFactory.REQUIRED_OPTIONS;
import static org.junit.jupiter.api.Assertions.*;

class RectifierFormatFactoryTest {

    @Test
    void createRuntimeDecoderBuildsRowDataFromOptionsAndPhysicalSchema() throws Exception {
        var schema = createRuntimeDecoder(
                FooTableData.factoryOptions(),
                FooTableData.dataType(),
                0, 2, 3
        );
        schema.open(new TableTestSupport.TestInitializationContext());

        var row = schema.deserialize("""
                {"id":6,"enable":true,"status":6,"content":"666"}
                """.getBytes());
        assertNotNull(row);
        assertEquals(3, row.getArity());
        assertEquals(6L, row.getLong(0));
        assertEquals(6, row.getInt(1));
        assertEquals("prefix:666", row.getString(2).toString());

        assertNull(schema.deserialize("""
                {"id":7,"enable":false,"status":7,"content":"777"}
                """.getBytes()));
        assertNull(schema.deserialize("""
                {"id":8,"enable":true,"status":0,"content":"888"}
                """.getBytes()));
    }

    @Test
    void options() {
        assertEquals(1, REQUIRED_OPTIONS.size());
        assertTrue(REQUIRED_OPTIONS.contains(RectifierFormatOptions.SOURCE_FORMAT));

        assertEquals(5, OPTIONAL_OPTIONS.size());
        assertTrue(OPTIONAL_OPTIONS.contains(RectifierFormatOptions.SOURCE_OPTIONS));
        assertTrue(OPTIONAL_OPTIONS.contains(RectifierFormatOptions.NAME));
        assertTrue(OPTIONAL_OPTIONS.contains(RectifierFormatOptions.PREINSTALLS));
        assertTrue(OPTIONAL_OPTIONS.contains(RectifierFormatOptions.FILTERS));
        assertTrue(OPTIONAL_OPTIONS.contains(RectifierFormatOptions.COLUMNS));
    }

    @Test
    void unknownPropertyBindingIsRejected() {
        var options = FooTableData.factoryOptions();
        options.set(RectifierFormatOptions.COLUMNS, Map.of("missing", "$.missing"));

        assertThatThrownBy(() -> createRuntimeDecoder(options, FooTableData.dataType(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing")
                .hasMessageContaining("Invalid column names");
    }

    @Test
    void unsupportedFieldTypeIsRejected() {
        var options = FooTableData.factoryOptions();
        var unsupportedType = DataTypes.ROW(
                DataTypes.FIELD("price", DataTypes.DECIMAL(10, 2))
        );

        assertThatThrownBy(() -> createRuntimeDecoder(options, unsupportedType, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid column names")
                .hasMessageContaining("price")
                .hasMessageContaining("id")
                .hasMessageContaining("status")
                .hasMessageContaining("content")
        ;
    }

    private static DeserializationSchema<RowData> createRuntimeDecoder(
            Configuration options,
            DataType physicalDataType,
            int... projections
    ) {
        var factory = new RectifierFormatFactory();
        var format = factory.createDecodingFormat(TableTestSupport.factoryContext(), options);
        return format.createRuntimeDecoder(
                new TableTestSupport.TestDynamicTableSourceContext(),
                physicalDataType,
                projection(projections)
        );
    }

    private static int[][] projection(int... indexes) {
        return IntStream.of(indexes)
                .mapToObj(index -> new int[]{index})
                .toArray(int[][]::new);
    }
}

