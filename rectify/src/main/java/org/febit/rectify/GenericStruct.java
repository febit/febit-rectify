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

import java.io.Serializable;
import java.util.Objects;

/**
 * @author zqq90
 */
public class GenericStruct implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final GenericResultModel MODEL = new GenericResultModel();
    private final Object[] values;

    private GenericStruct(int fieldCount) {
        this.values = new Object[fieldCount];
    }

    public static ResultModel<GenericStruct> model() {
        return MODEL;
    }

    public static GenericStruct of(Schema schema) {
        Objects.requireNonNull(schema);
        if (!schema.isStructType()) {
            throw new IllegalArgumentException("Not a struct: " + schema);
        }
        return new GenericStruct(schema.fieldSize());
    }

    /**
     * Set field value at position.
     *
     * @param pos position
     * @param val value
     */
    public void set(int pos, Object val) {
        this.values[pos] = val;
    }

    /**
     * Get field value at position.
     *
     * @param pos position
     * @return field value
     */
    public Object get(int pos) {
        return this.values[pos];
    }

    private static class GenericResultModel implements ResultModel<GenericStruct> {

        @Override
        public GenericStruct newStruct(Schema schema) {
            return GenericStruct.of(schema);
        }

        @Override
        public void setField(GenericStruct record, Schema.Field field, Object val) {
            record.set(field.pos(), val);
        }

        @Override
        public Object getField(GenericStruct record, Schema.Field field) {
            return record.get(field.pos());
        }
    }
}
