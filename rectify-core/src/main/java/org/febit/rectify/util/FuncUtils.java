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
import org.febit.rectify.lib.IFunctions;
import org.febit.rectify.lib.IProto;
import org.febit.wit.exception.UncheckedException;
import org.febit.wit.runtime.function.FunctionDeclare;
import org.febit.wit.util.ClassUtils;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.type.TypeFactory;
import tools.jackson.databind.util.LookupCache;
import tools.jackson.databind.util.SimpleLookupCache;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@UtilityClass
public class FuncUtils {
    private static final TypeFactory TYPE_FACTORY = TypeFactory.createDefaultInstance().withCache(createCache());

    private static LookupCache<Object, JavaType> createCache() {
        return new SimpleLookupCache<>(16, 128);
    }

    public static void scanConstFields(Class<?> cls, BiConsumer<String, Object> consumer) {
        Stream.of(cls.getFields())
                .filter(ClassUtils::isStatic)
                .filter(ClassUtils::isFinal)
                .forEach(field -> sinkConst(consumer, field, null));
    }

    public static Map<Object, Object> scanProto(IProto proto) {
        var map = new HashMap<>();
        Stream.of(proto.getClass().getFields())
                .filter(f -> !ClassUtils.isStatic(f))
                .filter(ClassUtils::isFinal)
                .forEach(field -> sinkConst(map::put, field, proto));
        return Collections.unmodifiableMap(new HashMap<>(map));
    }

    private static void sinkConst(
            BiConsumer<String, @Nullable Object> consumer,
            Field field,
            @Nullable Object owner
    ) {
        var fieldValue = resolveConstFrom(field, owner);
        var originName = field.getName();
        var aliasAnno = field.getAnnotation(IFunctions.Alias.class);
        if (aliasAnno == null) {
            consumer.accept(originName, fieldValue);
            return;
        }
        if (aliasAnno.keepOriginName()) {
            consumer.accept(originName, fieldValue);
        }
        for (var alias : aliasAnno.value()) {
            consumer.accept(alias, fieldValue);
        }
    }

    @Nullable
    private static Object resolveConstFrom(Field field, @Nullable Object owner) {
        Object original;
        try {
            original = field.get(owner);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new UncheckedException(e);
        }
        if (original == null) {
            return null;
        }
        if (original instanceof IFunction f) {
            return toFunctionDeclare(f, field);
        }
        if (original instanceof IProto proto) {
            return scanProto(proto);
        }
        return original;
    }

    @SuppressWarnings({
            "java:S3776", // Cognitive Complexity of methods should not be too high
            "rawtypes",
            "unchecked"
    })
    private static FunctionDeclare toFunctionDeclare(IFunction func, Field field) {
        if (func instanceof Function0 f) {
            return (c, args) -> f.apply();
        }
        if (func instanceof Consumer0 f) {
            return (c, args) -> {
                f.accept();
                return null;
            };
        }
        if (func instanceof ThrowingFunction0 f) {
            return (c, args) -> Unchecked.func0(f).apply();
        }
        if (func instanceof ThrowingConsumer0 f) {
            return (c, args) -> {
                Unchecked.consumer0(f).accept();
                return null;
            };
        }
        if (func instanceof ThrowingCallable f) {
            return (c, args) -> Unchecked.callable(f).call();
        }
        if (func instanceof ThrowingSupplier f) {
            return (c, args) -> Unchecked.supplier(f).get();
        }
        if (func instanceof ThrowingRunnable f) {
            return (c, args) -> {
                Unchecked.runnable(f).run();
                return null;
            };
        }

        var javaType = TYPE_FACTORY.constructType(field.getGenericType());
        if (func instanceof Function1 f) {
            return FuncFunctionDeclare.of(f, javaType);
        }
        if (func instanceof Function2 f) {
            return FuncFunctionDeclare.of(f, javaType);
        }
        if (func instanceof Function3 f) {
            return FuncFunctionDeclare.of(f, javaType);
        }
        if (func instanceof Function4 f) {
            return FuncFunctionDeclare.of(f, javaType);
        }
        if (func instanceof Function5 f) {
            return FuncFunctionDeclare.of(f, javaType);
        }
        if (func instanceof Consumer1 f) {
            return FuncFunctionDeclare.of(f, javaType);
        }
        if (func instanceof Consumer2 f) {
            return FuncFunctionDeclare.of(f, javaType);
        }
        if (func instanceof Consumer3 f) {
            return FuncFunctionDeclare.of(f, javaType);
        }
        if (func instanceof Consumer4 f) {
            return FuncFunctionDeclare.of(f, javaType);
        }
        if (func instanceof Consumer5 f) {
            return FuncFunctionDeclare.of(f, javaType);
        }
        if (func instanceof ThrowingConsumer1 f) {
            return FuncFunctionDeclare.of(Unchecked.consumer1(f), javaType);
        }
        if (func instanceof ThrowingConsumer2 f) {
            return FuncFunctionDeclare.of(Unchecked.consumer2(f), javaType);
        }
        if (func instanceof ThrowingConsumer3 f) {
            return FuncFunctionDeclare.of(Unchecked.consumer3(f), javaType);
        }
        if (func instanceof ThrowingConsumer4 f) {
            return FuncFunctionDeclare.of(Unchecked.consumer4(f), javaType);
        }
        if (func instanceof ThrowingConsumer5 f) {
            return FuncFunctionDeclare.of(Unchecked.consumer5(f), javaType);
        }
        if (func instanceof ThrowingFunction1 f) {
            return FuncFunctionDeclare.of(Unchecked.func1(f), javaType);
        }
        if (func instanceof ThrowingFunction2 f) {
            return FuncFunctionDeclare.of(Unchecked.func2(f), javaType);
        }
        if (func instanceof ThrowingFunction3 f) {
            return FuncFunctionDeclare.of(Unchecked.func3(f), javaType);
        }
        if (func instanceof ThrowingFunction4 f) {
            return FuncFunctionDeclare.of(Unchecked.func4(f), javaType);
        }
        if (func instanceof ThrowingFunction5 f) {
            return FuncFunctionDeclare.of(Unchecked.func5(f), javaType);
        }
        throw new IllegalArgumentException("Unsupported function: " + func.getClass());
    }

}
