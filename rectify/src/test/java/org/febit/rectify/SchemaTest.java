/**
 * Copyright 2018-present febit.org (support@febit.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.rectify;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.febit.rectify.Schema.Type.BIGINT;
import static org.febit.rectify.Schema.Type.BOOLEAN;
import static org.febit.rectify.Schema.Type.BYTES;
import static org.febit.rectify.Schema.Type.DOUBLE;
import static org.febit.rectify.Schema.Type.FLOAT;
import static org.febit.rectify.Schema.Type.INT;
import static org.febit.rectify.Schema.Type.STRING;
import static org.febit.rectify.Schema.forArray;
import static org.febit.rectify.Schema.forMap;
import static org.febit.rectify.Schema.forOptional;
import static org.febit.rectify.Schema.forPrimitive;
import static org.febit.rectify.Schema.forStruct;
import static org.febit.rectify.Schema.newField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({
        "squid:S1192" // String literals should not be duplicated
})
class SchemaTest {

    @Test
    void testParseAsFieldList() {
        Schema schema = TestSchemas.COMPLEX;

        Schema.Field field;

        assertEquals("demo", schema.name());
        assertEquals(11, schema.fieldSize());
        assertTrue(schema.isStructType());
        assertNull(schema.comment());

        field = schema.getField("id");
        assertTrue(field.schema().isIntType());
        assertNull(schema.comment());
        assertEquals(0, field.pos());

        field = schema.getField("name");
        assertTrue(field.schema().isStringType());
        assertEquals(1, field.pos());

        field = schema.getField("ints");
        assertEquals(2, field.pos());
        assertNull(field.comment());
        assertTrue(field.schema().isArrayType());
        assertTrue(field.schema().valueType().isIntType());

        assertTrue(schema.getField("float").schema().isFloatType());
        assertTrue(schema.getField("double").schema().isDoubleType());

        field = schema.getField("strings");
        assertEquals(5, field.pos());
        assertEquals("comment test", field.comment());
        assertTrue(field.schema().isArrayType());
        assertTrue(field.schema().valueType().isStringType());

        field = schema.getField("longMap");
        assertEquals(6, field.pos());
        assertNull(field.comment());
        assertTrue(field.schema().isMapType());
        assertTrue(field.schema().valueType().isBigintType());

        field = schema.getField("stringMap");
        assertEquals(7, field.pos());
        assertTrue(field.schema().isMapType());
        assertTrue(field.schema().valueType().isStringType());

        field = schema.getField("optionalStringMap");
        assertEquals(8, field.pos());
        assertTrue(field.schema().isOptionalType());
        assertTrue(field.schema().valueType().isMapType());
        assertTrue(field.schema().valueType().valueType().isStringType());

        field = schema.getField("session");
        assertEquals(9, field.pos());
        assertTrue(field.schema().isStructType());
        assertEquals(4, field.schema().fieldSize());
        assertTrue(field.schema().getField("id").schema().isStringType());
        assertTrue(field.schema().getField("launch").schema().isBigintType());
        assertTrue(field.schema().getField("du").schema().isBigintType());
        assertTrue(field.schema().getField("date").schema().isIntType());

        field = schema.getField("events");
        assertEquals(10, field.pos());
        assertTrue(field.schema().isArrayType());

        Schema eventSchema = field.schema().valueType();
        assertTrue(eventSchema.isStructType());
        assertEquals("demo._col10.item", eventSchema.fullname());

        assertTrue(eventSchema.getField("name").schema().isStringType());
        assertEquals(1, eventSchema.getField("name").pos());

        assertTrue(eventSchema.getField("attrs").schema().isMapType());
        assertEquals(3, eventSchema.getField("attrs").pos());

        assertTrue(eventSchema.getField("du").schema().isBigintType());
        assertEquals(0, eventSchema.getField("du").pos());

        assertTrue(eventSchema.getField("ts").schema().isOptionalType());
        assertTrue(eventSchema.getField("ts").schema().valueType().isBigintType());
        assertEquals(2, eventSchema.getField("ts").pos());

        assertTrue(eventSchema.getField("struct").schema().isStructType());
        assertEquals(4, eventSchema.getField("struct").pos());
        assertEquals("demo._col10.item", eventSchema.getField("struct").schema().namespace());
        assertEquals("demo._col10.item.struct", eventSchema.getField("struct").schema().fullname());

        assertTrue(eventSchema.getField("flag").schema().isBooleanType());
        assertEquals(5, eventSchema.getField("flag").pos());

        assertEquals(
                eventSchema.getField("flag"),
                schema.fields().get(10).schema() // event array
                        .valueType() // event type
                        .fields().get(5) // flag
        );
    }

    @Test
    void testToString() {
        Schema schema;

        assertEquals("int", forPrimitive(INT).toString());
        assertEquals("bigint", forPrimitive(BIGINT).toString());
        assertEquals("boolean", forPrimitive(BOOLEAN).toString());
        assertEquals("bytes", forPrimitive(BYTES).toString());
        assertEquals("string", forPrimitive(STRING).toString());
        assertEquals("float", forPrimitive(FLOAT).toString());
        assertEquals("double", forPrimitive(DOUBLE).toString());

        assertEquals("optional<double>", forOptional(forPrimitive(DOUBLE)).toString());
        assertEquals("array<int>", forArray(forPrimitive(INT)).toString());
        assertEquals("map<string>", forMap(forPrimitive(STRING)).toString());

        schema = forStruct("org.febit", "demo",
                Arrays.asList(
                        newField("id", forPrimitive(STRING), "ID"),
                        newField("ints", forArray(forPrimitive(INT))),
                        newField("optionalStringMap", forOptional(forMap(forPrimitive(STRING)))),
                        newField("complex", forStruct("febit.demo", "complex",
                                Arrays.asList(
                                        newField("intMap", forMap(forPrimitive(INT))),
                                        newField("name", forPrimitive(STRING))
                                ),
                                "Complex"))
                ),
                "this is a demo"
        );

        assertEquals(
                "struct<"
                        + "id:string,"
                        + "ints:array<int>,"
                        + "optionalStringMap:optional<map<string>>,"
                        + "complex:struct<intMap:map<int>,name:string>"
                        + ">",
                schema.toString()
        );

        assertEquals(
                "string id #ID\n" +
                        "array<int> ints\n" +
                        "optional<map<string>> optionalStringMap\n" +
                        "struct<intMap:map<int>,name:string> complex\n",
                schema.toFieldLinesString()
        );
    }
}
