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

import org.febit.rectify.function.IFunctions;
import org.febit.rectify.function.ObjFunc;
import org.febit.rectify.util.Convert;

@SuppressWarnings({"unused"})
public class ConvertFunctions implements IFunctions {

    public static final ObjFunc toNumber = Convert::toNumber;
    public static final ObjFunc toByte = Convert::toByte;
    public static final ObjFunc toShort = Convert::toShort;
    public static final ObjFunc toInteger = Convert::toInteger;
    public static final ObjFunc toLong = Convert::toLong;
    public static final ObjFunc toFloat = Convert::toFloat;
    public static final ObjFunc toDouble = Convert::toDouble;
    public static final ObjFunc toBigDecimal = Convert::toBigDecimal;

    public static final ObjFunc toBoolean = Convert::toBoolean;
    public static final ObjFunc toString = Convert::toString;
}
