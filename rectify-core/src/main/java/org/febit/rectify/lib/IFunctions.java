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

import org.febit.rectify.RectifierEnginePlugin;
import org.febit.rectify.util.FuncUtils;
import org.febit.wit.Engine;

import java.lang.annotation.*;

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
        FuncUtils.scanConstFields(getClass(), engine.getGlobalManager()::setConst);
    }
}
