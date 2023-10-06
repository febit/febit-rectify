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
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.febit.lang.modeler.Modeler;
import org.febit.lang.modeler.Schema;
import org.febit.lang.modeler.Schemas;
import org.febit.lang.modeler.StructSpec;
import org.febit.lang.modeler.StructSpecs;
import org.febit.rectify.engine.ScriptBuilder;
import org.febit.wit.Context;
import org.febit.wit.Engine;
import org.febit.wit.Template;
import org.febit.wit.Vars;
import org.febit.wit.debug.BreakpointListener;
import org.febit.wit.exceptions.ResourceNotFoundException;

import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Rectifier config & factory, serializable.
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

    @Nullable
    private BreakpointListener breakpointListener;

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
        var builder = Schemas.newStruct();
        this.columns.stream()
                .filter(filter)
                .forEach(col -> {
                    var colSchema = Schema.parse(name, col.name(), col.type());
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

    public RectifierConf breakpointListener(@Nullable BreakpointListener listener) {
        setBreakpointListener(listener);
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

    @SuppressWarnings("UnusedReturnValue")
    public RectifierConf frontSegments(Collection<String> codes) {
        codes.forEach(this::frontSegment);
        return this;
    }

    public RectifierConf column(String type, String name, String expr) {
        return column(type, name, expr, null, null);
    }

    public RectifierConf column(String type, String name, String expr, @Nullable String checkExpr) {
        return column(type, name, expr, checkExpr, null);
    }

    public RectifierConf column(
            String type, String name,
            String expr,
            @Nullable String checkExpr,
            @Nullable String comment
    ) {
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
        return build(StructSpecs.asMap());
    }

    /**
     * Create a {@code Rectifier} by conf.
     *
     * @param outputSpec  output StructSpec
     * @param <I>         input Type
     * @param <O>         out type
     * @return Rectifier
     */
    public <I, O> Rectifier<I, O> build(StructSpec<O, ?> outputSpec) {
        // init script
        var breakpointListener = this.breakpointListener;
        var isDebugEnabled = breakpointListener != null;
        var schema = resolveSchema();

        final Template script;
        try {
            var code = "code: " + ScriptBuilder.build(this, isDebugEnabled);
            script = EngineLazyHolder.ENGINE.getTemplate(code);
            // fast-fail check
            script.reload();
        } catch (ResourceNotFoundException ex) {
            throw new UncheckedIOException("Failed to create script.", ex);
        }

        Function<Vars, Context> func = isDebugEnabled
                ? vars -> script.debug(vars, breakpointListener)
                : script::merge;

        return new RectifierImpl<>(
                schema, Modeler.builder().structSpec(outputSpec).emptyIfAbsent().build(),
                () -> collectHints(script),
                func
        );
    }

    private static List<String> collectHints(Template script) {
        var hints = new ArrayList<String>();

        // vars
        hints.add(ScriptBuilder.VAR_INPUT);
        hints.add(ScriptBuilder.VAR_CURR_FIELD);

        // globals
        var gm = script.getEngine().getGlobalManager();
        gm.forEachGlobal((k, v) -> hints.add(k));
        gm.forEachConst((k, v) -> hints.add(k));

        return List.copyOf(hints);
    }


    public <S, I> Rectifier<S, Map<String, Object>> build(SourceFormat<S, I> sourceFormat) {
        return build(sourceFormat, StructSpecs.asMap());
    }

    public <S, I, O> Rectifier<S, O> build(SourceFormat<S, I> sourceFormat, StructSpec<O, ?> outputSpec) {
        Rectifier<I, O> inner = build(outputSpec);
        return inner.with(sourceFormat);
    }

    public <S, I> Rectifier<S, Map<String, Object>> build(Function<S, I> transfer) {
        return build(transfer, StructSpecs.asMap());
    }

    public <S, I, O> Rectifier<S, O> build(Function<S, I> transfer, StructSpec<O, ?> outputSpec) {
        Rectifier<I, O> inner = build(outputSpec);
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

        @Nullable
        private final String checkExpr;

        @Nullable
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

        @Nullable
        public String checkExpr() {
            return checkExpr;
        }

        @Nullable
        public String comment() {
            return comment;
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
        }
    }
}
