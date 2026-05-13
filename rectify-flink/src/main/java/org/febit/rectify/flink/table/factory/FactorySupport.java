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

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.table.types.logical.RowType;
import org.febit.lang.util.JacksonUtils;
import org.febit.rectify.RectifierSettings;
import org.febit.rectify.SourceFormat;
import org.febit.rectify.flink.FlinkRectifier;
import org.febit.rectify.flink.table.TableTypeUtils;
import org.febit.rectify.format.AccessLogSourceFormat;
import org.febit.rectify.format.BytesSourceFormatWrapper;
import org.febit.rectify.format.JsonSourceFormat;

import java.util.Set;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.febit.rectify.flink.table.factory.RectifierFormatOptions.COLUMNS;
import static org.febit.rectify.flink.table.factory.RectifierFormatOptions.FILTERS;
import static org.febit.rectify.flink.table.factory.RectifierFormatOptions.NAME;
import static org.febit.rectify.flink.table.factory.RectifierFormatOptions.PREINSTALLS;
import static org.febit.rectify.flink.table.factory.RectifierFormatOptions.SOURCE_FORMAT;
import static org.febit.rectify.flink.table.factory.RectifierFormatOptions.SOURCE_OPTIONS;

@UtilityClass
public class FactorySupport {

    public static final String IDENTIFIER = "febit-rectifier";

    public static final Set<ConfigOption<?>> REQUIRED_OPTIONS = Set.of(
            SOURCE_FORMAT
    );

    public static final Set<ConfigOption<?>> OPTIONAL_OPTIONS = Set.of(
            SOURCE_OPTIONS,
            NAME,
            PREINSTALLS,
            FILTERS,
            COLUMNS
    );

    public static FlinkRectifier<byte[]> createBytesBasedRectifier(ReadableConfig config, RowType producedType) {
        var sourceFormat = createSourceFormat(config, BytesSourceFormatWrapper::new);
        var settings = createSettings(config, producedType);
        return FlinkRectifier.of(settings, sourceFormat);
    }

    public static FlinkRectifier<String> createStringBasedRectifier(ReadableConfig config, RowType producedType) {
        var sourceFormat = createSourceFormat(config, f -> f);
        var settings = createSettings(config, producedType);
        return FlinkRectifier.of(settings, sourceFormat);
    }

    private static <I> SourceFormat<I, Object> createSourceFormat(
            ReadableConfig conf,
            Function<SourceFormat<String, Object>, SourceFormat<I, Object>> wrapper
    ) {
        var name = conf.get(RectifierFormatOptions.SOURCE_FORMAT);
        var props = conf.get(RectifierFormatOptions.SOURCE_OPTIONS);

        var format = switch (name) {
            case JsonSourceFormat.NAME -> new JsonSourceFormat();
            case AccessLogSourceFormat.NAME -> {
                var options = JacksonUtils.to(props, AccessLogSourceFormat.Options.class);
                requireNonNull(options, "Properties is required for access log format");
                yield AccessLogSourceFormat.create(options);
            }
            default -> throw new IllegalArgumentException("Unsupported format: " + name);
        };

        return wrapper.apply(format);
    }

    private static RectifierSettings createSettings(ReadableConfig conf, RowType producedType) {
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
                .filter(name -> producedType.getFieldIndex(name) < 0)
                .toList();
        if (!invalidColumns.isEmpty()) {
            throw new IllegalArgumentException("Invalid column names in 'columns' option: " + invalidColumns
                    + ", they conflict with physical field names: " + producedType.getFieldNames());
        }

        for (var field : producedType.getFields()) {
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
