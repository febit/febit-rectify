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
package org.febit.rectify.sqlline;

import org.febit.rectify.ResultModel;
import org.febit.rectify.Schema;

/**
 * @author zqq90
 */
class ObjectArrayResultModel implements ResultModel<Object[]> {

    private static final ObjectArrayResultModel INSTANCE = new ObjectArrayResultModel();

    static ObjectArrayResultModel get() {
        return INSTANCE;
    }

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
