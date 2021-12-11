/**
 * Copyright 2018-present febit.org (support@febit.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.rectify;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@RequiredArgsConstructor(staticName = "wrap")
class RectifierFunctionWrapper<S, I, O> implements Rectifier<S, O> {

    private final Function<S, I> transfer;
    private final Rectifier<I, O> delegated;

    @Override
    public void process(S source, BiConsumer<O, ResultRaw> onSucceed, BiConsumer<ResultRaw, String> onFailed) {
        val in = transfer.apply(source);
        delegated.process(in, onSucceed, onFailed);
    }

    @Override
    public Schema schema() {
        return delegated.schema();
    }

    @Override
    public List<String> getHints() {
        return delegated.getHints();
    }
}
