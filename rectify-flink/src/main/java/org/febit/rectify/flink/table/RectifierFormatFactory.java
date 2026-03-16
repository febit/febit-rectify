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
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.Projection;
import org.apache.flink.table.connector.format.DecodingFormat;
import org.apache.flink.table.connector.format.ProjectableDecodingFormat;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.factories.DeserializationFormatFactory;
import org.apache.flink.table.factories.DynamicTableFactory;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.RowType;

import java.util.Set;

public class RectifierFormatFactory implements DeserializationFormatFactory {

    public static final String IDENTIFIER = "febit-rectifier";

    @Override
    public DecodingFormat<DeserializationSchema<RowData>> createDecodingFormat(
            DynamicTableFactory.Context context,
            ReadableConfig options
    ) {
        FactoryUtil.validateFactoryOptions(this, options);

        return new ProjectableDecodingFormat<>() {
            @Override
            public DeserializationSchema<RowData> createRuntimeDecoder(
                    DynamicTableSource.Context context,
                    DataType physicalDataType,
                    int[][] projections) {
                var producedDataType = Projection.of(projections).project(physicalDataType);
                var rowType = (RowType) producedDataType.getLogicalType();
                var rowDataTypeInfo = context.createTypeInformation(producedDataType);

                var projectedNames = toProjectedNames(
                        (RowType) physicalDataType.getLogicalType(), projections);

                return null;
            }

            @Override
            public ChangelogMode getChangelogMode() {
                return ChangelogMode.insertOnly();
            }

            @Override
            public boolean supportsNestedProjection() {
                return true;
            }

            private String[][] toProjectedNames(RowType type, int[][] projectedFields) {
                String[][] projectedNames = new String[projectedFields.length][];
                for (int i = 0; i < projectedNames.length; i++) {
                    int[] fieldIndices = projectedFields[i];
                    String[] fieldNames = new String[fieldIndices.length];
                    projectedNames[i] = fieldNames;

                    // convert fieldIndices to fieldNames
                    RowType currentType = type;
                    for (int j = 0; j < fieldIndices.length; j++) {
                        int index = fieldIndices[j];
                        fieldNames[j] = currentType.getFieldNames().get(index);
                        if (j != fieldIndices.length - 1) {
                            currentType = (RowType) currentType.getTypeAt(index);
                        }
                    }
                }
                return projectedNames;
            }
        };
    }

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Set<ConfigOption<?>> requiredOptions() {
        return Set.of();
    }

    @Override
    public Set<ConfigOption<?>> optionalOptions() {
        return Set.of();
    }
}
