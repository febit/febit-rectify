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

import org.febit.lang.func.Function0;
import org.febit.lang.func.Function1;
import org.febit.rectify.lib.BindingAlias;
import org.febit.rectify.lib.Library;
import org.febit.rectify.lib.Namespace;
import org.febit.rectify.wit.function.LibFunction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({
        "unused",
        "java:S1118", // Utility classes should not have public constructors
})
public class CollectionLibrary implements Library {

    @BindingAlias(value = {"List"}, keepDeclaredName = false)
    public static final ListNamespace LIST = new ListNamespace();

    @BindingAlias(value = {"Set"}, keepDeclaredName = false)
    public static final SetNamespace SET = new SetNamespace();

    @BindingAlias(value = {"Map"}, keepDeclaredName = false)
    public static final MapNamespace MAP = new MapNamespace();

    public static class ListNamespace implements Namespace {
        public final Function0<Object> create = ArrayList::new;

        public final LibFunction of = List::of;
        public final LibFunction ofNullable = ListNamespace::ofNullable;
        public final Function0<List<Object>> empty = Collections::emptyList;

        public final Function1<Object, List<Object>> copyOf = ListNamespace::copyOf;
        public final Function1<Object, List<@Nullable Object>>
                copyOfNullable = ListNamespace::copyOfNullable;

        private static List<@Nullable Object> ofNullable(@Nullable Object... elements) {
            return copyOfNullable(Arrays.asList(elements));
        }

        private static List<Object> copyOf(Object collection) {
            return List.copyOf((Collection<?>) collection);
        }

        private static List<@Nullable Object> copyOfNullable(Object collection) {
            var list = new ArrayList<>((Collection<? extends @Nullable Object>) collection);
            return Collections.unmodifiableList(list);
        }
    }

    public static class SetNamespace implements Namespace {

        public final Function0<Object> create = HashSet::new;

        public final LibFunction of = Set::of;
        public final LibFunction ofNullable = SetNamespace::ofNullable;
        public final Function0<Set<Object>> empty = Collections::emptySet;

        public final Function1<Object, Set<Object>> copyOf = SetNamespace::copyOf;
        public final Function1<Object, Set<@Nullable Object>>
                copyOfNullable = SetNamespace::copyOfNullable;

        private static Set<@Nullable Object> ofNullable(@Nullable Object... elements) {
            return copyOfNullable(Arrays.asList(elements));
        }

        private static Set<Object> copyOf(Object collection) {
            return Set.copyOf((Collection<?>) collection);
        }

        private static Set<@Nullable Object> copyOfNullable(Object collection) {
            var set = new HashSet<>((Collection<? extends @Nullable Object>) collection);
            return Collections.unmodifiableSet(set);
        }
    }

    public static class MapNamespace implements Namespace {
        public final Function0<Object> create = LinkedHashMap::new;

        public final Function0<Map<Object, Object>> empty = Collections::emptyMap;
        public final Function1<Object, Map<Object, Object>> copyOf = MapNamespace::copyOf;
        public final Function1<Object, Map<Object, Object>> copyOfNullable = MapNamespace::copyOfNullable;

        private static Map<Object, Object> copyOf(Object map) {
            return Map.copyOf((Map<?, ?>) map);
        }

        private static Map<Object, Object> copyOfNullable(Object map) {
            var source = (Map<?, ?>) map;
            var copy = new LinkedHashMap<>(source);
            return Collections.unmodifiableMap(copy);
        }
    }

}
