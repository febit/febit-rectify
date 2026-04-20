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
package org.febit.rectify.lib;

import lombok.experimental.UtilityClass;
import org.febit.lang.func.IFunction;
import org.febit.rectify.wit.function.WitFuncUtils;
import org.febit.wit.exception.UncheckedException;
import org.febit.wit.util.Modifiers;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@UtilityClass
public class LibUtils {

    public static void collect(Class<?> cls, BiConsumer<String, @Nullable Object> consumer) {
        Stream.of(cls.getFields())
                .filter(Modifiers::isStatic)
                .filter(Modifiers::isFinal)
                .forEach(field -> collect(field, null, consumer));
    }

    private static void collect(
            Field field, @Nullable Object owner, BiConsumer<String, @Nullable Object> consumer
    ) {
        var fieldValue = resolveConst(field, owner);
        var originName = field.getName();
        var aliasAnno = field.getAnnotation(ILib.Alias.class);
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
    private static Object resolveConst(Field field, @Nullable Object owner) {
        Object value;
        try {
            value = field.get(owner);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new UncheckedException(e);
        }
        if (value == null) {
            return null;
        }
        if (value instanceof IFunction f) {
            return WitFuncUtils.wrap(f, field.getGenericType());
        }
        if (value instanceof IProto proto) {
            return inspectProto(proto);
        }
        return value;
    }

    private static Map<Object, @Nullable Object> inspectProto(IProto proto) {
        var map = new HashMap<>();
        Stream.of(proto.getClass().getFields())
                .filter(Modifiers::isNotStatic)
                .filter(Modifiers::isFinal)
                .forEach(field -> collect(field, proto, (k, v) -> {
                    if (v != null) {
                        map.put(k, v);
                    }
                }));
        return Map.copyOf(map);
    }

}
