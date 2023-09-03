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
package org.febit.rectify.function;

import jakarta.annotation.Nullable;
import org.febit.rectify.engine.ExitException;
import org.febit.rectify.engine.FilterBreakpoint;
import org.febit.rectify.engine.ScriptBuilder;

@SuppressWarnings({"unused"})
public class BasicFunctions implements IFunctions {

    @Alias(value = {ScriptBuilder.VAR_EXIT}, keepOriginName = false)
    public static final StrFunc EXIT = BasicFunctions::exit;

    @Alias(value = {ScriptBuilder.VAR_CHECK_FILTER}, keepOriginName = false)
    public static final ObjFunc CHECK_FILTER = BasicFunctions::checkFilter;

    @Alias(value = {ScriptBuilder.VAR_NEW_FILTER_BREAKPOINT}, keepOriginName = false)
    public static final IntStrStrFunc NEW_FILTER_BREAKPOINT = FilterBreakpoint::of;

    private static Object exit(@Nullable String reason) {
        throw new ExitException(reason);
    }

    @Nullable
    private static Object checkFilter(@Nullable Object isAccept) {
        // Pass, if expr returns NULL
        if (isAccept == null) {
            return null;
        }
        if (isAccept instanceof Boolean) {
            // Pass, if expr is TRUE
            if ((Boolean) isAccept) {
                return null;
            }
            // Exit, if expr is FALSE
            return exit(null);
        }
        // Exit, if expr is STRING, as reason.
        if (isAccept instanceof String) {
            return exit((String) isAccept);
        }
        // Pass, by default
        return null;
    }
}
