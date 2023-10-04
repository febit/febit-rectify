package org.febit.lang.modeler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public enum SchemaType {

    ARRAY(Object[].class),
    LIST(List.class),
    MAP(Map.class),

    STRING(String.class),
    BYTES(byte[].class),
    BOOLEAN(Boolean.class),
    INT(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),

    INSTANT(Instant.class),
    DATE(LocalDate.class),
    TIME(LocalTime.class),
    DATETIME(LocalDateTime.class),
    DATETIME_ZONED("datetimetz", ZonedDateTime.class),

    ENUM(Enum.class),
    OPTIONAL(Object.class),
    STRUCT(Object.class),
    JSON(Object.class),
    RAW(Object.class),
    ;

    @Getter
    private final String typeString;

    @Getter
    private final Class<?> javaType;

    SchemaType(Class<?> javaType) {
        this.javaType = javaType;
        this.typeString = name().toLowerCase();
    }

    public String toTypeString() {
        return typeString;
    }

    public String toJavaTypeString() {
        if ("java.lang".equals(javaType.getPackageName())) {
            return javaType.getSimpleName();
        }
        return javaType.getCanonicalName();
    }
}
