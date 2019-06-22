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
package org.febit.rectify.flink;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.operators.StreamFlatMap;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.febit.lang.TerConsumer;
import org.febit.rectify.Rectifier;
import org.febit.rectify.RectifierConf;
import org.febit.rectify.ResultRaw;
import org.febit.rectify.Schema;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @param <I>
 * @author zqq90
 */
public class FlinkRectifier<I> implements Serializable {

    private static final long serialVersionUID = 1L;
    private final RectifierConf conf;
    private transient RowTypeInfo typeInfo;
    private transient TableSchema tableSchema;
    private transient Rectifier<I, Row> rectifier;
    private FlinkRectifier(RectifierConf conf) {
        Objects.requireNonNull(conf);
        this.conf = conf;
        init();
    }

    public static <I> FlinkRectifier<I> create(RectifierConf conf) {
        return new FlinkRectifier<>(conf);
    }

    public static <I> FlatMapOperator<I, Row> rectify(DataSet<I> dataSet, RectifierConf conf) {
        FlinkRectifier<I> rectifier = create(conf);
        return rectifier.rectify(dataSet);
    }

    public static <I> SingleOutputStreamOperator<Row> rectify(DataStream<I> dataStream, RectifierConf conf) {
        FlinkRectifier<I> rectifier = create(conf);
        return rectifier.rectify(dataStream);
    }

    private void init() {
        Rectifier<I, Row> processor = Rectifier.create(conf, RowResultModel.get());
        this.rectifier = processor;
        this.typeInfo = SchemaTypeInfoUtil.ofRecord(processor.schema());
        this.tableSchema = new TableSchema(
                this.typeInfo.getFieldNames(),
                this.typeInfo.getFieldTypes()
        );
    }

    protected void process(I raw, Collector<Row> out) {
        process(raw, out::collect);
    }

    protected void processRaw(I in, TerConsumer<Row, ResultRaw, String> out) {
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
            throw new RuntimeException("Not found field in schema '" + conf + "' : " + fieldName);
        }
        return index;
    }

    public List<String> getFieldNameList() {
        return Collections.unmodifiableList(Arrays.asList(this.typeInfo.getFieldNames()));
    }

    public TableSchema getTableSchema() {
        return this.tableSchema;
    }

    public Schema getRectifierSchema() {
        return this.rectifier.schema();
    }

    public String explainSource() {
        return toString();
    }

    public FlatMapOperator<I, Row> rectify(DataSet<I> dataSet) {
        FlatMapFunction<I, Row> flatMapper = this::process;
        return new FlatMapOperator<>(dataSet, getReturnType(),
                dataSet.clean(flatMapper), "FlinkRectifier");
    }

    public SingleOutputStreamOperator<Row> rectify(DataStream<I> dataStream) {
        FlatMapFunction<I, Row> flatMapper = this::process;
        return dataStream.transform("FlinkRectifier", getReturnType(),
                new StreamFlatMap<>(dataStream.getExecutionEnvironment().clean(flatMapper)));
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init();
    }

    @Override
    public String toString() {
        return "FlinkRectifier{"
                + "conf=" + conf
                + '}';
    }

}
