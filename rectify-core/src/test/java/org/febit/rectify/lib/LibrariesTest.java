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
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class LibrariesTest {

    @SuppressWarnings({"unused"})
    public static class TestLib {

        public static final Object CONST = new Object();

        @BindingAlias({"obj1Alias1", "obj1Alias2"})
        public static final Object CONST_WITH_ALIAS = new Object();

        @BindingAlias(value = {"obj2Alias1"}, keepDeclaredName = false)
        public static final Object CONST_WITH_ALIAS_NO_DECLARED = new Object();

        public static Object NON_FINAL = new Object();
        public final Object NON_STATIC = new Object();
        private static final Object PRIVATE = new Object();

        public static final DemoNamespace DEMO = new DemoNamespace();

        public static class DemoNamespace implements Namespace {
            public final Object nullObj = null;
            public final Object obj = new Object();
            public final Function1<@Nullable String, Boolean> isNull = Objects::isNull;
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void collect() {
        var map = new HashMap<String, Object>();
        Libraries.collect(TestLib.class, map::put);

        assertThat(map)
                .containsEntry("CONST", TestLib.CONST)
                .containsKey("DEMO")
                .containsEntry("CONST_WITH_ALIAS", TestLib.CONST_WITH_ALIAS)
                .containsEntry("obj1Alias1", TestLib.CONST_WITH_ALIAS)
                .containsEntry("obj1Alias2", TestLib.CONST_WITH_ALIAS)
                .containsEntry("obj2Alias1", TestLib.CONST_WITH_ALIAS_NO_DECLARED)
                .doesNotContainKey("CONST_WITH_ALIAS_NO_DECLARED")
                .doesNotContainKeys(
                        "NON_FINAL", "NON_STATIC", "PRIVATE"
                );

        assertThat((Map<String, Object>) map.get("DEMO"))
                .containsKey("obj")
                .containsKey("isNull")
                .doesNotContainKey("nullObj");
    }
}
