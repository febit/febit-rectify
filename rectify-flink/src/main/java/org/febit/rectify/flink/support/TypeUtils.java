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
package org.febit.rectify.flink.support;

import lombok.experimental.UtilityClass;
import org.apache.flink.api.common.typeinfo.BasicArrayTypeInfo;
import org.apache.flink.api.common.typeinfo.PrimitiveArrayTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.ListTypeInfo;
import org.apache.flink.api.java.typeutils.MapTypeInfo;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.febit.lang.modeler.Schema;

import java.util.Objects;

@UtilityClass
public class TypeUtils {

    public static RowTypeInfo toRowType(Schema schema) {
        Objects.requireNonNull(schema);
        if (!schema.isStructType()) {
            throw new IllegalArgumentException("Only struct type can be converted to RowTypeInfo,"
                    + " but got: " + schema.type());
        }
        var fields = schema.fields();
        var fieldTypes = new TypeInformation[fields.size()];
        var fieldNames = new String[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);
            fieldNames[i] = field.name();
            fieldTypes[i] = toType(field.schema());
        }
        return new RowTypeInfo(fieldTypes, fieldNames);
    }

    @SuppressWarnings({
            "squid:S1452" // Generic wildcard types should not be used in return parameters
    })
    public static TypeInformation<?> toType(Schema schema) {
        Objects.requireNonNull(schema);
        return switch (schema.type()) {
            case BYTE -> Types.BYTE;
            case BOOLEAN -> Types.BOOLEAN;
            case SHORT -> Types.SHORT;
            case INT -> Types.INT;
            case LONG -> Types.LONG;
            case FLOAT -> Types.FLOAT;
            case DOUBLE -> Types.DOUBLE;
            case DECIMAL -> Types.BIG_DEC;
            case STRING -> Types.STRING;
            case DATE -> Types.LOCAL_DATE;
            case TIME -> Types.LOCAL_TIME;
            case DATETIME -> Types.LOCAL_DATE_TIME;
            case INSTANT -> Types.INSTANT;
            // XXX - Flink SQL does not support ZonedDateTime, use Instant instead
            case DATETIME_ZONED -> Types.INSTANT;
            case BYTES -> PrimitiveArrayTypeInfo.BYTE_PRIMITIVE_ARRAY_TYPE_INFO;
            case STRUCT -> toRowType(schema);
            case LIST -> new ListTypeInfo<>(
                    toType(schema.valueType())
            );
            case ARRAY -> {
                var elementType = schema.valueType();
                while (elementType.isOptionalType()) {
                    elementType = elementType.valueType();
                }
                yield switch (elementType.type()) {
                    case BYTE -> PrimitiveArrayTypeInfo.BYTE_PRIMITIVE_ARRAY_TYPE_INFO;
                    case BOOLEAN -> PrimitiveArrayTypeInfo.BOOLEAN_PRIMITIVE_ARRAY_TYPE_INFO;
                    case SHORT -> PrimitiveArrayTypeInfo.SHORT_PRIMITIVE_ARRAY_TYPE_INFO;
                    case INT -> PrimitiveArrayTypeInfo.INT_PRIMITIVE_ARRAY_TYPE_INFO;
                    case LONG -> PrimitiveArrayTypeInfo.LONG_PRIMITIVE_ARRAY_TYPE_INFO;
                    case FLOAT -> PrimitiveArrayTypeInfo.FLOAT_PRIMITIVE_ARRAY_TYPE_INFO;
                    case DOUBLE -> PrimitiveArrayTypeInfo.DOUBLE_PRIMITIVE_ARRAY_TYPE_INFO;
                    case STRING -> BasicArrayTypeInfo.STRING_ARRAY_TYPE_INFO;
                    default -> Types.OBJECT_ARRAY(toType(elementType));
                };
            }
            case MAP -> new MapTypeInfo<>(Types.STRING,
                    toType(schema.valueType())
            );
            case OPTIONAL -> toType(schema.valueType());
            case ENUM,
                 JSON,
                 RAW -> throw new UnsupportedOperationException("type is not supported yet: " + schema.type());
            default -> throw new IllegalArgumentException("Unknown type: " + schema);
        };
    }
}
