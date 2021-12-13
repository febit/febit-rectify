package org.febit.rectify.function;

import org.febit.wit.InternalContext;
import org.febit.wit.lang.MethodDeclare;

@FunctionalInterface
public interface VoidFunc extends MethodDeclare {

    Object invoke();

    @Override
    default Object invoke(InternalContext context, Object[] args) {
        return invoke();
    }
}
