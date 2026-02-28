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

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.febit.lang.modeler.Modeler;
import org.febit.lang.modeler.Schema;
import org.febit.lang.modeler.Schemas;
import org.febit.lang.modeler.StructSpec;
import org.febit.lang.modeler.StructSpecs;
import org.febit.rectify.util.IndexedArray;
import org.febit.rectify.util.IndexedArrayAccessor;
import org.febit.rectify.wit.ScriptBuilder;
import org.febit.rectify.wit.SerializableBreakpointHandler;
import org.febit.wit.Script;
import org.febit.wit.Wit;
import org.febit.wit.exception.NoSuchSourceException;
import org.febit.wit.io.DiscardOut;
import org.febit.wit.loader.Loaders;
import org.febit.wit.loader.impl.StringLoader;
import org.febit.wit.runtime.Source;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Predicate;

;

/**
 * Rectifier config & factory, serializable.
 */
@Getter
@Accessors(fluent = true, chain = true)
@SuppressWarnings({"unused", "UnusedReturnValue"})
@lombok.Builder(
        builderClassName = "Builder",
        toBuilder = true
)
public class RectifierSettings implements Serializable {

    private final List<Column> columns;
    private final List<Setup> setups;

    @lombok.Builder.Default
    private final String name = "Unnamed";
    @lombok.Builder.Default
    private final WitFactory engineFactory = RectifierSettings::defaultWit;

    @Nullable
    private final SerializableBreakpointHandler breakpointHandler;

    private static Wit defaultWit() {
        return WitLazyHolder.WIT;
    }

    private static List<String> collectHints(Script script) {
        var hints = new ArrayList<String>();

        // vars
        hints.add(ScriptBuilder.VAR_INPUT);
        hints.add(ScriptBuilder.VAR_CURR_FIELD);

        // globals
        var staticHeaps = script.engine().staticHeaps();
        staticHeaps.constants().each((k, v) -> hints.add(k));
        staticHeaps.variables().each((k, v) -> hints.add(k));

        return List.copyOf(hints);
    }

    public Schema schema() {
        return schema(col -> true);
    }

    public Schema schema(Predicate<Column> filter) {
        var struct = Schemas.newStruct();
        this.columns.stream()
                .filter(filter)
                .forEach(col -> {
                    var colSchema = Schema.parse(name, col.name(), col.type());
                    struct.field(col.name(), colSchema, col.comment());
                });
        return struct.build();
    }

    /**
     * Create a {@code Rectifier} by settings.
     *
     * @param <I> input type
     * @return Rectifier
     */
    public <I> Rectifier<I, Map<String, Object>> create() {
        return create(StructSpecs.asMap());
    }

    /**
     * Create a {@code Rectifier} by settings.
     *
     * @param outputSpec output StructSpec
     * @param <I>        input Type
     * @param <O>        out type
     * @return Rectifier
     */
    public <I, O> Rectifier<I, O> create(StructSpec<O, ?> outputSpec) {
        // init script
        var schema = schema();
        var myBreakpointHandler = this.breakpointHandler;

        final Script script;
        try {
            var code = "code: " + ScriptBuilder.build(this, myBreakpointHandler != null);
            script = WitLazyHolder.WIT.script(code);
            // fast-fail check
            script.reload();
        } catch (NoSuchSourceException ex) {
            throw new UncheckedIOException("Failed to create script.", ex);
        }

        return new RectifierImpl<>(
                schema, Modeler.builder().structSpec(outputSpec).emptyIfAbsent().build(),
                () -> collectHints(script),
                vars -> script.eval(
                        vars,
                        new DiscardOut(),
                        myBreakpointHandler
                )
        );
    }

    public <S, I> Rectifier<S, Map<String, Object>> create(SourceFormat<S, I> sourceFormat) {
        return create(sourceFormat, StructSpecs.asMap());
    }

    public <S, I, O> Rectifier<S, O> create(SourceFormat<S, I> sourceFormat, StructSpec<O, ?> outputSpec) {
        Rectifier<I, O> inner = create(outputSpec);
        return inner.with(sourceFormat);
    }

    public <S, I> Rectifier<S, Map<String, Object>> create(Function<S, I> transfer) {
        return create(transfer, StructSpecs.asMap());
    }

    public <S, I, O> Rectifier<S, O> create(Function<S, I> transfer, StructSpec<O, ?> outputSpec) {
        Rectifier<I, O> inner = create(outputSpec);
        return inner.with(transfer);
    }

    @Override
    public String toString() {
        return "RectifierSettings{"
                + "name='" + name + '\''
                + '}';
    }

    public static class Builder {

        public Builder() {
            this.columns = new ArrayList<>();
            this.setups = new ArrayList<>();
        }

        @Tolerate
        public Builder setups(Setup... setups) {
            return setups(Arrays.asList(setups));
        }

        @Tolerate
        public Builder setup(Setup setup) {
            this.setups.add(setup);
            return this;
        }

        @Tolerate
        public Builder setup(String code) {
            return setup(context -> context.append(code));
        }

        @Tolerate
        public Builder filter(String expr) {
            return setup(context -> ScriptBuilder.appendFilter(context, expr));
        }

        @Tolerate
        public Builder filters(Collection<String> filters) {
            filters.forEach(this::filter);
            return this;
        }

        @Tolerate
        public Builder columns(Column... columns) {
            return columns(Arrays.asList(columns));
        }

        @Tolerate
        public Builder column(Column column) {
            this.columns.add(column);
            return this;
        }

        @Tolerate
        public Builder column(String type, String name, @Nullable String expression) {
            return column(type, name, expression, null, null);
        }

        @Tolerate
        public Builder column(
                String type,
                String name,
                @Nullable String expression,
                @Nullable String validation
        ) {
            return column(type, name, expression, validation, null);
        }

        @Tolerate
        @lombok.Builder(
                builderClassName = "ColumnBuilder",
                buildMethodName = "commit",
                builderMethodName = "column"
        )
        public Builder column(
                @lombok.NonNull
                @SuppressWarnings("NullableProblems")
                String type,
                @lombok.NonNull
                @SuppressWarnings("NullableProblems")
                String name,
                @Nullable String expression,
                @Nullable String validation,
                @Nullable String comment
        ) {
            return column(
                    new Column(type, name, expression, validation, comment)
            );
        }
    }

    private static class WitLazyHolder {
        static final Wit WIT;

        static {
            var builder = Wit.builder();
            builder.accessor(IndexedArray.class, new IndexedArrayAccessor());
            builder.loader(Loaders.dispatcher()
                    .rule("code:", StringLoader.builder()
                            .beginWith(Source.BeginWith.SCRIPT)
                            .build())
                    .fallback(Loaders.noop())
                    .build());

            ServiceLoader.load(RectifierWitModule.class)
                    .forEach(builder::module);

            ServiceLoader.load(RectifierWitCustomizer.class)
                    .forEach(customizer -> customizer.customize(builder));

            WIT = builder.build();
        }
    }

    @FunctionalInterface
    public interface WitFactory extends Serializable {
        Wit get();
    }

    @FunctionalInterface
    public interface Setup extends Serializable {
        void render(ScriptBuilder.Context context);
    }

    @lombok.Builder(builderClassName = "Builder")
    public record Column(
            @lombok.NonNull
            @SuppressWarnings("NullableProblems")
            String type,
            @lombok.NonNull
            @SuppressWarnings("NullableProblems")
            String name,

            @Nullable String expression,
            @Nullable String validation,
            @Nullable String comment
    ) implements Serializable {
    }
}
