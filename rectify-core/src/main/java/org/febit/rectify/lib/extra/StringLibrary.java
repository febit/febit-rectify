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
package org.febit.rectify.lib.extra;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.febit.lang.func.Function1;
import org.febit.lang.func.Function2;
import org.febit.lang.func.Function3;
import org.febit.rectify.lib.BindingAlias;
import org.febit.rectify.lib.Library;
import org.febit.rectify.lib.Namespace;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@SuppressWarnings({
        "unused",
        "java:S1118", // Utility classes should not have public constructors
})
public class StringLibrary implements Library {

    @BindingAlias(value = {"Str", "Strings"}, keepDeclaredName = false)
    public static final StringNamespace STR = new StringNamespace();

    public static class StringNamespace implements Namespace {
        public final Function1<@Nullable String, Boolean> isEmpty = StringUtils::isEmpty;
        public final Function1<@Nullable String, Boolean> isNotEmpty = StringUtils::isNotEmpty;
        public final Function1<@Nullable String, Boolean> isBlank = StringUtils::isBlank;
        public final Function1<@Nullable String, Boolean> isNotBlank = StringUtils::isNotBlank;

        public final Function1<@Nullable String, Boolean> isNumeric = StringUtils::isNumeric;
        public final Function1<@Nullable String, Boolean> isAlpha = StringUtils::isAlpha;
        public final Function1<@Nullable String, Boolean> isAlphaSpace = StringUtils::isAlphaSpace;
        public final Function1<@Nullable String, Boolean> isAlphaNumeric = StringUtils::isAlphanumeric;
        public final Function1<@Nullable String, Boolean> isAlphaNumericSpace = StringUtils::isAlphanumericSpace;

        public final Function1<@Nullable String, @Nullable String> trim = text -> ifPresent(text, String::trim);

        @BindingAlias({"toLowerCase"})
        public final Function1<@Nullable String, @Nullable String> lower = text -> ifPresent(text, String::toLowerCase);
        @BindingAlias({"toUpperCase"})
        public final Function1<@Nullable String, @Nullable String> upper = text -> ifPresent(text, String::toUpperCase);

        public final Function3<@Nullable String, @Nullable Integer, @Nullable Integer, @Nullable String> sub =
                (text, start, end) -> ifPresent(text,
                        t -> t.substring(
                                start == null ? 0 : start,
                                end == null ? t.length() : end
                        )
                );

        public final Function2<@Nullable String, @Nullable String, @Nullable String> removeStart =
                (text, fix) -> ifPresent(text,
                        t -> Strings.CS.removeStart(t, fix)
                );

        public final Function2<@Nullable String, @Nullable String, @Nullable String> removeEnd =
                (text, fix) -> ifPresent(text,
                        t -> Strings.CS.removeEnd(t, fix)
                );

        public final Function2<@Nullable String, @Nullable String, @Nullable String> before =
                (text, flag) -> ifPresent(text,
                        t -> StringUtils.substringBefore(t, flag)
                );

        public final Function2<@Nullable String, @Nullable String, @Nullable String> after =
                (text, flag) -> ifPresent(text,
                        t -> StringUtils.substringAfter(t, flag)
                );

        public final Function2<@Nullable String, @Nullable String, String @Nullable []> split =
                (text, separator) -> ifPresent(text,
                        t -> StringUtils.splitByWholeSeparator(t, separator)
                );

        public final Function3<@Nullable String, @Nullable String, @Nullable String, @Nullable String> replace =
                (text, search, replacement) -> ifPresent(text,
                        t -> Strings.CS.replace(t, search, replacement)
                );

        @Nullable
        private static <T> T ifPresent(@Nullable String text, Function<String, @Nullable T> action) {
            if (text == null) {
                return null;
            }
            return action.apply(text);
        }
    }
}
