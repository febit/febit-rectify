package org.febit.lang.modeler;

import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StructSchemaBuilder {

    @Nullable
    private String name;
    @Nullable
    private String namespace;
    @Nullable
    private String comment;

    private final List<Schema.Field> fields = new ArrayList<>();

    public StructSchemaBuilder name(String name) {
        int split = name.lastIndexOf('.');
        if (split < 0) {
            this.name = name;
        } else {
            namespace(name.substring(0, split));
            this.name = name.substring(split + 1);
        }
        return this;
    }

    public StructSchemaBuilder namespace(@Nullable String namespace) {
        this.namespace = "".equals(namespace) ? null : namespace;
        return this;
    }

    public StructSchemaBuilder comment(@Nullable String comment) {
        this.comment = comment;
        return this;
    }

    public int fieldsSize() {
        return fields.size();
    }

    public StructSchemaBuilder field(String name, Schema schema) {
        return field(name, schema, null);
    }

    public StructSchemaBuilder field(String name, Schema schema, @Nullable String comment) {
        Schemas.checkName(name);
        var field = new StructSchema.FieldImpl(fields.size(), name, schema, Schemas.escapeForLineComment(comment));
        fields.add(field);
        return this;
    }

    public Schema build() {
        return new StructSchema(namespace, name, fields, comment);
    }
}
