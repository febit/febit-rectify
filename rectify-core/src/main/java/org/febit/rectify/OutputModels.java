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

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Factories of OutputModel.
 */
@UtilityClass
public class OutputModels {

    private static final ObjectArrayOutputModel OBJECT_ARRAY_OUTPUT_MODEL = new ObjectArrayOutputModel();
    private static final MapOutputModel MAP_OUTPUT_MODEL = new MapOutputModel();

    public static OutputModel<Object[]> asObjectArray() {
        return OBJECT_ARRAY_OUTPUT_MODEL;
    }

    public static OutputModel<Map<String, Object>> asMap() {
        return MAP_OUTPUT_MODEL;
    }

    private static class MapOutputModel implements OutputModel<Map<String, Object>> {

        @Override
        public Map<String, Object> newStruct(Schema schema) {
            int size = schema.fieldSize();
            int cap = Math.max(4, Math.min(1 << 30,
                    (int) ((float) size / 0.75F + 1.0F)
            ));
            return new HashMap<>(cap, 0.75F);
        }

        @Override
        public void setField(Map<String, Object> record, Schema.Field field, @Nullable Object value) {
            record.put(field.name(), value);
        }

        @Nullable
        @Override
        public Object getField(Map<String, Object> record, Schema.Field field) {
            return record.get(field.name());
        }
    }

    private static class ObjectArrayOutputModel implements OutputModel<Object[]> {

        @Override
        public Object[] newStruct(Schema schema) {
            return new Object[schema.fieldSize()];
        }

        @Override
        public void setField(Object[] record, Schema.Field field, @Nullable Object value) {
            record[field.pos()] = value;
        }

        @Nullable
        @Override
        public Object getField(Object[] record, Schema.Field field) {
            return record[field.pos()];
        }
    }

}
