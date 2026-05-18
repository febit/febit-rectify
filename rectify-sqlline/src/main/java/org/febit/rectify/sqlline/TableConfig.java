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
package org.febit.rectify.sqlline;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import org.febit.lang.util.JacksonUtils;
import org.febit.rectify.RectifierSettings;
import org.jspecify.annotations.Nullable;

import java.io.Reader;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record TableConfig(
        @lombok.NonNull
        String name,
        @lombok.NonNull
        Source source,

        @Nullable
        List<String> setups,
        @Nullable
        List<String> filters,
        @Nullable
        List<Column> columns
) implements Serializable {

    @Jacksonized
    @lombok.Builder(
            builderClassName = "Builder"
    )
    public record Source(
            @lombok.NonNull
            String path,
            @lombok.NonNull
            SourceFormatConfig format
    ) implements Serializable {
    }

    @Jacksonized
    @lombok.Builder(
            builderClassName = "Builder"
    )
    public record SourceFormatConfig(
            @lombok.NonNull
            String kind,
            @Singular
            Map<String, Object> options
    ) implements Serializable {

        public static class Builder {

            @JsonCreator
            public Builder() {
            }

            @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
            public Builder(String kind) {
                kind(kind);
            }
        }
    }

    @lombok.Builder(
            builderClassName = "Builder"
    )
    public record Column(
            @lombok.NonNull
            String name,
            @lombok.NonNull
            String type,
            @Nullable
            String expr,
            @Nullable
            String validation
    ) implements Serializable {

        public RectifierSettings.Field toProperty() {
            return RectifierSettings.Field.builder()
                    .name(name)
                    .type(type)
                    .expr(expr)
                    .validation(validation)
                    .build();
        }
    }

    public static TableConfig fromYaml(Reader reader) {
        var conf = JacksonUtils.yaml().parse(reader, TableConfig.class);
        Objects.requireNonNull(conf);
        return conf;
    }

    public static TableConfig fromYaml(String yaml) {
        var conf = JacksonUtils.yaml().parse(yaml, TableConfig.class);
        Objects.requireNonNull(conf);
        return conf;
    }

    public RectifierSettings toRectifierSettings() {
        var builder = RectifierSettings.builder()
                .name(name);
        if (setups != null) {
            setups.forEach(builder::setup);
        }
        if (filters != null) {
            filters.forEach(builder::filter);
        }
        if (columns != null) {
            for (var column : columns) {
                builder.field(column.toProperty());
            }
        }
        return builder.build();
    }
}
