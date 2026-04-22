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

import org.febit.rectify.SourceFormat;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public record BytesSourceFormatWrapper(
        SourceFormat<String, Object> delegate
) implements SourceFormat<byte[], Object> {

    @Override
    public void process(byte @Nullable [] input, Consumer<Object> sink) {
        var text = input == null
                ? null
                : new String(input, StandardCharsets.UTF_8);
        delegate.process(text, sink);
    }
}
