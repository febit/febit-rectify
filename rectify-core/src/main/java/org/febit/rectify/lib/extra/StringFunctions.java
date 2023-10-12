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
import org.febit.lang.func.Function1;
import org.febit.lang.func.Function2;
import org.febit.lang.func.Function3;
import org.febit.rectify.lib.IFunctions;
import org.febit.rectify.lib.IProto;

import java.util.function.Function;

@SuppressWarnings({"unused"})
public class StringFunctions implements IFunctions {

    @Alias(value = {"Str", "Strings"}, keepOriginName = false)
    public static final Proto STR = new Proto();

    private static <T> T ifPresent(String text, Function<String, T> action) {
        if (text == null) {
            return null;
        }
        return action.apply(text);
    }

    public static class Proto implements IProto {
        public final Function1<String, Boolean> isEmpty = StringUtils::isEmpty;
        public final Function1<String, Boolean> isNotEmpty = StringUtils::isNotEmpty;
        public final Function1<String, Boolean> isBlank = StringUtils::isBlank;
        public final Function1<String, Boolean> isNotBlank = StringUtils::isNotBlank;

        public final Function1<String, Boolean> isNumeric = StringUtils::isNumeric;
        public final Function1<String, Boolean> isAlpha = StringUtils::isAlpha;
        public final Function1<String, Boolean> isAlphaSpace = StringUtils::isAlphaSpace;
        public final Function1<String, Boolean> isAlphaNumeric = StringUtils::isAlphanumeric;
        public final Function1<String, Boolean> isAlphaNumericSpace = StringUtils::isAlphanumericSpace;

        public final Function1<String, String> trim = text -> ifPresent(text, String::trim);

        public final Function1<String, String> lower = text -> ifPresent(text, String::toLowerCase);
        public final Function1<String, String> upper = text -> ifPresent(text, String::toUpperCase);

        public final Function1<String, String> toLowerCase = lower;
        public final Function1<String, String> toUpperCase = upper;

        public final Function3<String, Integer, Integer, String> sub = (text, start, end) -> ifPresent(text,
                t -> t.substring(
                        start == null ? 0 : start,
                        end == null ? t.length() : end
                )
        );

        public final Function2<String, String, String> removeStart = (text, fix) -> ifPresent(text,
                t -> StringUtils.removeStart(t, fix)
        );

        public final Function2<String, String, String> removeEnd = (text, fix) -> ifPresent(text,
                t -> StringUtils.removeEnd(t, fix)
        );

        public final Function2<String, String, String> before = (text, flag) -> ifPresent(text,
                t -> StringUtils.substringBefore(t, flag)
        );

        public final Function2<String, String, String> after = (text, flag) -> ifPresent(text,
                t -> StringUtils.substringAfter(t, flag)
        );

        public final Function2<String, String, String[]> split = (text, separator) -> ifPresent(text,
                t -> StringUtils.splitByWholeSeparator(t, separator)
        );

        public final Function3<String, String, String, String> replace = (text, sub, with) -> ifPresent(text,
                t -> StringUtils.replace(t, sub, with)
        );
    }
}
