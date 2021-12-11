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
package org.febit.rectify.engine;

import org.febit.rectify.EnginePlugin;
import org.febit.util.ArraysUtil;
import org.febit.wit.Engine;
import org.febit.wit.InternalContext;
import org.febit.wit.global.GlobalManager;
import org.febit.wit.lang.UnConstableMethodDeclare;

@SuppressWarnings({
        "squid:S1172" // Unused method parameters should be removed
})
public class InternalMethods implements EnginePlugin {

    private static FilterBreakpoint newFilterBreakpoint(InternalContext context, Object[] args) {
        Object index = ArraysUtil.get(args, 0, 0);
        Object field = ArraysUtil.get(args, 1);
        Object expr = ArraysUtil.get(args, 2);
        return FilterBreakpoint.of(
                index instanceof Number
                        ? ((Number) index).intValue()
                        : Integer.parseInt(index.toString()),
                field != null ? field.toString() : null,
                expr != null ? expr.toString() : null
        );
    }

    private static Object exit(Object reason) {
        throw new ExitException(reason != null ? reason.toString() : null);
    }

    private static Object exit(InternalContext context, Object[] args) {
        Object msg = ArraysUtil.get(args, 0);
        return exit(msg);
    }

    private static Object checkFilter(InternalContext context, Object[] args) {
        // Pass, if args is empty
        if (args == null || args.length == 0) {
            return null;
        }
        final Object isAccept = args[0];
        // Pass, if expr returns NULL
        if (isAccept == null) {
            return null;
        }
        if (isAccept instanceof Boolean) {
            // Pass, if expr is TRUE
            if (((Boolean) isAccept).booleanValue()) {
                return null;
            }
            // Exit, if expr is FALSE
            return exit(null);
        }
        // Exit, if expr is STRING, as reason.
        if (isAccept instanceof String) {
            return exit(isAccept);
        }
        // Pass, by default
        return null;
    }

    @Override
    public void apply(Engine engine) {
        GlobalManager globalManager = engine.getGlobalManager();

        globalManager.setConstMethod(ScriptBuilder.VAR_EXIT,
                (UnConstableMethodDeclare) InternalMethods::exit);
        globalManager.setConstMethod(ScriptBuilder.VAR_CHECK_FILTER,
                (UnConstableMethodDeclare) InternalMethods::checkFilter);
        globalManager.setConstMethod(ScriptBuilder.VAR_NEW_FILTER_BREAKPOINT,
                InternalMethods::newFilterBreakpoint);
    }
}
