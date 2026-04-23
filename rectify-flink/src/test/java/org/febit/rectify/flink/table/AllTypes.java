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
package org.febit.rectify.flink.table;

import org.febit.rectify.flink.table.AllTypesTableData.StructFoo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@lombok.Builder(
        builderClassName = "Builder"
)
record AllTypes(
        Integer id,
        Boolean bool,
        Byte tinyInt,
        Short shortNum,
        Integer intNum,
        Long longNum,
        Float floatNum,
        Double doubleNum,
        BigDecimal decimalNum,
        String text,
        LocalDate date,
        LocalTime time,
        LocalDateTime datetime,
        Instant instant,
        ZonedDateTime datatimetz,
        List<Integer> list,
        byte[] bytes,
        Map<String, Integer> map,
        StructFoo structFoo,
        Boolean[] arrayBool,
        Byte[] arrayByte,
        Short[] arrayShort,
        Integer[] arrayInt,
        Long[] arrayLong,
        Float[] arrayFloat,
        Double[] arrayDouble,
        BigDecimal[] arrayDecimal,
        String[] arrayString,
        StructFoo[] arrayFoo
) {

}
