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
package org.febit.rectify.engine;

import org.febit.rectify.EnginePlugin;
import org.febit.wit.Engine;
import org.febit.wit.Init;
import org.febit.wit.loggers.Logger;

import java.util.ServiceLoader;

public class EnginePluginCollector {

    @Init
    public void init(Engine engine, Logger logger) {
        for (EnginePlugin plugin : ServiceLoader.load(EnginePlugin.class)) {
            String name = plugin.getClass().getName();
            logger.info("Applying rectify engine spi plugin: {}.", name);
            engine.inject(name, plugin);
            plugin.apply(engine);
        }
    }
}
