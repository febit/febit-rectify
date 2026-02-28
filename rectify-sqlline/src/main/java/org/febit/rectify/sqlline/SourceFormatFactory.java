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

import lombok.experimental.UtilityClass;
import org.febit.lang.util.JacksonUtils;
import org.febit.rectify.SourceFormat;
import org.febit.rectify.format.AccessLogSourceFormat;
import org.febit.rectify.format.JsonSourceFormat;

import java.util.Objects;

@UtilityClass
public class SourceFormatFactory {

    public static SourceFormat<String, Object> create(
            TableSettings.Source source) {
        return switch (source.format()) {
            case "json" -> new JsonSourceFormat();
            case "access" -> {
                var options = JacksonUtils.to(source.properties(), AccessLogSourceFormat.Options.class);
                Objects.requireNonNull(options, "Properties is required for access log format");
                yield AccessLogSourceFormat.create(options);
            }
            default -> throw new IllegalArgumentException("Unsupported format: " + source.format());
        };
    }

}
