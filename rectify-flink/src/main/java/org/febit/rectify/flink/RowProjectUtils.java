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
package org.febit.rectify.flink;

import lombok.experimental.UtilityClass;
import org.apache.flink.types.Row;
import org.jspecify.annotations.Nullable;

@UtilityClass
public class RowProjectUtils {

    public static Row project(Row row, int[][] projections) {
        Row projected = Row.withPositions(row.getKind(), projections.length);
        for (int i = 0; i < projections.length; i++) {
            projected.setField(i, getField(row, projections[i]));
        }
        return projected;
    }

    @Nullable
    private static Object getField(@Nullable Object current, int[] path) {
        Object value = current;
        for (int index : path) {
            if (value == null) {
                return null;
            }
            if (!(value instanceof Row row)) {
                throw new IllegalArgumentException("Nested projection requires ROW values: " + value.getClass());
            }
            value = row.getField(index);
        }
        return value;
    }
}
