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

import org.febit.lang.TerConsumer;
import org.febit.rectify.engine.ExitException;
import org.febit.rectify.engine.ScriptBuilder;
import org.febit.rectify.util.ResultModelUtil;
import org.febit.wit.Context;
import org.febit.wit.Engine;
import org.febit.wit.Template;
import org.febit.wit.Vars;
import org.febit.wit.debug.BreakpointListener;
import org.febit.wit.exceptions.ResourceNotFoundException;
import org.febit.wit.exceptions.ScriptRuntimeException;
import org.febit.wit.global.GlobalManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Rectifier.
 *
 * @param <IN>  input Type
 * @param <OUT> out type
 * @author zqq90
 */
public class Rectifier<IN, OUT> {

    protected final boolean debug;
    protected final RectifierConf conf;
    protected final ResultModel<?> resultModel;
    protected Schema schema;
    protected Template script;
    protected SourceFormat<IN> sourceFormat;

    protected Rectifier(RectifierConf conf, boolean debug, ResultModel<OUT> resultModel) {
        Objects.requireNonNull(conf);
        Objects.requireNonNull(resultModel);
        this.conf = conf;
        this.debug = debug;
        this.resultModel = resultModel;
    }

    /**
     * Get script hints.
     *
     * @return hints string list
     */
    public static List<String> getHints() {
        List<String> hints = new ArrayList<>();

        // vars
        hints.add(ScriptBuilder.VAR_INPUT);
        hints.add(ScriptBuilder.VAR_CURR);

        // globals
        GlobalManager gm = EngineHolder.ENGINE.getGlobalManager();
        gm.forEachGlobal((k, v) -> hints.add(k));
        gm.forEachConst((k, v) -> hints.add(k));

        return hints;
    }

    /**
     * Create a {@code Rectifier} by conf.
     *
     * @param conf conf
     * @param <I>  input type
     * @return Rectifier
     */
    public static <I> Rectifier<I, GenericStruct> create(RectifierConf conf) {
        return create(conf, GenericStruct.model(), null);
    }

    /**
     * Create a {@code DebugRectifier} by conf.
     *
     * WARN: Poor performance, not for production environment.
     *
     * @param conf               conf
     * @param breakpointListener Wit breakpoint listener
     * @param <I>                input type
     * @return Rectifier
     */
    public static <I> Rectifier<I, GenericStruct> create(RectifierConf conf, BreakpointListener breakpointListener) {
        return create(conf, GenericStruct.model(), breakpointListener);
    }

    /**
     * Create a {@code Rectifier} by conf.
     *
     * @param conf        conf
     * @param resultModel ResultModel
     * @param <I>         input Type
     * @param <O>         out type
     * @return Rectifier
     */
    public static <I, O> Rectifier<I, O> create(RectifierConf conf, ResultModel<O> resultModel) {
        return create(conf, resultModel, null);
    }

    /**
     * Create a {@code DebugRectifier} by conf.
     *
     * WARN: Poor performance, not for production environment.
     *
     * @param conf               conf
     * @param resultModel        ResultModel
     * @param breakpointListener Wit breakpoint listener
     * @param <I>                input Type
     * @param <O>                out type
     * @return
     */
    public static <I, O> Rectifier<I, O> create(RectifierConf conf, ResultModel<O> resultModel, BreakpointListener breakpointListener) {
        Rectifier<I, O> processor;
        if (breakpointListener == null) {
            processor = new Rectifier<>(conf, false, resultModel);
        } else {
            processor = new DebugRectifier<>(conf, resultModel, breakpointListener);
        }
        processor.init();
        return processor;
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

    /**
     * Process input one by one.
     *
     * @param input    input
     * @param consumer consumer
     */
    public void process(IN input, TerConsumer<OUT, ResultRaw, String> consumer) {
        this.sourceFormat.process(input, record -> process(record, consumer));
    }

    private void init() {

        schema = conf.schema();
        sourceFormat = SourceFormats.create(conf);

        // init script
        try {
            String code = "code: " + conf.script(debug);
            script = EngineHolder.ENGINE.getTemplate(code);
            // fast-fail check
            script.reload();
        } catch (ResourceNotFoundException ex) {
            throw new RuntimeException("Failed to create template.", ex);
        }
    }

    /**
     * Execute script.
     *
     * @param input input
     * @return LogResult or a reason string
     */
    protected Object executeScript(Input input) {
        final ResultRaw result = new ResultRaw();
        try {
            executeScript((accepter) -> {
                accepter.set(ScriptBuilder.VAR_INPUT, input);
                accepter.set(ScriptBuilder.VAR_RESULT, result);
            });
        } catch (ScriptRuntimeException e) {
            ExitException exitException = searchExitException(e);
            if (exitException != null) {
                return exitException.getReason();
            }
            throw e;
        }
        return result;
    }

    protected Context executeScript(Vars vars) {
        return script.merge(vars);
    }

    private void process(Input input, TerConsumer<OUT, ResultRaw, String> action) {
        Object resultOrReason = executeScript(input);
        if (!(resultOrReason instanceof ResultRaw)) {
            action.accept(null, null,
                    resultOrReason == null ? null : resultOrReason.toString());
            return;
        }
        ResultRaw raw = (ResultRaw) resultOrReason;
        @SuppressWarnings("unchecked")
        OUT record = (OUT) ResultModelUtil.convert(schema, raw, resultModel);
        action.accept(record, raw, null);
    }

    public boolean isDebug() {
        return debug;
    }

    public Schema schema() {
        return schema;
    }

    private static class EngineHolder {
        static final Engine ENGINE = Engine.create("febit-rectifier-engine.wim");
    }

    private static class DebugRectifier<IN, OUT> extends Rectifier<IN, OUT> {

        private final BreakpointListener breakpointListener;

        private DebugRectifier(RectifierConf conf, ResultModel<OUT> resultModel, BreakpointListener breakpointListener) {
            super(conf, true, resultModel);
            Objects.requireNonNull(breakpointListener);
            this.breakpointListener = breakpointListener;
        }

        protected Context executeScript(Vars vars) {
            return script.debug(vars, breakpointListener);
        }
    }
}
