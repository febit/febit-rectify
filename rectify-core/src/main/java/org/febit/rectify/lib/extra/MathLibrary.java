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

import lombok.experimental.UtilityClass;
import org.febit.lang.func.Function0;
import org.febit.lang.func.Function1;
import org.febit.lang.func.Function2;
import org.febit.lang.util.ConvertUtils;
import org.febit.rectify.lib.BindingAlias;
import org.febit.rectify.lib.Library;
import org.febit.rectify.lib.Namespace;
import org.febit.rectify.wit.function.LibFunction;
import org.febit.wit.exception.ScriptEvaluateException;
import org.febit.wit.ir.support.ALU;
import org.febit.wit.util.ClassUtils;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.febit.rectify.lib.extra.MathLibrary.Support.compare;
import static org.febit.rectify.lib.extra.MathLibrary.Support.normalize;
import static org.febit.rectify.lib.extra.MathLibrary.Support.precedence;
import static org.febit.rectify.lib.extra.MathLibrary.Support.requireInt;
import static org.febit.rectify.lib.extra.MathLibrary.Support.requireNumber;
import static org.febit.rectify.lib.extra.MathLibrary.Support.toBigDecimal;
import static org.febit.rectify.lib.extra.MathLibrary.Support.toBigDecimalExact;
import static org.febit.rectify.lib.extra.MathLibrary.Support.toBigInteger;
import static org.febit.rectify.lib.extra.MathLibrary.Support.toBigIntegerExact;
import static org.febit.rectify.lib.extra.MathLibrary.Support.toByteExact;
import static org.febit.rectify.lib.extra.MathLibrary.Support.toDouble;
import static org.febit.rectify.lib.extra.MathLibrary.Support.toFloat;
import static org.febit.rectify.lib.extra.MathLibrary.Support.toShortExact;
import static org.febit.rectify.lib.extra.MathLibrary.Support.unaryDouble;
import static org.febit.rectify.lib.extra.MathLibrary.Support.unsupportedTypeException;

/**
 * StrictMath-backed numeric helpers exposed to scripts.
 */
@SuppressWarnings({
        "java:S1118", // Utility classes should not have public constructors
        "java:S2259", // Null pointers should not be dereferenced
})
public class MathLibrary implements Library {

    private static final int PRECEDENCE_BIG_DECIMAL = (1 << 8) - 1;
    private static final int PRECEDENCE_BIG_INTEGER = (1 << 7) - 1;
    private static final int PRECEDENCE_DOUBLE = (1 << 6) - 1;
    private static final int PRECEDENCE_FLOAT = (1 << 5) - 1;
    private static final int PRECEDENCE_LONG = (1 << 4) - 1;
    private static final int PRECEDENCE_INTEGER = (1 << 3) - 1;
    private static final int PRECEDENCE_SHORT = (1 << 2) - 1;
    private static final int PRECEDENCE_BYTE = (1 << 1) - 1;

    @BindingAlias(value = {"Math"}, keepDeclaredName = false)
    public static final MathNamespace MATH = new MathNamespace();

    /**
     * Math / Maths namespace.
     */
    @SuppressWarnings({
            "unused",
            "java:S1170", // should be "static final" rather than merely "final"
    })
    public static class MathNamespace implements Namespace {

        /**
         * @see Math#PI
         */
        @BindingAlias(value = {"PI"}, keepDeclaredName = false)
        public final double pi = StrictMath.PI;
        /**
         * @see Math#E
         */
        @BindingAlias(value = {"E"}, keepDeclaredName = false)
        public final double e = StrictMath.E;

        /**
         * @see Math#random()
         */
        public final Function0<Double> random = StrictMath::random;

        /**
         * Absolute value.
         *
         * @see Math#abs(int)
         * @see Math#abs(long)
         * @see Math#abs(float)
         * @see Math#abs(double)
         */
        public final Function1<@Nullable Object, Object> abs = BasicOps::abs;
        /**
         * Minimum of two numbers.
         *
         * @see Math#min(int, int)
         * @see Math#min(long, long)
         * @see Math#min(float, float)
         * @see Math#min(double, double)
         */
        public final Function2<@Nullable Object, @Nullable Object, Object> min = BasicOps::min;
        /**
         * Maximum of two numbers.
         *
         * @see Math#max(int, int)
         * @see Math#max(long, long)
         * @see Math#max(float, float)
         * @see Math#max(double, double)
         */
        public final Function2<@Nullable Object, @Nullable Object, Object> max = BasicOps::max;
        /**
         * Scale by power of two.
         *
         * @see Math#scalb(float, int)
         * @see Math#scalb(double, int)
         */
        public final Function2<@Nullable Object, @Nullable Object, Object> scalb = BasicOps::scalb;
        /**
         * IEEE 754 remainder.
         *
         * @see Math#IEEEremainder(double, double)
         */
        @BindingAlias({"IEEEremainder"})
        public final Function2<@Nullable Object, @Nullable Object, Double> ieeeRemainder = BasicOps::ieeeRemainder;

        /**
         * Floor.
         *
         * @see Math#floor(double)
         */
        public final Function1<@Nullable Object, Object> floor = RoundingOps::floor;
        /**
         * Ceil.
         *
         * @see Math#ceil(double)
         */
        public final Function1<@Nullable Object, Object> ceil = RoundingOps::ceil;
        /**
         * Round to nearest even integer.
         *
         * @see Math#rint(double)
         */
        public final Function1<@Nullable Object, Object> rint = RoundingOps::rint;
        /**
         * Round to nearest integer.
         *
         * @see Math#round(float)
         * @see Math#round(double)
         */
        public final Function1<@Nullable Object, Object> round = RoundingOps::round;

