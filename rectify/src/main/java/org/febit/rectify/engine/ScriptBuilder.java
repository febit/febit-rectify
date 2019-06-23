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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.febit.rectify.RectifierConf;
import org.febit.util.StringUtil;

import java.util.List;

/**
 * @author zqq90
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScriptBuilder {

    public static final String VAR_INPUT = "$";
    public static final String VAR_CURR = "$$";
    public static final String VAR_EXIT = "$$_EXIT";
    public static final String VAR_RESULT = "$$_RESULT";
    public static final String VAR_SCHEMA_NAME = "$$_SCHEMA_NAME";
    public static final String VAR_CURR_COLUMN = "$$_CURR_COLUMN";
    public static final String VAR_CHECK_FILTER = "$$_CHECK_FILTER";
    public static final String VAR_NEW_FILTER_BREAKPOINT = "$$_NEW_FILTER_BREAKPOINT";

    private static String escapeForString(String str) {
        if (str == null) {
            return "null";
        }
        return StringUtil.escapeForJsonString(str, true);
    }

    public static String buildScript(RectifierConf conf, boolean debug) {
        return buildScript(new Context(conf, debug));
    }

    public static String buildScript(final Context context) {
        final RectifierConf conf = context.getConf();

        // Vars
        context.append("const ").append(VAR_SCHEMA_NAME)
                .append(" = ")
                .appendStringValue(conf.name())
                .append(";\n");
        context.append(""
                + "var " + VAR_INPUT + ";   // 日志解析后的内容\n"
                + "var " + VAR_CURR + ";  // 当前字段值\n"
                + "var " + VAR_RESULT + ";  // 结果集\n"
                + "var " + VAR_CURR_COLUMN + " = -1;  // 当前字段索引\n"
        );

        // Global Segments
        context.append("\n"
                + "// -- Global Segments:\n"
        );
        for (RectifierConf.GlobalSegment segment : conf.globalSegments()) {
            context.append("//-\n");
            segment.appendTo(context);
            context.append(";\n");
        }

        // Columns
        final List<RectifierConf.Column> columns = conf.columns();
        context.append("\n// -- Columns\n");
        for (int i = 0; i < columns.size(); i++) {
            appendColumn(context, columns.get(i), i);
        }

        return context.toString();
    }

    public static void appendGlobalFilter(Context context, String filterExpr) {
        appendFilter(context, filterExpr, context.getAndIncFilterCounter(), null);
    }

    private static void appendFilter(Context context, String filterExpr,
                                     int index, String column) {
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
        final String escapedName = escapeForString(column.name());

        String expr = column.expr();
        if (StringUtil.isBlank(expr)) {
            expr = VAR_INPUT + '[' + escapedName + ']';
        }

        context.append("// -\n");
        context.append(VAR_CURR_COLUMN + " = " + index + "; \n");
        context.append(VAR_CURR + " = " + VAR_RESULT + '[')
                .append(escapedName)
                .append("] = ")
                .append(expr)
                .append(";\n");

        if (StringUtil.isNotEmpty(column.checkExpr())) {
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

        public Context appendStringValue(String str) {
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
