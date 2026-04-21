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
package org.febit.rectify.lib.extra;

import org.febit.lang.func.Function1;
import org.febit.rectify.lib.BindingAlias;
import org.febit.rectify.lib.Library;
import org.febit.rectify.lib.Namespace;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;

import static org.febit.lang.util.JacksonUtils.json;
import static org.febit.lang.util.JacksonUtils.prettyJson;

@SuppressWarnings({
        "unused",
        "java:S1118", // Utility classes should not have public constructors
})
public class JsonLibrary implements Library {

    /**
     * Namespace: JSON.
     */
    @BindingAlias(value = {"JSON"}, keepDeclaredName = false)
    public static final JsonNamespace JSON = new JsonNamespace();

    public static class JsonNamespace implements Namespace {

        public final Function1<@Nullable Object, String> stringify = json()::stringify;
        public final Function1<@Nullable Object, String> prettyStringify = prettyJson()::stringify;

        public final Function1<@Nullable Object, @Nullable Object> toMap = json()::toMap;
        public final Function1<@Nullable Object, @Nullable Object> toList = json()::toList;

        public final Function1<@Nullable String, @Nullable Object> parse = text -> parseIfPresent(text, JacksonSupport.T_OBJECT);
        public final Function1<@Nullable String, @Nullable Object> parseAsMap = text -> parseIfPresent(text, JacksonSupport.T_MAP);
        public final Function1<@Nullable String, @Nullable Object> parseAsList = text -> parseIfPresent(text, JacksonSupport.T_LIST);

        @Nullable
        private static Object parseIfPresent(@Nullable String text, JavaType type) {
            if (text == null || text.isEmpty()) {
                return null;
            }
            return json().parse(text, type);
        }
    }

}
