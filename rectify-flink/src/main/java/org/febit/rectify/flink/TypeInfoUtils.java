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
        return switch (schema.type()) {
            case BOOLEAN -> Types.BOOLEAN;
            case INT -> Types.INT;
            case LONG -> Types.LONG;
            case FLOAT -> Types.FLOAT;
            case DOUBLE -> Types.DOUBLE;
            case STRING -> Types.STRING;
            case DATE -> Types.LOCAL_DATE;
            case TIME -> Types.LOCAL_TIME;
            case DATETIME -> Types.LOCAL_DATE_TIME;
            case INSTANT -> Types.INSTANT;
            case STRUCT -> ofRowType(schema);
            case ARRAY -> {
                var elementType = of(schema.valueType());
                yield new ListTypeInfo<>(elementType);
            }
            case MAP -> {
                var valType = of(schema.valueType());
                yield new MapTypeInfo<>(Types.STRING, valType);
            }
            case OPTIONAL -> of(schema.valueType());
            case DATETIME_ZONED, BYTES ->
                //  FIXME: should support DATETIME_ZONED
                    throw new UnsupportedOperationException("type is not supported yet: " + schema.type());
            default -> throw new IllegalArgumentException("Unknown type " + schema);
        };
    }
}
