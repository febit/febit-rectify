/**
 * Copyright 2018-present febit.org (support@febit.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.rectify.sqlline;

import lombok.Data;
import lombok.val;
import org.febit.rectify.RectifierConf;

import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 * @author zqq90
 */
@Data
public class TableConfig {

    private String name;
    private String source;
    private String sourceFormat;
    private Map<String, String> sourceFormatProps;
    private List<String> globalCodes;
    private List<RectifierConf.Column> columns;

    public static TableConfig fromYaml(Reader reader) {
        return JacksonYamlUtils.parse(reader, TableConfig.class);
    }

    public static TableConfig fromYaml(String yaml) {
        return JacksonYamlUtils.parse(yaml, TableConfig.class);
    }

    public RectifierConf toRectifierConf() {
        val builder = RectifierConf.builder()
                .name(name)
                .sourceFormat(sourceFormat);
        if (sourceFormatProps != null) {
            builder.sourceFormatProps(sourceFormatProps);
        }
        if (globalCodes != null) {
            builder.globalCodes(globalCodes);
        }
        if (columns != null) {
            builder.columns(columns);
        }
        return builder.build();
    }
}
