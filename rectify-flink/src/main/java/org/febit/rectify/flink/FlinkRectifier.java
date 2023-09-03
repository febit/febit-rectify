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

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.febit.rectify.LazyRectifier;
import org.febit.rectify.RectifierConf;
import org.febit.rectify.RectifierConsumer;
import org.febit.rectify.RectifierProvider;
import org.febit.rectify.Schema;
import org.febit.rectify.SourceFormat;

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
public class FlinkRectifier<I> implements Serializable {

    private static final long serialVersionUID = 2L;

    private final LazyRectifier<I, Row> rectifier;
    private final RowTypeInfo typeInfo;

    protected FlinkRectifier(RectifierProvider<I, Row> rectifierProvider, RowTypeInfo typeInfo) {
        Objects.requireNonNull(rectifierProvider);
        Objects.requireNonNull(typeInfo);
        this.rectifier = LazyRectifier.of(rectifierProvider);
        this.typeInfo = typeInfo;
    }

    public static <I> FlinkRectifier<I> create(RectifierConf conf) {
        return new FlinkRectifier<>(
                () -> conf.build(RowOutputModel.get()),
                TypeInfoUtils.ofRowType(conf.resolveSchema())
        );
    }

    public static <I> FlinkRectifier<I> create(SourceFormat<I, Object> sourceFormat, RectifierConf conf) {
        return new FlinkRectifier<>(
                () -> conf.build(sourceFormat, RowOutputModel.get()),
                TypeInfoUtils.ofRowType(conf.resolveSchema())
        );
    }

    public static <I> FlatMapOperator<I, Row> operator(DataSet<I> dataSet, RectifierConf conf) {
        FlinkRectifier<I> rectifier = create(conf);
        return rectifier.operator(dataSet);
    }

    public static <I> FlatMapOperator<I, Row> operator(DataSet<I> dataSet, SourceFormat<I, Object> sourceFormat, RectifierConf conf) {
        FlinkRectifier<I> rectifier = create(sourceFormat, conf);
        return rectifier.operator(dataSet);
    }

    protected void process(I raw, Collector<Row> out) {
        process(raw, out::collect);
    }

    protected void processRaw(I in, RectifierConsumer<Row> out) {
        this.rectifier.process(in, out);
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
            throw new NoSuchElementException("Not found field in schema '" + rectifier.schema().fullname() + "' : " + fieldName);
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

    public FlatMapOperator<I, Row> operator(DataSet<I> dataSet) {
        var flatMapper = (FlatMapFunction<I, Row>) this::process;
        return new FlatMapOperator<>(dataSet, getReturnType(),
                dataSet.clean(flatMapper), "FlinkRectifier");
    }

    @Override
    public String toString() {
        return "FlinkRectifier{"
                + "schema=" + rectifier.schema().fullname()
                + '}';
    }

}
