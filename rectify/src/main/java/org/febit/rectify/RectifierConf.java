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
package org.febit.rectify;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.val;
import org.febit.rectify.engine.ScriptBuilder;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Rectifier Config.
 *
 * @author zqq90
 */
@AllArgsConstructor
@lombok.Builder(builderClassName = "Builder")
public class RectifierConf implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String sourceFormat;

    @Singular
    private final Map<String, String> sourceFormatProps;

    @Singular
    private final List<GlobalSegment> globalSegments;

    @Singular
    private final List<Column> columns;

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
        val fields = this.columns.stream()
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

    public Map<String, String> sourceFormatProps() {
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

    private interface BuilderExtra {

        Builder column(Column column);

        Builder globalSegment(GlobalSegment segment);

        default Builder column(String type, String name, String expr) {
            return column(type, name, expr, null, null);
        }

        default Builder column(String type, String name, String expr, String checkExpr) {
            return column(type, name, expr, checkExpr, null);
        }

        default Builder column(String type, String name,
                               String expr, String checkExpr,
                               String comment) {
            Column column = new Column(type, name, expr, checkExpr, comment);
            return column(column);
        }

        default Builder globalCode(String code) {
            return globalSegment(context -> context.append(code));
        }

        default Builder globalCodes(Collection<String> codes) {
            codes.forEach(this::globalCode);
            return (Builder) this;
        }

        default Builder globalFilter(String expr) {
            return globalSegment(context -> ScriptBuilder.appendGlobalFilter(context, expr));
        }

        default Builder globalFilters(Collection<String> exprs) {
            exprs.forEach(this::globalFilter);
            return (Builder) this;
        }
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @JsonDeserialize(builder = Column.Builder.class)
    @lombok.Builder(builderClassName = "Builder")
    public static class Column implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String type;
        private final String name;
        private final String expr;
        private final String checkExpr;
        private final String comment;

        public String type() {
            return type;
        }

        public String name() {
            return name;
        }

        public String expr() {
            return expr;
        }

        public String checkExpr() {
            return checkExpr;
        }

        public String comment() {
            return comment;
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
        }
    }

    public static class Builder implements BuilderExtra {
    }
}
