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
import org.febit.wit.runtime.WitFunction;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@RequiredArgsConstructor(staticName = "create")
public class AdaptFunction implements WitFunction.Constable {

    private final Function<@Nullable Object, @Nullable Object>[] paramConverters;
    private final Function<@Nullable Object[], @Nullable Object> func;

    @Nullable
    @Override
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
