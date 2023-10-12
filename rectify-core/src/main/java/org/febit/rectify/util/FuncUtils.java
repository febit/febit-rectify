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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.LRUMap;
import com.fasterxml.jackson.databind.util.LookupCache;
import jakarta.annotation.Nullable;
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
import org.febit.wit.exceptions.UncheckedException;
import org.febit.wit.lang.MethodDeclare;
import org.febit.wit.util.ClassUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@UtilityClass
public class FuncUtils {
    private static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance().withCache(createCache());

    private static LookupCache<Object, JavaType> createCache() {
        return new LRUMap<>(16, 128);
    }

    public static void scanConstFields(Class<?> cls, BiConsumer<String, Object> consumer) {
        Stream.of(cls.getFields())
                .filter(ClassUtil::isStatic)
                .filter(ClassUtil::isFinal)
                .forEach(field -> sinkConst(consumer, field, null));
    }

    public static Map<Object, Object> scanProto(IProto proto) {
        var map = new HashMap<>();
        Stream.of(proto.getClass().getFields())
                .filter(f -> !ClassUtil.isStatic(f))
                .filter(ClassUtil::isFinal)
                .forEach(field -> sinkConst(map::put, field, proto));
        return Map.copyOf(map);
    }

    private static void sinkConst(BiConsumer<String, Object> consumer, Field field, @Nullable Object owner) {
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
        if (original instanceof IFunction) {
            return resolveMethodDeclare((IFunction) original, field);
        }
        if (original instanceof IProto) {
            return scanProto((IProto) original);
        }
        return original;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static MethodDeclare resolveMethodDeclare(IFunction func, Field field) {
        if (func instanceof Function0) {
            return (c, args) -> ((Function0) func).apply();
        }
        if (func instanceof Consumer0) {
            return (c, args) -> {
                ((Consumer0) func).accept();
                return null;
            };
        }
        if (func instanceof ThrowingFunction0) {
            return (c, args) -> Unchecked.func0((ThrowingFunction0) func).apply();
        }
        if (func instanceof ThrowingConsumer0) {
            return (c, args) -> {
                Unchecked.consumer0((ThrowingConsumer0) func).accept();
                return null;
            };
        }
        if (func instanceof ThrowingCallable) {
            return (c, args) -> Unchecked.callable((ThrowingCallable) func).call();
        }
        if (func instanceof ThrowingSupplier) {
            return (c, args) -> Unchecked.supplier((ThrowingSupplier) func).get();
        }
        if (func instanceof ThrowingRunnable) {
            return (c, args) -> {
                Unchecked.runnable((ThrowingRunnable) func).run();
                return null;
            };
        }

        var javaType = TYPE_FACTORY.constructType(field.getGenericType());
        if (func instanceof Function1) {
            return FuncMethodDeclare.of((Function1) func, javaType);
        }
        if (func instanceof Function2) {
            return FuncMethodDeclare.of((Function2) func, javaType);
        }
        if (func instanceof Function3) {
            return FuncMethodDeclare.of((Function3) func, javaType);
        }
        if (func instanceof Function4) {
            return FuncMethodDeclare.of((Function4) func, javaType);
        }
        if (func instanceof Function5) {
            return FuncMethodDeclare.of((Function5) func, javaType);
        }
        if (func instanceof Consumer1) {
            return FuncMethodDeclare.of((Consumer1) func, javaType);
        }
        if (func instanceof Consumer2) {
            return FuncMethodDeclare.of((Consumer2) func, javaType);
        }
        if (func instanceof Consumer3) {
            return FuncMethodDeclare.of((Consumer3) func, javaType);
        }
        if (func instanceof Consumer4) {
            return FuncMethodDeclare.of((Consumer4) func, javaType);
        }
        if (func instanceof Consumer5) {
            return FuncMethodDeclare.of((Consumer5) func, javaType);
        }
        if (func instanceof ThrowingConsumer1) {
            return FuncMethodDeclare.of(Unchecked.consumer1((ThrowingConsumer1) func), javaType);
        }
        if (func instanceof ThrowingConsumer2) {
            return FuncMethodDeclare.of(Unchecked.consumer2((ThrowingConsumer2) func), javaType);
        }
        if (func instanceof ThrowingConsumer3) {
            return FuncMethodDeclare.of(Unchecked.consumer3((ThrowingConsumer3) func), javaType);
        }
        if (func instanceof ThrowingConsumer4) {
            return FuncMethodDeclare.of(Unchecked.consumer4((ThrowingConsumer4) func), javaType);
        }
        if (func instanceof ThrowingConsumer5) {
            return FuncMethodDeclare.of(Unchecked.consumer5((ThrowingConsumer5) func), javaType);
        }
        if (func instanceof ThrowingFunction1) {
            return FuncMethodDeclare.of(Unchecked.func1((ThrowingFunction1) func), javaType);
        }
        if (func instanceof ThrowingFunction2) {
            return FuncMethodDeclare.of(Unchecked.func2((ThrowingFunction2) func), javaType);
        }
        if (func instanceof ThrowingFunction3) {
            return FuncMethodDeclare.of(Unchecked.func3((ThrowingFunction3) func), javaType);
        }
        if (func instanceof ThrowingFunction4) {
            return FuncMethodDeclare.of(Unchecked.func4((ThrowingFunction4) func), javaType);
        }
        if (func instanceof ThrowingFunction5) {
            return FuncMethodDeclare.of(Unchecked.func5((ThrowingFunction5) func), javaType);
        }
        throw new IllegalArgumentException("Unsupported function: " + func.getClass());
    }

}
