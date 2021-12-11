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

import org.febit.wit.Context;
import org.febit.wit.Template;
import org.febit.wit.Vars;
import org.febit.wit.debug.BreakpointListener;

import java.util.Objects;

class RectifierDebugImpl<I, O> extends RectifierImpl<I, O> {

    private final BreakpointListener breakpointListener;

    protected RectifierDebugImpl(Template script, Schema schema, ResultModel<O> resultModel, BreakpointListener breakpointListener) {
        super(script, schema, resultModel);
        Objects.requireNonNull(breakpointListener);
        this.breakpointListener = breakpointListener;
    }

    @Override
    protected Context executeScript(Vars vars) {
        return script.debug(vars, breakpointListener);
    }
}
