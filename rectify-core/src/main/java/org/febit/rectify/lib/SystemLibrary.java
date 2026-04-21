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
package org.febit.rectify.lib;

import org.febit.lang.func.Function1;
import org.febit.lang.func.Function3;
import org.febit.rectify.wit.ExitException;
import org.febit.rectify.wit.FilterBreakpoint;
import org.febit.rectify.wit.ScriptBuilder;
import org.jspecify.annotations.Nullable;

@SuppressWarnings({
        "unused",
        "java:S1118", // Utility classes should not have public constructors
})
public class SystemLibrary implements Library {

    @BindingAlias(value = {ScriptBuilder.VAR_EXIT}, keepDeclaredName = false)
    public static final Function1<@Nullable String, Object> EXIT = SystemLibrary::exit;

    @BindingAlias(value = {ScriptBuilder.VAR_FILTER_VERIFY}, keepDeclaredName = false)
    public static final Function1<@Nullable Object, @Nullable Object> VERIFY_FILTER = SystemLibrary::verifyFilter;

    @BindingAlias(value = {ScriptBuilder.VAR_CREATE_FILTER_BREAKPOINT}, keepDeclaredName = false)
    public static final Function3<@Nullable Integer, @Nullable String, @Nullable String, FilterBreakpoint>
            CREATE_FILTER_BREAKPOINT = FilterBreakpoint::of;

    private static Object exit(@Nullable String reason) {
        throw new ExitException(reason);
    }

    @Nullable
    private static Object verifyFilter(@Nullable Object accepted) {
        return switch (accepted) {
            // OK - if accepted is null
            case null -> null;
            case Boolean bool -> {
                // OK - if accepted is TRUE
                if (bool) {
                    yield null;
                }
                // Exit - if accepted is FALSE, without reason.
                yield exit(null);
            }
            // Exit - if accepted is String, with reason.
            case String str -> exit(str);
            // OK - for other cases.
            default -> null;
        };
    }
}
