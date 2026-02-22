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

import org.febit.lang.func.Function1;
import org.febit.lang.util.ConvertUtils;
import org.febit.rectify.lib.IFunctions;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

@SuppressWarnings({
        "java:S1118", // Utility classes should not have public constructors
        "unused",
})
public class ConvertFunctions implements IFunctions {

    public static final Function1<@Nullable Object, @Nullable Number> toNumber = ConvertUtils::toNumber;
    public static final Function1<@Nullable Object, @Nullable Byte> toByte = ConvertUtils::toByte;
    public static final Function1<@Nullable Object, @Nullable Short> toShort = ConvertUtils::toShort;
    public static final Function1<@Nullable Object, @Nullable Integer> toInteger = ConvertUtils::toInteger;
    public static final Function1<@Nullable Object, @Nullable Long> toLong = ConvertUtils::toLong;
    public static final Function1<@Nullable Object, @Nullable Float> toFloat = ConvertUtils::toFloat;
    public static final Function1<@Nullable Object, @Nullable Double> toDouble = ConvertUtils::toDouble;
    public static final Function1<@Nullable Object, @Nullable BigDecimal> toBigDecimal = ConvertUtils::toBigDecimal;
    public static final Function1<@Nullable Object, @Nullable Boolean> toBoolean = ConvertUtils::toBoolean;
    public static final Function1<@Nullable Object, @Nullable String> toString = ConvertUtils::toString;
}
