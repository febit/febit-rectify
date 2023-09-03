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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.febit.rectify.RectifierConf;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScriptBuilder {

    public static final String VAR_INPUT = "$";
    public static final String VAR_CURR_FIELD = "$$";
    public static final String VAR_EXIT = "$$_EXIT";
    public static final String VAR_RESULT = "$$_RESULT";
    public static final String VAR_SCHEMA_NAME = "$$_SCHEMA_NAME";
    public static final String VAR_CURR_FIELD_INDEX = "$$_CURR_FIELD_INDEX";
    public static final String VAR_CHECK_FILTER = "$$_CHECK_FILTER";
    public static final String VAR_NEW_FILTER_BREAKPOINT = "$$_NEW_FILTER_BREAKPOINT";

    static String escapeForString(@Nullable String str) {
        if (str == null) {
            return "null";
        }
        final int len = str.length();
        final StringBuilder buf = new StringBuilder(len + 16);
        buf.append('"');

        char ch;
        for (int i = 0; i < len; i++) {
            ch = str.charAt(i);
            char replace;
            switch (ch) {
                case '\b':
                case '\f':
                    // Ignore ctrl chars
                    continue;
                case '\n':
                    replace = 'n';
                    break;
                case '\r':
                    replace = 'r';
                    break;
                case '\t':
                    replace = 't';
                    break;
                case '"':
                    replace = '"';
                    break;
                case '/':
                    replace = '/';
                    break;
                case '\\':
                    replace = '\\';
                    break;
                default:
                    buf.append(ch);
                    continue;
            }
            buf.append('\\').append(replace);
        }
        buf.append('"');
        return buf.toString();
    }

    public static String build(RectifierConf conf, boolean debug) {
        return build(new Context(conf, debug));
    }

    public static String build(final Context context) {
        final RectifierConf conf = context.getConf();

        // Vars
        context.append("const ").append(VAR_SCHEMA_NAME)
                .append(" = ")
                .appendStringValue(conf.getName())
                .append(";\n");
        context.append("\n"
                + "// -- Internal Vars:\n"
                + "var " + VAR_INPUT + ";   // formatted input\n"
                + "var " + VAR_CURR_FIELD + ";  // value of current column/field\n"
                + "var " + VAR_RESULT + ";  // raw result (output)\n"
                + "var " + VAR_CURR_FIELD_INDEX + " = -1;  // index of current column/field\n"
        );

        // Global Segments
        context.append("\n"
                + "// -- Global Segments:\n"
        );
        for (RectifierConf.Segment segment : conf.getFrontSegments()) {
            context.append("//-\n");
            segment.appendTo(context);
            context.append(";\n");
        }

        // Columns
        final List<RectifierConf.Column> columns = conf.getColumns();
        context.append("\n// -- Columns\n");
        for (int i = 0; i < columns.size(); i++) {
            appendColumn(context, columns.get(i), i);
        }

        return context.toString();
    }

    public static void appendFilter(Context context, String filterExpr) {
        appendFilter(context, filterExpr, context.getAndIncFilterCounter(), null);
    }

    private static void appendFilter(
            Context context, String filterExpr,
            int index,
            @Nullable String column) {
        context.append(VAR_CHECK_FILTER + "(");
        if (context.isDebugEnabled()) {
            context.append("[? " + VAR_NEW_FILTER_BREAKPOINT + "(")
                    .append(index)
                    .append(", ")
                    .appendStringValue(column)
                    .append(", ")
                    .appendStringValue(filterExpr)
                    .append(") : (");
        }
        context.append(filterExpr);
        if (context.isDebugEnabled()) {
            context.append(") ?]");
        }
        context.append(");\n");
    }

    public static void appendColumn(final Context context, final RectifierConf.Column column,
                                    final int index) {
        var escapedName = escapeForString(column.name());
        var expr = column.expr();
        if (StringUtils.isBlank(expr)) {
            expr = VAR_INPUT + '[' + escapedName + ']';
        }

        context.append("// -\n");
        context.append(VAR_CURR_FIELD_INDEX + " = " + index + "; \n");
        context.append(VAR_CURR_FIELD + " = " + VAR_RESULT + '[')
                .append(escapedName)
                .append("] = ")
                .append(expr)
                .append(";\n");

        if (StringUtils.isNotEmpty(column.checkExpr())) {
            appendFilter(context, column.checkExpr(), index, column.name());
        }
    }

    public static class Context {

        private final boolean debug;
        private final StringBuilder buf;
        private final RectifierConf conf;
        private int filterCounter;

        public Context(RectifierConf conf, boolean debug) {
            this.debug = debug;
            this.conf = conf;
            this.buf = new StringBuilder(1024);
        }

        public int getAndIncFilterCounter() {
            return filterCounter++;
        }

        public Context append(String code) {
            this.buf.append(code);
            return this;
        }

        public Context append(int num) {
            this.buf.append(num);
            return this;
        }

        public Context append(char c) {
            this.buf.append(c);
            return this;
        }

        public Context appendStringValue(@Nullable String str) {
            return append(escapeForString(str));
        }

        public boolean isDebugEnabled() {
            return debug;
        }

        public RectifierConf getConf() {
            return conf;
        }

        @Override
        public String toString() {
            return this.buf.toString();
        }
    }

}
