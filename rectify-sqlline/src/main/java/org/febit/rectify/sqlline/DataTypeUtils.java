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

import lombok.experimental.UtilityClass;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.type.SqlTypeName;
import org.febit.lang.modeler.Schema;

import java.util.ArrayList;

@UtilityClass
class DataTypeUtils {

    static RelDataType toDataType(Schema schema, RelDataTypeFactory typeFactory) {
        switch (schema.type()) {
            case OPTIONAL:
                return typeFactory.createTypeWithNullability(
                        toDataType(schema.valueType(), typeFactory),
                        true
                );
            case STRUCT:
                var fields = schema.fields();
                var fieldNames = new ArrayList<String>(fields.size());
                var fieldTypes = new ArrayList<RelDataType>(fields.size());
                for (var field : fields) {
                    fieldNames.add(field.name());
                    fieldTypes.add(toDataType(field.schema(), typeFactory));
                }
                return typeFactory.createStructType(fieldTypes, fieldNames);
            case ARRAY:
                return typeFactory.createArrayType(
                        toDataType(schema.valueType(), typeFactory),
                        -1
                );
            case MAP:
                return typeFactory.createMapType(
                        typeFactory.createSqlType(SqlTypeName.VARCHAR),
                        toDataType(schema.valueType(), typeFactory)
                );
            case STRING:
                return typeFactory.createSqlType(SqlTypeName.VARCHAR);
            case BYTES:
                return typeFactory.createSqlType(SqlTypeName.BINARY);
            case BOOLEAN:
                return typeFactory.createSqlType(SqlTypeName.BOOLEAN);
            case INT:
                return typeFactory.createSqlType(SqlTypeName.INTEGER);
            case LONG:
                return typeFactory.createSqlType(SqlTypeName.BIGINT);
            case FLOAT:
                return typeFactory.createSqlType(SqlTypeName.FLOAT);
            case DOUBLE:
                return typeFactory.createSqlType(SqlTypeName.DOUBLE);
            case DATE:
                return typeFactory.createSqlType(SqlTypeName.DATE);
            case TIME:
                return typeFactory.createSqlType(SqlTypeName.TIME);
            case INSTANT:
            case DATETIME:
                return typeFactory.createSqlType(SqlTypeName.TIMESTAMP);
            case DATETIME_ZONED:
                return typeFactory.createSqlType(SqlTypeName.TIMESTAMP_WITH_LOCAL_TIME_ZONE);
            default:
                throw new IllegalArgumentException("Unsupported type: " + schema.type());
        }
    }
}
