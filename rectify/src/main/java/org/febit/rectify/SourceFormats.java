/**
 * Copyright 2018-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.rectify;

import jodd.bean.BeanUtil;
import org.febit.util.CollectionUtil;
import org.febit.util.Priority;
import org.febit.util.Props;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * @author zqq90
 */
public class SourceFormats {

    private static class ProviderHolder {
        static final SourceFormatProvider[] PROVIDERS;
        static final List<String> ALL_SUPPORTS;

        static {
            List<SourceFormatProvider> list = CollectionUtil.read(ServiceLoader.load(SourceFormatProvider.class));

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

    public static List<String> supports() {
        return ProviderHolder.ALL_SUPPORTS;
    }

    public static <T> SourceFormat<T> create(RectifierConf schema) {
        return create(schema.sourceFormat(), schema.sourceFormatProps());
    }

    public static <T> SourceFormat<T> create(String name, String props) {
        @SuppressWarnings("unchecked")
        SourceFormat<T> format = (SourceFormat<T>) lookup(name);
        if (format == null) {
            throw new RuntimeException("Not found SourceFormat named: " + name);
        }
        injectConfigs(format, props);
        format.init();
        return format;
    }

    private static SourceFormat<?> lookup(String name) {
        if (name == null) {
            return null;
        }
        for (SourceFormatProvider sourceFormatProvider : ProviderHolder.PROVIDERS) {
            SourceFormat<?> format = sourceFormatProvider.lookup(name);
            if (format != null) {
                return format;
            }
        }
        return null;
    }

    private static void injectConfigs(SourceFormat sourceFormat, String props) {
        injectConfigs(sourceFormat, Props.shadowLoader()
                .loadString(props).get());
    }

    private static void injectConfigs(SourceFormat sourceFormat, Props props) {
        if (sourceFormat == null) {
            return;
        }
        BeanUtil beanUtil = BeanUtil.declaredForced;
        props.forEach((k, v) -> beanUtil.setProperty(sourceFormat, k, v));
    }

}
