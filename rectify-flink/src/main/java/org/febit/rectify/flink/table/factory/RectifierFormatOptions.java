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
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;

import java.util.List;
import java.util.Map;

@UtilityClass
public class RectifierFormatOptions {

    public static final ConfigOption<String> SOURCE_FORMAT = ConfigOptions.key("source.format")
            .stringType()
            .noDefaultValue()
            .withDescription("Raw input format for febit-rectifier,"
                    + " such as 'json' or 'access'.");

    public static final ConfigOption<String> SOURCE_OPTIONS = ConfigOptions.key("source.options")
            .stringType()
            .defaultValue("{}")
            .withDescription("Source format options in JSON format,"
                    + " passed to the underlying source format,"
                    + " used for configuring the raw record parsing logic.");

    public static final ConfigOption<String> NAME = ConfigOptions.key("name")
            .stringType()
            .defaultValue("Unnamed")
            .withDescription("Schema name for generated metadata,"
                    + " used for debugging and schema inference,"
                    + " it does not affect the actual processing logic.");

    public static final ConfigOption<List<String>> PREINSTALLS = ConfigOptions.key("preinstalls")
            .stringType()
            .asList()
            .defaultValues()
            .withDescription("Global pre-install scripts,"
                    + " executed before processing any record,"
                    + " can be used to define UDFs or perform global initialization.");

    public static final ConfigOption<List<String>> FILTERS = ConfigOptions.key("filters")
            .stringType()
            .asList()
            .defaultValues()
            .withDescription("Filter expressions,"
                    + " if any expression evaluates to false,"
                    + " the record will be filtered out");

    public static final ConfigOption<Map<String, String>> COLUMNS = ConfigOptions.key("columns")
            .mapType()
            .defaultValue(Map.of())
            .withDescription("Column expressions,"
                    + " the key is the output column name,"
                    + " and the value is the expression to compute the column value.");

}
