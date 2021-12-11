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

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.util.Sources;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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
            log.warn("Directory not found: {}", directoryFile);
            files = new File[0];
        }
        final Map<String, Table> map = new HashMap<>();
        for (File file : files) {
            TableConfig config;
            try {
                config = TableConfig.fromYaml(Sources.of(file).reader());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            val table = createTable(config);
            map.put(config.getName(), table);
        }
        return Collections.unmodifiableMap(map);
    }

    private Table createTable(TableConfig config) {
        val source = Sources.of(new File(directoryFile, config.getSource()));
        return new RectifyTable(source, config.toRectifierConf(), config.createSourceFormat());
    }
}
