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
package org.febit.rectify;

import jakarta.annotation.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Rectifier<I, O> {

    /**
     * Process input one by one.
     *
     * @param input input
     */
    void process(@Nullable I input, BiConsumer<O, RawOutput> onSucceed, BiConsumer<String, RawOutput> onFailed);

    /**
     * Process input one by one.
     *
     * @param input input
     */
    default void process(@Nullable I input, RectifierConsumer<O> onCompleted) {
        process(input, onCompleted::onSucceed, onCompleted::onFailed);
    }

    /**
     * Process input one by one.
     *
     * @param input input
     */
    default void process(@Nullable I input, Consumer<O> onSucceed, Consumer<String> onFailed) {
        process(input, (o, raw) -> onSucceed.accept(o), (reason, raw) -> onFailed.accept(reason));
    }

    Schema schema();

    List<String> getHints();

    default <S> Rectifier<S, O> with(SourceFormat<S, I> sourceFormat) {
        return RectifierSourceFormatWrapper.wrap(sourceFormat, this);
    }

    default <S> Rectifier<S, O> with(Function<S, I> transfer) {
        return RectifierFunctionWrapper.wrap(transfer, this);
    }
}
