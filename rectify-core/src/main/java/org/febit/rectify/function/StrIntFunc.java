package org.febit.rectify.function;

import org.febit.wit.InternalContext;
import org.febit.wit.lang.MethodDeclare;

import static org.febit.rectify.util.Args.int1;
import static org.febit.rectify.util.Args.string0;

@FunctionalInterface
public interface StrIntFunc extends MethodDeclare {

    Object invoke(String text, Integer i);

    @Override
    default Object invoke(InternalContext context, Object[] args) {
        return invoke(string0(args), int1(args));
    }
}
