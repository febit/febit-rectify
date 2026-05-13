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
package org.febit.rectify.flink.table.factory.file;

import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.connector.file.src.FileSourceSplit;
import org.apache.flink.connector.file.src.impl.StreamFormatAdapter;
import org.apache.flink.connector.file.src.reader.BulkFormat;
import org.apache.flink.connector.file.table.format.BulkDecodingFormat;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.Projection;
import org.apache.flink.table.connector.format.ProjectableDecodingFormat;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.DataType;
import org.febit.rectify.flink.table.TableTypeUtils;
import org.febit.rectify.flink.table.factory.FactorySupport;
import org.febit.rectify.flink.table.factory.RowDataProjectConverter;

public record RectifierBulkDecodingFormat(
        ReadableConfig config
) implements BulkDecodingFormat<RowData>,
        ProjectableDecodingFormat<BulkFormat<RowData, FileSourceSplit>> {

    @Override
    public BulkFormat<RowData, FileSourceSplit> createRuntimeDecoder(
            DynamicTableSource.Context context, DataType physicalDataType, int[][] projections) {

        var projectedDataType = Projection.of(projections).project(physicalDataType);
        var producedType = context.<RowData>createTypeInformation(projectedDataType);
        var dataStructureConverter = context.createDataStructureConverter(projectedDataType);
        var converter = RowDataProjectConverter.of(dataStructureConverter, projections);
        var rowType = TableTypeUtils.toRowType(physicalDataType);
        var rectifier = FactorySupport.createStringBasedRectifier(config, rowType);
        return new StreamFormatAdapter<>(
                RectifierStreamFormat.create(rectifier, producedType, converter)
        );
    }

    @Override
    public ChangelogMode getChangelogMode() {
        return ChangelogMode.insertOnly();
    }

    @Override
    public boolean supportsNestedProjection() {
        return true;
    }
}