        /**
         * Exact addition.
         *
         * @see Math#addExact(int, int)
         * @see Math#addExact(long, long)
         */
        public final Function2<@Nullable Object, @Nullable Object, Object> addExact = ExactArithmeticOps::addExact;
        /**
         * Exact subtraction.
         *
         * @see Math#subtractExact(int, int)
         * @see Math#subtractExact(long, long)
         */
        public final Function2<@Nullable Object, @Nullable Object, Object> subtractExact = ExactArithmeticOps::subtractExact;
        /**
         * Exact multiplication.
         *
         * @see Math#multiplyExact(int, int)
         * @see Math#multiplyExact(long, long)
         */
        public final Function2<@Nullable Object, @Nullable Object, Object> multiplyExact = ExactArithmeticOps::multiplyExact;
        /**
         * Exact division.
         *
         * @see Math#divideExact(int, int)
         * @see Math#divideExact(long, long)
         */
        public final Function2<@Nullable Object, @Nullable Object, Object> divideExact = ExactArithmeticOps::divideExact;
        /**
         * Floor division.
         *
         * @see Math#floorDiv(int, int)
         * @see Math#floorDiv(long, long)
         */
        public final Function2<@Nullable Object, @Nullable Object, Object> floorDiv = ExactArithmeticOps::floorDiv;
        /**
         * Exact floor division.
         *
         * @see Math#floorDivExact(int, int)
         * @see Math#floorDivExact(long, long)
         */
        public final Function2<@Nullable Object, @Nullable Object, Object> floorDivExact = ExactArithmeticOps::floorDivExact;
        /**
         * Floor modulus.
         *
         * @see Math#floorMod(int, int)
         * @see Math#floorMod(long, long)
         */
        public final Function2<@Nullable Object, @Nullable Object, Object> floorMod = ExactArithmeticOps::floorMod;
        /**
         * Ceiling division.
         *
         * @see Math#ceilDiv(int, int)
         * @see Math#ceilDiv(long, long)
         */
        public final Function2<@Nullable Object, @Nullable Object, Object> ceilDiv = ExactArithmeticOps::ceilDiv;
        /**
         * Exact ceiling division.
         *
         * @see Math#ceilDivExact(int, int)
         * @see Math#ceilDivExact(long, long)
         */
        public final Function2<@Nullable Object, @Nullable Object, Object> ceilDivExact = ExactArithmeticOps::ceilDivExact;
        /**
         * Ceiling modulus.
         *
         * @see Math#ceilMod(int, int)
         * @see Math#ceilMod(long, long)
         */
        public final Function2<@Nullable Object, @Nullable Object, Object> ceilMod = ExactArithmeticOps::ceilMod;
        /**
         * Exact increment.
         *
         * @see Math#incrementExact(int)
         * @see Math#incrementExact(long)
         */
        public final Function1<@Nullable Object, Object> incrementExact = ExactArithmeticOps::incrementExact;
        /**
         * Exact decrement.
         *
         * @see Math#decrementExact(int)
         * @see Math#decrementExact(long)
         */
        public final Function1<@Nullable Object, Object> decrementExact = ExactArithmeticOps::decrementExact;
        /**
         * Exact negation.
         *
         * @see Math#negateExact(int)
         * @see Math#negateExact(long)
         */
        public final Function1<@Nullable Object, Object> negateExact = ExactArithmeticOps::negateExact;
        /**
         * Exact absolute value.
         *
         * @see Math#absExact(int)
         * @see Math#absExact(long)
         */
        public final Function1<@Nullable Object, Object> absExact = ExactArithmeticOps::absExact;
        /**
         * Exact narrowing to int.
         *
         * @see Math#toIntExact(long)
         */
        public final Function1<@Nullable Object, Integer> toIntExact = ExactArithmeticOps::toIntExact;

        /**
         * Sum of numbers.
         * <p>
         * Will flatten nested iterables and ignore non-numeric values.
         */
        public final LibFunction sumOf = ReduceOps::sumOf;

        /**
         * Maximum of numbers.
         * <p>
         * Will flatten nested iterables and ignore non-numeric values.
         */
        public final LibFunction maxOf = ReduceOps::maxOf;

        /**
         * Minimum of numbers.
         * <p>
         * Will flatten nested iterables and ignore non-numeric values.
         */
        public final LibFunction minOf = ReduceOps::minOf;

        /**
         * Exponential.
         *
         * @see Math#exp(double)
         */
        public final Function1<@Nullable Object, Double> exp = ExponentialLogOps::exp;
        /**
         * Exponential minus one.
         *
         * @see Math#expm1(double)
         */
        public final Function1<@Nullable Object, Double> expm1 = ExponentialLogOps::expm1;
        /**
         * Natural logarithm.
         *
         * @see Math#log(double)
         */
        public final Function1<@Nullable Object, Double> ln = ExponentialLogOps::ln;
        /**
         * Base-10 logarithm.
         *
         * @see Math#log10(double)
         */
        public final Function1<@Nullable Object, Double> log10 = ExponentialLogOps::log10;
        /**
         * Natural logarithm of one plus x.
         *
         * @see Math#log1p(double)
         */
        public final Function1<@Nullable Object, Double> log1p = ExponentialLogOps::log1p;
        /**
         * Square root.
         *
         * @see Math#sqrt(double)
         */
        public final Function1<@Nullable Object, Double> sqrt = ExponentialLogOps::sqrt;
        /**
         * Cube root.
         *
         * @see Math#cbrt(double)
         */
        public final Function1<@Nullable Object, Double> cbrt = ExponentialLogOps::cbrt;

