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
package org.febit.rectify.flink.table;

import org.apache.flink.api.common.io.GenericInputFormat;
import org.apache.flink.api.common.io.InputFormat;
import org.apache.flink.api.common.io.NonParallelInput;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.format.DecodingFormat;
import org.apache.flink.table.connector.format.ProjectableDecodingFormat;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.InputFormatProvider;
import org.apache.flink.table.connector.source.ScanTableSource;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.factories.DeserializationFormatFactory;
import org.apache.flink.table.factories.DynamicTableSourceFactory;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.flink.table.types.DataType;

import java.io.IOException;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

public class TestingBytesTableSourceFactory implements DynamicTableSourceFactory {

    static final String IDENTIFIER = "testing-bytes";

    private static final ConfigOption<String> DATA = ConfigOptions.key("data")
            .stringType()
            .noDefaultValue()
            .withDescription("Newline-delimited raw input payloads for tests.");

    @Override
    public DynamicTableSource createDynamicTableSource(Context context) {
        var helper = FactoryUtil.createTableFactoryHelper(this, context);
        var decodingFormat = helper.discoverDecodingFormat(DeserializationFormatFactory.class, FactoryUtil.FORMAT);
        helper.validate();

        return new TestingBytesTableSource(
                decodingFormat,
                context.getPhysicalRowDataType(),
                parseMessages(helper.getOptions().get(DATA))
        );
    }

    private static List<byte[]> parseMessages(String data) {
        return data.lines()
                .filter(line -> !line.isBlank())
                .map(line -> line.getBytes(StandardCharsets.UTF_8))
                .toList();
    }

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Set<ConfigOption<?>> requiredOptions() {
        return Set.of(FactoryUtil.FORMAT, DATA);
    }

    @Override
    public Set<ConfigOption<?>> optionalOptions() {
        return Set.of();
    }

    static final class TestingBytesTableSource implements ScanTableSource {

        private final DecodingFormat<DeserializationSchema<RowData>> decodingFormat;
        private final DataType physicalDataType;
        private final List<byte[]> messages;

        private TestingBytesTableSource(
                DecodingFormat<DeserializationSchema<RowData>> decodingFormat,
                DataType physicalDataType,
                List<byte[]> messages
        ) {
            this.decodingFormat = decodingFormat;
            this.physicalDataType = physicalDataType;
            this.messages = messages;
        }

        @Override
        public ChangelogMode getChangelogMode() {
            return decodingFormat.getChangelogMode();
        }

        @Override
        public ScanRuntimeProvider getScanRuntimeProvider(ScanContext runtimeProviderContext) {
            var deserializer = createRuntimeDecoder(runtimeProviderContext);
            return new TestingScanRuntimeProvider(deserializer, messages);
        }

        private DeserializationSchema<RowData> createRuntimeDecoder(ScanContext runtimeProviderContext) {
            if (decodingFormat instanceof ProjectableDecodingFormat<DeserializationSchema<RowData>> projectableFormat) {
                return projectableFormat.createRuntimeDecoder(
                        runtimeProviderContext,
                        physicalDataType,
                        fullProjection(physicalDataType)
                );
            }
            return decodingFormat.createRuntimeDecoder(runtimeProviderContext, physicalDataType);
        }

        private static int[][] fullProjection(DataType physicalDataType) {
            var rowType = TableTypeUtils.toRowType(physicalDataType);
            return IntStream.range(0, rowType.getFieldCount())
                    .mapToObj(index -> new int[]{index})
                    .toArray(int[][]::new);
        }

        @Override
        public DynamicTableSource copy() {
            return new TestingBytesTableSource(decodingFormat, physicalDataType, messages);
        }

        @Override
        public String asSummaryString() {
            return IDENTIFIER;
        }
    }

    record TestingScanRuntimeProvider(
            DeserializationSchema<RowData> deserializer,
            List<byte[]> messages
    ) implements ScanTableSource.ScanRuntimeProvider, InputFormatProvider {

        @Override
        public boolean isBounded() {
            return true;
        }

        @Override
        public InputFormat<RowData, ?> createInputFormat() {
            return new TestingInputFormat(deserializer, messages);
        }

        @Override
        public Optional<Integer> getParallelism() {
            return Optional.of(1);
        }
    }

    private static final class TestingInputFormat extends GenericInputFormat<RowData>
            implements NonParallelInput {

        @Serial
        private static final long serialVersionUID = 1L;

        private final DeserializationSchema<RowData> deserializer;
        private final List<byte[]> messages;

        private transient int nextIndex;
        private transient RowData pendingRow;

        private TestingInputFormat(
                DeserializationSchema<RowData> deserializer,
                List<byte[]> messages
        ) {
            this.deserializer = deserializer;
            this.messages = messages;
        }

        @Override
        public void openInputFormat() throws IOException {
            try {
                deserializer.open(new TableTestSupport.TestInitializationContext());
            } catch (Exception ex) {
                throw new IOException("Failed to open test deserializer.", ex);
            }
            nextIndex = 0;
            pendingRow = null;
        }

        @Override
        public boolean reachedEnd() throws IOException {
            if (pendingRow != null) {
                return false;
            }
            while (nextIndex < messages.size()) {
                var row = deserializer.deserialize(messages.get(nextIndex++));
                if (row != null) {
                    pendingRow = row;
                    return false;
                }
            }
            return true;
        }

        @Override
        public RowData nextRecord(RowData reuse) throws IOException {
            if (reachedEnd()) {
                return null;
            }
            var result = pendingRow;
            pendingRow = null;
            return result;
        }
    }
}

