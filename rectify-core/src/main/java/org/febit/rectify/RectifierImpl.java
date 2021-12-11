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

import org.febit.rectify.engine.ExitException;
import org.febit.rectify.engine.ScriptBuilder;
import org.febit.rectify.util.ResultModelUtils;
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

    protected final ResultModel<O> resultModel;
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
        hints.add(ScriptBuilder.VAR_CURR);

        // globals
        GlobalManager gm = script.getEngine().getGlobalManager();
        gm.forEachGlobal((k, v) -> hints.add(k));
        gm.forEachConst((k, v) -> hints.add(k));

        return hints;
    }

    protected RectifierImpl(Template script, Schema schema, ResultModel<O> resultModel) {
        Objects.requireNonNull(script);
        Objects.requireNonNull(schema);
        Objects.requireNonNull(resultModel);
        this.script = script;
        this.schema = schema;
        this.resultModel = resultModel;
    }

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

    protected Context executeScript(Vars vars) {
        return script.merge(vars);
    }

    public void process(I input, BiConsumer<O, ResultRaw> onSuccess, BiConsumer<ResultRaw, String> onFailed) {
        final ResultRaw resultRaw = new ResultRaw();
        try {
            executeScript(accepter -> {
                accepter.set(ScriptBuilder.VAR_INPUT, input);
                accepter.set(ScriptBuilder.VAR_RESULT, resultRaw);
            });
        } catch (ScriptRuntimeException e) {
            ExitException exitException = searchExitException(e);
            if (exitException != null) {
                onFailed.accept(resultRaw, exitException.getReason());
                return;
            }
            throw e;
        }
        @SuppressWarnings("unchecked")
        O record = (O) ResultModelUtils.convert(schema, resultRaw, resultModel);
        onSuccess.accept(record, resultRaw);
    }

    @Override
    public Schema schema() {
        return schema;
    }

}
