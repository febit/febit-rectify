package org.febit.rectify.function;

import org.febit.wit.InternalContext;
import org.febit.wit.lang.MethodDeclare;

import static org.febit.rectify.util.Args.int0;
import static org.febit.rectify.util.Args.int1;

@FunctionalInterface
public interface IntIntFunc extends MethodDeclare {

    Object invoke(Integer i, Integer j);

    @Override
    default Object invoke(InternalContext context, Object[] args) {
        return invoke(int0(args), int1(args));
    }
}
