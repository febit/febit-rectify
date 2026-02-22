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

import lombok.experimental.UtilityClass;
import org.febit.lang.Lazy;
import org.febit.lang.func.SerializableSupplier;
import org.jspecify.annotations.Nullable;

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

    private record LazyRectifier<I, O>(
            Lazy<Rectifier<I, O>> lazy
    ) implements Rectifier.Decorator<I, O, I, O>, SerializableRectifier<I, O> {

        public Rectifier<I, O> delegate() {
            return lazy.get();
        }

        @Override
        public void process(
                @Nullable I input,
                BiConsumer<O, RawOutput> onSucceed,
                BiConsumer<String, RawOutput> onFailed
        ) {
            delegate().process(input, onSucceed, onFailed);
        }
    }

    private record TransformedRectifier<S, I, O>(
            Rectifier<I, O> delegate,
            Function<@Nullable S, I> transfer
    ) implements Rectifier.Decorator<S, O, I, O> {

        @Override
        public void process(
                @Nullable S source,
                BiConsumer<O, RawOutput> onSucceed,
                BiConsumer<String, RawOutput> onFailed
        ) {
            var in = transfer.apply(source);
            delegate.process(in, onSucceed, onFailed);
        }
    }

    private record SourceFormattedRectifier<S, I, O>(
            Rectifier<I, O> delegate,
            SourceFormat<S, I> sourceFormat
    ) implements Rectifier.Decorator<S, O, I, O> {

        public void process(
                @Nullable S source,
                BiConsumer<O, RawOutput> onSucceed,
                BiConsumer<String, RawOutput> onFailed
        ) {
            sourceFormat.process(source, in -> delegate.process(in, onSucceed, onFailed));
        }
    }
}
