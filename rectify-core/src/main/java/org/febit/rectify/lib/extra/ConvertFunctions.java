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

import java.math.BigDecimal;

@SuppressWarnings({"unused"})
public class ConvertFunctions implements IFunctions {

    public static final Function1<Object, Number> toNumber = ConvertUtils::toNumber;
    public static final Function1<Object, Byte> toByte = ConvertUtils::toByte;
    public static final Function1<Object, Short> toShort = ConvertUtils::toShort;
    public static final Function1<Object, Integer> toInteger = ConvertUtils::toInteger;
    public static final Function1<Object, Long> toLong = ConvertUtils::toLong;
    public static final Function1<Object, Float> toFloat = ConvertUtils::toFloat;
    public static final Function1<Object, Double> toDouble = ConvertUtils::toDouble;
    public static final Function1<Object, BigDecimal> toBigDecimal = ConvertUtils::toBigDecimal;

    public static final Function1<Object, Boolean> toBoolean = ConvertUtils::toBoolean;
    public static final Function1<Object, String> toString = ConvertUtils::toString;
}
