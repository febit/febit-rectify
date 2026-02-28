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
import org.febit.rectify.SourceFormat;
import org.jspecify.annotations.Nullable;

import java.io.Reader;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record TableSettings(
        @lombok.NonNull
        @SuppressWarnings("NullableProblems")
        String name,

        @lombok.NonNull
        @SuppressWarnings("NullableProblems")
        String path,

        @lombok.NonNull
        @SuppressWarnings("NullableProblems")
        Source source,

        @Nullable
        List<String> setups,
        @Nullable
        List<RectifierSettings.Column> columns
) implements Serializable {

    @Jacksonized
    @lombok.Builder(
            builderClassName = "Builder"
    )
    public record Source(
            @lombok.NonNull
            @SuppressWarnings("NullableProblems")
            String format,
            @Singular
            Map<String, Object> properties
    ) implements Serializable {

        public static class Builder {

            @JsonCreator
            public Builder() {
            }

            @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
            public Builder(String format) {
                format(format);
            }
        }
    }

    public static TableSettings fromYaml(Reader reader) {
        var conf = JacksonUtils.yaml().parse(reader, TableSettings.class);
        Objects.requireNonNull(conf);
        return conf;
    }

    public static TableSettings fromYaml(String yaml) {
        var conf = JacksonUtils.yaml().parse(yaml, TableSettings.class);
        Objects.requireNonNull(conf);
        return conf;
    }

    public SourceFormat<String, Object> createSourceFormat() {
        return SourceFormatFactory.create(source);
    }

    public RectifierSettings toRectifierSettings() {
        var builder = RectifierSettings.builder()
                .name(name);
        if (setups != null) {
            setups.forEach(builder::setup);
        }
        if (columns != null) {
            builder.columns(columns);
        }
        return builder.build();
    }
}
