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
package org.febit.rectify;

import org.junit.jupiter.api.Test;

import static org.febit.rectify.Schema.Type.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({
        "squid:S1192" // String literals should not be duplicated
})
class SchemasTest {

    @Test
    void testParseAsFieldList() {
        Schema schema = TestSchemas.COMPLEX;

        Schema.Field field;

        assertEquals("demo", schema.name());
        assertEquals(12, schema.fieldSize());
        assertTrue(schema.isStructType());
        assertNull(schema.comment());

        field = schema.field("id");
        assertTrue(field.schema().isIntType());
        assertNull(schema.comment());
        assertEquals(0, field.pos());

        field = schema.field("name");
        assertTrue(field.schema().isStringType());
        assertEquals(1, field.pos());

        field = schema.field("ints");
        assertEquals(2, field.pos());
        assertNull(field.comment());
        assertTrue(field.schema().isArrayType());
        assertTrue(field.schema().valueType().isIntType());

        assertTrue(schema.field("float").schema().isFloatType());
        assertTrue(schema.field("double").schema().isDoubleType());

        field = schema.field("strings");
        assertEquals(5, field.pos());
        assertEquals("comment test", field.comment());
        assertTrue(field.schema().isArrayType());
        assertTrue(field.schema().valueType().isStringType());

        field = schema.field("longMap");
        assertEquals(6, field.pos());
        assertNull(field.comment());
        assertTrue(field.schema().isMapType());
        assertTrue(field.schema().valueType().isBigintType());

        field = schema.field("stringMap");
        assertEquals(7, field.pos());
        assertTrue(field.schema().isMapType());
        assertTrue(field.schema().valueType().isStringType());

        field = schema.field("optionalStringMap");
        assertEquals(8, field.pos());
        assertTrue(field.schema().isOptionalType());
        assertTrue(field.schema().valueType().isMapType());
        assertTrue(field.schema().valueType().valueType().isStringType());

        field = schema.field("session");
        assertEquals(9, field.pos());
        assertTrue(field.schema().isStructType());
        assertEquals(4, field.schema().fieldSize());
        assertTrue(field.schema().field("id").schema().isStringType());
        assertTrue(field.schema().field("launch").schema().isBigintType());
        assertTrue(field.schema().field("du").schema().isBigintType());
        assertTrue(field.schema().field("date").schema().isIntType());

        field = schema.field("events");
        assertEquals(10, field.pos());
        assertTrue(field.schema().isArrayType());

        Schema eventSchema = field.schema().valueType();
        assertTrue(eventSchema.isStructType());
        assertEquals("demo._col10.item", eventSchema.fullname());

        assertTrue(eventSchema.field("name").schema().isStringType());
        assertEquals(1, eventSchema.field("name").pos());

        assertTrue(eventSchema.field("attrs").schema().isMapType());
        assertEquals(3, eventSchema.field("attrs").pos());

        assertTrue(eventSchema.field("du").schema().isBigintType());
        assertEquals(0, eventSchema.field("du").pos());

        assertTrue(eventSchema.field("ts").schema().isOptionalType());
        assertTrue(eventSchema.field("ts").schema().valueType().isBigintType());
        assertEquals(2, eventSchema.field("ts").pos());

        assertTrue(eventSchema.field("struct").schema().isStructType());
        assertEquals(4, eventSchema.field("struct").pos());
        assertEquals("demo._col10.item", eventSchema.field("struct").schema().namespace());
        assertEquals("demo._col10.item.struct", eventSchema.field("struct").schema().fullname());

        assertTrue(eventSchema.field("flag").schema().isBooleanType());
        assertEquals(5, eventSchema.field("flag").pos());

        assertEquals(
                eventSchema.field("flag"),
                schema.fields().get(10).schema() // event array
                        .valueType() // event type
                        .fields().get(5) // flag
        );

        // Times
        field = schema.field("times");
        assertEquals(11, field.pos());
        assertTrue(field.schema().isArrayType());

        Schema timeSchema = field.schema().valueType();
        assertTrue(timeSchema.isStructType());
        assertEquals("demo._col11.item", timeSchema.fullname());

        assertTrue(timeSchema.field("time").schema().isType(TIME));
        assertTrue(timeSchema.field("date").schema().isType(DATE));
        assertTrue(timeSchema.field("dt").schema().isType(DATETIME));
        assertTrue(timeSchema.field("dtz").schema().isType(DATETIME_WITH_TIMEZONE));
        assertTrue(timeSchema.field("instant").schema().isType(INSTANT));
    }

    @Test
    void testToString() {
        Schema schema;

        assertEquals("int", Schemas.ofPrimitive(INT).toString());
        assertEquals("int64", Schemas.ofPrimitive(INT64).toString());
        assertEquals("boolean", Schemas.ofPrimitive(BOOLEAN).toString());
        assertEquals("bytes", Schemas.ofPrimitive(BYTES).toString());
        assertEquals("string", Schemas.ofPrimitive(STRING).toString());
        assertEquals("float", Schemas.ofPrimitive(FLOAT).toString());
        assertEquals("double", Schemas.ofPrimitive(DOUBLE).toString());

        assertEquals("optional<double>", Schemas.ofOptional(Schemas.ofPrimitive(DOUBLE)).toString());
        assertEquals("array<int>", Schemas.ofArray(Schemas.ofPrimitive(INT)).toString());
        assertEquals("map<string>", Schemas.ofMap(Schemas.ofPrimitive(STRING)).toString());

        schema = Schemas.structSchemaBuilder()
                .space("org.febit")
                .name("demo")
                .comment("this is a demo")
                .field("id", Schemas.ofPrimitive(STRING), "ID")
                .field("ints",
                        Schemas.ofArray(Schemas.ofPrimitive(INT))
                )
                .field("optionalStringMap",
                        Schemas.ofOptional(Schemas.ofMap(Schemas.ofPrimitive(STRING)))
                )
                .field("complex",
                        Schemas.structSchemaBuilder()
                                .space("febit.demo")
                                .name("complex")
                                .comment("Complex")
                                .field("intMap", Schemas.ofMap(Schemas.ofPrimitive(INT)))
                                .field("name", Schemas.ofPrimitive(STRING))
                                .build()
                )
                .field("times",
                        Schemas.structSchemaBuilder()
                                .space("febit.demo")
                                .name("times")
                                .comment("times")
                                .field("t", Schemas.ofPrimitive(TIME))
                                .field("d", Schemas.ofPrimitive(DATE))
                                .field("dt", Schemas.ofPrimitive(DATETIME))
                                .field("dtz", Schemas.ofPrimitive(DATETIME_WITH_TIMEZONE))
                                .field("i", Schemas.ofPrimitive(INSTANT))
                                .build()
                )
                .build();

        assertEquals(
                "struct<"
                        + "id:string,"
                        + "ints:array<int>,"
                        + "optionalStringMap:optional<map<string>>,"
                        + "complex:struct<intMap:map<int>,name:string>,"
                        + "times:struct<t:time,d:date,dt:datetime,dtz:datetimez,i:instant>"
                        + ">",
                schema.toString()
        );

        assertEquals(
                "string id #ID\n"
                        + "array<int> ints\n"
                        + "optional<map<string>> optionalStringMap\n"
                        + "struct<intMap:map<int>,name:string> complex\n"
                        + "struct<t:time,d:date,dt:datetime,dtz:datetimez,i:instant> times\n",
                schema.toFieldLinesString()
        );
    }
}