        /**
         * Power.
         *
         * @see Math#pow(double, double)
         */
        public final Function2<@Nullable Object, @Nullable Object, Double> pow = ExponentialLogOps::pow;
        /**
         * Sine.
         *
         * @see Math#sin(double)
         */
        public final Function1<@Nullable Object, Double> sin = TrigonometricOps::sin;
        /**
         * Cosine.
         *
         * @see Math#cos(double)
         */
        public final Function1<@Nullable Object, Double> cos = TrigonometricOps::cos;
        /**
         * Tangent.
         *
         * @see Math#tan(double)
         */
        public final Function1<@Nullable Object, Double> tan = TrigonometricOps::tan;
        /**
         * Arc sine.
         *
         * @see Math#asin(double)
         */
        public final Function1<@Nullable Object, Double> asin = TrigonometricOps::asin;
        /**
         * Arc cosine.
         *
         * @see Math#acos(double)
         */
        public final Function1<@Nullable Object, Double> acos = TrigonometricOps::acos;
        /**
         * Arc tangent.
         *
         * @see Math#atan(double)
         */
        public final Function1<@Nullable Object, Double> atan = TrigonometricOps::atan;
        /**
         * Two-argument arc tangent.
         *
         * @see Math#atan2(double, double)
         */
        public final Function2<@Nullable Object, @Nullable Object, Double> atan2 = TrigonometricOps::atan2;
        /**
         * Hyperbolic sine.
         *
         * @see Math#sinh(double)
         */
        public final Function1<@Nullable Object, Double> sinh = TrigonometricOps::sinh;
        /**
         * Hyperbolic cosine.
         *
         * @see Math#cosh(double)
         */
        public final Function1<@Nullable Object, Double> cosh = TrigonometricOps::cosh;
        /**
         * Hyperbolic tangent.
         *
         * @see Math#tanh(double)
         */
        public final Function1<@Nullable Object, Double> tanh = TrigonometricOps::tanh;
        /**
         * Degrees to radians.
         *
         * @see Math#toRadians(double)
         */
        public final Function1<@Nullable Object, Double> toRadians = TrigonometricOps::toRadians;
        /**
         * Radians to degrees.
         *
         * @see Math#toDegrees(double)
         */
        public final Function1<@Nullable Object, Double> toDegrees = TrigonometricOps::toDegrees;
        /**
         * Hypotenuse.
         *
         * @see Math#hypot(double, double)
         */
        public final Function2<@Nullable Object, @Nullable Object, Double> hypot = TrigonometricOps::hypot;
    }

    @FunctionalInterface
    private interface DoubleUnaryOperator {
        double apply(double value);
    }

    @UtilityClass
    static class Support {

        static int precedence(Number value) {
            return switch (value) {
                case Integer i -> PRECEDENCE_INTEGER;
                case AtomicInteger atomic -> PRECEDENCE_INTEGER;
                case Long l -> PRECEDENCE_LONG;
                case AtomicLong l -> PRECEDENCE_LONG;
                case LongAdder l -> PRECEDENCE_LONG;
                case LongAccumulator l -> PRECEDENCE_LONG;
                case Short i -> PRECEDENCE_SHORT;
                case Double v -> PRECEDENCE_DOUBLE;
                case DoubleAdder d -> PRECEDENCE_DOUBLE;
                case Float v -> PRECEDENCE_FLOAT;
                case Byte b -> PRECEDENCE_BYTE;
                case BigInteger bi -> PRECEDENCE_BIG_INTEGER;
                default -> PRECEDENCE_BIG_DECIMAL;
            };
        }

        static int precedence(Number left, Number right) {
            return precedence(left) | precedence(right);
        }

        static int requireInt(@Nullable Object value) {
            var number = requireNumber(value);
            try {
                return switch (precedence(number)) {
                    case PRECEDENCE_BYTE,
                         PRECEDENCE_SHORT,
                         PRECEDENCE_INTEGER -> number.intValue();
                    case PRECEDENCE_LONG -> StrictMath.toIntExact(number.longValue());
                    case PRECEDENCE_FLOAT,
                         PRECEDENCE_DOUBLE -> {
                        double current = toDouble(number);
                        if (Double.isFinite(current)
                                && current >= Integer.MIN_VALUE
                                && current <= Integer.MAX_VALUE
                                && StrictMath.rint(current) == current) {
                            yield (int) current;
                        }
                        throw new ArithmeticException();
                    }
                    case PRECEDENCE_BIG_INTEGER -> toBigInteger(number).intValueExact();
                    case PRECEDENCE_BIG_DECIMAL -> toBigDecimal(number).intValueExact();
                    default -> throw new ArithmeticException();
                };
            } catch (ArithmeticException e) {
                throw new ScriptEvaluateException("value is not an int: " + number);
            }
        }

        static Number requireNumber(@Nullable Object value) {
            return switch (value) {
                case null -> throw new ScriptEvaluateException("value is null");
                case Number number -> number;
                case Character c -> Integer.valueOf(c);
                default -> {
                    try {
                        var number = ConvertUtils.toNumber(value);
                        if (number != null) {
                            yield number;
                        }
                    } catch (IllegalArgumentException ignored) {
                        // ignored
                    }
                    throw new ScriptEvaluateException("value is not a number: " + value.getClass().getCanonicalName());
                }
            };
        }

        static float toFloat(Number value) {
            return switch (value) {
                case Float f -> f;
                case Double d -> new BigDecimal(d.toString()).floatValue();
                case BigInteger integer -> new BigDecimal(integer).floatValue();
                default -> value.floatValue();
            };
        }

        static double toDouble(Number value) {
            return switch (value) {
                case Double d -> d;
                case Float f -> new BigDecimal(f.toString()).doubleValue();
                case BigDecimal decimal -> decimal.doubleValue();
                case BigInteger integer -> new BigDecimal(integer).doubleValue();
                default -> value.doubleValue();
            };
        }

        static BigInteger toBigInteger(Number value) {
            return switch (value) {
                case BigInteger integer -> integer;
                case Byte b -> BigInteger.valueOf(value.longValue());
                case Short i -> BigInteger.valueOf(value.longValue());
                case Integer i -> BigInteger.valueOf(value.longValue());
                case Long l -> BigInteger.valueOf(value.longValue());
                case AtomicInteger atomic -> BigInteger.valueOf(atomic.longValue());
                case AtomicLong l -> BigInteger.valueOf(l.longValue());
                case LongAdder l -> BigInteger.valueOf(l.longValue());
                case LongAccumulator l -> BigInteger.valueOf(l.longValue());
                case BigDecimal decimal -> decimal.toBigInteger();
                default -> new BigDecimal(value.toString()).toBigInteger();
            };
        }

