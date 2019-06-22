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

import org.febit.rectify.SourceFormat;
import org.febit.rectify.SourceFormatProvider;
import org.febit.util.Priority;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author zqq90
 */
@Priority.Low
public class DefaultSourceFormatProvider implements SourceFormatProvider {

    private static final List<String> SUPPORTS = Collections.unmodifiableList(Arrays.asList(
            "direct",
            "json",
            "access"
    ));

    @Override
    public SourceFormat<?> lookup(String name) {
        SourceFormat format;
        switch (name) {
            case "direct":
                format = new DirectSourceFormat();
                break;
            case "json":
                format = new JsonSourceFormat();
                break;
            case "access":
                format = new AccessLogSourceFormat();
                break;
            default:
                return null;
        }
        return format;
    }

    @Override
    public List<String> supports() {
        return SUPPORTS;
    }
}
