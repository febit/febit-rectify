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
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.ArrayType;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.MapType;
import org.apache.flink.table.types.logical.RowType;
import org.febit.lang.modeler.Schema;
import org.febit.lang.modeler.SchemaType;
import org.febit.lang.modeler.Schemas;

@UtilityClass
public class TableTypeUtils {

    public static RowType toRowType(DataType physical) {
        var logicalType = physical.getLogicalType();
        if (!(logicalType instanceof RowType rowType)) {
            throw new IllegalArgumentException("Only ROW physical data types are supported: " + logicalType);
        }
        return rowType;
    }

    public static Schema toSchema(LogicalType logicalType) {
        var schema = switch (logicalType.getTypeRoot()) {
            case CHAR, VARCHAR -> Schemas.ofPrimitive(SchemaType.STRING);
            case BOOLEAN -> Schemas.ofPrimitive(SchemaType.BOOLEAN);
            case TINYINT, SMALLINT -> Schemas.ofPrimitive(SchemaType.SHORT);
            case INTEGER -> Schemas.ofPrimitive(SchemaType.INT);
            case BIGINT -> Schemas.ofPrimitive(SchemaType.LONG);
            case FLOAT -> Schemas.ofPrimitive(SchemaType.FLOAT);
            case DOUBLE -> Schemas.ofPrimitive(SchemaType.DOUBLE);
            case DATE -> Schemas.ofPrimitive(SchemaType.DATE);
            case TIME_WITHOUT_TIME_ZONE -> Schemas.ofPrimitive(SchemaType.TIME);
            case TIMESTAMP_WITHOUT_TIME_ZONE -> Schemas.ofPrimitive(SchemaType.DATETIME);
            case TIMESTAMP_WITH_TIME_ZONE -> Schemas.ofPrimitive(SchemaType.DATETIME_ZONED);
            case ARRAY -> Schemas.ofArray(toSchema(((ArrayType) logicalType).getElementType()));
            case MAP -> {
                var mapType = (MapType) logicalType;
                yield Schemas.ofMap(
                        toSchema(mapType.getKeyType()),
                        toSchema(mapType.getValueType())
                );
            }
            case ROW -> {
                var rowType = (RowType) logicalType;
                var builder = Schemas.newStruct();
                for (var field : rowType.getFields()) {
                    builder.field(field.getName(), toSchema(field.getType()));
                }
                yield builder.build();
            }
            case BINARY, VARBINARY, DECIMAL,
                 TIMESTAMP_WITH_LOCAL_TIME_ZONE,
                 INTERVAL_YEAR_MONTH, INTERVAL_DAY_TIME,
                 MULTISET, DISTINCT_TYPE, STRUCTURED_TYPE, NULL, RAW, SYMBOL,
                 UNRESOLVED, DESCRIPTOR, VARIANT -> throw new IllegalArgumentException(
                    "Unsupported table field type for rectifier: " + logicalType.asSummaryString());
        };
        return logicalType.isNullable()
                ? Schemas.ofOptional(schema)
                : schema;
    }
}