        static BigDecimal toBigDecimal(Number value) {
            return switch (value) {
                case BigDecimal decimal -> decimal;
                case Byte b -> BigDecimal.valueOf(value.longValue());
                case Short i -> BigDecimal.valueOf(value.longValue());
                case Integer i -> BigDecimal.valueOf(value.longValue());
                case Long l -> BigDecimal.valueOf(value.longValue());
                case AtomicInteger atomic -> BigDecimal.valueOf(atomic.longValue());
                case AtomicLong l -> BigDecimal.valueOf(l.longValue());
                case LongAdder l -> BigDecimal.valueOf(l.longValue());
                case LongAccumulator l -> BigDecimal.valueOf(l.longValue());
                case BigInteger integer -> new BigDecimal(integer);
                default -> new BigDecimal(value.toString());
            };
        }

        static byte toByteExact(int value) {
            byte narrowed = (byte) value;
            if (narrowed != value) {
                throw new ArithmeticException("byte overflow");
            }
            return narrowed;
        }

        static short toShortExact(int value) {
            short narrowed = (short) value;
            if (narrowed != value) {
                throw new ArithmeticException("short overflow");
            }
            return narrowed;
        }

        static BigInteger toBigIntegerExact(Number value) {
            return switch (precedence(value)) {
                case PRECEDENCE_BYTE,
                     PRECEDENCE_SHORT,
                     PRECEDENCE_INTEGER,
                     PRECEDENCE_LONG -> BigInteger.valueOf(value.longValue());
                case PRECEDENCE_BIG_INTEGER -> (BigInteger) value;
                case PRECEDENCE_BIG_DECIMAL -> ((BigDecimal) value).toBigIntegerExact();
                default -> throw unsupportedTypeException(value);
            };
        }

        static BigDecimal toBigDecimalExact(Number value) {
            return switch (precedence(value)) {
                case PRECEDENCE_BYTE,
                     PRECEDENCE_SHORT,
                     PRECEDENCE_INTEGER,
                     PRECEDENCE_LONG -> BigDecimal.valueOf(value.longValue());
                case PRECEDENCE_BIG_INTEGER -> new BigDecimal((BigInteger) value);
                case PRECEDENCE_BIG_DECIMAL -> ((BigDecimal) value).stripTrailingZeros();
                default -> throw unsupportedTypeException(value);
            };
        }

        static Number normalize(Number value) {
            return switch (precedence(value)) {
                case PRECEDENCE_BYTE -> value.byteValue();
                case PRECEDENCE_SHORT -> value.shortValue();
                case PRECEDENCE_INTEGER -> value.intValue();
                case PRECEDENCE_LONG -> value.longValue();
                case PRECEDENCE_FLOAT -> toFloat(value);
                case PRECEDENCE_DOUBLE -> toDouble(value);
                case PRECEDENCE_BIG_INTEGER -> toBigInteger(value);
                case PRECEDENCE_BIG_DECIMAL -> toBigDecimal(value);
                default -> throw unsupportedTypeException(value);
            };
        }

        static int compare(Number left, Number right) {
            return switch (precedence(left, right)) {
                case PRECEDENCE_BYTE,
                     PRECEDENCE_SHORT,
                     PRECEDENCE_INTEGER -> Integer.compare(left.intValue(), right.intValue());
                case PRECEDENCE_LONG -> Long.compare(left.longValue(), right.longValue());
                case PRECEDENCE_FLOAT -> Float.compare(toFloat(left), toFloat(right));
                case PRECEDENCE_DOUBLE -> Double.compare(toDouble(left), toDouble(right));
                case PRECEDENCE_BIG_INTEGER -> isNotDoubleOrFloat(left, right)
                        ? toBigInteger(left).compareTo(toBigInteger(right))
                        : toBigDecimal(left).compareTo(toBigDecimal(right));
                case PRECEDENCE_BIG_DECIMAL -> toBigDecimal(left).compareTo(toBigDecimal(right));
                default -> throw unsupportedTypeException(left, right);
            };
        }

        static boolean isNotDoubleOrFloat(Number value) {
            return !(value instanceof Float) && !(value instanceof Double);
        }

        static boolean isNotDoubleOrFloat(Number left, Number right) {
            return isNotDoubleOrFloat(left) && isNotDoubleOrFloat(right);
        }

        static ScriptEvaluateException unsupportedTypeException(@Nullable Object value) {
            return new ScriptEvaluateException("Unsupported type: " + ClassUtils.nameOf(value));
        }

        static ScriptEvaluateException unsupportedTypeException(@Nullable Object left, @Nullable Object right) {
            return new ScriptEvaluateException("Unsupported type: left ["
                    + ClassUtils.nameOf(left) + "], right [" + ClassUtils.nameOf(right) + "]");
        }

        static Double unaryDouble(@Nullable Object value, DoubleUnaryOperator operator) {
            return operator.apply(toDouble(requireNumber(value)));
        }

        static Stream<Number> flat(Object... args) {
            return Stream.of(args)
                    .flatMap(Support::flat0)
                    .filter(Objects::nonNull)
                    .map(Support::normalize);
        }

        private static Stream<Number> flat0(@Nullable Object arg) {
            return switch (arg) {
                case null -> Stream.empty();
                case Number number -> Stream.of(number);
                case Character c -> Stream.of((int) c);
                case Stream<?> stream -> stream.flatMap(MathLibrary.Support::flat0);
                case Iterable<?> iterable -> StreamSupport.stream(iterable.spliterator(), false)
                        .flatMap(MathLibrary.Support::flat0);
                case Object[] array -> Stream.of(array)
                        .flatMap(MathLibrary.Support::flat0);
                default -> throw new ScriptEvaluateException(
                        "value is not a number or iterable: " + arg.getClass().getCanonicalName());
            };
        }
    }

    @UtilityClass
    static class BasicOps {

        static Object abs(@Nullable Object value) {
            var number = requireNumber(value);
            return switch (precedence(number)) {
                case PRECEDENCE_BYTE -> toByteExact(StrictMath.abs(number.byteValue()));
                case PRECEDENCE_SHORT -> toShortExact(StrictMath.abs(number.shortValue()));
                case PRECEDENCE_INTEGER -> StrictMath.abs(number.intValue());
                case PRECEDENCE_LONG -> StrictMath.abs(number.longValue());
                case PRECEDENCE_FLOAT -> StrictMath.abs(toFloat(number));
                case PRECEDENCE_DOUBLE -> StrictMath.abs(toDouble(number));
                case PRECEDENCE_BIG_INTEGER -> toBigInteger(number).abs();
                case PRECEDENCE_BIG_DECIMAL -> toBigDecimal(number).abs();
                default -> throw unsupportedTypeException(number);
            };
        }

