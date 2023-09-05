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

import lombok.extern.slf4j.Slf4j;
import org.febit.rectify.function.IFunctions;
import org.febit.rectify.function.ObjFunc;
import org.febit.rectify.function.ObjObjFunc;
import org.febit.rectify.function.VoidFunc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@SuppressWarnings({"unused"})
public class CommonFunctions implements IFunctions {

    private static final AtomicLong NEXT_ID = new AtomicLong(1);

    public static final VoidFunc noop = () -> null;

    public static final VoidFunc seq = NEXT_ID::getAndIncrement;
    public static final VoidFunc uuid = UUID::randomUUID;

    public static final VoidFunc newList = ArrayList::new;
    public static final VoidFunc newSet = HashSet::new;
    public static final VoidFunc newMap = LinkedHashMap::new;
    public static final ObjFunc size = org.febit.wit.util.CollectionUtil::getSize;

    public static final ObjFunc isNull = Objects::isNull;
    public static final ObjFunc nonNull = Objects::nonNull;
    public static final ObjObjFunc isEquals = Objects::equals;
}
