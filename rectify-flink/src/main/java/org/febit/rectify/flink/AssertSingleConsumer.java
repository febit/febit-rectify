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
package org.febit.rectify.flink;

import java.util.function.Consumer;

/**
 * @param <T>
 */
public class AssertSingleConsumer<T> implements Consumer<T> {

    private T value;

    @Override
    public void accept(T next) {
        if (value != null) {
            throw new IllegalStateException("Assert single item, but got more");
        }
        this.value = next;
    }

    public T getValue() {
        return value;
    }
}
