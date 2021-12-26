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
package org.febit.rectify;

import org.febit.wit.Engine;
import org.febit.wit.loggers.Logger;
import org.febit.wit.plugin.EnginePlugin;

/**
 * Plugin interface to extend engine for Rectifier.
 */
@FunctionalInterface
public interface RectifierEnginePlugin extends EnginePlugin {

    default void initAndApply(Engine engine, Logger logger) {
        String name = getClass().getName();
        logger.info("Applying rectify engine spi plugin: {}.", name);
        engine.inject(name, this);
        apply(engine);
    }
}

