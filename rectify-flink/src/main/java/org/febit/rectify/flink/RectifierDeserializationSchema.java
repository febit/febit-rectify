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

import jakarta.annotation.Nullable;
import lombok.Getter;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;
import org.febit.lang.util.SingleElementConsumer;
import org.febit.rectify.RectifierConf;
import org.febit.rectify.Schema;
import org.febit.rectify.SourceFormat;

public class RectifierDeserializationSchema implements DeserializationSchema<Row> {

    private static final long serialVersionUID = 1L;

    @Getter
    private final FlinkRectifier<byte[]> rectifier;

    protected RectifierDeserializationSchema(FlinkRectifier<byte[]> rectifier) {
        this.rectifier = rectifier;
    }

    @Nullable
    @Override
    public Row deserialize(byte[] message) {
        var cell = new SingleElementConsumer<Row>();
        this.rectifier.process(message, cell);
        return cell.getValue();
    }

    @Override
    public boolean isEndOfStream(Row nextElement) {
        return false;
    }

    @Override
    public RowTypeInfo getProducedType() {
        return this.rectifier.getReturnType();
    }

    public Schema getRectifierSchema() {
        return this.rectifier.getRectifierSchema();
    }

    public static RectifierDeserializationSchema of(SourceFormat<byte[], Object> sourceFormat, RectifierConf conf) {
        return of(FlinkRectifier.create(sourceFormat, conf));
    }

    public static RectifierDeserializationSchema of(FlinkRectifier<byte[]> rectifier) {
        return new RectifierDeserializationSchema(rectifier);
    }
}
