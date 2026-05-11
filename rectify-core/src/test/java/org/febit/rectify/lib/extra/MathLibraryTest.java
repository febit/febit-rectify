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

import org.febit.rectify.lib.Lib;
import org.febit.rectify.lib.extra.MathLibrary.Support;
import org.febit.wit.exception.ScriptEvaluateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

import static org.febit.rectify.lib.LibraryTestSupport.namespace;
import static org.junit.jupiter.api.Assertions.*;

class MathLibraryTest {

    private static final double DOUBLE_DELTA = 0.000000000001d;
    private static final float FLOAT_DELTA = 0.0000001f;

    private final Lib math = namespace(MathLibrary.class, "Math");

    static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }

    private Object call(String name, Object... args) {
        return math.call(name, args);
    }

    @Test
    void sumOf() {
        // Empty input.
        assertInt(0, call("sumOf", List.of()));
        // Flat values.
        assertInt(6, call("sumOf", List.of(1, 2, 3)));
        assertDouble(6.0d, call("sumOf", 1.5d, 2.5d, List.of(2)));
        // Mixed scalar + nested iterable.
        assertBigDecimal("10.0", call("sumOf", 0, 1, 2, 3, List.of(
                decimal("1.5"), decimal("2.5")
        )));
    }

    @Test
    void maxOf() {
        // Same type inputs
        assertByte((byte) 1, call("maxOf", (byte) 1));
        assertShort((short) 2, call("maxOf", (short) 2));
        assertInt(3, call("maxOf", 3));
        assertLong(4L, call("maxOf", 4L));
        assertFloat(1.5f, call("maxOf", 1.5f));
        assertDouble(2.5d, call("maxOf", 2.5d));
        assertBigInteger("6", call("maxOf", new BigInteger("6")));
        assertBigDecimal("7.5", call("maxOf", decimal("7.5")));

        // Mixed types with nested inputs
        assertInt(3, call("maxOf", List.of(1, 2, 3)));
        assertDouble(2.5d, call("maxOf", 1.5d, 2.5d));
        assertInt(3, call("maxOf", 0, 1, 2, 3, List.of(
                decimal("1.5"), decimal("2.5")
        )));

        // Empty
        var empty = List.of();
        assertThrows(ScriptEvaluateException.class, () -> call("maxOf"));
        assertThrows(ScriptEvaluateException.class, () -> call("maxOf", empty));
        assertThrows(ScriptEvaluateException.class, () -> call("maxOf", empty, empty, empty));
    }

    @Test
    void minOf() {
        var empty = List.of();
        assertThrows(ScriptEvaluateException.class, () -> call("minOf", empty));

        assertInt(1, call("minOf", List.of(1, 2, 3)));
        assertDouble(1.5d, call("minOf", 1.5d, 2.5d));
        assertInt(0, call("minOf", 0, 1, 2, 3, List.of(
                decimal("1.5"), decimal("2.5")
        )));
    }

    @Test
    void reduceNestedInputs() {
        var inputs = Arrays.asList(
                null,
                List.of(1, new Object[]{2, List.of(3, 'A')}),
                new AtomicInteger(4)
        );

        assertInt(75, call("sumOf", inputs.stream()));
        assertInt(65, call("maxOf", inputs.stream()));
        assertInt(1, call("minOf", inputs.stream()));
    }

    @Test
    void reduceInvalidInputs() {
        var invalid = new Object();
        var invalidList = List.of(invalid);

        assertThrows(ScriptEvaluateException.class, () -> call("sumOf", invalid));
        assertThrows(ScriptEvaluateException.class, () -> call("maxOf", invalidList));
        assertThrows(ScriptEvaluateException.class, () -> call("minOf", invalidList));
    }

    @Test
    void basic() {
        var random = (Double) call("random");
        assertTrue(random >= 0.0d && random < 1.0d);

        assertBigDecimal("12.5", call("abs", "-12.5"));
        assertBigInteger("3", call("abs", new BigInteger("-3")));
        assertByte((byte) 3, call("abs", (byte) -3));
        assertShort((short) 3, call("abs", (short) -3));
        assertLong(3L, call("abs", -3L));
        assertFloat(1.5f, call("abs", -1.5f));
        assertDouble(1.5d, call("abs", -1.5d));
        assertBigDecimal("3", call("abs", decimal("-3.0")));
        assertInt(3, call("min", 3, 5));
        assertLong(5L, call("max", 3, 5L));
        assertBigDecimal("2.5", call("max", decimal("2.5"), 2));
        assertByte((byte) 1, call("min", (byte) 1, 2));
        assertShort((short) 2, call("max", (short) 2, (byte) 1));
        assertBigInteger("1", call("min", BigInteger.ONE, 2L));
        assertFalse(Double.isNaN((Double) call("min", Double.NaN, 1.0d)));
        assertTrue(Double.isNaN((Double) call("max", Double.NaN, 1.0d)));
    }

    @Test
    void scalb() {
        assertFloat(StrictMath.scalb(1.5f, 2), call("scalb", 1.5f, 2));
        assertDouble(StrictMath.scalb(1.5d, 2), call("scalb", 1.5d, 2));
        assertDouble(StrictMath.scalb(Double.POSITIVE_INFINITY, 2), call("scalb", Double.POSITIVE_INFINITY, 2));
        assertBigDecimal("1.5", call("scalb", new BigInteger("3"), -1));
        assertBigDecimal("6.0", call("scalb", decimal("1.5"), 2));
        assertBigDecimal("0.375", call("scalb", decimal("1.5"), -2));
    }

    @Test
    void ieeeRemainder() {
        assertDouble(StrictMath.IEEEremainder(5.0d, 2.0d), call("ieeeRemainder", 5.0d, 2.0d));
        assertSame(math.get("ieeeRemainder"), math.get("IEEEremainder"));
    }

    @Test
    void supportAdapters() {
        assertInt('A', call("abs", 'A'));
        assertInt(2, call("max", 1.5d, 2));
        assertFloat(1.5f, call("min", 1.5f, 2));
    }

    @Test
    void supportAtomics() {
        var atomicInteger = new AtomicInteger(3);
        var atomicLong = new AtomicLong(2);
        var accumulator = new LongAccumulator(Long::sum, 0L);
        accumulator.accumulate(7L);

        assertBigInteger("4", call("max", atomicLong, new BigInteger("4")));
        assertBigDecimal("4", call("max", atomicInteger, decimal("4")));
        assertBigDecimal("2.5", call("max", decimal("2.5"), atomicLong));
        assertBigInteger("6", call("min", accumulator, new BigInteger("6")));
    }

    @Test
    void supportAdders() {
        var doubleAdder = new DoubleAdder();
        doubleAdder.add(1.25d);
        var longAdder = new LongAdder();
        longAdder.add(5L);

        assertDouble(2.5d, call("scalb", doubleAdder, 1));
        assertBigInteger("2", call("max", doubleAdder, new BigInteger("2")));
        assertBigDecimal("1.5", call("max", decimal("1.5"), doubleAdder));
        assertBigDecimal("5", call("max", decimal("5"), longAdder));
    }

    @Test
    void toFloat() {
        assertFloat(1.5F, Support.toFloat(1.5F));
        assertFloat(1.5F, Support.toFloat(1.5D));
        assertFloat(1.5F, Support.toFloat(decimal("1.5")));

        assertFloat(15.0F, Support.toFloat((byte) 15));
        assertFloat(15.0F, Support.toFloat((short) 15));
        assertFloat(15.0F, Support.toFloat(15));
        assertFloat(15.0F, Support.toFloat(15L));
        assertFloat(15.0F, Support.toFloat(new BigInteger("15")));
        assertFloat(15.0F, Support.toFloat(new AtomicInteger(15)));
    }

    @Test
    void sqrtUnaryInputs() {
        assertDouble(2.0d, call("sqrt", 4.0f));
        assertDouble(3.0d, call("sqrt", new BigInteger("9")));
        assertDouble(4.0d, call("sqrt", decimal("16.0")));
        assertDouble(5.0d, call("sqrt", new AtomicLong(25L)));
    }

    @Test
    void maxAgainstIntegralKinds() {
        assertBigInteger("4", call("max", new BigInteger("4"), (byte) 3));
        assertBigInteger("4", call("max", new BigInteger("4"), (short) 3));
        assertBigInteger("4", call("max", new BigInteger("4"), 3));
        assertBigInteger("4", call("max", new BigInteger("4"), 3L));
        assertBigInteger("4", call("max", new BigInteger("4"), new AtomicInteger(3)));

        assertBigDecimal("4", call("max", decimal("4.0"), (byte) 3));
        assertBigDecimal("4", call("max", decimal("4.0"), (short) 3));
        assertBigDecimal("4", call("max", decimal("4.0"), 3));
        assertBigDecimal("4", call("max", decimal("4.0"), 3L));
        assertBigDecimal("4", call("max", decimal("4.0"), new LongAccumulator(Long::sum, 3L)));
    }

    @Test
    void scalbScaleFactorEdges() {
        var decimalOnePointFive = decimal("1.5");
        var decimalTwo = decimal("2");

        assertDouble(12.0d, call("scalb", 3, 2.0d));
        assertDouble(12.0d, call("scalb", 3, 2.0f));
        assertDouble(12.0d, call("scalb", 3, 2L));
        assertDouble(12.0d, call("scalb", 3, new BigInteger("2")));
        assertDouble(12.0d, call("scalb", 3, new AtomicInteger(2)));
        assertDouble(12.0d, call("scalb", 3, decimalTwo));
        assertBigInteger("12", call("scalb", new BigInteger("3"), 2));
        assertBigDecimal("1.5", call("scalb", decimalOnePointFive, 0));
    }

    @Test
    void scalbScaleFactorLimits() {
        var one = decimal("1");
        var decimalOnePointFive = decimal("1.5");

        assertThrows(ScriptEvaluateException.class, () -> call("scalb", one, Integer.MIN_VALUE));
        assertThrows(ScriptEvaluateException.class, () -> call("scalb", 1.0d, Long.MAX_VALUE));
        assertThrows(ScriptEvaluateException.class, () -> call("scalb", 1.0d, Double.NaN));
        assertThrows(ScriptEvaluateException.class, () -> call("scalb", 1.0d, decimalOnePointFive));
    }

    @Test
    void exactPrimitive() {
        assertInt(StrictMath.addExact(1, 2), call("addExact", 1, 2));
        assertLong(StrictMath.addExact(1L, 2L), call("addExact", 1L, 2L));
        assertInt(StrictMath.subtractExact(7, 2), call("subtractExact", 7, 2));
        assertLong(StrictMath.multiplyExact(7L, 2), call("multiplyExact", 7L, 2));
        assertInt(StrictMath.divideExact(8, 2), call("divideExact", 8, 2));
        assertInt(StrictMath.floorDiv(-3, 2), call("floorDiv", -3, 2));
        assertLong(StrictMath.floorDiv(7L, 2L), call("floorDiv", 7L, 2L));
        assertInt(StrictMath.floorDivExact(8, 2), call("floorDivExact", 8, 2));
        assertInt(StrictMath.floorMod(-3, 2), call("floorMod", -3, 2));
        assertLong(StrictMath.floorMod(7L, 2L), call("floorMod", 7L, 2L));
        assertInt(StrictMath.ceilDiv(3, 2), call("ceilDiv", 3, 2));
        assertLong(StrictMath.ceilDiv(7L, 2L), call("ceilDiv", 7L, 2L));
        assertInt(StrictMath.ceilDivExact(8, 2), call("ceilDivExact", 8, 2));
        assertInt(StrictMath.ceilMod(3, 2), call("ceilMod", 3, 2));
        assertLong(StrictMath.ceilMod(7L, 2L), call("ceilMod", 7L, 2L));
        assertInt(StrictMath.incrementExact(1), call("incrementExact", 1));
        assertByte((byte) 2, call("incrementExact", (byte) 1));
        assertLong(0L, call("decrementExact", 1L));
        assertShort((short) 1, call("decrementExact", (short) 2));
        assertInt(StrictMath.negateExact(1), call("negateExact", 1));
        assertByte((byte) -1, call("negateExact", (byte) 1));
        assertLong(StrictMath.absExact(-1L), call("absExact", -1L));
        assertShort((short) 3, call("absExact", (short) -3));
        assertInt(StrictMath.toIntExact(7L), call("toIntExact", 7L));
    }

    @Test
    void exactLongPaths() {
        assertLong(5L, call("subtractExact", 8L, 3L));
        assertLong(24L, call("multiplyExact", 8L, 3L));
        assertLong(4L, call("divideExact", 8L, 2L));
        assertLong(1L, call("floorDivExact", 3L, 2L));
        assertLong(2L, call("ceilDivExact", 3L, 2L));
        assertLong(2L, call("incrementExact", 1L));
        assertLong(-3L, call("negateExact", 3L));
    }

    @Test
    void exactNarrowPaths() {
        assertByte((byte) 3, call("addExact", (byte) 1, (byte) 2));
        assertShort((short) 3, call("addExact", (short) 1, (short) 2));
        assertShort((short) 5, call("subtractExact", (short) 8, (short) 3));
        assertInt(24, call("multiplyExact", 8, 3));
        assertInt(0, call("decrementExact", 1));
        assertInt(3, call("absExact", -3));
        assertInt(7, call("toIntExact", (short) 7));
    }

    @Test
    void exactBigNumbers() {
        assertBigInteger("3", call("addExact", new BigInteger("1"), 2));
        assertBigInteger("4", call("divideExact", new BigInteger("8"), new BigInteger("2")));
        assertBigInteger("-2", call("floorDiv", new BigInteger("-3"), new BigInteger("2")));
        assertBigInteger("-1", call("ceilMod", new BigInteger("3"), new BigInteger("2")));
        assertBigInteger("3", call("absExact", new BigInteger("-3")));
        assertBigDecimal("3", call("addExact", decimal("1.0"), decimal("2")));
        assertBigDecimal("3", call("absExact", decimal("-3.0")));
        assertInt(7, call("toIntExact", decimal("7.0")));
    }

    @Test
    void exactBigNumbersSubMul() {
        assertBigInteger("5", call("subtractExact", new BigInteger("8"), new BigInteger("3")));
        assertBigInteger("24", call("multiplyExact", new BigInteger("8"), new BigInteger("3")));
        assertBigDecimal("5", call("subtractExact", decimal("8.0"), decimal("3")));
        assertBigDecimal("24", call("multiplyExact", decimal("8.0"), decimal("3")));
        assertInt(7, call("toIntExact", new BigInteger("7")));
    }

    @Test
    void exactMixedConversions() {
        assertBigDecimal("3", call("addExact", 1L, decimal("2")));
        assertBigDecimal("5", call("subtractExact", new BigInteger("8"), decimal("3")));
        assertBigDecimal("24", call("multiplyExact", decimal("8"), 3L));
        assertBigInteger("3", call("addExact", new AtomicLong(1L), new BigInteger("2")));
    }

    @Test
    void exactMixedWidths() {
        assertByte((byte) 1, call("subtractExact", (byte) 3, (byte) 2));
        assertShort((short) 6, call("multiplyExact", (short) 3, (short) 2));
        assertLong(14L, call("multiplyExact", 2, 7L));
        assertShort((short) 4, call("divideExact", (short) 8, (short) 2));
        assertLong(3L, call("floorDiv", 7L, 2));
        assertInt(1, call("floorMod", 7L, 2));
        assertLong(4L, call("ceilDiv", 7L, 2));
        assertInt(-1, call("ceilMod", 7L, 2));
        assertByte((byte) 1, call("floorDivExact", (byte) 3, (byte) 2));
        assertShort((short) 2, call("ceilDivExact", (short) 3, (short) 2));
    }

    @Test
    void exactBigIntegerCorners() {
        var three = new BigInteger("3");
        var minusThree = new BigInteger("-3");
        var two = new BigInteger("2");

        assertBigInteger("1", call("floorDiv", three, two));
        assertBigInteger("-2", call("floorDivExact", minusThree, two));
        assertBigInteger("1", call("floorMod", three, two));
        assertBigInteger("2", call("ceilDiv", three, two));
        assertBigInteger("-1", call("ceilDiv", minusThree, two));
        assertBigInteger("2", call("ceilDivExact", three, two));
        var inexact = new BigInteger("3");
        var divisor = new BigInteger("2");
        assertThrows(ArithmeticException.class, () -> call("divideExact", inexact, divisor));
    }

    @Test
    void exactBigIntegerSigns() {
        var three = new BigInteger("3");
        var minusThree = new BigInteger("-3");
        var minusTwo = new BigInteger("-2");
        var two = new BigInteger("2");

        assertBigInteger("-2", call("floorDiv", three, minusTwo));
        assertBigInteger("1", call("floorMod", minusThree, two));
        assertBigInteger("-1", call("ceilDiv", three, minusTwo));
        assertBigInteger("2", call("ceilDiv", minusThree, minusTwo));
        assertBigInteger("1", call("ceilMod", minusThree, minusTwo));
    }

    @Test
    void exactSingleBigInteger() {
        var three = new BigInteger("3");

        assertBigInteger("4", call("incrementExact", three));
        assertBigInteger("2", call("decrementExact", three));
        assertBigInteger("-3", call("negateExact", three));
    }

    @Test
    void exactSingleBigDecimal() {
        var decimalThree = decimal("3.0");

        assertBigDecimal("4", call("incrementExact", decimalThree));
        assertBigDecimal("2", call("decrementExact", decimalThree));
        assertBigDecimal("-3", call("negateExact", decimalThree));
    }

    @Test
    void exactRejectFloatingPoint() {
        assertThrows(ScriptEvaluateException.class, () -> call("incrementExact", 1.5d));
        assertThrows(ScriptEvaluateException.class, () -> call("toIntExact", 1.5d));
    }

    @Test
    void rounding() {
        assertInt(2, call("floor", 2));
        assertBigInteger("2", call("floor", new BigInteger("2")));
        assertBigDecimal("1", call("floor", decimal("1.9")));
        assertDouble(StrictMath.floor(1.9d), call("floor", 1.9d));
        assertInt(2, call("ceil", 2));
        assertBigInteger("2", call("ceil", new BigInteger("2")));
        assertBigDecimal("2", call("ceil", decimal("1.1")));
        assertDouble(StrictMath.ceil(1.1d), call("ceil", 1.1d));
        assertInt(2, call("rint", 2));
        assertBigInteger("2", call("rint", new BigInteger("2")));
        assertBigDecimal("2", call("rint", decimal("1.5")));
        assertDouble(StrictMath.rint(1.5d), call("rint", 1.5d));
        assertBigInteger("2", call("round", new BigInteger("2")));
        assertBigDecimal("2", call("round", decimal("1.6")));
        assertBigDecimal("2", call("round", decimal("1.5")));
        assertBigDecimal("1", call("round", decimal("1.4")));
        assertBigDecimal("-1", call("round", decimal("-1.4")));
        assertBigDecimal("-1", call("round", decimal("-1.5")));
        assertBigDecimal("-2", call("round", decimal("-1.6")));
        assertInt(2, call("round", 1.5f));
        assertInt(-1, call("round", -1.5f));
        assertLong(2L, call("round", 1.5d));
        assertLong(-1L, call("round", -1.5d));
    }

    @Test
    void trigonometric() {
        assertDouble(StrictMath.exp(1.0d), call("exp", 1.0d));
        assertDouble(StrictMath.expm1(1.0d), call("expm1", 1.0d));
        assertDouble(StrictMath.log(10.0d), call("ln", 10.0d));
        assertDouble(StrictMath.log10(100.0d), call("log10", 100.0d));
        assertDouble(StrictMath.log1p(1.0d), call("log1p", 1.0d));
        assertDouble(StrictMath.sqrt(16.0d), call("sqrt", 16.0d));
        assertDouble(StrictMath.cbrt(27.0d), call("cbrt", 27.0d));
        assertDouble(StrictMath.pow(2.0d, 3.0d), call("pow", 2.0d, 3.0d));
        assertDouble(StrictMath.sin(StrictMath.PI / 2), call("sin", StrictMath.PI / 2));
        assertDouble(StrictMath.cos(0.0d), call("cos", 0.0d));
        assertDouble(StrictMath.tan(0.0d), call("tan", 0.0d));
        assertDouble(StrictMath.asin(1.0d), call("asin", 1.0d));
        assertDouble(StrictMath.acos(1.0d), call("acos", 1.0d));
        assertDouble(StrictMath.atan(1.0d), call("atan", 1.0d));
        assertDouble(StrictMath.atan2(1.0d, 1.0d), call("atan2", 1.0d, 1.0d));
        assertDouble(StrictMath.sinh(1.0d), call("sinh", 1.0d));
        assertDouble(StrictMath.cosh(1.0d), call("cosh", 1.0d));
        assertDouble(StrictMath.tanh(1.0d), call("tanh", 1.0d));
        assertDouble(StrictMath.toRadians(180.0d), call("toRadians", 180.0d));
        assertDouble(StrictMath.toDegrees(StrictMath.PI), call("toDegrees", StrictMath.PI));
        assertDouble(StrictMath.hypot(3.0d, 4.0d), call("hypot", 3.0d, 4.0d));

    }

    @Test
    void strictInvalidAndOverflow() {
        assertThrows(ScriptEvaluateException.class, () -> call("abs", (Object) null));
        assertThrows(ScriptEvaluateException.class, () -> call("sqrt", "not-a-number"));
        assertThrows(ScriptEvaluateException.class, () -> call("scalb", 1.0d, 1.5d));
        assertThrows(ScriptEvaluateException.class, () -> call("scalb", 1.0d, "not-an-integer"));
        assertThrows(ScriptEvaluateException.class, () -> call("addExact", 1.1d, 2));
        assertThrows(ArithmeticException.class, () -> call("incrementExact", Byte.MAX_VALUE));
        assertThrows(ArithmeticException.class, () -> call("absExact", Byte.MIN_VALUE));
        assertThrows(ArithmeticException.class, () -> call("negateExact", Short.MIN_VALUE));
        assertThrows(ArithmeticException.class, () -> call("incrementExact", Integer.MAX_VALUE));
        assertThrows(ArithmeticException.class, () -> call("divideExact", 1, 0));
        assertThrows(ArithmeticException.class, () -> call("toIntExact", Long.MAX_VALUE));
    }

    @Test
    void strictDomainBehavior() {
        assertTrue(Double.isNaN((Double) call("sqrt", -1.0d)));
        assertEquals(Double.POSITIVE_INFINITY, call("exp", Double.POSITIVE_INFINITY));
    }

    @Test
    void exactRejectBigDecimalOperands() {
        var exactEight = decimal("8.0");
        var positiveThree = decimal("3.0");
        var negativeThree = decimal("-3.0");
        var two = decimal("2");

        assertThrows(ScriptEvaluateException.class,
                () -> call("divideExact", exactEight, two));
        assertThrows(ScriptEvaluateException.class,
                () -> call("floorDiv", negativeThree, two));
        assertThrows(ScriptEvaluateException.class,
                () -> call("ceilDiv", positiveThree, two));
        assertThrows(ScriptEvaluateException.class,
                () -> call("ceilMod", positiveThree, two));
    }

    private static void assertInt(int expected, Object actual) {
        assertInstanceOf(Integer.class, actual);
        assertEquals(expected, actual);
    }

    private static void assertByte(byte expected, Object actual) {
        assertInstanceOf(Byte.class, actual);
        assertEquals(expected, actual);
    }

    private static void assertShort(short expected, Object actual) {
        assertInstanceOf(Short.class, actual);
        assertEquals(expected, actual);
    }

    private static void assertLong(long expected, Object actual) {
        assertInstanceOf(Long.class, actual);
        assertEquals(expected, actual);
    }

    private static void assertBigInteger(String expected, Object actual) {
        assertInstanceOf(BigInteger.class, actual);
        assertEquals(new BigInteger(expected), actual);
    }

    private static void assertBigDecimal(String expected, Object actual) {
        assertInstanceOf(BigDecimal.class, actual);
        assertEquals(0, decimal(expected).compareTo((BigDecimal) actual));
    }

    private static void assertDouble(double expected, Object actual) {
        assertInstanceOf(Double.class, actual);
        assertEquals(expected, (Double) actual, DOUBLE_DELTA);
    }

    private static void assertFloat(float expected, Object actual) {
        assertInstanceOf(Float.class, actual);
        assertEquals(expected, (Float) actual, FLOAT_DELTA);
    }
}
