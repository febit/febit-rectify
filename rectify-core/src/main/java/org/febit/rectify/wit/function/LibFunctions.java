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
package org.febit.rectify.wit.function;

import lombok.experimental.UtilityClass;
import org.febit.lang.Unchecked;
import org.febit.lang.func.Consumer0;
import org.febit.lang.func.Consumer1;
import org.febit.lang.func.Consumer2;
import org.febit.lang.func.Consumer3;
import org.febit.lang.func.Consumer4;
import org.febit.lang.func.Consumer5;
import org.febit.lang.func.Function0;
import org.febit.lang.func.Function1;
import org.febit.lang.func.Function2;
import org.febit.lang.func.Function3;
import org.febit.lang.func.Function4;
import org.febit.lang.func.Function5;
import org.febit.lang.func.IFunction;
import org.febit.lang.func.ThrowingCallable;
import org.febit.lang.func.ThrowingConsumer0;
import org.febit.lang.func.ThrowingConsumer1;
import org.febit.lang.func.ThrowingConsumer2;
import org.febit.lang.func.ThrowingConsumer3;
import org.febit.lang.func.ThrowingConsumer4;
import org.febit.lang.func.ThrowingConsumer5;
import org.febit.lang.func.ThrowingFunction0;
import org.febit.lang.func.ThrowingFunction1;
import org.febit.lang.func.ThrowingFunction2;
import org.febit.lang.func.ThrowingFunction3;
import org.febit.lang.func.ThrowingFunction4;
import org.febit.lang.func.ThrowingFunction5;
import org.febit.lang.func.ThrowingRunnable;
import org.febit.lang.func.ThrowingSupplier;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.type.TypeFactory;
import tools.jackson.databind.util.SimpleLookupCache;

import java.lang.reflect.Type;

@UtilityClass
public class LibFunctions {

    private static final TypeFactory TYPE_FACTORY = TypeFactory.createDefaultInstance()
            .withCache(new SimpleLookupCache<>(16, 128));

    @SuppressWarnings({
            "java:S3776", // Cognitive Complexity of methods should not be too high
            "rawtypes",
            "unchecked"
    })
    public static LibFunction wrap(IFunction target, Type targetGenericType) {
        var javaType = TYPE_FACTORY.constructType(targetGenericType);
        return switch (target) {
            case Consumer0 f -> args -> {
                f.accept();
                return null;
            };
            case Function0 f -> args -> f.apply();
            case ThrowingConsumer0 f -> {
                var unchecked = Unchecked.consumer0(f);
                yield args -> {
                    unchecked.accept();
                    return null;
                };
            }
            case ThrowingFunction0 f -> {
                var unchecked = Unchecked.func0(f);
                yield args -> unchecked.apply();
            }
            case ThrowingCallable f -> {
                var unchecked = Unchecked.callable(f);
                yield args -> unchecked.call();
            }
            case ThrowingSupplier f -> {
                var unchecked = Unchecked.supplier(f);
                yield args -> unchecked.get();
            }
            case ThrowingRunnable f -> {
                var unchecked = Unchecked.runnable(f);
                yield args -> {
                    unchecked.run();
                    return null;
                };
            }
            case Consumer1 f -> adapt(f, javaType);
            case Consumer2 f -> adapt(f, javaType);
            case Consumer3 f -> adapt(f, javaType);
            case Consumer4 f -> adapt(f, javaType);
            case Consumer5 f -> adapt(f, javaType);
            case Function1 f -> adapt(f, javaType);
            case Function2 f -> adapt(f, javaType);
            case Function3 f -> adapt(f, javaType);
            case Function4 f -> adapt(f, javaType);
            case Function5 f -> adapt(f, javaType);
            case ThrowingConsumer1 f -> adapt(f, javaType);
            case ThrowingConsumer2 f -> adapt(f, javaType);
            case ThrowingConsumer3 f -> adapt(f, javaType);
            case ThrowingConsumer4 f -> adapt(f, javaType);
            case ThrowingConsumer5 f -> adapt(f, javaType);
            case ThrowingFunction1 f -> adapt(f, javaType);
            case ThrowingFunction2 f -> adapt(f, javaType);
            case ThrowingFunction3 f -> adapt(f, javaType);
            case ThrowingFunction4 f -> adapt(f, javaType);
            case ThrowingFunction5 f -> adapt(f, javaType);
            case null, default -> throw new IllegalArgumentException(
                    "Unsupported function: " + target.getClass());
        };
    }

