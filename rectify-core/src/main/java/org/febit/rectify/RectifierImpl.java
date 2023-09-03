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
import org.febit.rectify.engine.ExitException;
import org.febit.rectify.engine.ScriptBuilder;
import org.febit.rectify.util.OutputModelUtils;
import org.febit.wit.Context;
import org.febit.wit.Template;
import org.febit.wit.Vars;
import org.febit.wit.exceptions.ScriptRuntimeException;
import org.febit.wit.global.GlobalManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Rectifier impl.
 *
 * @param <I> input Type
 * @param <O> out type
 */
public class RectifierImpl<I, O> implements Rectifier<I, O> {

    protected final OutputModel<O> outputModel;
    protected final Schema schema;
    protected final Template script;

    /**
     * Get script hints.
     *
     * @return hints string list
     */
    public List<String> getHints() {
        List<String> hints = new ArrayList<>();

        // vars
        hints.add(ScriptBuilder.VAR_INPUT);
        hints.add(ScriptBuilder.VAR_CURR_FIELD);

        // globals
        GlobalManager gm = script.getEngine().getGlobalManager();
        gm.forEachGlobal((k, v) -> hints.add(k));
        gm.forEachConst((k, v) -> hints.add(k));

        return hints;
    }

    protected RectifierImpl(Template script, Schema schema, OutputModel<O> outputModel) {
        Objects.requireNonNull(script);
        Objects.requireNonNull(schema);
        Objects.requireNonNull(outputModel);
        this.script = script;
        this.schema = schema;
        this.outputModel = outputModel;
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

    @SuppressWarnings("UnusedReturnValue")
    protected Context executeScript(Vars vars) {
        return script.merge(vars);
    }

    public void process(@Nullable I input, BiConsumer<O, RawOutput> onSuccess, BiConsumer<String, RawOutput> onFailed) {
        final RawOutput rawOutput = new RawOutput();
        try {
            executeScript(accepter -> {
                accepter.set(ScriptBuilder.VAR_INPUT, input);
                accepter.set(ScriptBuilder.VAR_RESULT, rawOutput);
            });
        } catch (ScriptRuntimeException e) {
            var exitException = searchExitException(e);
            if (exitException != null) {
                onFailed.accept(exitException.getReason(), rawOutput);
                return;
            }
            onFailed.accept("RUNTIME_ERROR: " + e.getMessage(), rawOutput);
            return;
        }
        @SuppressWarnings("unchecked")
        O record = (O) OutputModelUtils.convert(schema, rawOutput, outputModel);
        onSuccess.accept(record, rawOutput);
    }

    @Override
    public Schema schema() {
        return schema;
    }

}
