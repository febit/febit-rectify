package org.febit.rectify;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.febit.rectify.util.StringWalker;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Internal schema utils
 */
@UtilityClass
class Schemas {

    private static final String TYPE_NAME_END_CHARS = "\r\n \t\f\b<>[],.:;+-=#";
    private static final String LINE_BREAKERS = "\r\n";
    private static final String LINE_BREAKERS_REPLACE = "  ";
    private static final Pattern NAME_PATTERN = Pattern.compile("^[_a-zA-Z][_a-zA-Z0-9]{1,64}$");
    static final String NOT_A_STRUCT = "Not a struct: ";

    private static void checkName(String name) {
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Illegal name: " + name);
        }
    }

    private static String escapeForLineComment(String remark) {
        if (remark == null) {
            return null;
        }
        return StringUtils.replaceChars(remark, LINE_BREAKERS, LINE_BREAKERS_REPLACE);
    }

    public static Schema ofPrimitive(Schema.Type type) {
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

    public static Schema ofArray(Schema valueType) {
        return new ValuedSchema(Schema.Type.ARRAY, valueType);
    }

    public static Schema ofOptional(Schema valueType) {
        return new ValuedSchema(Schema.Type.OPTIONAL, valueType);
    }

    public static Schema ofMap(Schema valueType) {
        return new ValuedSchema(Schema.Type.MAP, valueType);
    }

    static Schema parseLinesAsStruct(String name, String... lines) {

        val builder = structSchemaBuilder();
        builder.name(name);

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()
                    || line.charAt(0) == '#'
                    || line.charAt(0) == '-') {
                continue;
            }
            parseField(name, line, builder);
        }
        return builder.build();
    }

    @RequiredArgsConstructor
    static class FieldImpl implements Schema.Field {

        private static final long serialVersionUID = 1L;

        private final int pos;
        private final String name;
        private final Schema schema;
        private final String comment;

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

    private static abstract class BaseSchema implements Schema {

        @Getter
        final Schema.Type type;

        BaseSchema(Schema.Type type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type.getName();
        }
    }

    private static class PrimitiveSchema extends BaseSchema {

        private static final long serialVersionUID = 1L;

        public PrimitiveSchema(Type type) {
            super(type);
        }
    }

    private static class ValuedSchema extends BaseSchema {

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

    public static StructSchemaBuilder structSchemaBuilder() {
        return new StructSchemaBuilder();
    }

    public static class StructSchemaBuilder {
        private String name;
        private String space;
        private String comment;

        private final List<Schema.Field> fields = new ArrayList<>();

        public StructSchemaBuilder name(String name) {
            int split = name.lastIndexOf('.');
            if (split < 0) {
                this.name = name;
            } else {
                space(name.substring(0, split));
                this.name = name.substring(split + 1);
            }
            return this;
        }

        public StructSchemaBuilder space(String space) {
            this.space = "".equals(space) ? null : space;
            return this;
        }

        public StructSchemaBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public int fieldsSize() {
            return fields.size();
        }

        public StructSchemaBuilder field(String name, Schema schema) {
            return field(name, schema, null);
        }

        public StructSchemaBuilder field(String name, Schema schema, String comment) {
            checkName(name);
            FieldImpl field = new FieldImpl(fields.size(), name, schema, escapeForLineComment(comment));
            fields.add(field);
            return this;
        }

        public StructSchema build() {
            return new StructSchema(space, name, fields, comment);
        }
    }

    private static class StructSchema extends BaseSchema {

        private static final long serialVersionUID = 1L;
        private final String name;
        private final String space;
        private final String fullname;
        private final String comment;

        private final List<Field> fields;
        private final Map<String, Field> fieldMap;

        private StructSchema(String space, String name, List<Field> fields, String comment) {
            super(Type.STRUCT);

            if (name != null) {
                checkName(name);
            }

            this.name = name;
            this.space = space;
            this.fullname = (space == null) ? name : space + "." + name;
            this.comment = escapeForLineComment(comment);

            this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
            this.fieldMap = new HashMap<>();
            for (Schema.Field field : fields) {
                fieldMap.put(field.name(), field);
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
        public Field field(String fieldName) {
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

                if (StringUtils.isNotEmpty(field.comment())) {
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

    private static boolean isTypeNameEnding(char c) {
        return TYPE_NAME_END_CHARS.indexOf(c) >= 0;
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
            throw new IllegalArgumentException("Unexpected EOF");
        }
        if (walker.peek() != c) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Unexpected char '{0}' at {1}, but need `{2}` ", walker.peek(), walker.pos(), c));
        }
        walker.jump(1);
    }

    static Schema parse(String space, String name, String str) {
        StringWalker walker = new StringWalker(str);
        return readType(space, name, walker);
    }

    private static void parseField(String space, String line, StructSchemaBuilder builder) {
        StringWalker walker = new StringWalker(line);
        walker.skipBlanks();
        Schema schema = readType(space, "_col" + builder.fieldsSize(), walker);
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
            throw new IllegalArgumentException("Invalid content: " + walker.readToEnd());
        }
        builder.field(name, schema, comment);
    }

    private static Schema readType(String space, String name, StringWalker walker) {
        walker.skipBlanks();
        String typeName = walker.readToFlag(Schemas::isTypeNameEnding, true).toLowerCase();
        switch (typeName) {
            case "int":
            case "integer":
                return ofPrimitive(Schema.Type.INT);
            case "long":
            case "bigint":
                return ofPrimitive(Schema.Type.BIGINT);
            case "string":
                return ofPrimitive(Schema.Type.STRING);
            case "bool":
            case "boolean":
                return ofPrimitive(Schema.Type.BOOLEAN);
            case "bytes":
                return ofPrimitive(Schema.Type.BYTES);
            case "float":
                return ofPrimitive(Schema.Type.FLOAT);
            case "double":
                return ofPrimitive(Schema.Type.DOUBLE);
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
                throw new IllegalArgumentException("Not support type name: " + typeName);
        }
    }

    private static Schema readOptionalType(String space, String name, StringWalker walker) {
        walker.skipBlanks();
        requireAndJumpChar(walker, '<');
        walker.skipBlanks();
        Schema elementType = readType(space, name, walker);
        walker.skipBlanks();
        requireAndJumpChar(walker, '>');
        return ofOptional(elementType);
    }

    private static Schema readArrayType(String space, String name, StringWalker walker) {
        walker.skipBlanks();
        requireAndJumpChar(walker, '<');
        walker.skipBlanks();
        Schema elementType = readType(buildNamespace(space, name), "item", walker);
        walker.skipBlanks();
        requireAndJumpChar(walker, '>');
        return ofArray(elementType);
    }

    private static Schema readMapType(String space, String name, StringWalker walker) {
        walker.skipBlanks();
        requireAndJumpChar(walker, '<');
        walker.skipBlanks();
        Schema valueType = readType(buildNamespace(space, name), "value", walker);
        walker.skipBlanks();
        requireAndJumpChar(walker, '>');
        return ofMap(valueType);
    }

    private static Schema readStructType(String space, String name, StringWalker walker) {
        walker.skipBlanks();
        requireAndJumpChar(walker, '<');
        walker.skipBlanks();

        val builder = structSchemaBuilder();
        builder.space(space);
        builder.name(name);

        String childSpace = buildNamespace(space, name);
        while (!walker.isEnd() && walker.peek() != '>') {
            walker.skipBlanks();
            String fieldName = walker.readTo(':', false).trim();
            Schema fieldType = readType(childSpace, fieldName, walker);
            builder.field(fieldName, fieldType, null);
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
        return builder.build();
    }
}
