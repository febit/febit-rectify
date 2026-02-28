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

import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.util.Sources;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
class RectifySchema extends AbstractSchema {
    private final File directoryFile;

    @Nullable
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
        var files = directoryFile.listFiles((dir, name) -> name.endsWith(".rectify.yml"));
        if (files == null) {
            log.warn("Directory not found: {}", directoryFile);
            files = new File[0];
        }
        var map = new HashMap<String, Table>();
        for (var file : files) {
            TableSettings config;
            try {
                config = TableSettings.fromYaml(Sources.of(file).reader());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            var table = createTable(config);
            map.put(config.name(), table);
        }
        return Collections.unmodifiableMap(map);
    }

    private Table createTable(TableSettings settings) {
        var source = Sources.of(new File(directoryFile, settings.path()));
        return new RectifyTable(source, settings.toRectifierSettings(), settings.createSourceFormat());
    }
}
