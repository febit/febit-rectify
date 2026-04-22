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

import lombok.experimental.UtilityClass;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.table.catalog.ObjectIdentifier;
import org.apache.flink.table.catalog.ResolvedCatalogTable;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.ScanTableSource;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.factories.DynamicTableFactory;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.types.Row;
import org.apache.flink.util.UserCodeClassLoader;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@UtilityClass
public class TableTestSupport {

    static DynamicTableFactory.Context factoryContext() {
        return new TestFactoryContext();
    }

    static List<RowData> collectRows(
            TestingBytesTableSourceFactory.TestingScanRuntimeProvider runtimeProvider
    ) throws Exception {
        var rows = new ArrayList<RowData>();
        var deserializer = runtimeProvider.deserializer();
        deserializer.open(new TestInitializationContext());
        for (var message : runtimeProvider.messages()) {
            var row = deserializer.deserialize(message);
            if (row != null) {
                rows.add(row);
            }
        }
        return rows;
    }

    static void assertRow(RowData row, TableTestData.ExpectedRow expected) {
        assertEquals(4, row.getArity());
        assertEquals(expected.id(), row.getLong(0));
        assertTrue(row.getBoolean(1));
        assertEquals(expected.status(), row.getInt(2));
        assertEquals(expected.content(), row.getString(3).toString());
    }

    static final class TestDynamicTableSourceContext implements ScanTableSource.ScanContext {

        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeInformation<T> createTypeInformation(DataType producedDataType) {
            return (TypeInformation<T>) Types.GENERIC(RowData.class);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeInformation<T> createTypeInformation(LogicalType producedLogicalType) {
            return (TypeInformation<T>) Types.GENERIC(RowData.class);
        }

        @Override
        public DynamicTableSource.DataStructureConverter createDataStructureConverter(DataType producedDataType) {
            return new TestRowDataConverter((RowType) producedDataType.getLogicalType());
        }
    }

    static final class TestInitializationContext implements DeserializationSchema.InitializationContext {

        @Override
        public MetricGroup getMetricGroup() {
            return null;
        }

        @Override
        public UserCodeClassLoader getUserCodeClassLoader() {
            return new UserCodeClassLoader() {
                @Override
                public ClassLoader asClassLoader() {
                    return getClass().getClassLoader();
                }

                @Override
                public void registerReleaseHookIfAbsent(String releaseHookName, Runnable releaseHook) {
                    // no-op for tests
                }
            };
        }
    }

    private static final class TestFactoryContext implements DynamicTableFactory.Context {

        @Override
        public ObjectIdentifier getObjectIdentifier() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResolvedCatalogTable getCatalogTable() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ReadableConfig getConfiguration() {
            return new Configuration();
        }

        @Override
        public ClassLoader getClassLoader() {
            return getClass().getClassLoader();
        }

        @Override
        public boolean isTemporary() {
            return true;
        }
    }

    private record TestRowDataConverter(RowType rowType) implements DynamicTableSource.DataStructureConverter {

        @Override
        public void open(Context context) {
            // no-op for tests
        }

        @Override
        public Object toInternal(Object externalStructure) {
            return toRowData((Row) externalStructure, rowType);
        }

        private static GenericRowData toRowData(Row row, RowType rowType) {
            var result = new GenericRowData(row.getKind(), row.getArity());
            for (int i = 0; i < row.getArity(); i++) {
                result.setField(i, toInternalValue(row.getField(i), rowType.getTypeAt(i)));
            }
            return result;
        }

        private static Object toInternalValue(Object value, LogicalType logicalType) {
            if (value == null) {
                return null;
            }
            return switch (logicalType.getTypeRoot()) {
                case CHAR, VARCHAR -> StringData.fromString((String) value);
                case BOOLEAN -> value;
                case TINYINT -> ((Number) value).byteValue();
                case SMALLINT -> ((Number) value).shortValue();
                case INTEGER -> ((Number) value).intValue();
                case BIGINT -> ((Number) value).longValue();
                case FLOAT -> ((Number) value).floatValue();
                case DOUBLE -> ((Number) value).doubleValue();
                case ROW -> toRowData((Row) value, (RowType) logicalType);
                default -> throw new IllegalArgumentException(
                        "Unsupported logical type in test converter: " + logicalType.asSummaryString());
            };
        }
    }
}
