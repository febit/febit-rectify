package org.febit.rectify.function;

import org.febit.rectify.EnginePlugin;
import org.febit.wit.Engine;
import org.febit.wit.util.JavaNativeUtil;

public interface IFunctions extends EnginePlugin {

    @Override
    default void apply(Engine engine) {
        JavaNativeUtil.addConstFields(engine.getGlobalManager(), engine.getNativeFactory(), getClass());
        JavaNativeUtil.addStaticMethods(engine.getGlobalManager(), engine.getNativeFactory(), getClass());
    }
}
