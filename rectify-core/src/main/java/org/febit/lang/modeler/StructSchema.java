package org.febit.lang.modeler;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.febit.lang.util.Maps;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Accessors(fluent = true)
class StructSchema implements Schema {

    private static final long serialVersionUID = 1L;

    @Getter
    @Nullable
    private final String namespace;

    @Getter
    @Nullable
    private final String name;

    @Getter
    @Nullable
    private final String fullname;

    @Getter
    @Nullable
    private final String comment;

    private final List<Field> fields;
    private final Map<String, Field> fieldMap;

    StructSchema(
            @Nullable String namespace,
            @Nullable String name,
            List<Field> fields,
            @Nullable String comment
    ) {
        if (name != null) {
            Schemas.checkName(name);
        }

        this.name = name;
        this.namespace = namespace;
        this.fullname = (namespace == null) ? name : namespace + "." + name;
        this.comment = Schemas.escapeForLineComment(comment);

        this.fields = List.copyOf(fields);
        this.fieldMap = Maps.mapping(fields, Field::name);
    }

    @Override
    public SchemaType type() {
        return SchemaType.STRUCT;
    }

    @Override
    public Field field(String name) {
        var field = fieldMap.get(name);
        if (field == null) {
            throw new NoSuchElementException("No such field: " + name);
        }
        return field;
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
        var buf = new StringBuilder();
        for (var field : fields) {
            buf.append(field.schema())
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
    public String toJavaTypeString() {
        var buf = new StringBuilder();
        buf.append("struct<");
        for (var field : fields) {
            if (field.pos() != 0) {
                buf.append(',');
            }
            buf.append(field.name())
                    .append(':')
                    .append(field.schema().toJavaTypeString());
        }
        buf.append('>');
        return buf.toString();
    }

    @Override
    public String toTypeString() {
        var buf = new StringBuilder();
        buf.append("struct<");
        for (var field : fields) {
            if (field.pos() != 0) {
                buf.append(',');
            }
            buf.append(field.name())
                    .append(':')
                    .append(field.schema().toTypeString());
        }
        buf.append('>');
        return buf.toString();
    }

    @Override
    public String toString() {
        return toTypeString();
    }

    @Accessors(fluent = true)
    @RequiredArgsConstructor
    static class FieldImpl implements Field {

        private static final long serialVersionUID = 1L;

        @Getter
        private final int pos;
        @Getter
        private final String name;
        @Getter
        private final Schema schema;

        @Getter
        @Nullable
        private final String comment;

        @Override
        public String toString() {
            return name + ':' + schema.toJavaTypeString();
        }
    }
}
