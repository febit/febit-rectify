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
package org.febit.rectify.flink;

import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.febit.lang.modeler.Schema;
import org.febit.rectify.RectifierSettings;
import org.febit.rectify.RectifierSink;
import org.febit.rectify.Rectifiers;
import org.febit.rectify.SerializableRectifier;
import org.febit.rectify.SourceFormat;
import org.febit.rectify.flink.support.RowStructSpec;
import org.febit.rectify.flink.support.TypeUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A wrapper around a Rectifier that produces Flink Rows.
 *
 * @param <I> the type of the input to the rectifier
 */
public record FlinkRectifier<I>(
        SerializableRectifier<I, Row> rectifier,
        RowTypeInfo producedType
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public FlinkRectifier {
        requireNonNull(rectifier, "rectifier must not be null");
        requireNonNull(producedType, "producedType must not be null");
    }

    public static <I> FlinkRectifier<I> of(
            SerializableRectifier<I, Row> rectifier,
            RowTypeInfo producedType
    ) {
        return new FlinkRectifier<>(rectifier, producedType);
    }

    public static <I> FlinkRectifier<I> of(RectifierSettings settings) {
        return of(
                Rectifiers.lazy(() -> settings.create(RowStructSpec.get())),
                TypeUtils.toRowType(settings.schema())
        );
    }

    public static <I> FlinkRectifier<I> of(RectifierSettings settings, SourceFormat<I, Object> sourceFormat) {
        return of(
                Rectifiers.lazy(() -> settings.create(sourceFormat, RowStructSpec.get())),
                TypeUtils.toRowType(settings.schema())
        );
    }

    private void process0(I in, RectifierSink<Row> sink) {
        this.rectifier.process(in, sink);
    }

    public void process(I raw, Collector<Row> out) {
        process(raw, out::collect);
    }

    public void process(I in, Consumer<Row> out) {
        process0(in, (row, raw, error) -> {
            if (row != null) {
                out.accept(row);
            }
        });
    }

    public Schema producedSchema() {
        return this.rectifier.schema();
    }

    @Override
    public String toString() {
        return "FlinkRectifier{"
                + "schema=" + producedSchema().fullname()
                + '}';
    }

}
