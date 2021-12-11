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
package org.febit.rectify.impls;

import jodd.net.URLDecoder;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.febit.lang.Defaults;
import org.febit.rectify.SourceFormat;
import org.febit.rectify.util.IndexedArrayBag;
import org.febit.util.StringWalker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class AccessLogSourceFormat implements SourceFormat<String, Object> {

    // settings
    @Setter
    protected List<String> keys;
    @Setter
    protected List<String> encodedColumns;

    // internal
    private IndexedArrayBag.Indexer indexer;
    private int[] encodedIndexes;

    private static String safeDecodeQuery(String src) {
        if (StringUtils.isEmpty(src)) {
            return src;
        }
        try {
            return URLDecoder.decodeQuery(src, "UTF-8");
        } catch (Exception e) {
            return src;
        }
    }

    private static String fixAccessLogValue(String value) {
        if (value == null
                || value.equals("-")) {
            return null;
        }
        return value;
    }

    static String[] parseAccessLog(String src) {
        if (StringUtils.isEmpty(src)) {
            return Defaults.EMPTY_STRINGS;
        }

        List<String> values = new ArrayList<>();
        StringWalker walker = new StringWalker(src);
        walker.skipSpaces();

        while (!walker.isEnd()) {
            switch (walker.peek()) {
                case '[':
                    walker.jump(1);
                    values.add(fixAccessLogValue(walker.readTo(']', false)));
                    break;
                case '"':
                    walker.jump(1);
                    values.add(fixAccessLogValue(walker.readTo('"', false)));
                    break;
                default:
                    values.add(fixAccessLogValue(walker.readTo(' ', false)));
            }
            walker.skipSpaces();
        }
        return values.toArray(new String[0]);
    }

    public void init() {
        Objects.requireNonNull(keys, "AccessLogSourceFormat: keys is required");
        this.indexer = IndexedArrayBag.buildIndexer(keys);
        // resolve encoding columns
        if (encodedColumns != null) {
            encodedIndexes = new int[encodedColumns.size()];
            for (int i = 0; i < encodedColumns.size(); i++) {
                encodedIndexes[i] = indexer.getIndex(encodedColumns.get(i));
            }
        } else {
            encodedIndexes = Defaults.emptyInts();
        }
    }

    @Override
    public void process(String input, Consumer<Object> collector) {
        if (StringUtils.isEmpty(input)) {
            return;
        }
        String[] values = parseAccessLog(input);
        if (values.length == 0) {
            return;
        }
        decode(values);
        collector.accept(IndexedArrayBag.of(indexer, values));
    }

    private void decode(String[] values) {
        int max = values.length - 1;
        for (int index : encodedIndexes) {
            if (index > max) {
                continue;
            }
            values[index] = safeDecodeQuery(values[index]);
        }
    }
}
