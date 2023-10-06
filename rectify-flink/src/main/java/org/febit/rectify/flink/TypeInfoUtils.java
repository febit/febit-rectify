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

import lombok.experimental.UtilityClass;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.ListTypeInfo;
import org.apache.flink.api.java.typeutils.MapTypeInfo;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.febit.lang.modeler.Schema;

import java.util.Objects;

@UtilityClass
public class TypeInfoUtils {

    public static RowTypeInfo ofRowType(Schema schema) {
        Objects.requireNonNull(schema);
        if (!schema.isStructType()) {
            throw new IllegalArgumentException("Not a record: " + schema);
        }
        var fields = schema.fields();
        var fieldTypes = new TypeInformation[fields.size()];
        var fieldNames = new String[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);
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
        switch (schema.type()) {
            case BOOLEAN:
                return Types.BOOLEAN;
            case INT:
                return Types.INT;
            case LONG:
                return Types.LONG;
            case FLOAT:
                return Types.FLOAT;
            case DOUBLE:
                return Types.DOUBLE;
            case STRING:
                return Types.STRING;
            case DATE:
                return Types.LOCAL_DATE;
            case TIME:
                return Types.LOCAL_TIME;
            case DATETIME:
                return Types.LOCAL_DATE_TIME;
            case INSTANT:
                return Types.INSTANT;
            case STRUCT:
                return ofRowType(schema);
            case ARRAY:
                var elementType = of(schema.valueType());
                return new ListTypeInfo<>(elementType);
            case MAP:
                var valType = of(schema.valueType());
                return new MapTypeInfo<>(Types.STRING, valType);
            case OPTIONAL:
                return of(schema.valueType());
            case DATETIME_ZONED: // FIXME: should support ZONED_DATE_TIME
            case BYTES:
                throw new UnsupportedOperationException("type is not supported yet: " + schema.type());
            default:
                throw new IllegalArgumentException("Unknown type " + schema);
        }
    }
}
