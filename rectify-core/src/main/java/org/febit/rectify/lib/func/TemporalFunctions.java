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

import org.febit.rectify.function.IFunctions;
import org.febit.rectify.function.ObjFunc;
import org.febit.rectify.function.StrStrFunc;
import org.febit.rectify.function.TemporalFunc;
import org.febit.rectify.function.TemporalIntFunc;
import org.febit.rectify.function.TemporalStrFunc;
import org.febit.rectify.function.VoidFunc;
import org.febit.rectify.util.TimeConvert;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
        public final ObjFunc zone = TimeConvert::toZone;

        public final StrStrFunc parse = TimeConvert::parse;
        public final TemporalStrFunc format = TimeConvert::format;

        public final ObjFunc toMillis = TimeConvert::toMillis;
        public final ObjFunc toInstant = TimeConvert::toInstant;

        public final ObjFunc toHour = TimeConvert::toHour;
        public final ObjFunc toTime = TimeConvert::toTime;
        public final ObjFunc toDate = TimeConvert::toDate;
        public final ObjFunc toDateTime = TimeConvert::toDateTime;
        public final ObjFunc toDateNumber = TimeConvert::toDateNumber;
        public final ObjFunc toZonedDateTime = TimeConvert::toZonedDateTime;

        public final ObjFunc toUtcHour = TimeConvert::toUtcHour;
        public final ObjFunc toUtcTime = TimeConvert::toUtcTime;
        public final ObjFunc toUtcDate = TimeConvert::toUtcDate;
        public final ObjFunc toUtcDateTime = TimeConvert::toUtcDateTime;
        public final ObjFunc toUtcDateNumber = TimeConvert::toUtcDateNumber;
        public final ObjFunc toUtcZonedDateTime = TimeConvert::toUtcZonedDateTime;

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
}
