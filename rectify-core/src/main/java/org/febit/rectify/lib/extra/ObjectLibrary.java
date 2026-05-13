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
package org.febit.rectify.lib.extra;

import org.febit.lang.func.Function0;
import org.febit.lang.func.Function1;
import org.febit.lang.func.Function2;
import org.febit.rectify.lib.BindingAlias;
import org.febit.rectify.lib.Library;
import org.febit.rectify.lib.Namespace;
import org.febit.wit.ir.support.ALU;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings({
        "unused",
        "java:S1118", // Utility classes should not have public constructors
})
public class ObjectLibrary implements Library {

    /**
     * Namespace: Object.
     */
    @BindingAlias(value = {"Object"}, keepDeclaredName = false)
    public static final ObjectNamespace OBJECT = new ObjectNamespace();

    public static class ObjectNamespace implements Namespace {

        private final AtomicLong next = new AtomicLong(1);

        public final Function0<@Nullable Object> noop = () -> null;

        public final Function0<Long> seq = next::getAndIncrement;

        public final Function1<@Nullable Object, Integer> sizeOf = ALU::size;

        public final Function1<@Nullable Object, Boolean> isNull = Objects::isNull;
        public final Function1<@Nullable Object, Boolean> nonNull = Objects::nonNull;
        public final Function2<@Nullable Object, @Nullable Object, Boolean> isEquals = Objects::equals;
    }
}