        static Object min(@Nullable Object left, @Nullable Object right) {
            var first = requireNumber(left);
            var second = requireNumber(right);
            return compare(first, second) <= 0
                    ? first : second;
        }

        static Object max(@Nullable Object left, @Nullable Object right) {
            var first = requireNumber(left);
            var second = requireNumber(right);
            return compare(first, second) >= 0
                    ? first : second;
        }

        static Object scalb(@Nullable Object value, @Nullable Object scaleFactor) {
            var number = requireNumber(value);
            var factor = requireInt(scaleFactor);
            return switch (precedence(number)) {
                case PRECEDENCE_FLOAT -> StrictMath.scalb(toFloat(number), factor);
                case PRECEDENCE_BYTE,
                     PRECEDENCE_SHORT,
                     PRECEDENCE_INTEGER,
                     PRECEDENCE_LONG,
                     PRECEDENCE_DOUBLE -> StrictMath.scalb(toDouble(number), factor);
                case PRECEDENCE_BIG_INTEGER -> scalb(toBigInteger(number), factor);
                case PRECEDENCE_BIG_DECIMAL -> scalb(toBigDecimal(number), factor);
                default -> throw unsupportedTypeException(number);
            };
        }

        static Double ieeeRemainder(@Nullable Object first, @Nullable Object second) {
            return StrictMath.IEEEremainder(
                    toDouble(requireNumber(first)),
                    toDouble(requireNumber(second))
            );
        }

        private static BigDecimal scalb(BigDecimal value, int factor) {
            if (factor == 0) {
                return value;
            }
            if (factor == Integer.MIN_VALUE) {
                throw new ScriptEvaluateException("scale factor is too small: " + factor);
            }
            var power = new BigDecimal(BigInteger.ONE.shiftLeft(StrictMath.abs(factor)));
            return factor > 0
                    ? value.multiply(power)
                    : value.divide(power, MathContext.UNLIMITED);
        }

        private static Object scalb(BigInteger value, int factor) {
            return factor >= 0
                    ? value.shiftLeft(factor)
                    : scalb(new BigDecimal(value), factor);
        }
    }

    @UtilityClass
    static class RoundingOps {

        static Object floor(@Nullable Object value) {
            var number = requireNumber(value);
            return switch (precedence(number)) {
                case PRECEDENCE_BYTE,
                     PRECEDENCE_SHORT,
                     PRECEDENCE_INTEGER,
                     PRECEDENCE_LONG,
                     PRECEDENCE_BIG_INTEGER -> normalize(number);
                case PRECEDENCE_BIG_DECIMAL -> toBigDecimal(number).setScale(0, RoundingMode.FLOOR);
                default -> unaryDouble(value, StrictMath::floor);
            };
        }

        static Object ceil(@Nullable Object value) {
            var number = requireNumber(value);
            return switch (precedence(number)) {
                case PRECEDENCE_BYTE,
                     PRECEDENCE_SHORT,
                     PRECEDENCE_INTEGER,
                     PRECEDENCE_LONG,
                     PRECEDENCE_BIG_INTEGER -> normalize(number);
                case PRECEDENCE_BIG_DECIMAL -> toBigDecimal(number).setScale(0, RoundingMode.CEILING);
                default -> unaryDouble(value, StrictMath::ceil);
            };
        }

        static Object rint(@Nullable Object value) {
            var number = requireNumber(value);
            return switch (precedence(number)) {
                case PRECEDENCE_BYTE,
                     PRECEDENCE_SHORT,
                     PRECEDENCE_INTEGER,
                     PRECEDENCE_LONG,
                     PRECEDENCE_BIG_INTEGER -> normalize(number);
                case PRECEDENCE_BIG_DECIMAL -> toBigDecimal(number).setScale(0, RoundingMode.HALF_EVEN);
                default -> unaryDouble(value, StrictMath::rint);
            };
        }

        static Object round(@Nullable Object value) {
            var number = requireNumber(value);
            return switch (precedence(number)) {
                case PRECEDENCE_BYTE,
                     PRECEDENCE_SHORT,
                     PRECEDENCE_INTEGER,
                     PRECEDENCE_LONG,
                     PRECEDENCE_BIG_INTEGER -> normalize(number);
                case PRECEDENCE_BIG_DECIMAL -> {
                    var bd = toBigDecimal(number);
                    yield bd.compareTo(BigDecimal.ZERO) >= 0
                            ? bd.setScale(0, RoundingMode.HALF_UP)
                            : bd.setScale(0, RoundingMode.HALF_DOWN);
                }
                case PRECEDENCE_FLOAT -> StrictMath.round(toFloat(number));
                case PRECEDENCE_DOUBLE -> StrictMath.round(toDouble(number));
                default -> throw unsupportedTypeException(number);
            };
        }
    }

    @UtilityClass
    static class ExactArithmeticOps {

        static Object addExact(@Nullable Object left, @Nullable Object right) {
            var first = requireNumber(left);
            var second = requireNumber(right);
            return switch (precedence(first, second)) {
                case PRECEDENCE_BYTE -> toByteExact(StrictMath.addExact(first.intValue(), second.intValue()));
                case PRECEDENCE_SHORT -> toShortExact(StrictMath.addExact(first.intValue(), second.intValue()));
                case PRECEDENCE_INTEGER -> StrictMath.addExact(first.intValue(), second.intValue());
                case PRECEDENCE_LONG -> StrictMath.addExact(first.longValue(), second.longValue());
                case PRECEDENCE_BIG_INTEGER -> toBigIntegerExact(first).add(toBigIntegerExact(second));
                case PRECEDENCE_BIG_DECIMAL -> toBigDecimalExact(first).add(toBigDecimalExact(second));
                default -> throw unsupportedTypeException(first, second);
            };
        }

