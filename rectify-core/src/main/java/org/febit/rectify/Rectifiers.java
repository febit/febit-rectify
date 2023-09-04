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
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.febit.lang.Lazy;
import org.febit.lang.SerializableSupplier;

import java.util.function.BiConsumer;
import java.util.function.Function;

@UtilityClass
public class Rectifiers {

    public static <I, O> SerializableRectifier<I, O> lazy(SerializableSupplier<Rectifier<I, O>> provider) {
        return new LazyRectifier<>(Lazy.of(provider));
    }

    public static <S, I, O> Rectifier<S, O> transformed(Rectifier<I, O> rectifier, Function<S, I> transfer) {
        return new TransformedRectifier<>(rectifier, transfer);
    }

    public static <S, I, O> Rectifier<S, O> formatted(Rectifier<I, O> rectifier, SourceFormat<S, I> sourceFormat) {
        return new SourceFormattedRectifier<>(rectifier, sourceFormat);
    }

    @RequiredArgsConstructor
    private static class LazyRectifier<I, O>
            implements Rectifier.Delegated<I, O, I, O>, SerializableRectifier<I, O> {

        private final Lazy<Rectifier<I, O>> delegated;

        public Rectifier<I, O> delegated() {
            return delegated.get();
        }

        @Override
        public void process(
                @Nullable I input,
                BiConsumer<O, RawOutput> onSucceed,
                BiConsumer<String, RawOutput> onFailed
        ) {
            delegated().process(input, onSucceed, onFailed);
        }
    }

    @RequiredArgsConstructor
    private static class TransformedRectifier<S, I, O> implements Rectifier.Delegated<S, O, I, O> {

        private final Rectifier<I, O> delegated;
        private final Function<S, I> transfer;

        public Rectifier<I, O> delegated() {
            return delegated;
        }

        @Override
        public void process(
                @Nullable S source,
                BiConsumer<O, RawOutput> onSucceed,
                BiConsumer<String, RawOutput> onFailed
        ) {
            var in = transfer.apply(source);
            delegated.process(in, onSucceed, onFailed);
        }
    }

    @RequiredArgsConstructor(staticName = "wrap")
    private static class SourceFormattedRectifier<S, I, O> implements Rectifier.Delegated<S, O, I, O> {

        private final Rectifier<I, O> delegated;
        private final SourceFormat<S, I> sourceFormat;

        public Rectifier<I, O> delegated() {
            return delegated;
        }

        public void process(@Nullable S source, BiConsumer<O, RawOutput> onSucceed, BiConsumer<String, RawOutput> onFailed) {
            sourceFormat.process(source, in -> delegated.process(in, onSucceed, onFailed));
        }
    }
}
