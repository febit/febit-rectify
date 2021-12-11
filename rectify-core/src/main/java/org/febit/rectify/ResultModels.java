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

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Factories for ResultModel.
 */
@UtilityClass
public class ResultModels {

    private static final ObjectArrayResultModel OBJECT_ARRAY_RESULT_MODEL = new ObjectArrayResultModel();
    private static final MapResultModel MAP_RESULT_MODEL = new MapResultModel();

    public static ResultModel<Object[]> asObjectArray() {
        return OBJECT_ARRAY_RESULT_MODEL;
    }

    public static ResultModel<Map<String, Object>> asMap() {
        return MAP_RESULT_MODEL;
    }

    private static class MapResultModel implements ResultModel<Map<String, Object>> {

        @Override
        public Map<String, Object> newStruct(Schema schema) {
            int size = schema.fieldSize();
            int cap = Math.max(4, Math.min(1 << 30,
                    (int) ((float) size / 0.75F + 1.0F)
            ));
            return new HashMap<>(cap, 0.75F);
        }

        @Override
        public void setField(Map<String, Object> record, Schema.Field field, Object val) {
            record.put(field.name(), val);
        }

        @Override
        public Object getField(Map<String, Object> record, Schema.Field field) {
            return record.get(field.name());
        }
    }

    private static class ObjectArrayResultModel implements ResultModel<Object[]> {

        @Override
        public Object[] newStruct(Schema schema) {
            return new Object[schema.fieldSize()];
        }

        @Override
        public void setField(Object[] record, Schema.Field field, Object val) {
            record[field.pos()] = val;
        }

        @Override
        public Object getField(Object[] record, Schema.Field field) {
            return record[field.pos()];
        }
    }

}
