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

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;
import org.febit.rectify.RectifierConf;
import org.febit.rectify.Schema;

/**
 * @param <I>
 * @author zqq90
 */
public abstract class BaseDeserializationSchema<I> implements DeserializationSchema<Row> {

    private static final long serialVersionUID = 1L;

    private final FlinkRectifier<I> rectifier;

    protected BaseDeserializationSchema(Class<I> sourceType, RectifierConf conf) {
        this(FlinkRectifier.create(sourceType, conf));
    }

    protected BaseDeserializationSchema(FlinkRectifier<I> rectifier) {
        this.rectifier = rectifier;
    }

    protected abstract I deserializeRaw(byte[] message);

    @Override
    public Row deserialize(byte[] message) {
        AssertSingleConsumer<Row> cell = new AssertSingleConsumer<>();
        this.rectifier.process(deserializeRaw(message), cell);
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

    public FlinkRectifier<I> getRectifier() {
        return rectifier;
    }

    public Schema getRectifierSchema() {
        return this.rectifier.getRectifierSchema();
    }
}
