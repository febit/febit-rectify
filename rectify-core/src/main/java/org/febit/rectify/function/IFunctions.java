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
package org.febit.rectify.function;

import lombok.val;
import org.febit.rectify.RectifierEnginePlugin;
import org.febit.wit.Engine;
import org.febit.wit.exceptions.UncheckedException;
import org.febit.wit.util.ClassUtil;

import java.lang.annotation.*;
import java.lang.reflect.Field;

public interface IFunctions extends RectifierEnginePlugin {

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @interface Alias {
        String[] value();

        boolean keepOriginName() default true;
    }

    @Override
    default void apply(Engine engine) {
        val globalMgr = engine.getGlobalManager();

        for (Field field : getClass().getFields()) {
            if (!ClassUtil.isStatic(field)
                    || !ClassUtil.isFinal(field)) {
                continue;
            }
            Object fieldValue;
            try {
                fieldValue = field.get(null);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new UncheckedException(e);
            }

            val originName = field.getName();
            val aliasAnno = field.getAnnotation(Alias.class);
            if (aliasAnno == null) {
                globalMgr.setConst(originName, fieldValue);
                return;
            }
            if (aliasAnno.keepOriginName()) {
                globalMgr.setConst(originName, fieldValue);
                return;
            }
            for (val alias : aliasAnno.value()) {
                globalMgr.setConst(alias, fieldValue);
            }
        }
    }
}
