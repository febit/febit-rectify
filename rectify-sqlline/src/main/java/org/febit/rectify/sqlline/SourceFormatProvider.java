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
package org.febit.rectify.sqlline;

import org.febit.rectify.SourceFormat;
import org.febit.rectify.format.AccessLogSourceFormat;
import org.febit.rectify.format.JsonSourceFormat;

import java.util.Collection;
import java.util.Map;

public class SourceFormatProvider {

    public static SourceFormat<String, Object> create(String name, Map<String, Object> props) {
        switch (name) {
            case "json":
                return new JsonSourceFormat();
            case "access": {
                @SuppressWarnings({"unchecked"})
                var columns = (Collection<String>) props.get("columns");
                return AccessLogSourceFormat.create(columns);
            }
            default:
                throw new IllegalArgumentException("Unsupported format: " + name);
        }
    }

}
