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

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.febit.lang.util.SingleElementConsumer;
import org.febit.rectify.RectifierSettings;
import org.febit.rectify.SourceFormat;
import org.jspecify.annotations.Nullable;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public record RowRectifierSchema(
        FlinkRectifier<byte[]> rectifier
) implements DeserializationSchema<Row> {

    @Serial
    private static final long serialVersionUID = 1L;

    public RowRectifierSchema {
        requireNonNull(rectifier, "rectifier must not be null");
    }

    public static RowRectifierSchema of(RectifierSettings settings, SourceFormat<byte[], Object> sourceFormat) {
        return of(FlinkRectifier.of(settings, sourceFormat));
    }

    public static RowRectifierSchema of(FlinkRectifier<byte[]> rectifier) {
        return new RowRectifierSchema(rectifier);
    }

    @Nullable
    @Override
    public Row deserialize(byte[] message) {
        var cell = new SingleElementConsumer<Row>();
        this.rectifier.process(message, cell);
        return cell.getValue();
    }

    @Override
    public void deserialize(byte[] message, Collector<Row> out) {
        this.rectifier.process(message, out);
    }

    @Override
    public RowTypeInfo getProducedType() {
        return this.rectifier.producedType();
    }

    @Override
    public boolean isEndOfStream(Row nextElement) {
        return false;
    }

}
