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
package org.febit.rectify.engine;

import jakarta.annotation.Nullable;

import java.util.Objects;

public class FilterBreakpoint {

    private final int index;
    @Nullable
    private final String field;
    private final String expr;

    private FilterBreakpoint(int index, @Nullable String field, String expr) {
        this.index = index;
        this.field = field;
        this.expr = expr;
    }

    public static FilterBreakpoint of(@Nullable Integer index, String expr) {
        return of(index, null, expr);
    }

    public static FilterBreakpoint of(@Nullable Integer index, @Nullable String field, @Nullable String expr) {
        if (index == null) {
            index = 0;
        }
        Objects.requireNonNull(expr);
        return new FilterBreakpoint(index, field, expr);
    }

    @Override
    public String toString() {
        return "FilterBreakpoint{" + "index=" + index + ", field=" + field + ", expr=" + expr + '}';
    }

    public int getIndex() {
        return index;
    }

    @Nullable
    public String getField() {
        return field;
    }

    public String getExpr() {
        return expr;
    }
}
