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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Lazy impl of Rectifier.
 * <p>
 * {@linkplain RectifierImpl} is not serializable
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LazyRectifier<I, O> implements Rectifier<I, O>, Serializable {

    private final Serializable lock = new Serializable() {
    };

    private final RectifierProvider<I, O> provider;

    private transient Rectifier<I, O> delegated;

    public static <I, O> LazyRectifier<I, O> of(RectifierProvider<I, O> provider) {
        return new LazyRectifier<>(provider);
    }

    protected Rectifier<I, O> delegated() {
        Rectifier<I, O> rectifier = this.delegated;
        if (rectifier != null) {
            return rectifier;
        }
        synchronized (lock) {
            rectifier = this.delegated;
            if (rectifier == null) {
                this.delegated = rectifier = provider.get();
            }
            Objects.requireNonNull(rectifier);
            return rectifier;
        }
    }

    @Override
    public void process(I input, BiConsumer<O, ResultRaw> onSucceed, BiConsumer<ResultRaw, String> onFailed) {
        delegated().process(input, onSucceed, onFailed);
    }

    @Override
    public Schema schema() {
        return delegated().schema();
    }

    @Override
    public List<String> getHints() {
        return delegated().getHints();
    }

}
