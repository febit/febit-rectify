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
package org.febit.rectify.flink;

import lombok.experimental.UtilityClass;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ListTypeInfo;
import org.apache.flink.api.java.typeutils.MapTypeInfo;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.febit.rectify.Schema;

import java.util.List;
import java.util.Objects;

@UtilityClass
public class TypeInfoUtils {

    public static RowTypeInfo ofRowType(Schema schema) {
        Objects.requireNonNull(schema);
        if (!schema.isStructType()) {
            throw new IllegalArgumentException("Not a record: " + schema);
        }
        List<Schema.Field> fields = schema.fields();
        TypeInformation<?>[] fieldTypes = new TypeInformation[fields.size()];
        String[] fieldNames = new String[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            Schema.Field field = fields.get(i);
            fieldNames[i] = field.name();
            fieldTypes[i] = of(field.schema());
        }
        return new RowTypeInfo(fieldTypes, fieldNames);
    }

    @SuppressWarnings({
            "squid:S1452" // Generic wildcard types should not be used in return parameters
    })
    public static TypeInformation<?> of(Schema schema) {
        Objects.requireNonNull(schema);
        switch (schema.getType()) {
            case BOOLEAN:
                return BasicTypeInfo.BOOLEAN_TYPE_INFO;
            case INT:
                return BasicTypeInfo.INT_TYPE_INFO;
            case BIGINT:
                return BasicTypeInfo.LONG_TYPE_INFO;
            case FLOAT:
                return BasicTypeInfo.FLOAT_TYPE_INFO;
            case DOUBLE:
                return BasicTypeInfo.DOUBLE_TYPE_INFO;
            case STRING:
                return BasicTypeInfo.STRING_TYPE_INFO;
            case STRUCT:
                return ofRowType(schema);
            case ARRAY:
                TypeInformation<?> elementType = of(schema.valueType());
                return new ListTypeInfo<>(elementType);
            case MAP:
                TypeInformation<?> valType = of(schema.valueType());
                return new MapTypeInfo<>(BasicTypeInfo.STRING_TYPE_INFO, valType);
            case OPTIONAL:
                return of(schema.valueType());
            case BYTES:
                throw new UnsupportedOperationException("type is not supported yet: " + schema.getType());
            default:
                throw new IllegalArgumentException("Unknown type " + schema);
        }
    }
}