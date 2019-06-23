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
package org.febit.rectify;

import jodd.bean.BeanUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.febit.util.CollectionUtil;
import org.febit.util.Priority;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * @author zqq90
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SourceFormats {

    public static List<String> supports() {
        return ProviderHolder.ALL_SUPPORTS;
    }

    public static <T> SourceFormat<T> create(RectifierConf schema, Class<T> sourceType) {
        return create(schema.sourceFormat(), sourceType, schema.sourceFormatProps());
    }

    public static <T> SourceFormat<T> create(String name, Class<T> sourceType, Map<String, String> props) {
        SourceFormat<T> format = lookup(name, sourceType);
        if (format == null) {
            throw new IllegalArgumentException("Not found SourceFormat named '" + name + "' for type: " + sourceType);
        }
        injectConfigs(format, props);
        format.init();
        return format;
    }

    private static <T> SourceFormat<T> lookup(String name, Class<T> sourceType) {
        if (name == null) {
            return null;
        }
        for (val provider : ProviderHolder.PROVIDERS) {
            SourceFormat<T> format = provider.lookup(name, sourceType);
            if (format != null) {
                return format;
            }
        }
        return null;
    }

    private static void injectConfigs(SourceFormat sourceFormat, Map<String, String> props) {
        if (sourceFormat == null || props == null) {
            return;
        }
        BeanUtil beanUtil = BeanUtil.declaredForced;
        props.forEach((k, v) -> beanUtil.setProperty(sourceFormat, k, v));
    }

    private static class ProviderHolder {
        static final SourceFormatProvider[] PROVIDERS;
        static final List<String> ALL_SUPPORTS;

        static {
            val list = CollectionUtil.read(ServiceLoader.load(SourceFormatProvider.class));

            PROVIDERS = list.stream()
                    .sorted(Priority.DESC)
                    .toArray(SourceFormatProvider[]::new);

            ALL_SUPPORTS = list.stream()
                    .flatMap(p -> p.supports().stream())
                    .distinct()
                    .sorted()
                    .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        }
    }

}
