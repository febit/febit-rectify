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
package org.febit.rectify.flink.table.factory;

import lombok.Getter;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.table.connector.RuntimeConverter;
import org.apache.flink.table.connector.source.DynamicTableSource.DataStructureConverter;
import org.apache.flink.table.data.RowData;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.febit.lang.util.SingleElementConsumer;
import org.febit.rectify.flink.FlinkRectifier;
import org.febit.rectify.flink.support.ProjectUtils;
import org.jspecify.annotations.Nullable;

import java.io.Serial;

final class RectifierFormatSchema implements DeserializationSchema<RowData> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final FlinkRectifier<byte[]> rectifier;
    @Getter
    private final TypeInformation<RowData> producedType;
    private final DataStructureConverter converter;
    private final int[][] projections;

    RectifierFormatSchema(
            FlinkRectifier<byte[]> rectifier,
            TypeInformation<RowData> producedType,
            DataStructureConverter converter,
            int[][] projections
    ) {
        this.rectifier = rectifier;
        this.converter = converter;
        this.producedType = producedType;
        this.projections = projections;
    }

    @Override
    public void open(InitializationContext context) {
        converter.open(RuntimeConverter.Context.create(
                context.getUserCodeClassLoader().asClassLoader()
        ));
    }

    @Nullable
    private RowData project(@Nullable Row row) {
        if (row == null) {
            return null;
        }
        var projected = ProjectUtils.project(row, projections);
        return (RowData) converter.toInternal(projected);
    }

    @Nullable
    @Override
    public RowData deserialize(byte[] message) {
        var cell = new SingleElementConsumer<Row>();
        this.rectifier.process(message, cell);
        return project(cell.getValue());
    }

    @Override
    public void deserialize(byte[] message, Collector<RowData> out) {
        this.rectifier.process(message, row -> {
            var projected = project(row);
            if (projected != null) {
                out.collect(projected);
            }
        });
    }

    @Override
    public boolean isEndOfStream(RowData nextElement) {
        return false;
    }
}

