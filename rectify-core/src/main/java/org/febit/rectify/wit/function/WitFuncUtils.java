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
package org.febit.rectify.wit.function;

import lombok.experimental.UtilityClass;
import org.febit.lang.Unchecked;
import org.febit.lang.func.Consumer0;
import org.febit.lang.func.Consumer1;
import org.febit.lang.func.Consumer2;
import org.febit.lang.func.Consumer3;
import org.febit.lang.func.Consumer4;
import org.febit.lang.func.Consumer5;
import org.febit.lang.func.Function0;
import org.febit.lang.func.Function1;
import org.febit.lang.func.Function2;
import org.febit.lang.func.Function3;
import org.febit.lang.func.Function4;
import org.febit.lang.func.Function5;
import org.febit.lang.func.IFunction;
import org.febit.lang.func.ThrowingCallable;
import org.febit.lang.func.ThrowingConsumer0;
import org.febit.lang.func.ThrowingConsumer1;
import org.febit.lang.func.ThrowingConsumer2;
import org.febit.lang.func.ThrowingConsumer3;
import org.febit.lang.func.ThrowingConsumer4;
import org.febit.lang.func.ThrowingConsumer5;
import org.febit.lang.func.ThrowingFunction0;
import org.febit.lang.func.ThrowingFunction1;
import org.febit.lang.func.ThrowingFunction2;
import org.febit.lang.func.ThrowingFunction3;
import org.febit.lang.func.ThrowingFunction4;
import org.febit.lang.func.ThrowingFunction5;
import org.febit.lang.func.ThrowingRunnable;
import org.febit.lang.func.ThrowingSupplier;
import org.febit.lang.util.ConvertUtils;
import org.febit.rectify.util.Args;
import org.febit.wit.runtime.WitFunction;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.type.TypeFactory;
import tools.jackson.databind.util.SimpleLookupCache;

import java.lang.reflect.Type;
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

@UtilityClass
public class WitFuncUtils {

    private static final TypeFactory TYPE_FACTORY = TypeFactory.createDefaultInstance()
            .withCache(new SimpleLookupCache<>(16, 128));

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

