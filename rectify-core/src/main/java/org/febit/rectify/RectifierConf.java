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
package org.febit.rectify;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;
import org.febit.rectify.engine.ScriptBuilder;
import org.febit.wit.Engine;
import org.febit.wit.Template;
import org.febit.wit.debug.BreakpointListener;
import org.febit.wit.exceptions.ResourceNotFoundException;

import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Rectifier Config / Factory, Serializable.
 */
@Getter
@Setter
@SuppressWarnings({"unused"})
public class RectifierConf implements Serializable {

    private static final long serialVersionUID = 1L;

    private static class EngineLazyHolder {
        static final Engine ENGINE = Engine.create("febit-rectifier-engine.wim");
    }

    private String name = "Unnamed";
    private List<Column> columns = new ArrayList<>();
    private List<Segment> frontSegments = new ArrayList<>();
    private EngineProvider engineProvider = RectifierConf::defaultEngine;

    public static RectifierConf create() {
        return new RectifierConf();
    }

    private static Engine defaultEngine() {
        return EngineLazyHolder.ENGINE;
    }

    public Schema resolveSchema() {
        return resolveSchema(col -> true);
    }

    public Schema resolveSchema(Predicate<Column> filter) {
        val builder = Schemas.structSchemaBuilder();
        this.columns.stream()
                .filter(filter)
                .forEach(col -> {
                    val colSchema = Schema.parse(name, col.name(), col.type());
                    builder.field(col.name(), colSchema, col.comment());
                });
        return builder.build();
    }

    public RectifierConf engineSupplier(EngineProvider provider) {
        this.engineProvider = provider;
        return this;
    }

    public RectifierConf name(String name) {
        setName(name);
        return this;
    }

    public RectifierConf columns(Column... columns) {
        return columns(Arrays.asList(columns));
    }

    public RectifierConf columns(List<Column> columns) {
        this.columns.addAll(columns);
        return this;
    }

    public RectifierConf column(Column column) {
        this.columns.add(column);
        return this;
    }

    public RectifierConf frontSegment(Segment segment) {
        this.frontSegments.add(segment);
        return this;
    }

    public RectifierConf frontSegments(Segment... segments) {
        this.frontSegments.addAll(Arrays.asList(segments));
        return this;
    }

    public RectifierConf frontSegment(String code) {
        return frontSegment(context -> context.append(code));
    }

    public RectifierConf frontSegments(Collection<String> codes) {
        codes.forEach(this::frontSegment);
        return this;
    }

    public RectifierConf column(String type, String name, String expr) {
        return column(type, name, expr, null, null);
    }

    public RectifierConf column(String type, String name, String expr, String checkExpr) {
        return column(type, name, expr, checkExpr, null);
    }

    public RectifierConf column(String type, String name,
                                String expr, String checkExpr,
                                String comment) {
        Column column = new Column(type, name, expr, checkExpr, comment);
        return column(column);
    }

    public RectifierConf frontFilter(String expr) {
        return frontSegment(context -> ScriptBuilder.appendFilter(context, expr));
    }

    public RectifierConf frontFilters(Collection<String> exprs) {
        exprs.forEach(this::frontFilter);
        return this;
    }

    /**
     * Create a {@code Rectifier} by conf.
     *
     * @param <I> input type
     * @return Rectifier
     */
    public <I> Rectifier<I, Map<String, Object>> build() {
        return build(ResultModels.asMap(), null);
    }

    /**
     * Create a {@code DebugRectifier} by conf.
     * <p>
     * WARN: Poor performance, not for production environment.
     *
     * @param breakpointListener Wit breakpoint listener
     * @param <I>                input type
     * @return Rectifier
     */
    public <I> Rectifier<I, Map<String, Object>> build(BreakpointListener breakpointListener) {
        return build(ResultModels.asMap(), breakpointListener);
    }

    /**
     * Create a {@code Rectifier} by conf.
     *
     * @param resultModel ResultModel
     * @param <I>         input Type
     * @param <O>         out type
     * @return Rectifier
     */
    public <I, O> Rectifier<I, O> build(ResultModel<O> resultModel) {
        return build(resultModel, null);
    }

    /**
     * Create a {@code DebugRectifier} by conf.
     * <p>
     * WARN: Poor performance, not for production environment.
     *
     * @param resultModel        ResultModel
     * @param breakpointListener Wit breakpoint listener
     * @param <I>                input Type
     * @param <O>                out type
     */
    public <I, O> Rectifier<I, O> build(ResultModel<O> resultModel, BreakpointListener breakpointListener) {
        // init script
        val isDebugEnabled = breakpointListener != null;
        val schema = resolveSchema();

        final Template script;
        try {
            String code = "code: " + ScriptBuilder.build(this, isDebugEnabled);
            script = EngineLazyHolder.ENGINE.getTemplate(code);
            // fast-fail check
            script.reload();
        } catch (ResourceNotFoundException ex) {
            throw new UncheckedIOException("Failed to create script.", ex);
        }

        if (breakpointListener == null) {
            return new RectifierImpl<>(script, schema, resultModel);
        }
        return new RectifierDebugImpl<>(script, schema, resultModel, breakpointListener);
    }

    public <S, I> Rectifier<S, Map<String, Object>> build(SourceFormat<S, I> sourceFormat) {
        return build(sourceFormat, ResultModels.asMap(), null);
    }

    public <S, I, O> Rectifier<S, O> build(SourceFormat<S, I> sourceFormat, ResultModel<O> resultModel) {
        return build(sourceFormat, resultModel, null);
    }

    public <S, I, O> Rectifier<S, O> build(SourceFormat<S, I> sourceFormat, BreakpointListener breakpointListener) {
        return build(sourceFormat, null, breakpointListener);
    }

    public <S, I, O> Rectifier<S, O> build(SourceFormat<S, I> sourceFormat, ResultModel<O> resultModel, BreakpointListener breakpointListener) {
        Rectifier<I, O> inner = build(resultModel, breakpointListener);
        return inner.with(sourceFormat);
    }

    public <S, I> Rectifier<S, Map<String, Object>> build(Function<S, I> transfer) {
        return build(transfer, ResultModels.asMap(), null);
    }

    public <S, I, O> Rectifier<S, O> build(Function<S, I> transfer, ResultModel<O> resultModel) {
        return build(transfer, resultModel, null);
    }

    public <S, I, O> Rectifier<S, O> build(Function<S, I> transfer, BreakpointListener breakpointListener) {
        return build(transfer, null, breakpointListener);
    }

    public <S, I, O> Rectifier<S, O> build(Function<S, I> transfer, ResultModel<O> resultModel, BreakpointListener breakpointListener) {
        Rectifier<I, O> inner = build(resultModel, breakpointListener);
        return inner.with(transfer);
    }

    @Override
    public String toString() {
        return "RectifierConf{"
                + "name='" + name + '\''
                + '}';
    }

    @FunctionalInterface
    public interface EngineProvider extends Supplier<Engine>, Serializable {
    }

    @FunctionalInterface
    public interface Segment extends Serializable {
        void appendTo(ScriptBuilder.Context context);
    }

    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "create")
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
}
