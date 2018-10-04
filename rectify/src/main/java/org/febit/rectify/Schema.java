/**
 * Copyright 2018-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.rectify;

import org.febit.util.ArraysUtil;
import org.febit.util.StringUtil;
import org.febit.util.StringWalker;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Schema.
 *
 * @author zqq90
 */
public abstract class Schema implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final char[] LINE_BREAKERS = "\r\n".toCharArray();
    private static final char[] LINE_BREAKERS_REPLACE = "  ".toCharArray();
    private static final Pattern NAME_PATTERN = Pattern.compile("^[_a-zA-Z][_a-zA-Z0-9]{1,64}$");

    public enum Type {
        OPTIONAL, STRUCT, ARRAY, MAP,
        STRING, BYTES, BOOLEAN,
        INT, BIGINT, FLOAT, DOUBLE;

        private final String name;

        Type() {
            this.name = this.name().toLowerCase();
        }

        public String getName() {
            return name;
        }
    }

    public static Schema parseFieldLines(String name, String lines) {
        return SchemaParser.parseFieldLines(name, lines);
    }

    public static Schema parse(String str) {
        return parse(null, null, str);
    }

    public static Schema parse(String space, String name, String str) {
        return SchemaParser.parse(space, name, str);
    }

    public static Schema forPrimitive(Type type) {
        switch (type) {
            case STRING:
            case BYTES:
            case INT:
            case BIGINT:
            case FLOAT:
            case DOUBLE:
            case BOOLEAN:
                return new PrimitiveSchema(type);
            case STRUCT:
            case ARRAY:
            case MAP:
            case OPTIONAL:
            default:
                throw new IllegalArgumentException("Can't create Schema for type: " + type);
        }
    }

    public static Field newField(String name, Schema schema) {
        return newField(name, schema, null);
    }

    public static Field newField(String name, Schema schema, String comment) {
        return new Field(name, schema, comment);
    }

    public static Schema forStruct(String namespace, String name, List<Field> fields) {
        return forStruct(namespace, name, fields, null);
    }

    public static Schema forStruct(String namespace, String name, List<Field> fields, String comment) {
        return new StructSchema(namespace, name, fields, comment);
    }

    public static Schema forArray(Schema valueType) {
        return new ValuedSchema(Type.ARRAY, valueType);
    }

    public static Schema forOptional(Schema valueType) {
        return new ValuedSchema(Type.OPTIONAL, valueType);
    }

    public static Schema forMap(Schema valueType) {
        return new ValuedSchema(Type.MAP, valueType);
    }

    private static String checkName(String name) {
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Illegal name: " + name);
        }
        return name;
    }

    private static String escapeForLineComment(String remark) {
        if (remark == null) {
            return null;
        }
        return StringUtil.replaceChars(remark, LINE_BREAKERS, LINE_BREAKERS_REPLACE);
    }

    final Type type;

    Schema(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public boolean isType(Type type) {
        return this.type.equals(type);
    }

    public boolean isBooleanType() {
        return isType(Type.BOOLEAN);
    }

    public boolean isBytesType() {
        return isType(Type.BYTES);
    }

    public boolean isIntType() {
        return isType(Type.INT);
    }

    public boolean isBigintType() {
        return isType(Type.BIGINT);
    }

    public boolean isFloatType() {
        return isType(Type.FLOAT);
    }

    public boolean isDoubleType() {
        return isType(Type.DOUBLE);
    }

    public boolean isStringType() {
        return isType(Type.STRING);
    }

    public boolean isOptionalType() {
        return isType(Type.OPTIONAL);
    }

    public boolean isArrayType() {
        return isType(Type.ARRAY);
    }

    public boolean isMapType() {
        return isType(Type.MAP);
    }

    public boolean isStructType() {
        return isType(Type.STRUCT);
    }

    public String comment() {
        return null;
    }

    public String name() {
        return type.getName();
    }

    public String fullname() {
        return name();
    }

    @Override
    public String toString() {
        return type.getName();
    }

    /**
     * If this is a struct, returns the fields in lines string.
     */
    public String toFieldLinesString() {
        throw new UnsupportedOperationException("Not a struct: " + this);
    }

    /**
     * If this is a struct, enum or fixed, returns its namespace.
     */
    public String namespace() {
        throw new UnsupportedOperationException("Not a named type: " + this);
    }

    /**
     * If this is a struct, returns the Field with the given name.
     *
     * @param fieldname field name
     * @return field
     */
    public Field getField(String fieldname) {
        throw new UnsupportedOperationException("Not a struct: " + this);
    }

    /**
     * If this is a struct, returns the fields in it.
     */
    public List<Field> fields() {
        throw new UnsupportedOperationException("Not a struct: " + this);
    }

    /**
     * If this is a struct, returns the fields size.
     */
    public int fieldSize() {
        throw new UnsupportedOperationException("Not a struct: " + this);
    }

    /**
     * If this is an array, map or optional, returns its value type.
     */
    public Schema valueType() {
        throw new UnsupportedOperationException("Not an array, map or optional: " + this);
    }

    private static class PrimitiveSchema extends Schema {

        private static final long serialVersionUID = 1L;

        public PrimitiveSchema(Type type) {
            super(type);
        }
    }

    private static class ValuedSchema extends Schema {

        private static final long serialVersionUID = 1L;
        private final String tag;
        private final Schema valueType;

        ValuedSchema(Type type, Schema valueType) {
            super(type);
            this.tag = type.getName();
            this.valueType = valueType;
        }

        @Override
        public Schema valueType() {
            return valueType;
        }

        @Override
        public String toString() {
            return tag + '<' + this.valueType.toString() + '>';
        }
    }

    public static class Field implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String name;
        private final Schema schema;
        private final String comment;
        int pos = -1;

        Field(String name, Schema schema, String comment) {
            this.name = checkName(name);
            this.schema = schema;
            this.comment = escapeForLineComment(comment);
        }

        public String name() {
            return name;
        }

        public Schema schema() {
            return schema;
        }

        public String comment() {
            return comment;
        }

        public int pos() {
            return pos;
        }

        @Override
        public String toString() {
            return name + ':' + schema.toString();
        }
    }

    private static class StructSchema extends Schema {

        private static final long serialVersionUID = 1L;
        private final String name;
        private final String space;
        private final String fullname;
        private final String comment;

        private final List<Field> fields;
        private final Map<String, Field> fieldMap;

        public StructSchema(String space, String name, List<Field> fields, String comment) {
            super(Type.STRUCT);

            this.comment = escapeForLineComment(comment);

            // names
            if (name == null) {
                this.name = null;
                this.space = null;
                this.fullname = null;
            } else {
                int split = name.lastIndexOf('.');
                if (split < 0) {
                    this.name = checkName(name);
                } else {
                    space = name.substring(0, split);
                    this.name = checkName(name.substring(split + 1));
                }
                if ("".equals(space)) {
                    space = null;
                }
                this.space = space;
                this.fullname = (this.space == null)
                        ? this.name
                        : this.space + "." + this.name;
            }

            // fields
            this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
            this.fieldMap = new HashMap<>();
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                if (field.pos != -1) {
                    throw new IllegalArgumentException("Field already used: " + field);
                }
                field.pos = i;
                this.fieldMap.put(field.name(), field);
            }
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String comment() {
            return comment;
        }

        @Override
        public String namespace() {
            return space;
        }

        @Override
        public String fullname() {
            return fullname;
        }

        @Override
        public Field getField(String fieldName) {
            return fieldMap.get(fieldName);
        }

        @Override
        public List<Field> fields() {
            return fields;
        }

        @Override
        public int fieldSize() {
            return fields.size();
        }

        @Override
        public String toFieldLinesString() {
            StringBuilder buf = new StringBuilder();
            for (Field field : fields) {
                buf.append(field.schema().toString())
                        .append(' ')
                        .append(field.name());

                if (StringUtil.isNotEmpty(field.comment())) {
                    buf.append(" #")
                            .append(field.comment());
                }

                buf.append('\n');
            }
            return buf.toString();
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("struct<");
            for (Field field : fields) {
                if (field.pos() != 0) {
                    buf.append(',');
                }
                buf.append(field.name())
                        .append(':')
                        .append(field.schema());
            }
            buf.append('>');
            return buf.toString();
        }
    }

    private static class SchemaParser {

        private static final char[] TYPE_NAME_END_CHARS = "\r\n \t\f\b<>[],.:;+-=#".toCharArray();

        private static boolean isTypeNameEnding(char c) {
            return ArraysUtil.contains(TYPE_NAME_END_CHARS, c);
        }

        private static String buildNamespace(String space, String name) {
            if (name == null || name.isEmpty()) {
                return null;
            }
            if (space == null || space.isEmpty()) {
                return name;
            }
            return space + '.' + name;
        }

        private static void requireAndJumpChar(StringWalker walker, char c) {
            if (walker.isEnd()) {
                throw new RuntimeException("Unexpected EOF");
            }
            if (walker.peek() != c) {
                throw new RuntimeException(StringUtil.format(
                        "Unexpected char '{}' at {}, but need `{}` ", walker.peek(), walker.pos(), c));
            }
            walker.jump(1);
        }

        /**
         * Parse fields in lines.
         *
         * @param name schema name
         * @param lines lines
         * @return a struct schema
         */
        static Schema parseFieldLines(String name, String lines) {
            String[] linesArray = StringUtil.splitc(lines, LINE_BREAKERS);
            StringUtil.trim(linesArray);
            List<Field> fields = new ArrayList<>(linesArray.length);
            int i = 0;
            for (String line : linesArray) {
                if (line.isEmpty()
                        || line.charAt(0) == '#'
                        || line.charAt(0) == '-') {
                    continue;
                }
                Field field = parseField(name, i, line);
                fields.add(field);
                i++;
            }
            return Schema.forStruct(null, name, fields, null);
        }

        static Schema parse(String space, String name, String str) {
            StringWalker walker = new StringWalker(str);
            return readType(space, name, walker);
        }

        private static Field parseField(String space, int index, String line) {
            StringWalker walker = new StringWalker(line);
            walker.skipBlanks();
            Schema schema = readType(space, "_col" + index, walker);
            walker.skipBlanks();
            String name = walker.readUntilBlanks().trim();
            walker.skipBlanks();
            String comment = null;
            if (!walker.isEnd() && walker.peek() == '#') {
                comment = walker.readToEnd().substring(1).trim();
                if (comment.isEmpty()) {
                    comment = null;
                }
            }
            walker.skipBlanks();
            if (!walker.isEnd()) {
                throw new RuntimeException("Invalid content: " + walker.readToEnd());
            }
            return Schema.newField(name, schema, comment);
        }

        private static Schema readType(String space, String name, StringWalker walker) {
            walker.skipBlanks();
            String typeName = walker.readToFlag(SchemaParser::isTypeNameEnding, true).toLowerCase();
            switch (typeName) {
                case "int":
                    return Schema.forPrimitive(Type.INT);
                case "long":
                case "bigint":
                    return Schema.forPrimitive(Type.BIGINT);
                case "string":
                    return Schema.forPrimitive(Type.STRING);
                case "bool":
                case "boolean":
                    return Schema.forPrimitive(Type.BOOLEAN);
                case "bytes":
                    return Schema.forPrimitive(Type.BYTES);
                case "float":
                    return Schema.forPrimitive(Type.FLOAT);
                case "double":
                    return Schema.forPrimitive(Type.DOUBLE);
                case "array":
                case "list":
                    return readArrayType(space, name, walker);
                case "map":
                    return readMapType(space, name, walker);
                case "struct":
                    return readStructType(space, name, walker);
                case "optional":
                    return readOptionalType(space, name, walker);
                default:
                    throw new RuntimeException("Not support type name: " + typeName);
            }
        }

        private static Schema readOptionalType(String space, String name, StringWalker walker) {
            walker.skipBlanks();
            requireAndJumpChar(walker, '<');
            walker.skipBlanks();
            Schema elementType = readType(space, name, walker);
            walker.skipBlanks();
            requireAndJumpChar(walker, '>');
            return Schema.forOptional(elementType);
        }

        private static Schema readArrayType(String space, String name, StringWalker walker) {
            walker.skipBlanks();
            requireAndJumpChar(walker, '<');
            walker.skipBlanks();
            Schema elementType = readType(buildNamespace(space, name), "item", walker);
            walker.skipBlanks();
            requireAndJumpChar(walker, '>');
            return Schema.forArray(elementType);
        }

        private static Schema readMapType(String space, String name, StringWalker walker) {
            walker.skipBlanks();
            requireAndJumpChar(walker, '<');
            walker.skipBlanks();
            Schema valueType = readType(buildNamespace(space, name), "value", walker);
            walker.skipBlanks();
            requireAndJumpChar(walker, '>');
            return Schema.forMap(valueType);
        }

        private static Schema readStructType(String space, String name, StringWalker walker) {
            walker.skipBlanks();
            requireAndJumpChar(walker, '<');
            walker.skipBlanks();
            List<Field> fields = new ArrayList<>();
            String childSpace = buildNamespace(space, name);
            while (!walker.isEnd() && walker.peek() != '>') {
                walker.skipBlanks();
                String fieldName = walker.readTo(':', false).trim();
                Schema fieldType = readType(childSpace, fieldName, walker);
                fields.add(Schema.newField(fieldName, fieldType));
                walker.skipBlanks();
                if (walker.isEnd() || walker.peek() != ',') {
                    break;
                }
                // jump ','
                walker.jump(1);
                walker.skipBlanks();
            }
            walker.skipBlanks();
            requireAndJumpChar(walker, '>');
            return Schema.forStruct(space, name, fields, null);
        }
    }
}
