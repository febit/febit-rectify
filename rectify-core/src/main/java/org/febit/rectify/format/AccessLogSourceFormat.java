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
package org.febit.rectify.format;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;
import org.febit.lang.util.StringWalker;
import org.febit.rectify.SourceFormat;
import org.febit.rectify.util.IndexedArray;
import org.febit.rectify.util.Indexer;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessLogSourceFormat implements SourceFormat<String, Object> {

    private final Indexer<String> indexer;

    public static AccessLogSourceFormat create(Options options) {
        var columns = options.columns();
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Columns is required to create a AccessLogSourceFormat");
        }
        var indexer = Indexer.of(columns);
        return new AccessLogSourceFormat(indexer);
    }

    @Jacksonized
    @lombok.Builder(
            builderClassName = "Builder",
            toBuilder = true
    )
    public record Options(
            @Singular
            List<String> columns
    ) implements Serializable {
    }

    @Nullable
    private static String unescape(@Nullable String value) {
        if (value == null
                || value.equals("-")) {
            return null;
        }
        return value;
    }

    public static @Nullable String[] parse(String src) {
        if (StringUtils.isEmpty(src)) {
            return new String[0];
        }

        var values = new ArrayList<@Nullable String>();
        var walker = new StringWalker(src);
        walker.skipSpaces();

        while (!walker.isEnd()) {
            switch (walker.peek()) {
                case '[':
                    walker.jump(1);
                    values.add(unescape(walker.readTo(']', false)));
                    break;
                case '"':
                    walker.jump(1);
                    values.add(unescape(walker.readTo('"', false)));
                    break;
                default:
                    values.add(unescape(walker.readTo(' ', false)));
            }
            walker.skipSpaces();
        }
        return values.toArray(new String[0]);
    }

    @Override
    public void process(@Nullable String input, Consumer<Object> sink) {
        if (StringUtils.isEmpty(input)) {
            return;
        }
        var values = parse(input);
        if (values.length == 0) {
            return;
        }
        sink.accept(IndexedArray.of(indexer, values));
    }
}
