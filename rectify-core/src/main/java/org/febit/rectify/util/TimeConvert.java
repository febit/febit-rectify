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

import lombok.experimental.UtilityClass;
import org.febit.lang.util.TimeUtils;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

@UtilityClass
public class TimeConvert {

    private static DateTimeFormatter fmt(String pattern) {
        return DateTimeFormatter.ofPattern(pattern);
    }

    @Nullable
    private static <T> T convertOrParse(@Nullable Object obj, Function<TemporalAccessor, T> convert, Function<String, T> parser) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof TemporalAccessor) {
            return convert.apply((TemporalAccessor) obj);
        }
        var str = obj.toString();
        return parser.apply(str);
    }

    @Nullable
    private static <T> T utc(@Nullable Object obj, Function<ZonedDateTime, T> convert) {
        var time = toUtcZonedDateTime(obj);
        if (time == null) {
            return null;
        }
        return convert.apply(time);
    }

    @Nullable
    public static Temporal parse(@Nullable String obj, @Nullable String pattern) {
        if (obj == null) {
            return null;
        }
        if (pattern == null) {
            return toZonedDateTime(obj);
        }
        var parsed = fmt(pattern)
                .parse(obj);
        return toZonedDateTime(parsed);
    }

    @Nullable
    public static String format(@Nullable Object obj, @Nullable String pattern) {
        if (pattern == null) {
            return obj != null ? obj.toString() : null;
        }
        var time = toZonedDateTime(obj);
        if (time == null) {
            return null;
        }
        return fmt(pattern)
                .format(time);
    }

    @Nullable
    public static Instant toInstant(@Nullable Object obj) {
        if (obj instanceof Number) {
            return Instant.ofEpochMilli(((Number) obj).longValue());
        }
        return convertOrParse(obj, TimeUtils::instant, TimeUtils::parseInstant);
    }

    @Nullable
    public static Temporal toTemporal(@Nullable Object obj) {
        if (obj instanceof Temporal) {
            return (Temporal) obj;
        }
        return toZonedDateTime(obj);
    }

    @Nullable
    public static Long toMillis(@Nullable Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        var instant = toInstant(obj);
        if (instant == null) {
            return null;
        }
        return instant.toEpochMilli();
    }

    @Nullable
    public static ZoneOffset toZone(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof ZoneOffset) {
            return (ZoneOffset) obj;
        }
        return ZoneOffset.of(obj.toString());
    }

    @Nullable
    public static ZonedDateTime toZonedDateTime(@Nullable Object obj) {
        return convertOrParse(obj, TimeUtils::zonedDateTime, TimeUtils::parseZonedDateTime);
    }

    @Nullable
    public static LocalDateTime toDateTime(@Nullable Object obj) {
        return convertOrParse(obj, TimeUtils::localDateTime, TimeUtils::parseDateTime);
    }

    @Nullable
    public static LocalTime toTime(@Nullable Object obj) {
        return convertOrParse(obj, TimeUtils::localTime, TimeUtils::parseTime);
    }

    @Nullable
    public static LocalDate toDate(@Nullable Object obj) {
        return convertOrParse(obj, TimeUtils::localDate, TimeUtils::parseDate);
    }

    @Nullable
    public static Integer toHour(@Nullable Object obj) {
        return toHour(toTime(obj));
    }

    @Nullable
    public static Integer toDateNumber(@Nullable Object obj) {
        return toDateNumber(toDate(obj));
    }

    @Nullable
    public static ZonedDateTime toUtcZonedDateTime(@Nullable Object obj) {
        var time = toZonedDateTime(obj);
        if (time == null) {
            return null;
        }
        return time.withZoneSameInstant(ZoneOffset.UTC);
    }

    @Nullable
    public static LocalDateTime toUtcDateTime(@Nullable Object obj) {
        if (obj instanceof LocalDateTime) {
            return (LocalDateTime) obj;
        }
        return utc(obj, ZonedDateTime::toLocalDateTime);
    }

    @Nullable
    public static LocalDate toUtcDate(@Nullable Object obj) {
        if (obj instanceof LocalDate) {
            return (LocalDate) obj;
        }
        return utc(obj, ZonedDateTime::toLocalDate);
    }

    @Nullable
    public static LocalTime toUtcTime(@Nullable Object obj) {
        if (obj instanceof LocalTime) {
            return (LocalTime) obj;
        }
        var time = toZonedDateTime(obj);
        if (time == null) {
            return null;
        }
        return utc(obj, ZonedDateTime::toLocalTime);
    }

    @Nullable
    public static Integer toUtcHour(@Nullable Object obj) {
        return toHour(toUtcTime(obj));
    }

    @Nullable
    public static Integer toUtcDateNumber(@Nullable Object obj) {
        return toDateNumber(toUtcDate(obj));
    }

    @Nullable
    private static Integer toDateNumber(@Nullable LocalDate time) {
        if (time == null) {
            return null;
        }
        return time.getYear() * 10000
                + time.getMonth().getValue() * 100
                + time.getDayOfMonth();
    }

    @Nullable
    private static Integer toHour(@Nullable LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.getHour();
    }
}
