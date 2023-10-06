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

import org.febit.lang.util.ConvertUtils;
import org.febit.rectify.function.IFunctions;
import org.febit.rectify.function.ObjFunc;
import org.febit.rectify.function.StrStrFunc;
import org.febit.rectify.function.TemporalFunc;
import org.febit.rectify.function.TemporalIntFunc;
import org.febit.rectify.function.TemporalStrFunc;
import org.febit.rectify.function.VoidFunc;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

@SuppressWarnings({"unused"})
public class TemporalFunctions implements IFunctions {

    /**
     * Namespace: Dates.
     */
    @Alias(value = {"Dates"}, keepOriginName = false)
    public static final Proto DATES = new Proto();

    public static class Proto {

        public final VoidFunc now = Instant::now;
        public final ObjFunc zone = ConvertUtils::toZone;

        public final StrStrFunc parse = TemporalFunctions::parse;
        public final TemporalStrFunc format = TemporalFunctions::format;

        public final ObjFunc toMillis = ConvertUtils::toMillis;
        public final ObjFunc toInstant = ConvertUtils::toInstant;

        public final ObjFunc toHour = ConvertUtils::toHour;
        public final ObjFunc toTime = ConvertUtils::toTime;
        public final ObjFunc toDate = ConvertUtils::toDate;
        public final ObjFunc toDateTime = ConvertUtils::toDateTime;
        public final ObjFunc toDateNumber = ConvertUtils::toDateNumber;
        public final ObjFunc toZonedDateTime = ConvertUtils::toZonedDateTime;

        public final ObjFunc toUtcHour = ConvertUtils::toUtcHour;
        public final ObjFunc toUtcTime = ConvertUtils::toUtcTime;
        public final ObjFunc toUtcDate = ConvertUtils::toUtcDate;
        public final ObjFunc toUtcDateTime = ConvertUtils::toUtcDateTime;
        public final ObjFunc toUtcDateNumber = ConvertUtils::toUtcDateNumber;
        public final ObjFunc toUtcZonedDateTime = ConvertUtils::toUtcZonedDateTime;

        public final TemporalIntFunc addMillis = plusFunc(ChronoUnit.MILLIS);
        public final TemporalIntFunc addSeconds = plusFunc(ChronoUnit.SECONDS);
        public final TemporalIntFunc addMinutes = plusFunc(ChronoUnit.MINUTES);
        public final TemporalIntFunc addHours = plusFunc(ChronoUnit.HOURS);
        public final TemporalIntFunc addDays = plusFunc(ChronoUnit.DAYS);
        public final TemporalIntFunc addMonths = plusFunc(ChronoUnit.MONTHS);
        public final TemporalIntFunc addYear = plusFunc(ChronoUnit.YEARS);

        public final TemporalFunc firstDayOfMonth = adjustFunc(TemporalAdjusters.firstDayOfMonth());
        public final TemporalFunc lastDayOfMonth = adjustFunc(TemporalAdjusters.lastDayOfMonth());
        public final TemporalFunc firstDayOfNextMonth = adjustFunc(TemporalAdjusters.firstDayOfNextMonth());

        public final TemporalFunc firstDayOfYear = adjustFunc(TemporalAdjusters.firstDayOfYear());
        public final TemporalFunc lastDayOfYear = adjustFunc(TemporalAdjusters.lastDayOfYear());
        public final TemporalFunc firstDayOfNextYear = adjustFunc(TemporalAdjusters.firstDayOfNextYear());
    }

    private static TemporalIntFunc plusFunc(ChronoUnit unit) {
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

    private static TemporalFunc adjustFunc(TemporalAdjuster adjuster) {
        return temporal -> {
            if (temporal == null) {
                return null;
            }
            return temporal.with(adjuster);
        };
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
