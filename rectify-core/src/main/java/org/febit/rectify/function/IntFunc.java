package org.febit.rectify.function;

import org.febit.wit.InternalContext;
import org.febit.wit.lang.MethodDeclare;

import static org.febit.rectify.util.Args.int0;

@FunctionalInterface
public interface IntFunc extends MethodDeclare {

    Object invoke(Integer i);

    @Override
    default Object invoke(InternalContext context, Object[] args) {
        return invoke(int0(args));
    }
}
