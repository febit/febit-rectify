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
package org.febit.rectify.util;

import lombok.RequiredArgsConstructor;
import org.febit.lang.func.Consumer1;
import org.febit.lang.func.Consumer2;
import org.febit.lang.func.Consumer3;
import org.febit.lang.func.Consumer4;
import org.febit.lang.func.Consumer5;
import org.febit.lang.func.Function1;
import org.febit.lang.func.Function2;
import org.febit.lang.func.Function3;
import org.febit.lang.func.Function4;
import org.febit.lang.func.Function5;
import org.febit.lang.util.ConvertUtils;
import org.febit.wit.runtime.InternalContext;
import org.febit.wit.runtime.function.FunctionDeclare;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Map;
import java.util.function.Function;

import static java.util.Map.entry;

@RequiredArgsConstructor(staticName = "create")
public class FuncFunctionDeclare implements FunctionDeclare {

    private static final Map<Class<?>, Function<@Nullable Object, @Nullable Object>> CONVERTERS = Map.ofEntries(
            entry(Boolean.class, ConvertUtils::toBoolean),
            entry(String.class, ConvertUtils::toString),
            entry(Byte.class, ConvertUtils::toByte),
            entry(Short.class, ConvertUtils::toShort),
            entry(Integer.class, Args::asInt),
            entry(Long.class, ConvertUtils::toLong),
            entry(Float.class, ConvertUtils::toFloat),
            entry(Double.class, ConvertUtils::toDouble),
            entry(BigDecimal.class, ConvertUtils::toBigDecimal),
            entry(Temporal.class, ConvertUtils::toTemporal),
            entry(LocalDate.class, ConvertUtils::toDate),
            entry(LocalTime.class, ConvertUtils::toTime),
            entry(LocalDateTime.class, ConvertUtils::toDateTime),
            entry(Instant.class, ConvertUtils::toInstant),
            entry(ZonedDateTime.class, ConvertUtils::toZonedDateTime),
            entry(ZoneOffset.class, ConvertUtils::toZone),
            entry(Object.class, Function.identity())
    );

    private final Function<@Nullable Object, @Nullable Object>[] paramConverters;
    private final Function<@Nullable Object[], @Nullable Object> func;

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    static Function<@Nullable Object, @Nullable Object>[] resolveParamConverters(JavaType[] types, int start, int end) {
        if (start < 0) {
            throw new IllegalArgumentException("start < 0");
        }
        if (end > types.length) {
            throw new IllegalArgumentException("end > types.length");
        }
        if (end < start) {
            throw new IllegalArgumentException("end < start");
        }
        var size = end - start;
        var converters = new Function[size];
        for (int i = start; i < end; i++) {
            converters[i] = converter(types[i]);
        }
        return converters;
    }

    static Function<@Nullable Object, @Nullable Object> converter(JavaType type) {
        var cls = type.getRawClass();
        var converter = CONVERTERS.get(cls == null ? Object.class : cls);
        if (converter == null) {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
        return converter;
    }

    static FuncFunctionDeclare of(
            Function1<@Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = resolveParamConverters(
                javaType.findTypeParameters(Function1.class),
                0, 1
        );
        return create(paramTypes, args -> func.apply(args[0]));
    }

    static FuncFunctionDeclare of(
            Function2<@Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = resolveParamConverters(
                javaType.findTypeParameters(Function2.class),
                0, 2
        );
        return create(paramTypes, args -> func.apply(args[0], args[1]));
    }

    static FuncFunctionDeclare of(
            Function3<@Nullable Object, @Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = resolveParamConverters(
                javaType.findTypeParameters(Function3.class),
                0, 3
        );
        return create(paramTypes, args -> func.apply(args[0], args[1], args[2]));
    }

    static FuncFunctionDeclare of(
            Function4<@Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = resolveParamConverters(
                javaType.findTypeParameters(Function4.class),
                0, 4
        );
        return create(paramTypes, args -> func.apply(args[0], args[1], args[2], args[3]));
    }

    static FuncFunctionDeclare of(
            Function5<@Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = resolveParamConverters(
                javaType.findTypeParameters(Function5.class),
                0, 5
        );
        return create(paramTypes, args -> func.apply(args[0], args[1], args[2], args[3], args[4]));
    }

    static FuncFunctionDeclare of(Consumer1<@Nullable Object> func, JavaType javaType) {
        var paramTypes = resolveParamConverters(
                javaType.findTypeParameters(Consumer1.class),
                0, 1
        );
        return create(paramTypes, args -> {
            func.accept(args[0]);
            return null;
        });
    }

    static FuncFunctionDeclare of(Consumer2<@Nullable Object, @Nullable Object> func, JavaType javaType) {
        var paramTypes = resolveParamConverters(
                javaType.findTypeParameters(Consumer2.class),
                0, 2
        );
        return create(paramTypes, args -> {
            func.accept(args[0], args[1]);
            return null;
        });
    }

    static FuncFunctionDeclare of(
            Consumer3<@Nullable Object, @Nullable Object, @Nullable Object> func,
            JavaType javaType
    ) {
        var paramTypes = resolveParamConverters(
                javaType.findTypeParameters(Consumer3.class),
                0, 3
        );
        return create(paramTypes, args -> {
            func.accept(args[0], args[1], args[2]);
            return null;
        });
    }

    static FuncFunctionDeclare of(
            Consumer4<@Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object> func,
            JavaType javaType
    ) {
        var paramTypes = resolveParamConverters(
                javaType.findTypeParameters(Consumer4.class),
                0, 4
        );
        return create(paramTypes, args -> {
            func.accept(args[0], args[1], args[2], args[3]);
            return null;
        });
    }

    static FuncFunctionDeclare of(
            Consumer5<@Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object> func,
            JavaType javaType
    ) {
        var paramTypes = resolveParamConverters(
                javaType.findTypeParameters(Consumer5.class),
                0, 5
        );
        return create(paramTypes, args -> {
            func.accept(args[0], args[1], args[2], args[3], args[4]);
            return null;
        });
    }

    @Nullable
    @Override
    public Object apply(InternalContext context, @Nullable Object @Nullable [] rawArgs) {
        return apply(rawArgs);
    }

    @Nullable
    public Object apply(@Nullable Object @Nullable ... rawArgs) {
        var converters = this.paramConverters;
        var argsSize = converters.length;
        var args = new Object[argsSize];
        for (int i = 0; i < argsSize; i++) {
            args[i] = converters[i].apply(Args.argX(rawArgs, i));
        }
        return func.apply(args);
    }

}
