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
package org.febit.rectify.flink.table.factory.file;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.src.reader.SimpleStreamFormat;
import org.apache.flink.connector.file.src.reader.StreamFormat;
import org.apache.flink.core.fs.FSDataInputStream;
import org.apache.flink.table.data.RowData;
import org.apache.flink.types.Row;
import org.febit.rectify.flink.FlinkRectifier;
import org.febit.rectify.flink.table.factory.RowDataProjectConverter;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;

@RequiredArgsConstructor(staticName = "create")
public class RectifierStreamFormat extends SimpleStreamFormat<RowData> {

    @Getter
    private final FlinkRectifier<String> rectifier;
    @Getter
    private final TypeInformation<RowData> producedType;
    private final RowDataProjectConverter converter;

    @Override
    public Reader createReader(Configuration config, FSDataInputStream stream) {
        var input = new BufferedReader(new InputStreamReader(stream));
        return Reader.create(input, rectifier, converter);
    }

    @RequiredArgsConstructor(staticName = "create")
    public static final class Reader implements StreamFormat.Reader<RowData> {

        private final BufferedReader input;
        private final FlinkRectifier<String> rectifier;
        private final RowDataProjectConverter converter;

        private final ArrayDeque<RowData> buffer = new ArrayDeque<>();

        private void accept(@Nullable Row row) {
            var converted = converter.convert(row);
            if (converted != null) {
                buffer.add(converted);
            }
        }

        @Override
        public @Nullable RowData read() throws IOException {

            // Pop from buffer first, if not empty
            if (!buffer.isEmpty()) {
                return buffer.poll();
            }

            // Read lines until we find a valid one or reach the end
            while (true) {
                var line = input.readLine();
                if (line == null) {
                    return null;
                }
                this.rectifier.process(line, this::accept);
                if (!buffer.isEmpty()) {
                    return buffer.poll();
                }
            }
        }

        @Override
        public void close() throws IOException {
            input.close();
        }
    }

}
