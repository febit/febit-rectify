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

import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.febit.rectify.format.JsonSourceFormat;
import org.junit.jupiter.api.Test;

class FlinkStreamingRectifierTest {

    @Test
    void processDataStream() throws Exception {
        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        var rawStream = env.fromData(FlinkRectifierTest.source, BasicTypeInfo.STRING_TYPE_INFO);
        var rowStream = FlinkStreamingRectifier.operator(
                rawStream, new JsonSourceFormat(), FlinkRectifierTest.conf);
        rowStream.print();
        env.execute();
    }
}