    @SuppressWarnings({
            "java:S3776", // Cognitive Complexity of methods should not be too high
            "rawtypes",
            "unchecked"
    })
    public static WitFunction.Constable wrap(IFunction func, Type funcGenericType) {
        if (func instanceof Function0 f) {
            return args -> f.apply();
        }
        if (func instanceof Consumer0 f) {
            return args -> {
                f.accept();
                return null;
            };
        }
        if (func instanceof ThrowingFunction0 f) {
            return args -> Unchecked.func0(f).apply();
        }
        if (func instanceof ThrowingConsumer0 f) {
            return args -> {
                Unchecked.consumer0(f).accept();
                return null;
            };
        }
        if (func instanceof ThrowingCallable f) {
            return args -> Unchecked.callable(f).call();
        }
        if (func instanceof ThrowingSupplier f) {
            return args -> Unchecked.supplier(f).get();
        }
        if (func instanceof ThrowingRunnable f) {
            return args -> {
                Unchecked.runnable(f).run();
                return null;
            };
        }

        var javaType = TYPE_FACTORY.constructType(funcGenericType);
        if (func instanceof Function1 f) {
            return adapt(f, javaType);
        }
        if (func instanceof Function2 f) {
            return adapt(f, javaType);
        }
        if (func instanceof Function3 f) {
            return adapt(f, javaType);
        }
        if (func instanceof Function4 f) {
            return adapt(f, javaType);
        }
        if (func instanceof Function5 f) {
            return adapt(f, javaType);
        }
        if (func instanceof Consumer1 f) {
            return adapt(f, javaType);
        }
        if (func instanceof Consumer2 f) {
            return adapt(f, javaType);
        }
        if (func instanceof Consumer3 f) {
            return adapt(f, javaType);
        }
        if (func instanceof Consumer4 f) {
            return adapt(f, javaType);
        }
        if (func instanceof Consumer5 f) {
            return adapt(f, javaType);
        }
        if (func instanceof ThrowingConsumer1 f) {
            return adapt(Unchecked.consumer1(f), javaType);
        }
        if (func instanceof ThrowingConsumer2 f) {
            return adapt(Unchecked.consumer2(f), javaType);
        }
        if (func instanceof ThrowingConsumer3 f) {
            return adapt(Unchecked.consumer3(f), javaType);
        }
        if (func instanceof ThrowingConsumer4 f) {
            return adapt(Unchecked.consumer4(f), javaType);
        }
        if (func instanceof ThrowingConsumer5 f) {
            return adapt(Unchecked.consumer5(f), javaType);
        }
        if (func instanceof ThrowingFunction1 f) {
            return adapt(Unchecked.func1(f), javaType);
        }
        if (func instanceof ThrowingFunction2 f) {
            return adapt(Unchecked.func2(f), javaType);
        }
        if (func instanceof ThrowingFunction3 f) {
            return adapt(Unchecked.func3(f), javaType);
        }
        if (func instanceof ThrowingFunction4 f) {
            return adapt(Unchecked.func4(f), javaType);
        }
        if (func instanceof ThrowingFunction5 f) {
            return adapt(Unchecked.func5(f), javaType);
        }
        throw new IllegalArgumentException("Unsupported function: " + func.getClass());
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    private static Function<@Nullable Object, @Nullable Object>[] convertersForParams(
            JavaType[] types, int from, int to) {
        if (from < 0) {
            throw new IllegalArgumentException("from < 0");
        }
        if (to > types.length) {
            throw new IllegalArgumentException("to > types.length");
        }
        if (to < from) {
            throw new IllegalArgumentException("end < from");
        }
        var size = to - from;
        var converters = new Function[size];
        for (int i = from; i < to; i++) {
            converters[i] = converter(types[i]);
        }
        return converters;
    }

    private static Function<@Nullable Object, @Nullable Object> converter(JavaType type) {
        var cls = type.getRawClass();
        var converter = CONVERTERS.get(cls == null ? Object.class : cls);
        if (converter == null) {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
        return converter;
    }

    private static AdaptFunction adapt(
            Function1<@Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = convertersForParams(
                javaType.findTypeParameters(Function1.class),
                0, 1
        );
        return AdaptFunction.create(paramTypes, args -> func.apply(args[0]));
    }

    private static AdaptFunction adapt(
            Function2<@Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = convertersForParams(
                javaType.findTypeParameters(Function2.class),
                0, 2
        );
        return AdaptFunction.create(paramTypes, args -> func.apply(args[0], args[1]));
    }

    private static AdaptFunction adapt(
            Function3<@Nullable Object, @Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = convertersForParams(
                javaType.findTypeParameters(Function3.class),
                0, 3
        );
        return AdaptFunction.create(paramTypes, args -> func.apply(args[0], args[1], args[2]));
    }

    private static AdaptFunction adapt(
            Function4<@Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = convertersForParams(
                javaType.findTypeParameters(Function4.class),
                0, 4
        );
        return AdaptFunction.create(paramTypes, args -> func.apply(args[0], args[1], args[2], args[3]));
    }

    private static AdaptFunction adapt(
            Function5<@Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = convertersForParams(
                javaType.findTypeParameters(Function5.class),
                0, 5
        );
        return AdaptFunction.create(paramTypes, args -> func.apply(args[0], args[1], args[2], args[3], args[4]));
    }

    private static AdaptFunction adapt(Consumer1<@Nullable Object> func, JavaType javaType) {
        var paramTypes = convertersForParams(
                javaType.findTypeParameters(Consumer1.class),
                0, 1
        );
        return AdaptFunction.create(paramTypes, args -> {
            func.accept(args[0]);
            return null;
        });
    }

    private static AdaptFunction adapt(Consumer2<@Nullable Object, @Nullable Object> func, JavaType javaType) {
        var paramTypes = convertersForParams(
                javaType.findTypeParameters(Consumer2.class),
                0, 2
        );
        return AdaptFunction.create(paramTypes, args -> {
            func.accept(args[0], args[1]);
            return null;
        });
    }

    private static AdaptFunction adapt(
            Consumer3<@Nullable Object, @Nullable Object, @Nullable Object> func,
            JavaType javaType
    ) {
        var paramTypes = convertersForParams(
                javaType.findTypeParameters(Consumer3.class),
                0, 3
        );
        return AdaptFunction.create(paramTypes, args -> {
            func.accept(args[0], args[1], args[2]);
            return null;
        });
    }

    private static AdaptFunction adapt(
            Consumer4<@Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object> func,
            JavaType javaType
    ) {
        var paramTypes = convertersForParams(
                javaType.findTypeParameters(Consumer4.class),
                0, 4
        );
        return AdaptFunction.create(paramTypes, args -> {
            func.accept(args[0], args[1], args[2], args[3]);
            return null;
        });
    }

    private static AdaptFunction adapt(
            Consumer5<@Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object> func,
            JavaType javaType
    ) {
        var paramTypes = convertersForParams(
                javaType.findTypeParameters(Consumer5.class),
                0, 5
        );
        return AdaptFunction.create(paramTypes, args -> {
            func.accept(args[0], args[1], args[2], args[3], args[4]);
            return null;
        });
    }
}
