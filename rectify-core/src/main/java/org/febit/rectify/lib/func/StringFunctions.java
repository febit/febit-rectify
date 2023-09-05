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
package org.febit.rectify.lib.func;

import org.apache.commons.lang3.StringUtils;
import org.febit.rectify.function.IFunctions;
import org.febit.rectify.function.StrFunc;
import org.febit.rectify.function.StrIntIntFunc;
import org.febit.rectify.function.StrStrFunc;
import org.febit.rectify.function.StrStrStrFunc;

import java.util.function.Function;

@SuppressWarnings({"unused"})
public class StringFunctions implements IFunctions {

    @Alias(value = {"Str", "Strings"}, keepOriginName = false)
    public static final Proto STR = new Proto();

    public static class Proto {
        public final StrFunc isEmpty = StringUtils::isEmpty;
        public final StrFunc isNotEmpty = StringUtils::isNotEmpty;
        public final StrFunc isBlank = StringUtils::isBlank;
        public final StrFunc isNotBlank = StringUtils::isNotBlank;

        public final StrFunc isNumeric = StringUtils::isNumeric;
        public final StrFunc isAlpha = StringUtils::isAlpha;
        public final StrFunc isAlphaSpace = StringUtils::isAlphaSpace;
        public final StrFunc isAlphaNumeric = StringUtils::isAlphanumeric;
        public final StrFunc isAlphaNumericSpace = StringUtils::isAlphanumericSpace;

        public final StrFunc trim = text -> ifPresent(text, String::trim);

        public final StrFunc lower = text -> ifPresent(text, String::toLowerCase);
        public final StrFunc upper = text -> ifPresent(text, String::toUpperCase);

        public final StrFunc toLowerCase = lower;
        public final StrFunc toUpperCase = upper;

        public final StrIntIntFunc sub = (text, start, end) -> ifPresent(text,
                t -> t.substring(
                        start == null ? 0 : start,
                        end == null ? t.length() : end
                ));

        public final StrStrFunc removeStart = (text, fix) -> ifPresent(text,
                t -> StringUtils.removeStart(t, fix));

        public final StrStrFunc removeEnd = (text, fix) -> ifPresent(text,
                t -> StringUtils.removeEnd(t, fix));

        public final StrStrFunc before = (text, flag) -> ifPresent(text,
                t -> StringUtils.substringBefore(t, flag));

        public final StrStrFunc after = (text, flag) -> ifPresent(text,
                t -> StringUtils.substringAfter(t, flag));

        public final StrStrFunc split = (text, separator) -> ifPresent(text,
                t -> StringUtils.splitByWholeSeparator(t, separator));

        public final StrStrStrFunc replace = (text, sub, with) -> ifPresent(text,
                t -> StringUtils.replace(t, sub, with)
        );
    }

    private static Object ifPresent(String text, Function<String, Object> action) {
        if (text == null) {
            return null;
        }
        return action.apply(text);
    }
}
