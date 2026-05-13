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

import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.sql.type.SqlTypeName;
import org.febit.lang.modeler.SchemaType;
import org.febit.lang.modeler.Schemas;
import org.junit.jupiter.api.Test;

import static org.febit.rectify.sqlline.DataTypeUtils.toDataType;
import static org.junit.jupiter.api.Assertions.*;

class DataTypeUtilsTest {

    private final JavaTypeFactoryImpl typeFactory = new JavaTypeFactoryImpl();

    @Test
    void toDataTypePrimitive() {

        // simple scalar types
        assertEquals(SqlTypeName.VARCHAR, toDataType(Schemas.ofPrimitive(SchemaType.STRING), typeFactory).getSqlTypeName());
        assertEquals(SqlTypeName.BINARY, toDataType(Schemas.ofPrimitive(SchemaType.BYTES), typeFactory).getSqlTypeName());
        assertEquals(SqlTypeName.BOOLEAN, toDataType(Schemas.ofPrimitive(SchemaType.BOOLEAN), typeFactory).getSqlTypeName());
        assertEquals(SqlTypeName.INTEGER, toDataType(Schemas.ofPrimitive(SchemaType.INT), typeFactory).getSqlTypeName());
        assertEquals(SqlTypeName.BIGINT, toDataType(Schemas.ofPrimitive(SchemaType.LONG), typeFactory).getSqlTypeName());
        assertEquals(SqlTypeName.FLOAT, toDataType(Schemas.ofPrimitive(SchemaType.FLOAT), typeFactory).getSqlTypeName());
        assertEquals(SqlTypeName.DOUBLE, toDataType(Schemas.ofPrimitive(SchemaType.DOUBLE), typeFactory).getSqlTypeName());

        // temporal types
        assertEquals(SqlTypeName.DATE, toDataType(Schemas.ofPrimitive(SchemaType.DATE), typeFactory).getSqlTypeName());
        assertEquals(SqlTypeName.TIME, toDataType(Schemas.ofPrimitive(SchemaType.TIME), typeFactory).getSqlTypeName());
        assertEquals(SqlTypeName.TIMESTAMP, toDataType(Schemas.ofPrimitive(SchemaType.DATETIME), typeFactory).getSqlTypeName());
        assertEquals(SqlTypeName.TIMESTAMP, toDataType(Schemas.ofPrimitive(SchemaType.INSTANT), typeFactory).getSqlTypeName());
        assertEquals(
                SqlTypeName.TIMESTAMP_WITH_LOCAL_TIME_ZONE,
                toDataType(Schemas.ofPrimitive(SchemaType.DATETIME_ZONED), typeFactory).getSqlTypeName()
        );
    }

    @Test
    void toDataTypeComplex() {
        var schema = Schemas.newStruct()
                .field("id", Schemas.ofPrimitive(SchemaType.LONG))
                .field("tags", Schemas.ofArray(Schemas.ofPrimitive(SchemaType.STRING)))
                .field("attrs", Schemas.ofMap(
                        Schemas.ofPrimitive(SchemaType.STRING),
                        Schemas.ofOptional(Schemas.ofPrimitive(SchemaType.INT))
                ))
                .field("detail", Schemas.ofOptional(
                        Schemas.newStruct()
                                .field("status", Schemas.ofPrimitive(SchemaType.INT))
                                .field("createdAt", Schemas.ofPrimitive(SchemaType.INSTANT))
                                .build()
                ))
                .build();

        var type = toDataType(schema, typeFactory);
        assertTrue(type.isStruct());
        assertEquals(4, type.getFieldCount());

        var id = type.getFieldList().getFirst().getType();
        assertEquals(SqlTypeName.BIGINT, id.getSqlTypeName());
        assertFalse(id.isNullable());

        var tags = type.getFieldList().get(1).getType();
        assertEquals(SqlTypeName.ARRAY, tags.getSqlTypeName());
        var tagsComponent = tags.getComponentType();
        assertNotNull(tagsComponent);
        assertEquals(SqlTypeName.VARCHAR, tagsComponent.getSqlTypeName());

        var attrs = type.getFieldList().get(2).getType();
        assertEquals(SqlTypeName.MAP, attrs.getSqlTypeName());
        var attrsKey = attrs.getKeyType();
        assertNotNull(attrsKey);
        assertEquals(SqlTypeName.VARCHAR, attrsKey.getSqlTypeName());
        var attrsValue = attrs.getValueType();
        assertNotNull(attrsValue);
        assertEquals(SqlTypeName.INTEGER, attrsValue.getSqlTypeName());
        assertTrue(attrsValue.isNullable());

        var detail = type.getFieldList().get(3).getType();
        assertTrue(detail.isNullable());
        assertTrue(detail.isStruct());
        assertEquals(SqlTypeName.INTEGER, detail.getFieldList().get(0).getType().getSqlTypeName());
        assertEquals(SqlTypeName.TIMESTAMP, detail.getFieldList().get(1).getType().getSqlTypeName());
    }

    @Test
    void toDataTypeUnsupported() {
        var byteSchema = Schemas.ofPrimitive(SchemaType.BYTE);
        var shortSchema = Schemas.ofPrimitive(SchemaType.SHORT);
        var decimalSchema = Schemas.ofPrimitive(SchemaType.DECIMAL);

        assertThrows(IllegalArgumentException.class, () -> toDataType(byteSchema, typeFactory));
        assertThrows(IllegalArgumentException.class, () -> toDataType(shortSchema, typeFactory));
        assertThrows(IllegalArgumentException.class, () -> toDataType(decimalSchema, typeFactory));
    }
}
