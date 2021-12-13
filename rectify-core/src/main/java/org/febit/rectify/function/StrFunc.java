package org.febit.rectify.function;

import org.febit.wit.InternalContext;
import org.febit.wit.lang.MethodDeclare;

import static org.febit.rectify.util.Args.string0;

@FunctionalInterface
public interface StrFunc extends MethodDeclare {

    Object invoke(String text);

    @Override
    default Object invoke(InternalContext context, Object[] args) {
        return invoke(string0(args));
    }
}
