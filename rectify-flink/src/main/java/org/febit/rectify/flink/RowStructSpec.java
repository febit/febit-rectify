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
package org.febit.rectify.flink;

import jakarta.annotation.Nullable;
import org.apache.flink.types.Row;
import org.febit.lang.modeler.Schema;
import org.febit.lang.modeler.StructSpec;

import java.io.Serializable;

public class RowStructSpec implements StructSpec<Row, Row>, Serializable {

    private static final RowStructSpec INSTANCE = new RowStructSpec();

    public static RowStructSpec get() {
        return INSTANCE;
    }

    @Override
    public Row builder(Schema schema) {
        return new Row(schema.fieldsSize());
    }

    @Override
    public Row build(Schema schema, Row row) {
        return row;
    }

    @Override
    public void set(Row row, Schema.Field field, @Nullable Object val) {
        row.setField(field.pos(), val);
    }

    @Nullable
    @Override
    public Object get(Row row, Schema.Field field) {
        return row.getField(field.pos());
    }

}