        static Object subtractExact(@Nullable Object left, @Nullable Object right) {
            var first = requireNumber(left);
            var second = requireNumber(right);
            return switch (precedence(first, second)) {
                case PRECEDENCE_BYTE -> toByteExact(StrictMath.subtractExact(first.intValue(), second.intValue()));
                case PRECEDENCE_SHORT -> toShortExact(StrictMath.subtractExact(first.intValue(), second.intValue()));
                case PRECEDENCE_INTEGER -> StrictMath.subtractExact(first.intValue(), second.intValue());
                case PRECEDENCE_LONG -> StrictMath.subtractExact(first.longValue(), second.longValue());
                case PRECEDENCE_BIG_INTEGER -> toBigIntegerExact(first).subtract(toBigIntegerExact(second));
                case PRECEDENCE_BIG_DECIMAL -> toBigDecimalExact(first).subtract(toBigDecimalExact(second));
                default -> throw unsupportedTypeException(first, second);
            };
        }

        static Object multiplyExact(@Nullable Object left, @Nullable Object right) {
            var first = requireNumber(left);
            var second = requireNumber(right);
            var firstPrecedence = precedence(first);
            var secondPrecedence = precedence(second);
            return switch (firstPrecedence | secondPrecedence) {
                case PRECEDENCE_BYTE -> toByteExact(StrictMath.multiplyExact(first.intValue(), second.intValue()));
                case PRECEDENCE_SHORT -> toShortExact(StrictMath.multiplyExact(first.intValue(), second.intValue()));
                case PRECEDENCE_INTEGER -> StrictMath.multiplyExact(first.intValue(), second.intValue());
                case PRECEDENCE_LONG -> {
                    if (firstPrecedence == PRECEDENCE_LONG
                            && (secondPrecedence == PRECEDENCE_BYTE
                            || secondPrecedence == PRECEDENCE_SHORT
                            || secondPrecedence == PRECEDENCE_INTEGER)) {
                        yield StrictMath.multiplyExact(first.longValue(), second.intValue());
                    }
                    if ((firstPrecedence == PRECEDENCE_BYTE
                            || firstPrecedence == PRECEDENCE_SHORT
                            || firstPrecedence == PRECEDENCE_INTEGER)
                            && secondPrecedence == PRECEDENCE_LONG) {
                        yield StrictMath.multiplyExact(second.longValue(), first.intValue());
                    }
                    yield StrictMath.multiplyExact(first.longValue(), second.longValue());
                }
                case PRECEDENCE_BIG_INTEGER -> toBigIntegerExact(first).multiply(toBigIntegerExact(second));
                case PRECEDENCE_BIG_DECIMAL -> toBigDecimalExact(first).multiply(toBigDecimalExact(second));
                default -> throw unsupportedTypeException(first, second);
            };
        }

        static Object divideExact(@Nullable Object left, @Nullable Object right) {
            var first = requireNumber(left);
            var second = requireNumber(right);
            return switch (precedence(first, second)) {
                case PRECEDENCE_BYTE -> toByteExact(StrictMath.divideExact(first.intValue(), second.intValue()));
                case PRECEDENCE_SHORT -> toShortExact(StrictMath.divideExact(first.intValue(), second.intValue()));
                case PRECEDENCE_INTEGER -> StrictMath.divideExact(first.intValue(), second.intValue());
                case PRECEDENCE_LONG -> StrictMath.divideExact(first.longValue(), second.longValue());
                case PRECEDENCE_BIG_INTEGER -> divideExact(toBigIntegerExact(first), toBigIntegerExact(second));
                default -> throw unsupportedTypeException(first, second);
            };
        }

        private static BigInteger divideExact(BigInteger left, BigInteger right) {
            var result = left.divideAndRemainder(right);
            if (result[1].signum() != 0) {
                throw new ArithmeticException("BigInteger divideExact inexact");
            }
            return result[0];
        }

        static Object floorDiv(@Nullable Object left, @Nullable Object right) {
            var first = requireNumber(left);
            var second = requireNumber(right);
            var firstPrecedence = precedence(first);
            var secondPrecedence = precedence(second);
            return switch (firstPrecedence | secondPrecedence) {
                case PRECEDENCE_BYTE,
                     PRECEDENCE_SHORT,
                     PRECEDENCE_INTEGER -> StrictMath.floorDiv(first.intValue(), second.intValue());
                case PRECEDENCE_LONG -> {
                    if (firstPrecedence == PRECEDENCE_LONG
                            && (secondPrecedence == PRECEDENCE_BYTE
                            || secondPrecedence == PRECEDENCE_SHORT
                            || secondPrecedence == PRECEDENCE_INTEGER)) {
                        yield StrictMath.floorDiv(first.longValue(), second.intValue());
                    }
                    yield StrictMath.floorDiv(first.longValue(), second.longValue());
                }
                case PRECEDENCE_BIG_INTEGER -> floorDiv(toBigIntegerExact(first), toBigIntegerExact(second));
                default -> throw unsupportedTypeException(first, second);
            };
        }

        static Object floorDivExact(@Nullable Object left, @Nullable Object right) {
            var first = requireNumber(left);
            var second = requireNumber(right);
            return switch (precedence(first, second)) {
                case PRECEDENCE_BYTE -> toByteExact(StrictMath.floorDivExact(first.intValue(), second.intValue()));
                case PRECEDENCE_SHORT -> toShortExact(StrictMath.floorDivExact(first.intValue(), second.intValue()));
                case PRECEDENCE_INTEGER -> StrictMath.floorDivExact(first.intValue(), second.intValue());
                case PRECEDENCE_LONG -> StrictMath.floorDivExact(first.longValue(), second.longValue());
                case PRECEDENCE_BIG_INTEGER -> floorDiv(toBigIntegerExact(first), toBigIntegerExact(second));
                default -> throw unsupportedTypeException(first, second);
            };
        }

        private static BigInteger floorDiv(BigInteger left, BigInteger right) {
            var result = left.divideAndRemainder(right);
            if (result[1].signum() != 0 && left.signum() != right.signum()) {
                return result[0].subtract(BigInteger.ONE);
            }
            return result[0];
        }

