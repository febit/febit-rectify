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
package org.febit.rectify.util;

import org.febit.rectify.function.IFunctions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionUtilsTest {

    @SuppressWarnings({"unused"})
    public static class Proto {

        public static final Object CONST = new Object();

        @IFunctions.Alias({"obj1Alias1", "obj1Alias2"})
        public static final Object CONST_WITH_ALIAS = new Object();

        @IFunctions.Alias(value = {"obj2Alias1"}, keepOriginName = false)
        public static final Object CONST_WITH_ALIAS_NO_ORIGIN = new Object();

        public static Object NON_FINAL = new Object();
        public final Object NON_STATIC = new Object();
        private static final Object PRIVATE = new Object();
    }

    @Test
    void scanConstFields() {
        var map = new HashMap<String, Object>();
        FunctionUtils.scanConstFields(map::put, Proto.class);

        assertThat(map)
                .containsEntry("CONST", Proto.CONST)
                .containsEntry("CONST_WITH_ALIAS", Proto.CONST_WITH_ALIAS)
                .containsEntry("obj1Alias1", Proto.CONST_WITH_ALIAS)
                .containsEntry("obj1Alias2", Proto.CONST_WITH_ALIAS)
                .containsEntry("obj2Alias1", Proto.CONST_WITH_ALIAS_NO_ORIGIN)
                .doesNotContainKey("CONST_WITH_ALIAS_NO_ORIGIN")
                .doesNotContainKeys(
                        "NON_FINAL", "NON_STATIC", "PRIVATE"
                );

    }
}
