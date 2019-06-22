/**
 * Copyright 2018-present febit.org (support@febit.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.rectify.engine;

/**
 * @author zqq90
 */
public class FilterBreakpoint {

    private final int index;
    private final String field;
    private final String expr;
    private FilterBreakpoint(int index, String field, String expr) {
        this.index = index;
        this.field = field;
        this.expr = expr;
    }

    public static FilterBreakpoint of(int index, String expr) {
        return of(index, null, expr);
    }

    public static FilterBreakpoint of(int index, String field, String expr) {
        return new FilterBreakpoint(index, field, expr);
    }

    @Override
    public String toString() {
        return "FilterBreakpoint{" + "index=" + index + ", field=" + field + ", expr=" + expr + '}';
    }

    public int getIndex() {
        return index;
    }

    public String getField() {
        return field;
    }

    public String getExpr() {
        return expr;
    }
}
