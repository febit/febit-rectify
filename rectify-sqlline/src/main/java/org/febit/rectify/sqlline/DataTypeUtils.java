package org.febit.rectify.sqlline;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.type.SqlTypeName;
import org.febit.rectify.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zqq90
 */
class DataTypeUtils {

    static RelDataType toDataType(Schema schema, RelDataTypeFactory typeFactory) {
        switch (schema.getType()) {
            case OPTIONAL:
                return typeFactory.createTypeWithNullability(
                        toDataType(schema.valueType(), typeFactory),
                        true
                );
            case STRUCT:
                List<Schema.Field> fields = schema.fields();
                List<String> fieldNames = new ArrayList<>(fields.size());
                List<RelDataType> fieldTypes = new ArrayList<>(fields.size());
                for (Schema.Field field : fields) {
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
            case BIGINT:
                return typeFactory.createSqlType(SqlTypeName.BIGINT);
            case FLOAT:
                return typeFactory.createSqlType(SqlTypeName.FLOAT);
            case DOUBLE:
                return typeFactory.createSqlType(SqlTypeName.DOUBLE);
            default:
                throw new IllegalArgumentException("Unsupported type: " + schema.getType());
        }
    }
}
