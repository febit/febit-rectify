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
import lombok.val;
import org.febit.rectify.function.IFunctions;
import org.febit.wit.exceptions.UncheckedException;
import org.febit.wit.util.ClassUtil;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@UtilityClass
public class FunctionUtils {

    public static void scanConstFields(BiConsumer<String, Object> consumer, Class<?> cls) {
        Stream.of(cls.getFields())
                .filter(ClassUtil::isStatic)
                .filter(ClassUtil::isFinal)
                .forEach(field -> scanConst(consumer, field));
    }

    public static void scanConst(BiConsumer<String, Object> consumer, Field field) {
        Object fieldValue;
        try {
            fieldValue = field.get(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new UncheckedException(e);
        }
        val originName = field.getName();
        val aliasAnno = field.getAnnotation(IFunctions.Alias.class);
        if (aliasAnno == null) {
            consumer.accept(originName, fieldValue);
            return;
        }
        if (aliasAnno.keepOriginName()) {
            consumer.accept(originName, fieldValue);
        }
        for (val alias : aliasAnno.value()) {
            consumer.accept(alias, fieldValue);
        }
    }
}
