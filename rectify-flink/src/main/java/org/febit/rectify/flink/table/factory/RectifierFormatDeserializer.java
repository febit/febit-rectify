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
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.data.RowData;
import org.apache.flink.types.Row;
import org.febit.rectify.flink.RectifierDeserializationSchema;
import org.febit.rectify.flink.RowProjectUtils;
import org.jspecify.annotations.Nullable;

import java.io.Serial;

final class RectifierFormatDeserializer implements DeserializationSchema<RowData> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final RectifierDeserializationSchema deserializer;
    private final DynamicTableSource.DataStructureConverter converter;
    @Getter
    private final TypeInformation<RowData> producedType;
    private final int[][] projections;

    RectifierFormatDeserializer(
            RectifierDeserializationSchema deserializer,
            DynamicTableSource.DataStructureConverter converter,
            TypeInformation<RowData> producedType,
            int[][] projections
    ) {
        this.deserializer = deserializer;
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
    @Override
    public RowData deserialize(byte[] message) {
        Row row = deserializer.deserialize(message);
        if (row == null) {
            return null;
        }
        var projected = RowProjectUtils.project(row, projections);
        return (RowData) converter.toInternal(projected);
    }

    @Override
    public boolean isEndOfStream(RowData nextElement) {
        return false;
    }
}

