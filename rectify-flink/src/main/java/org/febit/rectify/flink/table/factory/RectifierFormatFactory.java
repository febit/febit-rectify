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

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.table.connector.format.ProjectableDecodingFormat;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.factories.DeserializationFormatFactory;
import org.apache.flink.table.factories.DynamicTableFactory;
import org.apache.flink.table.factories.FactoryUtil;

import java.util.Set;

import static org.febit.rectify.flink.table.factory.RectifierFormatOptions.COLUMNS;
import static org.febit.rectify.flink.table.factory.RectifierFormatOptions.FILTERS;
import static org.febit.rectify.flink.table.factory.RectifierFormatOptions.NAME;
import static org.febit.rectify.flink.table.factory.RectifierFormatOptions.PREINSTALLS;
import static org.febit.rectify.flink.table.factory.RectifierFormatOptions.SOURCE_FORMAT;
import static org.febit.rectify.flink.table.factory.RectifierFormatOptions.SOURCE_OPTIONS;

public class RectifierFormatFactory implements DeserializationFormatFactory {

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

    public static final String IDENTIFIER = "febit-rectifier";

    @Override
    public ProjectableDecodingFormat<DeserializationSchema<RowData>> createDecodingFormat(
            DynamicTableFactory.Context context,
            ReadableConfig options
    ) {
        FactoryUtil.validateFactoryOptions(this, options);
        return new RectifierDecodingFormat(options);
    }

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Set<ConfigOption<?>> requiredOptions() {
        return REQUIRED_OPTIONS;
    }

    @Override
    public Set<ConfigOption<?>> optionalOptions() {
        return OPTIONAL_OPTIONS;
    }
}
