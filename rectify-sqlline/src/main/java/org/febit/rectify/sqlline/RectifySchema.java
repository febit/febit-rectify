package org.febit.rectify.sqlline;

import com.google.common.collect.ImmutableMap;
import lombok.val;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.util.Sources;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * @author zqq90
 */
class RectifySchema extends AbstractSchema {
    private final File directoryFile;
    private Map<String, Table> tableMap;

    RectifySchema(File directoryFile) {
        this.directoryFile = directoryFile;
    }

    @Override
    protected Map<String, Table> getTableMap() {
        if (tableMap == null) {
            tableMap = createTableMap();
        }
        return tableMap;
    }

    private Map<String, Table> createTableMap() {
        File[] files = directoryFile.listFiles((dir, name) -> name.endsWith(".rectify.yml"));
        if (files == null) {
            System.out.println("directory " + directoryFile + " not found");
            files = new File[0];
        }
        final ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();
        for (File file : files) {
            TableConfig config;
            try {
                config = TableConfig.fromYaml(Sources.of(file).reader());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            val table = createTable(config);
            builder.put(config.getName(), table);
        }
        return builder.build();
    }

    private Table createTable(TableConfig config) {
        val source = Sources.of(new File(directoryFile, config.getSource()));
        return new RectifyTable(source, config.toRectifierConf());
    }
}
