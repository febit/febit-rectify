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
import org.febit.lang.func.Function2;
import org.febit.lang.util.ConvertUtils;
import org.febit.rectify.lib.IFunctions;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

@SuppressWarnings({
        "java:S1118", // Utility classes should not have public constructors
        "unused",
})
public class TemporalFunctions implements IFunctions {

    /**
     * Namespace: Dates.
     */
    @Alias(value = {"Dates"}, keepOriginName = false)
    public static final Proto DATES = new Proto();

    public static class Proto {

        public final Function0<Instant> now = Instant::now;
        public final Function1<Object, @Nullable ZoneOffset> zone = ConvertUtils::toZone;

        public final Function2<@Nullable String, @Nullable String, @Nullable Temporal> parse
                = TemporalFunctions::parse;
        public final Function2<@Nullable Temporal, @Nullable String, @Nullable String> format
                = TemporalFunctions::format;

        public final Function1<@Nullable Object, @Nullable Long> toMillis
                = ConvertUtils::toMillis;
        public final Function1<@Nullable Object, @Nullable Instant> toInstant
                = ConvertUtils::toInstant;

        public final Function1<@Nullable Object, @Nullable Integer> toHour
                = ConvertUtils::toHour;
        public final Function1<@Nullable Object, @Nullable LocalTime> toTime
                = ConvertUtils::toTime;
        public final Function1<@Nullable Object, @Nullable LocalDate> toDate
                = ConvertUtils::toDate;
        public final Function1<@Nullable Object, @Nullable LocalDateTime> toDateTime
                = ConvertUtils::toDateTime;
        public final Function1<@Nullable Object, @Nullable Integer> toDateNumber
                = ConvertUtils::toDateNumber;
        public final Function1<@Nullable Object, @Nullable ZonedDateTime> toZonedDateTime
                = ConvertUtils::toZonedDateTime;

        public final Function1<@Nullable Object, @Nullable Integer> toUtcHour
                = ConvertUtils::toUtcHour;
        public final Function1<@Nullable Object, @Nullable LocalTime> toUtcTime
                = ConvertUtils::toUtcTime;
        public final Function1<@Nullable Object, @Nullable LocalDate> toUtcDate
                = ConvertUtils::toUtcDate;
        public final Function1<@Nullable Object, @Nullable LocalDateTime> toUtcDateTime
                = ConvertUtils::toUtcDateTime;
        public final Function1<@Nullable Object, @Nullable Integer> toUtcDateNumber
                = ConvertUtils::toUtcDateNumber;
        public final Function1<@Nullable Object, @Nullable ZonedDateTime> toUtcZonedDateTime
                = ConvertUtils::toUtcZonedDateTime;

        public final Function2<@Nullable Temporal, @Nullable Integer, @Nullable Temporal> addMillis
                = plusFunc(ChronoUnit.MILLIS);
        public final Function2<@Nullable Temporal, @Nullable Integer, @Nullable Temporal> addSeconds
                = plusFunc(ChronoUnit.SECONDS);
        public final Function2<@Nullable Temporal, @Nullable Integer, @Nullable Temporal> addMinutes
                = plusFunc(ChronoUnit.MINUTES);
        public final Function2<@Nullable Temporal, @Nullable Integer, @Nullable Temporal> addHours
                = plusFunc(ChronoUnit.HOURS);
        public final Function2<@Nullable Temporal, @Nullable Integer, @Nullable Temporal> addDays
                = plusFunc(ChronoUnit.DAYS);
        public final Function2<@Nullable Temporal, @Nullable Integer, @Nullable Temporal> addMonths
                = plusFunc(ChronoUnit.MONTHS);
        public final Function2<@Nullable Temporal, @Nullable Integer, @Nullable Temporal> addYear
                = plusFunc(ChronoUnit.YEARS);

        public final Function1<@Nullable Temporal, @Nullable Temporal> firstDayOfMonth
                = adjustFunc(TemporalAdjusters.firstDayOfMonth());
        public final Function1<@Nullable Temporal, @Nullable Temporal> lastDayOfMonth
                = adjustFunc(TemporalAdjusters.lastDayOfMonth());
        public final Function1<@Nullable Temporal, @Nullable Temporal> firstDayOfNextMonth
                = adjustFunc(TemporalAdjusters.firstDayOfNextMonth());

        public final Function1<@Nullable Temporal, @Nullable Temporal> firstDayOfYear
                = adjustFunc(TemporalAdjusters.firstDayOfYear());
        public final Function1<@Nullable Temporal, @Nullable Temporal> lastDayOfYear
                = adjustFunc(TemporalAdjusters.lastDayOfYear());
        public final Function1<@Nullable Temporal, @Nullable Temporal> firstDayOfNextYear
                = adjustFunc(TemporalAdjusters.firstDayOfNextYear());

        private static Function1<@Nullable Temporal, @Nullable Temporal> adjustFunc(TemporalAdjuster adjuster) {
            return temporal -> {
                if (temporal == null) {
                    return null;
                }
                return temporal.with(adjuster);
            };
        }

        private static Function2<@Nullable Temporal, @Nullable Integer, @Nullable Temporal> plusFunc(ChronoUnit unit) {
            return (temporal, i) -> {
                if (temporal == null) {
                    return null;
                }
                if (i == null) {
                    return temporal;
                }
                return temporal.plus(i, unit);
            };
        }
    }

    private static DateTimeFormatter fmt(String pattern) {
        return DateTimeFormatter.ofPattern(pattern);
    }

    @Nullable
    private static Temporal parse(@Nullable String obj, @Nullable String pattern) {
        if (obj == null) {
            return null;
        }
        if (pattern == null) {
            return ConvertUtils.toZonedDateTime(obj);
        }
        var parsed = fmt(pattern)
                .parse(obj);
        return ConvertUtils.toZonedDateTime(parsed);
    }

    @Nullable
    private static String format(@Nullable Object obj, @Nullable String pattern) {
        if (pattern == null) {
            return obj != null ? obj.toString() : null;
        }
        var time = ConvertUtils.toZonedDateTime(obj);
        if (time == null) {
            return null;
        }
        return fmt(pattern)
                .format(time);
    }
}
