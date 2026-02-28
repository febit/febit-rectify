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
import org.febit.lang.modeler.Modeler;
import org.febit.lang.modeler.Schema;
import org.febit.rectify.wit.ExitException;
import org.febit.rectify.wit.ScriptBuilder;
import org.febit.wit.Context;
import org.febit.wit.Vars;
import org.febit.wit.exception.ScriptEvaluateException;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Rectifier impl.
 *
 * @param <I> input Type
 * @param <O> out type
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class RectifierImpl<I, O> implements Rectifier<I, O> {

    private final Schema schema;
    private final Modeler outputModeler;
    private final Supplier<List<String>> hints;
    private final Function<Vars, Context> script;

    /**
     * Get script hints.
     *
     * @return hints string list
     */
    @Override
    public List<String> hints() {
        return hints.get();
    }

    @Override
    public Schema schema() {
        return schema;
    }

    @Nullable
    private static ExitException searchExitException(Throwable exception) {
        int i = 0;
        do {
            if (exception == null
                    || (exception instanceof ExitException)) {
                return (ExitException) exception;
            }
            exception = exception.getCause();
            i++;
        } while (i < 10);
        return null;
    }

    @Override
    public void process(
            @Nullable I input,
            BiConsumer<@Nullable O, RawOutput> onSuccess,
            BiConsumer<@Nullable String, RawOutput> onFailed
    ) {
        var rawOutput = new RawOutput();
        try {
            script.apply(acceptor -> {
                acceptor.set(ScriptBuilder.VAR_INPUT, input);
                acceptor.set(ScriptBuilder.VAR_RESULT, rawOutput);
            });
        } catch (ScriptEvaluateException e) {
            var exitException = searchExitException(e);
            if (exitException != null) {
                onFailed.accept(exitException.getReason(), rawOutput);
                return;
            }
            onFailed.accept("RUNTIME_ERROR: " + e.getMessage(), rawOutput);
            return;
        }
        @SuppressWarnings("unchecked")
        O output = (O) outputModeler.process(schema, rawOutput);
        onSuccess.accept(output, rawOutput);
    }

}
