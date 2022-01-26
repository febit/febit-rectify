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

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

import static org.febit.rectify.Schemas.NOT_A_STRUCT;

/**
 * Schema.
 */
public interface Schema extends Serializable {

    static Schema parse(String str) {
        return parse(null, null, str);
    }

    static Schema parse(String space, String name, String str) {
        return Schemas.parse(space, name, str);
    }

    static Schema parseLinesAsStruct(String name, String... lines) {
        return Schemas.parseLinesAsStruct(name, lines);
    }

    Type getType();

    default boolean isType(Type type) {
        return getType().equals(type);
    }

    default boolean isBooleanType() {
        return isType(Type.BOOLEAN);
    }

    default boolean isBytesType() {
        return isType(Type.BYTES);
    }

    default boolean isIntType() {
        return isType(Type.INT);
    }

    default boolean isBigintType() {
        return isType(Type.BIGINT);
    }

    default boolean isFloatType() {
        return isType(Type.FLOAT);
    }

    default boolean isDoubleType() {
        return isType(Type.DOUBLE);
    }

    default boolean isStringType() {
        return isType(Type.STRING);
    }

    default boolean isOptionalType() {
        return isType(Type.OPTIONAL);
    }

    default boolean isArrayType() {
        return isType(Type.ARRAY);
    }

    default boolean isMapType() {
        return isType(Type.MAP);
    }

    default boolean isStructType() {
        return isType(Type.STRUCT);
    }

    default String comment() {
        return null;
    }

    default String name() {
        return getType().getName();
    }

    default String fullname() {
        return name();
    }

    /**
     * If this is a struct, returns the fields in lines string.
     */
    default String toFieldLinesString() {
        throw new UnsupportedOperationException(NOT_A_STRUCT + this);
    }

    /**
     * If this is a struct, enum or fixed, returns its namespace.
     */
    default String namespace() {
        throw new UnsupportedOperationException("Not a named type: " + this);
    }

    /**
     * If this is a struct, returns the Field with the given name.
     *
     * @param fieldname field name
     * @return field
     */
    default Field field(String fieldname) {
        throw new UnsupportedOperationException(NOT_A_STRUCT + this);
    }

    /**
     * If this is a struct, returns the fields in it.
     */
    default List<Field> fields() {
        throw new UnsupportedOperationException(NOT_A_STRUCT + this);
    }

    /**
     * If this is a struct, returns the fields size.
     */
    default int fieldSize() {
        throw new UnsupportedOperationException(NOT_A_STRUCT + this);
    }

    /**
     * If this is an array, map or optional, returns its value type.
     */
    default Schema valueType() {
        throw new UnsupportedOperationException("Not an array, map or optional: " + this);
    }

    enum Type {

        OPTIONAL,
        STRUCT,
        ARRAY,
        MAP,

        STRING,
        BYTES,
        BOOLEAN,
        INT,
        BIGINT,
        FLOAT,
        DOUBLE,
        ;

        @Getter
        private final String name;

        Type() {
            this.name = this.name().toLowerCase();
        }
    }

    interface Field extends Serializable {
        int pos();

        String name();

        Schema schema();

        String comment();
    }

}
