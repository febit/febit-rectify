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
