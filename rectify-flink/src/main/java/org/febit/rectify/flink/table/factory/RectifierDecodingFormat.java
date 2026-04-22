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

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.Projection;
import org.apache.flink.table.connector.format.ProjectableDecodingFormat;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.RowType;
import org.febit.lang.util.JacksonUtils;
import org.febit.rectify.RectifierSettings;
import org.febit.rectify.SourceFormat;
import org.febit.rectify.flink.RectifierDeserializationSchema;
import org.febit.rectify.flink.table.TableTypeUtils;
import org.febit.rectify.format.AccessLogSourceFormat;
import org.febit.rectify.format.BytesSourceFormatWrapper;
import org.febit.rectify.format.JsonSourceFormat;

import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;

public record RectifierDecodingFormat(
        ReadableConfig config
) implements ProjectableDecodingFormat<DeserializationSchema<RowData>> {

    @Override
    public DeserializationSchema<RowData> createRuntimeDecoder(
            DynamicTableSource.Context context,
            DataType physicalDataType,
            int[][] projections
    ) {
        var projectedDataType = Projection.of(projections).project(physicalDataType);
        var producedType = context.<RowData>createTypeInformation(projectedDataType);

        var rowType = TableTypeUtils.toRowType(physicalDataType);
        var deserializer = createDeserializer(config, rowType);
        var converter = context.createDataStructureConverter(projectedDataType);

        return new RectifierFormatDeserializer(deserializer, converter, producedType, projections);
    }

    @Override
    public ChangelogMode getChangelogMode() {
        return ChangelogMode.insertOnly();
    }

    @Override
    public boolean supportsNestedProjection() {
        return true;
    }

    private static RectifierDeserializationSchema createDeserializer(ReadableConfig config, RowType rowType) {
        var sourceFormat = createSourceFormat(config);
        var settings = createSettings(config, rowType);
        return RectifierDeserializationSchema.of(sourceFormat, settings);
    }

    private static SourceFormat<byte[], Object> createSourceFormat(ReadableConfig conf) {
        var name = conf.get(RectifierFormatOptions.SOURCE_FORMAT);
        var props = conf.get(RectifierFormatOptions.SOURCE_OPTIONS);

        return switch (name) {
            case JsonSourceFormat.NAME -> new BytesSourceFormatWrapper(new JsonSourceFormat());
            case AccessLogSourceFormat.NAME -> {
                var options = JacksonUtils.to(props, AccessLogSourceFormat.Options.class);
                Objects.requireNonNull(options, "Properties is required for access log format");
                yield new BytesSourceFormatWrapper(AccessLogSourceFormat.create(options));
            }
            default -> throw new IllegalArgumentException("Unsupported format: " + name);
        };
    }

    private static RectifierSettings createSettings(ReadableConfig conf, RowType rowType) {
        var builder = RectifierSettings.builder()
                .name(conf.get(RectifierFormatOptions.NAME));

        conf.get(RectifierFormatOptions.PREINSTALLS).stream()
                .filter(StringUtils::isNotBlank)
                .forEach(builder::preinstall);

        conf.get(RectifierFormatOptions.FILTERS).stream()
                .filter(StringUtils::isNotBlank)
                .forEach(builder::filter);

        var bindings = conf.get(RectifierFormatOptions.COLUMNS);
        var invalidColumns = bindings.keySet().stream()
                .filter(name -> rowType.getFieldIndex(name) < 0)
                .toList();
        if (!invalidColumns.isEmpty()) {
            throw new IllegalArgumentException("Invalid column names in 'columns' option: " + invalidColumns
                    + ", they conflict with physical field names: " + rowType.getFieldNames());
        }

        for (var field : rowType.getFields()) {
            var name = field.getName();
            var expr = bindings.get(name);
            builder.property()
                    .name(name)
                    .type(TableTypeUtils.toSchema(field.getType()).toTypeString())
                    .expression(isBlank(expr) ? null : expr)
                    .commit();
        }
        return builder.build();
    }

}