        static Object floorMod(@Nullable Object left, @Nullable Object right) {
            var first = requireNumber(left);
            var second = requireNumber(right);
            var firstPrecedence = precedence(first);
            var secondPrecedence = precedence(second);
            return switch (firstPrecedence | secondPrecedence) {
                case PRECEDENCE_BYTE,
                     PRECEDENCE_SHORT,
                     PRECEDENCE_INTEGER -> StrictMath.floorMod(first.intValue(), second.intValue());
                case PRECEDENCE_LONG -> {
                    if (firstPrecedence == PRECEDENCE_LONG
                            && (secondPrecedence == PRECEDENCE_BYTE
                            || secondPrecedence == PRECEDENCE_SHORT
                            || secondPrecedence == PRECEDENCE_INTEGER)) {
                        yield StrictMath.floorMod(first.longValue(), second.intValue());
                    }
                    yield StrictMath.floorMod(first.longValue(), second.longValue());
                }
                case PRECEDENCE_BIG_INTEGER -> floorMod(toBigIntegerExact(first), toBigIntegerExact(second));
                default -> throw unsupportedTypeException(first, second);
            };
        }

        private static BigInteger floorMod(BigInteger left, BigInteger right) {
            return left.subtract(floorDiv(left, right).multiply(right));
        }

        static Object ceilDiv(@Nullable Object left, @Nullable Object right) {
            var first = requireNumber(left);
            var second = requireNumber(right);
            var firstPrecedence = precedence(first);
            var secondPrecedence = precedence(second);
            return switch (firstPrecedence | secondPrecedence) {
                case PRECEDENCE_BYTE,
                     PRECEDENCE_SHORT,
                     PRECEDENCE_INTEGER -> StrictMath.ceilDiv(first.intValue(), second.intValue());
                case PRECEDENCE_LONG -> {
                    if (firstPrecedence == PRECEDENCE_LONG
                            && (secondPrecedence == PRECEDENCE_BYTE
                            || secondPrecedence == PRECEDENCE_SHORT
                            || secondPrecedence == PRECEDENCE_INTEGER)) {
                        yield StrictMath.ceilDiv(first.longValue(), second.intValue());
                    }
                    yield StrictMath.ceilDiv(first.longValue(), second.longValue());
                }
                case PRECEDENCE_BIG_INTEGER -> ceilDiv(toBigIntegerExact(first), toBigIntegerExact(second));
                default -> throw unsupportedTypeException(first, second);
            };
        }

        private static BigInteger ceilDiv(BigInteger left, BigInteger right) {
            var result = left.divideAndRemainder(right);
            if (result[1].signum() != 0 && left.signum() == right.signum()) {
                return result[0].add(BigInteger.ONE);
            }
            return result[0];
        }

        static Object ceilDivExact(@Nullable Object left, @Nullable Object right) {
            var first = requireNumber(left);
            var second = requireNumber(right);
            return switch (precedence(first, second)) {
                case PRECEDENCE_BYTE -> toByteExact(StrictMath.ceilDivExact(first.intValue(), second.intValue()));
                case PRECEDENCE_SHORT -> toShortExact(StrictMath.ceilDivExact(first.intValue(), second.intValue()));
                case PRECEDENCE_INTEGER -> StrictMath.ceilDivExact(first.intValue(), second.intValue());
                case PRECEDENCE_LONG -> StrictMath.ceilDivExact(first.longValue(), second.longValue());
                case PRECEDENCE_BIG_INTEGER -> ceilDiv(toBigIntegerExact(first), toBigIntegerExact(second));
                default -> throw unsupportedTypeException(first, second);
            };
        }

        static Object ceilMod(@Nullable Object left, @Nullable Object right) {
            var first = requireNumber(left);
            var second = requireNumber(right);
            var firstPrecedence = precedence(first);
            var secondPrecedence = precedence(second);
            return switch (firstPrecedence | secondPrecedence) {
                case PRECEDENCE_BYTE -> toByteExact(StrictMath.ceilMod(first.byteValue(), second.byteValue()));
                case PRECEDENCE_SHORT -> toShortExact(StrictMath.ceilMod(first.shortValue(), second.shortValue()));
                case PRECEDENCE_INTEGER -> StrictMath.ceilMod(first.intValue(), second.intValue());
                case PRECEDENCE_LONG -> {
                    if (firstPrecedence == PRECEDENCE_LONG
                            && (secondPrecedence == PRECEDENCE_BYTE
                            || secondPrecedence == PRECEDENCE_SHORT
                            || secondPrecedence == PRECEDENCE_INTEGER)) {
                        yield StrictMath.ceilMod(first.longValue(), second.intValue());
                    }
                    yield StrictMath.ceilMod(first.longValue(), second.longValue());
                }
                case PRECEDENCE_BIG_INTEGER -> ceilMod(toBigIntegerExact(first), toBigIntegerExact(second));
                default -> throw unsupportedTypeException(first, second);
            };
        }

        private static BigInteger ceilMod(BigInteger left, BigInteger right) {
            return left.subtract(ceilDiv(left, right).multiply(right));
        }

        static Object incrementExact(@Nullable Object value) {
            var number = requireNumber(value);
            return switch (precedence(number)) {
                case PRECEDENCE_BYTE -> toByteExact(StrictMath.incrementExact(number.intValue()));
                case PRECEDENCE_SHORT -> toShortExact(StrictMath.incrementExact(number.intValue()));
                case PRECEDENCE_INTEGER -> StrictMath.incrementExact(number.intValue());
                case PRECEDENCE_LONG -> StrictMath.incrementExact(number.longValue());
                case PRECEDENCE_BIG_INTEGER -> toBigIntegerExact(number).add(BigInteger.ONE);
                case PRECEDENCE_BIG_DECIMAL -> toBigDecimalExact(number).add(BigDecimal.ONE);
                default -> throw unsupportedTypeException(number);
            };
        }

