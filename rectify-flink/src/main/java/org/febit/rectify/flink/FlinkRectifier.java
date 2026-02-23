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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.febit.lang.modeler.Schema;
import org.febit.rectify.RectifierConf;
import org.febit.rectify.RectifierSink;
import org.febit.rectify.Rectifiers;
import org.febit.rectify.SerializableRectifier;
import org.febit.rectify.SourceFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @param <I>
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FlinkRectifier<I> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    private final SerializableRectifier<I, Row> rectifier;
    private final RowTypeInfo typeInfo;

    public static <I> FlinkRectifier<I> create(SerializableRectifier<I, Row> rectifier, RowTypeInfo typeInfo) {
        Objects.requireNonNull(rectifier, "rectifier");
        Objects.requireNonNull(typeInfo, "typeInfo");
        return new FlinkRectifier<>(rectifier, typeInfo);
    }

    public static <I> FlinkRectifier<I> create(RectifierConf conf) {
        return create(
                Rectifiers.lazy(() -> conf.build(RowStructSpec.get())),
                TypeInfoUtils.ofRowType(conf.resolveSchema())
        );
    }

    public static <I> FlinkRectifier<I> create(SourceFormat<I, Object> sourceFormat, RectifierConf conf) {
        return create(
                Rectifiers.lazy(() -> conf.build(sourceFormat, RowStructSpec.get())),
                TypeInfoUtils.ofRowType(conf.resolveSchema())
        );
    }

    protected void process(I raw, Collector<Row> out) {
        process(raw, out::collect);
    }

    protected void processRaw(I in, RectifierSink<Row> sink) {
        this.rectifier.process(in, sink);
    }

    public void process(I in, Consumer<Row> out) {
        processRaw(in, (record, raw, error) -> {
            if (record != null) {
                out.accept(record);
            }
        });
    }

    public RowTypeInfo getReturnType() {
        return this.typeInfo;
    }

    public int getFieldIndex(String fieldName) {
        return this.typeInfo.getFieldIndex(fieldName);
    }

    public int requireFieldIndex(String fieldName) {
        int index = this.typeInfo.getFieldIndex(fieldName);
        if (index < 0) {
            throw new NoSuchElementException(
                    "Not found field in schema '" + rectifier.schema().fullname() + "' : " + fieldName);
        }
        return index;
    }

    public List<String> getFieldNameList() {
        return Collections.unmodifiableList(Arrays.asList(this.typeInfo.getFieldNames()));
    }

    public Schema getRectifierSchema() {
        return this.rectifier.schema();
    }

    public String explainSource() {
        return toString();
    }

    @Override
    public String toString() {
        return "FlinkRectifier{"
                + "schema=" + rectifier.schema().fullname()
                + '}';
    }

}
