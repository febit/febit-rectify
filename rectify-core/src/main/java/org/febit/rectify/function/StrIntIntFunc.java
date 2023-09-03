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

import jakarta.annotation.Nullable;
import org.febit.wit.InternalContext;
import org.febit.wit.lang.MethodDeclare;

import static org.febit.rectify.util.Args.int1;
import static org.febit.rectify.util.Args.int2;
import static org.febit.rectify.util.Args.string0;

@FunctionalInterface
public interface StrIntIntFunc extends MethodDeclare {

    @Nullable
    Object invoke(@Nullable String text, @Nullable Integer i, @Nullable Integer j);

    @Nullable
    @Override
    default Object invoke(InternalContext context, Object[] args) {
        return invoke(string0(args), int1(args), int2(args));
    }
}
