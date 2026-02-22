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

import lombok.Data;
import org.febit.lang.util.JacksonUtils;
import org.febit.rectify.RectifierConf;
import org.febit.rectify.SourceFormat;
import org.jspecify.annotations.Nullable;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
public class TableConfig {

    private String name;
    private String source;
    private String sourceFormat;
    private Map<String, Object> sourceFormatProps;

    @Nullable
    private List<String> globalCodes;
    @Nullable
    private List<RectifierConf.Column> columns;

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

    public SourceFormat<String, Object> createSourceFormat() {
        return SourceFormatProvider.create(source, sourceFormatProps);
    }

    public RectifierConf toRectifierConf() {
        var conf = RectifierConf.create()
                .name(name);
        if (globalCodes != null) {
            conf.frontSegments(globalCodes);
        }
        if (columns != null) {
            conf.columns(columns);
        }
        return conf;
    }
}
