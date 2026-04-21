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
package org.febit.rectify.wit.function;

import org.febit.lang.func.Consumer0;
import org.febit.lang.func.Consumer1;
import org.febit.lang.func.Consumer2;
import org.febit.lang.func.Consumer3;
import org.febit.lang.func.Consumer4;
import org.febit.lang.func.Consumer5;
import org.febit.lang.func.Function0;
import org.febit.lang.func.Function1;
import org.febit.lang.func.Function2;
import org.febit.lang.func.Function3;
import org.febit.lang.func.Function4;
import org.febit.lang.func.Function5;
import org.febit.lang.func.IFunction;
import org.febit.lang.func.ThrowingCallable;
import org.febit.lang.func.ThrowingConsumer0;
import org.febit.lang.func.ThrowingConsumer1;
import org.febit.lang.func.ThrowingConsumer2;
import org.febit.lang.func.ThrowingConsumer3;
import org.febit.lang.func.ThrowingConsumer4;
import org.febit.lang.func.ThrowingConsumer5;
import org.febit.lang.func.ThrowingFunction0;
import org.febit.lang.func.ThrowingFunction1;
import org.febit.lang.func.ThrowingFunction2;
import org.febit.lang.func.ThrowingFunction3;
import org.febit.lang.func.ThrowingFunction4;
import org.febit.lang.func.ThrowingFunction5;
import org.febit.lang.func.ThrowingRunnable;
import org.febit.lang.func.ThrowingSupplier;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class LibFunctionsTest {

    static final AtomicReference<Trace> trace = new AtomicReference<>();

    private static Trace trace(String name, Object... args) {
        return new Trace(name, List.of(args));
    }

    @Test
    void wrapFunction0() {
        assertEquals("done", wrap("function0").apply());
    }

    @Test
    void wrapConsumer0() {
        trace.set(null);

        var func = wrap("consumer0");

        assertNull(func.apply());
        assertEquals(trace("consumer0"), trace.get());
    }

    @Test
    void wrapFunction1WithConversion() {
        assertEquals(124, wrap("function1").apply("123"));
    }

    @Test
    void wrapFunction2WithConversion() {
        assertEquals(7, wrap("function2").apply("3", "4"));
    }

    @Test
    void wrapFunction3WithConversion() {
        assertEquals(24, wrap("function3").apply("2", "3", "4"));
    }

    @Test
    void wrapFunction4WithConversion() {
        assertEquals(10, wrap("function4").apply("1", "2", "3", "4"));
    }

    @Test
    void wrapFunction5WithConversion() {
        assertEquals(15, wrap("function5").apply("1", "2", "3", "4", "5"));
    }

    @Test
    void wrapConsumer1WithConversion() {
        trace.set(null);

        var func = wrap("consumer1");

        assertNull(func.apply("7"));
        assertEquals(trace("consumer1", 7), trace.get());
    }

    @Test
    void wrapConsumer2WithConversion() {
        trace.set(null);

        var func = wrap("consumer2");

        assertNull(func.apply("2", "3"));
        assertEquals(trace("consumer2", 2, 3), trace.get());
    }

    @Test
    void wrapConsumer3WithConversion() {
        trace.set(null);

        var func = wrap("consumer3");

        assertNull(func.apply("2", "3", "4"));
        assertEquals(trace("consumer3", 2, 3, 4), trace.get());
    }

    @Test
    void wrapConsumer4WithConversion() {
        trace.set(null);

        var func = wrap("consumer4");

        assertNull(func.apply("2", "3", "4", "5"));
        assertEquals(trace("consumer4", 2, 3, 4, 5), trace.get());
    }

    @Test
    void wrapConsumer5WithConversion() {
        trace.set(null);

        var func = wrap("consumer5");

        assertNull(func.apply("2", "3", "4", "5", "6"));
        assertEquals(trace("consumer5", 2, 3, 4, 5, 6), trace.get());
    }

    @Test
    void wrapThrowingFunction0() {
        var func = wrap("throwingFunction0");
        assertEquals("x", func.apply());

        var boom = wrap("throwingFunction0Boom");
        assertThatThrownBy(boom::apply)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    @Test
    void wrapThrowingCallable() {
        assertEquals("callable", wrap("throwingCallable").apply());
    }

    @Test
    void wrapThrowingSupplier() {
        assertEquals("supplied", wrap("throwingSupplier").apply());
    }

    @Test
    void wrapThrowingConsumer0() {
        trace.set(null);

        var func = wrap("throwingConsumer0");

        assertNull(func.apply());
        assertEquals(trace("throwingConsumer0"), trace.get());
    }

    @Test
    void wrapThrowingFunction1WithConversion() {
        assertEquals(12, wrap("throwingFunction1").apply("10"));
    }

    @Test
    void wrapThrowingFunction2WithConversion() {
        assertEquals(11, wrap("throwingFunction2").apply("5", "6"));
    }

    @Test
    void wrapThrowingFunction3WithConversion() {
        assertEquals(24, wrap("throwingFunction3").apply("2", "3", "4"));
    }

    @Test
    void wrapThrowingFunction4WithConversion() {
        assertEquals(10, wrap("throwingFunction4").apply("1", "2", "3", "4"));
    }

    @Test
    void wrapThrowingFunction5WithConversion() {
        assertEquals(15, wrap("throwingFunction5").apply("1", "2", "3", "4", "5"));
    }

    @Test
    void wrapThrowingConsumer1WithConversion() {
        trace.set(null);

        var func = wrap("throwingConsumer1");

        assertNull(func.apply("11"));
        assertEquals(trace("throwingConsumer1", 11), trace.get());
    }

    @Test
    void wrapThrowingConsumer2WithConversion() {
        trace.set(null);

        var func = wrap("throwingConsumer2");

        assertNull(func.apply("7", "8"));
        assertEquals(trace("throwingConsumer2", 7, 8), trace.get());
    }

    @Test
    void wrapThrowingConsumer3WithConversion() {
        trace.set(null);

        var func = wrap("throwingConsumer3");

        assertNull(func.apply("7", "8", "9"));
        assertEquals(trace("throwingConsumer3", 7, 8, 9), trace.get());
    }

    @Test
    void wrapThrowingConsumer4WithConversion() {
        trace.set(null);

        var func = wrap("throwingConsumer4");

        assertNull(func.apply("7", "8", "9", "10"));
        assertEquals(trace("throwingConsumer4", 7, 8, 9, 10), trace.get());
    }

    @Test
    void wrapThrowingConsumer5WithConversion() {
        trace.set(null);

        var func = wrap("throwingConsumer5");

        assertNull(func.apply("7", "8", "9", "10", "11"));
        assertEquals(trace("throwingConsumer5", 7, 8, 9, 10, 11), trace.get());
    }

    @Test
    void wrapThrowingRunnable() {
        var func = wrap("throwingRunnable");
        trace.set(null);

        assertNull(assertDoesNotThrow((org.junit.jupiter.api.function.ThrowingSupplier<Object>) func::apply));
        assertEquals(trace("throwingRunnable"), trace.get());

        var boom = wrap("throwingRunnableBoom");
        assertThatThrownBy(boom::apply)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom-runnable");
    }

    @Test
    void wrapUnsupportedFunction() {
        var ex = assertThrows(IllegalArgumentException.class, () -> wrap("unsupported"));
        assertTrue(ex.getMessage().startsWith("Unsupported function: "));
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void wrapNullFunction() {
        assertThrows(NullPointerException.class, () -> LibFunctions.wrap(null, Object.class));
    }

    private static LibFunction wrap(String fieldName) {
        try {
            Field field = Fixtures.class.getField(fieldName);
            return LibFunctions.wrap((IFunction) field.get(null), field.getGenericType());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    record Trace(String name, List<Object> args) {
    }

    @SuppressWarnings("unused")
    public static class Fixtures {

        private static void trace(String name, Object... args) {
            trace.set(LibFunctionsTest.trace(name, args));
        }

        public static final Function0<String> function0 = () -> "done";
        public static final Consumer0 consumer0 = () -> trace("consumer0");
        public static final Function1<Integer, Integer> function1 = value -> value + 1;
        public static final Function2<Integer, Integer, Integer> function2 = Integer::sum;
        public static final Function3<Integer, Integer, Integer, Integer> function3 = (a, b, c) -> a * b * c;
        public static final Function4<Integer, Integer, Integer, Integer, Integer> function4 =
                (a, b, c, d) -> a + b + c + d;
        public static final Function5<Integer, Integer, Integer, Integer, Integer, Integer> function5 =
                (a, b, c, d, e) -> a + b + c + d + e;
        public static final Consumer1<Integer> consumer1 = a -> trace("consumer1", a);
        public static final Consumer2<Integer, Integer> consumer2 = (a, b) -> trace("consumer2", a, b);
        public static final Consumer3<Integer, Integer, Integer> consumer3 = (a, b, c) -> trace("consumer3", a, b, c);
        public static final Consumer4<Integer, Integer, Integer, Integer> consumer4 =
                (a, b, c, d) -> trace("consumer4", a, b, c, d);
        public static final Consumer5<Integer, Integer, Integer, Integer, Integer> consumer5 =
                (a, b, c, d, e) -> trace("consumer5", a, b, c, d, e);
        public static final ThrowingFunction0<String, RuntimeException> throwingFunction0 = () -> "x";
        public static final ThrowingFunction0<String, RuntimeException> throwingFunction0Boom = () -> {
            throw new IllegalStateException("boom");
        };
        public static final ThrowingCallable<String, RuntimeException> throwingCallable = () -> "callable";
        public static final ThrowingSupplier<String, RuntimeException> throwingSupplier = () -> "supplied";
        public static final ThrowingConsumer0<RuntimeException> throwingConsumer0 = () -> trace("throwingConsumer0");
        public static final ThrowingFunction1<Integer, Integer, RuntimeException> throwingFunction1 = value -> value
                + 2;
        public static final ThrowingFunction2<Integer, Integer, Integer, RuntimeException> throwingFunction2 = Integer::sum;
        public static final ThrowingFunction3<Integer, Integer, Integer, Integer, RuntimeException> throwingFunction3 =
                (a, b, c) -> a * b * c;
        public static final ThrowingFunction4<Integer, Integer, Integer, Integer, Integer, RuntimeException> throwingFunction4 =
                (a, b, c, d) -> a + b + c + d;
        public static final ThrowingFunction5<Integer, Integer, Integer, Integer, Integer, Integer, RuntimeException> throwingFunction5 =
                (a, b, c, d, e) -> a + b + c + d + e;
        public static final ThrowingConsumer1<Integer, RuntimeException> throwingConsumer1 = a -> trace("throwingConsumer1", a);
        public static final ThrowingConsumer2<Integer, Integer, RuntimeException> throwingConsumer2 =
                (a, b) -> trace("throwingConsumer2", a, b);
        public static final ThrowingConsumer3<Integer, Integer, Integer, RuntimeException> throwingConsumer3 =
                (a, b, c) -> trace("throwingConsumer3", a, b, c);
        public static final ThrowingConsumer4<Integer, Integer, Integer, Integer, RuntimeException> throwingConsumer4 =
                (a, b, c, d) -> trace("throwingConsumer4", a, b, c, d);
        public static final ThrowingConsumer5<Integer, Integer, Integer, Integer, Integer, RuntimeException> throwingConsumer5 =
                (a, b, c, d, e) -> trace("throwingConsumer5", a, b, c, d, e);
        public static final ThrowingRunnable<RuntimeException> throwingRunnable = () -> trace("throwingRunnable");
        public static final ThrowingRunnable<RuntimeException> throwingRunnableBoom = () -> {
            throw new IllegalStateException("boom-runnable");
        };
        public static final IFunction unsupported = new IFunction() {
        };
    }
}