    private static AdapterFunction adapt(
            ThrowingConsumer1<@Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(ThrowingConsumer1.class),
                0, 1
        );
        var unchecked = Unchecked.consumer1(func);
        return AdapterFunction.create(paramTypes, args -> {
            unchecked.accept(args[0]);
            return null;
        });
    }

    private static AdapterFunction adapt(
            ThrowingConsumer2<@Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(ThrowingConsumer2.class),
                0, 2
        );
        var unchecked = Unchecked.consumer2(func);
        return AdapterFunction.create(paramTypes, args -> {
            unchecked.accept(args[0], args[1]);
            return null;
        });
    }

    private static AdapterFunction adapt(
            ThrowingConsumer3<@Nullable Object, @Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(ThrowingConsumer3.class),
                0, 3
        );
        var unchecked = Unchecked.consumer3(func);
        return AdapterFunction.create(paramTypes, args -> {
            unchecked.accept(args[0], args[1], args[2]);
            return null;
        });
    }

    private static AdapterFunction adapt(
            ThrowingConsumer4<@Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(ThrowingConsumer4.class),
                0, 4
        );
        var unchecked = Unchecked.consumer4(func);
        return AdapterFunction.create(paramTypes, args -> {
            unchecked.accept(args[0], args[1], args[2], args[3]);
            return null;
        });
    }

    private static AdapterFunction adapt(
            ThrowingConsumer5<@Nullable Object, @Nullable Object, @Nullable Object,
                    @Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(ThrowingConsumer5.class),
                0, 5
        );
        var unchecked = Unchecked.consumer5(func);
        return AdapterFunction.create(paramTypes, args -> {
            unchecked.accept(args[0], args[1], args[2], args[3], args[4]);
            return null;
        });
    }

    private static AdapterFunction adapt(
            ThrowingFunction1<@Nullable Object, ?, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(ThrowingFunction1.class),
                0, 1
        );
        var unchecked = Unchecked.func1(func);
        return AdapterFunction.create(paramTypes, args -> unchecked.apply(args[0]));
    }

    private static AdapterFunction adapt(
            ThrowingFunction2<@Nullable Object, @Nullable Object, ?, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(ThrowingFunction2.class),
                0, 2
        );
        var unchecked = Unchecked.func2(func);
        return AdapterFunction.create(paramTypes, args -> unchecked.apply(args[0], args[1]));
    }

    private static AdapterFunction adapt(
            ThrowingFunction3<@Nullable Object, @Nullable Object, @Nullable Object, ?, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(ThrowingFunction3.class),
                0, 3
        );
        var unchecked = Unchecked.func3(func);
        return AdapterFunction.create(paramTypes, args -> unchecked.apply(args[0], args[1], args[2]));
    }

    private static AdapterFunction adapt(
            ThrowingFunction4<@Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object, ?, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(ThrowingFunction4.class),
                0, 4
        );
        var unchecked = Unchecked.func4(func);
        return AdapterFunction.create(paramTypes,
                args -> unchecked.apply(args[0], args[1], args[2], args[3]));
    }

    private static AdapterFunction adapt(
            ThrowingFunction5<@Nullable Object, @Nullable Object, @Nullable Object,
                    @Nullable Object, @Nullable Object, ?, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(ThrowingFunction5.class),
                0, 5
        );
        var unchecked = Unchecked.func5(func);
        return AdapterFunction.create(paramTypes,
                args -> unchecked.apply(args[0], args[1], args[2], args[3], args[4]));
    }

    private static AdapterFunction adapt(
            Function1<@Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(Function1.class),
                0, 1
        );
        return AdapterFunction.create(paramTypes, args -> func.apply(args[0]));
    }

    private static AdapterFunction adapt(
            Function2<@Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(Function2.class),
                0, 2
        );
        return AdapterFunction.create(paramTypes,
                args -> func.apply(args[0], args[1]));
    }

    private static AdapterFunction adapt(
            Function3<@Nullable Object, @Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(Function3.class),
                0, 3
        );
        return AdapterFunction.create(paramTypes,
                args -> func.apply(args[0], args[1], args[2]));
    }

    private static AdapterFunction adapt(
            Function4<@Nullable Object, @Nullable Object,
                    @Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(Function4.class),
                0, 4
        );
        return AdapterFunction.create(paramTypes, args ->
                func.apply(args[0], args[1], args[2], args[3]));
    }

    private static AdapterFunction adapt(
            Function5<@Nullable Object, @Nullable Object, @Nullable Object,
                    @Nullable Object, @Nullable Object, ?> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(Function5.class),
                0, 5
        );
        return AdapterFunction.create(paramTypes,
                args -> func.apply(args[0], args[1], args[2], args[3], args[4]));
    }

    private static AdapterFunction adapt(Consumer1<@Nullable Object> func, JavaType javaType) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(Consumer1.class),
                0, 1
        );
        return AdapterFunction.create(paramTypes, args -> {
            func.accept(args[0]);
            return null;
        });
    }

    private static AdapterFunction adapt(
            Consumer2<@Nullable Object, @Nullable Object> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(Consumer2.class),
                0, 2
        );
        return AdapterFunction.create(paramTypes, args -> {
            func.accept(args[0], args[1]);
            return null;
        });
    }

    private static AdapterFunction adapt(
            Consumer3<@Nullable Object, @Nullable Object, @Nullable Object> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(Consumer3.class),
                0, 3
        );
        return AdapterFunction.create(paramTypes, args -> {
            func.accept(args[0], args[1], args[2]);
            return null;
        });
    }

    private static AdapterFunction adapt(
            Consumer4<@Nullable Object, @Nullable Object, @Nullable Object, @Nullable Object> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(Consumer4.class),
                0, 4
        );
        return AdapterFunction.create(paramTypes, args -> {
            func.accept(args[0], args[1], args[2], args[3]);
            return null;
        });
    }

    private static AdapterFunction adapt(
            Consumer5<@Nullable Object, @Nullable Object, @Nullable Object,
                    @Nullable Object, @Nullable Object> func,
            JavaType javaType
    ) {
        var paramTypes = ParameterConverters.resolve(
                javaType.findTypeParameters(Consumer5.class),
                0, 5
        );
        return AdapterFunction.create(paramTypes, args -> {
            func.accept(args[0], args[1], args[2], args[3], args[4]);
            return null;
        });
    }
}
