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
package org.febit.rectify.impls;

import org.febit.rectify.Input;
import org.febit.rectify.SourceFormat;
import org.febit.rectify.util.JacksonUtils;
import org.febit.util.StringUtil;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author zqq90
 */
public class JsonSourceFormat implements SourceFormat<String> {

    @Override
    public void process(String input, Consumer<Input> collector) {
        if (StringUtil.isEmpty(input)) {
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
        collector.accept(MapInput.of(values));
    }
}
