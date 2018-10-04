/**
 * Copyright 2018-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.rectify;

import org.febit.rectify.engine.ScriptBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Rectifier Config.
 *
 * @author zqq90
 */
public class RectifierConf implements Serializable {

    private static final long serialVersionUID = 1L;

    public static RectifierConf.Builder builder() {
        return new Builder();
    }

    private final String name;
    private final String sourceFormat;
    private final String sourceFormatProps;
    private final List<GlobalSegment> globalSegments;
    private final List<Column> columns;

    private RectifierConf(String name, String sourceFormat, String sourceFormatProps,
                          List<GlobalSegment> globalSegments, List<Column> columns) {
        this.name = name;
        this.sourceFormat = sourceFormat;
        this.sourceFormatProps = sourceFormatProps;
        this.globalSegments = Collections.unmodifiableList(new ArrayList<>(globalSegments));
        this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
    }

    public String script(boolean debug) {
        return ScriptBuilder.buildScript(this, debug);
    }

    private Schema.Field columnToSchemaField(Column column) {
        Schema fieldSchema = Schema.parse(name, column.name(), column.type());
        return Schema.newField(column.name, fieldSchema, column.comment);
    }

    public Schema schema() {
        return schema(col -> true);
    }

    public Schema schema(Predicate<Column> filter) {
        List<Schema.Field> fields = this.columns.stream()
                .filter(filter)
                .map(this::columnToSchemaField)
                .collect(Collectors.toList());
        return Schema.forStruct(null, name, fields);
    }

    public String name() {
        return name;
    }

    public String sourceFormat() {
        return sourceFormat;
    }

    public String sourceFormatProps() {
        return sourceFormatProps;
    }

    public List<Column> columns() {
        return columns;
    }

    public List<GlobalSegment> globalSegments() {
        return globalSegments;
    }

    @Override
    public String toString() {
        return "RectifierConf{"
                + "name='" + name + '\''
                + '}';
    }

    @FunctionalInterface
    public interface GlobalSegment extends Serializable {
        void appendTo(ScriptBuilder.Context context);
    }

    public static class Column implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String type;
        private final String name;
        private final String convertExpr;
        private final String checkExpr;
        private final String comment;

        private Column(String type, String name, String convertExpr, String checkExpr, String comment) {
            this.type = type;
            this.name = name;
            this.convertExpr = convertExpr;
            this.checkExpr = checkExpr;
            this.comment = comment;
        }

        public String type() {
            return type;
        }

        public String name() {
            return name;
        }

        public String convertExpr() {
            return convertExpr;
        }

        public String checkExpr() {
            return checkExpr;
        }

        public String comment() {
            return comment;
        }
    }

    public static class Builder {

        private String name;
        private String sourceFormat;
        private String sourceFormatProps;
        private final List<GlobalSegment> globalSegments = new ArrayList<>();
        private final List<Column> columns = new ArrayList<>();

        public RectifierConf build() {
            return new RectifierConf(name, sourceFormat, sourceFormatProps,
                    globalSegments, columns);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder sourceFormat(String sourceFormat) {
            this.sourceFormat = sourceFormat;
            return this;
        }

        public Builder sourceFormatProps(String sourceFormatProps) {
            this.sourceFormatProps = sourceFormatProps;
            return this;
        }

        public Builder addColumn(String type, String name, String convertExpr) {
            return addColumn(type, name, convertExpr, null, null);
        }

        public Builder addColumn(String type, String name, String convertExpr, String checkExpr) {
            return addColumn(type, name, convertExpr, checkExpr, null);
        }

        public Builder addColumn(String type, String name,
                                 String convertExpr, String checkExpr,
                                 String comment) {
            Column column = new Column(type, name, convertExpr, checkExpr, comment);
            columns.add(column);
            return this;
        }

        public Builder addGlobalCode(String code) {
            globalSegments.add(context -> context.append(code));
            return this;
        }

        public Builder addGlobalFilter(String expr) {
            globalSegments.add(context -> ScriptBuilder.appendGlobalFilter(context, expr));
            return this;
        }
    }
}
