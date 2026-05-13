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

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import lombok.experimental.UtilityClass;
import org.febit.lang.func.Function0;
import org.febit.lang.func.Function1;
import org.febit.rectify.lib.BindingAlias;
import org.febit.rectify.lib.Library;
import org.febit.rectify.lib.Namespace;

import java.util.UUID;

@SuppressWarnings({
        "unused",
        "java:S1118", // Utility classes should not have public constructors
})
public class UuidLibrary implements Library {

    public static final UuidNamespace UUID = new UuidNamespace();

    public static class UuidNamespace implements Namespace {

        @BindingAlias(value = {"v1"})
        public final Function0<UUID> timeBased = UuidLibrary::timeBased;
        @BindingAlias(value = {"v4"})
        public final Function0<UUID> random = UuidLibrary::random;
        @BindingAlias(value = {"v7"})
        public final Function0<UUID> timeBasedEpochRandom = UuidLibrary::timeBasedEpochRandom;

        public final Function1<String, UUID> fromString = java.util.UUID::fromString;
    }

    /**
     * Generate time-based UUID (version 1).
     */
    private static UUID timeBased() {
        return TimeBasedHolder.GENERATOR.generate();
    }

    /**
     * Generate random-based UUID (version 4).
     */
    private static UUID random() {
        return RandomHolder.GENERATOR.generate();
    }

    /**
     * Generate time-based epoch random UUID (version 7).
     */
    private static UUID timeBasedEpochRandom() {
        return TimeBasedEpochRandomHolder.GENERATOR.generate();
    }

    @UtilityClass
    private static class TimeBasedHolder {
        private static final NoArgGenerator GENERATOR = Generators.timeBasedGenerator();
    }

    @UtilityClass
    private static class RandomHolder {
        private static final NoArgGenerator GENERATOR = Generators.randomBasedGenerator();
    }

    @UtilityClass
    private static class TimeBasedEpochRandomHolder {
        private static final NoArgGenerator GENERATOR = Generators.timeBasedEpochRandomGenerator();
    }

}
