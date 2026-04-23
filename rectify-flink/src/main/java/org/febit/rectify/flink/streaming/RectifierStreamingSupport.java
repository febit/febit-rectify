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

import lombok.experimental.UtilityClass;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.operators.StreamFlatMap;
import org.apache.flink.types.Row;
import org.febit.rectify.RectifierSettings;
import org.febit.rectify.SourceFormat;
import org.febit.rectify.flink.FlinkRectifier;

@UtilityClass
public class RectifierStreamingSupport {

    private static final String OP_FLATMAP = "FebitRectifierFlatMap";

    public static <I> SingleOutputStreamOperator<Row> flatMap(
            DataStream<I> stream, FlinkRectifier<I> rectifier) {
        var env = stream.getExecutionEnvironment();
        var operator = new StreamFlatMap<I, Row>(
                env.clean(rectifier::process)
        );
        return stream.transform(OP_FLATMAP, rectifier.producedType(), operator);
    }

    public static <I> SingleOutputStreamOperator<Row> flatMap(
            DataStream<I> stream,
            RectifierSettings settings,
            SourceFormat<I, Object> format
    ) {
        var rectifier = FlinkRectifier.of(settings, format);
        return flatMap(stream, rectifier);
    }
}
