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
import org.febit.rectify.lib.IFunctions;
import org.febit.rectify.lib.IProto;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@SuppressWarnings({
        "java:S1118", // Utility classes should not have public constructors
        "unused",
})
public class StringFunctions implements IFunctions {

    @Alias(value = {"Str", "Strings"}, keepOriginName = false)
    public static final Proto STR = new Proto();

    public static class Proto implements IProto {
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

        public final Function1<@Nullable String, @Nullable String> lower = text -> ifPresent(text, String::toLowerCase);
        public final Function1<@Nullable String, @Nullable String> upper = text -> ifPresent(text, String::toUpperCase);

        public final Function1<String, String> toLowerCase = lower;
        public final Function1<String, String> toUpperCase = upper;

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
                (text, sub, with) -> ifPresent(text,
                        t -> Strings.CS.replace(t, sub, with)
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
