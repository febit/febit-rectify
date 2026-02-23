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
package org.febit.rectify.flink.streaming;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.operators.StreamFlatMap;
import org.apache.flink.types.Row;
import org.febit.rectify.RectifierConf;
import org.febit.rectify.Rectifiers;
import org.febit.rectify.SerializableRectifier;
import org.febit.rectify.SourceFormat;
import org.febit.rectify.flink.FlinkRectifier;
import org.febit.rectify.flink.RowStructSpec;
import org.febit.rectify.flink.TypeInfoUtils;

public class FlinkStreamingRectifier<I> extends FlinkRectifier<I> {

    protected FlinkStreamingRectifier(SerializableRectifier<I, Row> rectifier, RowTypeInfo typeInfo) {
        super(rectifier, typeInfo);
    }

    public static <I> FlinkStreamingRectifier<I> create(
            SerializableRectifier<I, Row> rectifier,
            RowTypeInfo typeInfo
    ) {
        return new FlinkStreamingRectifier<>(rectifier, typeInfo);
    }

    public static <I> FlinkStreamingRectifier<I> create(RectifierConf conf) {
        return new FlinkStreamingRectifier<>(
                Rectifiers.lazy(() -> conf.build(RowStructSpec.get())),
                TypeInfoUtils.ofRowType(conf.resolveSchema())
        );
    }

    public static <I> FlinkStreamingRectifier<I> create(SourceFormat<I, Object> sourceFormat, RectifierConf conf) {
        return new FlinkStreamingRectifier<>(
                Rectifiers.lazy(() -> conf.build(sourceFormat, RowStructSpec.get())),
                TypeInfoUtils.ofRowType(conf.resolveSchema())
        );
    }

    public static <I> SingleOutputStreamOperator<Row> operator(DataStream<I> dataStream, RectifierConf conf) {
        var rectifier = FlinkStreamingRectifier.<I>create(conf);
        return rectifier.operator(dataStream);
    }

    public static <I> SingleOutputStreamOperator<Row> operator(
            DataStream<I> dataStream,
            SourceFormat<I, Object> sourceFormat,
            RectifierConf conf
    ) {
        var rectifier = create(sourceFormat, conf);
        return rectifier.operator(dataStream);
    }

    public SingleOutputStreamOperator<Row> operator(DataStream<I> dataStream) {
        var flatMapper = (FlatMapFunction<I, Row>) this::process;
        return dataStream.transform("FlinkRectifier", getReturnType(),
                new StreamFlatMap<>(dataStream.getExecutionEnvironment().clean(flatMapper)));
    }

}
