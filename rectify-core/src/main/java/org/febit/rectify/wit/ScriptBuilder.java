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
package org.febit.rectify.wit;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.febit.rectify.RectifierSettings;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScriptBuilder {

    public static final String VAR_INPUT = "$";

    public static final String VAR_EXIT = "$$_EXIT";
    public static final String VAR_RESULT = "$$_RESULT";
    public static final String VAR_SCHEMA_NAME = "$$_SCHEMA_NAME";

    public static final String VAR_PROPERTY_VALUE = "$$";
    public static final String VAR_PROPERTY_INDEX = "$$_PROPERTY_INDEX";

    public static final String VAR_FILTER_VERIFY = "$$_FILTER_VERIFY";
    public static final String VAR_CREATE_FILTER_BREAKPOINT = "$$_CREATE_FILTER_BREAKPOINT";

    static String escapeForString(@Nullable String str) {
        if (str == null) {
            return "null";
        }
        var len = str.length();
        var buf = new StringBuilder(len + 16);
        buf.append('"');

        char ch;
        for (int i = 0; i < len; i++) {
            ch = str.charAt(i);
            char escape;
            switch (ch) {
                case '\b', '\f' -> {
                    // Ignore ctrl chars
                    continue;
                    // Ignore ctrl chars
                }
                case '\n' -> escape = 'n';
                case '\r' -> escape = 'r';
                case '\t' -> escape = 't';
                case '"' -> escape = '"';
                case '\\' -> escape = '\\';
                default -> {
                    buf.append(ch);
                    continue;
                }
            }
            buf.append('\\').append(escape);
        }
        buf.append('"');
        return buf.toString();
    }

    public static String build(RectifierSettings settings, boolean debug) {
        return build(new Context(settings, debug));
    }

    private static String build(final Context context) {
        var settings = context.settings();

        // Vars
        context.append("const ").append(VAR_SCHEMA_NAME)
                .append(" = ")
                .appendStringValue(settings.name())
                .append(";\n");
        context.append("\n"
                + "// -- Internal Vars:\n"
                + "var " + VAR_INPUT + ";   // formatted input\n"
                + "var " + VAR_RESULT + ";  // raw result (output)\n"
                + "var " + VAR_PROPERTY_VALUE + ";  // value of current property\n"
                + "var " + VAR_PROPERTY_INDEX + " = -1;  // index of current property\n"
        );

        // Global Segments
        context.append("""

                // -- Global Segments:
                """
        );
        for (var setup : settings.preinstalls()) {
            context.append("//-\n");
            setup.setup(context);
            context.append(";\n");
        }

        // Properties
        var properties = settings.properties();
        context.append("""

                // -- Properties:
                """);
        for (int i = 0; i < properties.size(); i++) {
            appendProperty(context, properties.get(i), i);
        }

        return context.buildScript();
    }

    public static void appendFilter(Context context, String filterExpr) {
        appendFilter(context, filterExpr, context.filterCounter().getAndIncrement(), null);
    }

    private static void appendFilter(
            Context context,
            String filterExpr,
            int index,
            @Nullable String property
    ) {
        context.append(VAR_FILTER_VERIFY + "(");
        if (context.debugEnabled()) {
            context.append("[? " + VAR_CREATE_FILTER_BREAKPOINT + "(")
                    .append(index)
                    .append(", ")
                    .appendStringValue(property)
                    .append(", ")
                    .appendStringValue(filterExpr)
                    .append(") : (");
        }
        context.append(filterExpr);
        if (context.debugEnabled()) {
            context.append(") ?]");
        }
        context.append(");\n");
    }

    public static void appendProperty(
            Context context,
            RectifierSettings.Property property,
            int index
    ) {
        var escapedName = escapeForString(property.name());
        var expr = property.expression();
        if (StringUtils.isBlank(expr)) {
            expr = VAR_INPUT + '[' + escapedName + ']';
        }

        context.append("// -\n");
        context.append(VAR_PROPERTY_INDEX + " = " + index + "; \n");
        context.append(VAR_PROPERTY_VALUE + " = " + VAR_RESULT + '[')
                .append(escapedName)
                .append("] = ")
                .append(expr)
                .append(";\n");

        var validation = property.validation();
        if (StringUtils.isNotEmpty(validation)) {
            appendFilter(context, validation, index, property.name());
        }
    }

    @Accessors(fluent = true)
    public static class Context {

        @Getter
        private final boolean debugEnabled;

        @Getter
        private final RectifierSettings settings;
        @Getter
        private final AtomicInteger filterCounter = new AtomicInteger(0);

        private final StringBuilder buffer = new StringBuilder(1024);

        public Context(RectifierSettings settings, boolean debugEnabled) {
            this.settings = settings;
            this.debugEnabled = debugEnabled;
        }

        public Context append(String code) {
            this.buffer.append(code);
            return this;
        }

        public Context append(int num) {
            this.buffer.append(num);
            return this;
        }

        public Context append(char c) {
            this.buffer.append(c);
            return this;
        }

        public Context appendStringValue(@Nullable String str) {
            return append(escapeForString(str));
        }

        public String buildScript() {
            return this.buffer.toString();
        }
    }

}