        static Object decrementExact(@Nullable Object value) {
            var number = requireNumber(value);
            return switch (precedence(number)) {
                case PRECEDENCE_BYTE -> toByteExact(StrictMath.decrementExact(number.intValue()));
                case PRECEDENCE_SHORT -> toShortExact(StrictMath.decrementExact(number.intValue()));
                case PRECEDENCE_INTEGER -> StrictMath.decrementExact(number.intValue());
                case PRECEDENCE_LONG -> StrictMath.decrementExact(number.longValue());
                case PRECEDENCE_BIG_INTEGER -> toBigIntegerExact(number).subtract(BigInteger.ONE);
                case PRECEDENCE_BIG_DECIMAL -> toBigDecimalExact(number).subtract(BigDecimal.ONE);
                default -> throw unsupportedTypeException(number);
            };
        }

        static Object negateExact(@Nullable Object value) {
            var number = requireNumber(value);
            return switch (precedence(number)) {
                case PRECEDENCE_BYTE -> toByteExact(StrictMath.negateExact(number.intValue()));
                case PRECEDENCE_SHORT -> toShortExact(StrictMath.negateExact(number.intValue()));
                case PRECEDENCE_INTEGER -> StrictMath.negateExact(number.intValue());
                case PRECEDENCE_LONG -> StrictMath.negateExact(number.longValue());
                case PRECEDENCE_BIG_INTEGER -> toBigIntegerExact(number).negate();
                case PRECEDENCE_BIG_DECIMAL -> toBigDecimalExact(number).negate();
                default -> throw unsupportedTypeException(number);
            };
        }

        static Object absExact(@Nullable Object value) {
            var number = requireNumber(value);
            return switch (precedence(number)) {
                case PRECEDENCE_BYTE -> toByteExact(StrictMath.absExact(number.intValue()));
                case PRECEDENCE_SHORT -> toShortExact(StrictMath.absExact(number.intValue()));
                case PRECEDENCE_INTEGER -> StrictMath.absExact(number.intValue());
                case PRECEDENCE_LONG -> StrictMath.absExact(number.longValue());
                case PRECEDENCE_BIG_INTEGER -> toBigIntegerExact(number).abs();
                case PRECEDENCE_BIG_DECIMAL -> toBigDecimalExact(number).abs();
                default -> throw unsupportedTypeException(number);
            };
        }

        static Integer toIntExact(@Nullable Object value) {
            var number = requireNumber(value);
            return switch (precedence(number)) {
                case PRECEDENCE_BYTE,
                     PRECEDENCE_SHORT,
                     PRECEDENCE_INTEGER -> number.intValue();
                case PRECEDENCE_LONG -> StrictMath.toIntExact(number.longValue());
                case PRECEDENCE_BIG_INTEGER -> toBigIntegerExact(number).intValueExact();
                case PRECEDENCE_BIG_DECIMAL -> toBigDecimalExact(number).intValueExact();
                default -> throw unsupportedTypeException(number);
            };
        }

    }

    @UtilityClass
    static class ReduceOps {

        static Object sumOf(Object... args) {
            return Support.flat(args)
                    .reduce(0, ReduceOps::plus);
        }

        private static Number plus(Number left, Number right) {
            var result = (Number) ALU.plus(left, right);
            Objects.requireNonNull(result);
            return result;
        }

        static Object maxOf(Object... args) {
            return Support.flat(args)
                    .reduce((a, b) -> (Number) BasicOps.max(a, b))
                    .orElseThrow(() -> new ScriptEvaluateException("maxOf requires at least one argument"));
        }

        static Object minOf(Object... args) {
            return Support.flat(args)
                    .reduce((a, b) -> (Number) BasicOps.min(a, b))
                    .orElseThrow(() -> new ScriptEvaluateException("minOf requires at least one argument"));
        }
    }

    @UtilityClass
    static class ExponentialLogOps {

        static Double exp(@Nullable Object value) {
            return unaryDouble(value, StrictMath::exp);
        }

        static Double expm1(@Nullable Object value) {
            return unaryDouble(value, StrictMath::expm1);
        }

        static Double ln(@Nullable Object value) {
            return unaryDouble(value, StrictMath::log);
        }

        static Double log10(@Nullable Object value) {
            return unaryDouble(value, StrictMath::log10);
        }

        static Double log1p(@Nullable Object value) {
            return unaryDouble(value, StrictMath::log1p);
        }

        static Double sqrt(@Nullable Object value) {
            return unaryDouble(value, StrictMath::sqrt);
        }

        static Double cbrt(@Nullable Object value) {
            return unaryDouble(value, StrictMath::cbrt);
        }

        static Double pow(@Nullable Object left, @Nullable Object right) {
            return StrictMath.pow(
                    toDouble(requireNumber(left)),
                    toDouble(requireNumber(right))
            );
        }
    }

    @UtilityClass
    static class TrigonometricOps {

        static Double sin(@Nullable Object value) {
            return unaryDouble(value, StrictMath::sin);
        }

        static Double cos(@Nullable Object value) {
            return unaryDouble(value, StrictMath::cos);
        }

        static Double tan(@Nullable Object value) {
            return unaryDouble(value, StrictMath::tan);
        }

        static Double asin(@Nullable Object value) {
            return unaryDouble(value, StrictMath::asin);
        }

        static Double acos(@Nullable Object value) {
            return unaryDouble(value, StrictMath::acos);
        }

        static Double atan(@Nullable Object value) {
            return unaryDouble(value, StrictMath::atan);
        }

        static Double atan2(@Nullable Object left, @Nullable Object right) {
            return StrictMath.atan2(
                    toDouble(requireNumber(left)),
                    toDouble(requireNumber(right))
            );
        }

        static Double sinh(@Nullable Object value) {
            return unaryDouble(value, StrictMath::sinh);
        }

        static Double cosh(@Nullable Object value) {
            return unaryDouble(value, StrictMath::cosh);
        }

        static Double tanh(@Nullable Object value) {
            return unaryDouble(value, StrictMath::tanh);
        }

        static Double toRadians(@Nullable Object value) {
            return unaryDouble(value, StrictMath::toRadians);
        }

        static Double toDegrees(@Nullable Object value) {
            return unaryDouble(value, StrictMath::toDegrees);
        }

        static Double hypot(@Nullable Object left, @Nullable Object right) {
            return StrictMath.hypot(
                    toDouble(requireNumber(left)),
                    toDouble(requireNumber(right))
            );
        }
    }
}
