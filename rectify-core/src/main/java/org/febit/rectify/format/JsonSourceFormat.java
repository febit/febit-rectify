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
package org.febit.rectify.format;

import org.apache.commons.lang3.StringUtils;
import org.febit.rectify.SourceFormat;
import org.febit.rectify.util.JacksonUtils;

import java.util.Map;
import java.util.function.Consumer;

public class JsonSourceFormat implements SourceFormat<String, Object> {

    @Override
    public void process(String input, Consumer<Object> collector) {
        if (StringUtils.isEmpty(input)) {
            return;
        }
        Map<String, Object> values;
        try {
            values = JacksonUtils.parseAsNamedMap(input);
        } catch (Exception e) {
            //XXX: log this bad case
            return;
        }
        if (values == null || values.isEmpty()) {
            return;
        }
        collector.accept(values);
    }
}