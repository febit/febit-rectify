package org.febit.rectify.util;

import lombok.val;
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
        val map = new HashMap<String, Object>();
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
