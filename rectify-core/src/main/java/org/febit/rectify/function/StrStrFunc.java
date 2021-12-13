package org.febit.rectify.function;

import org.febit.wit.InternalContext;
import org.febit.wit.lang.MethodDeclare;

import static org.febit.rectify.util.Args.string0;
import static org.febit.rectify.util.Args.string1;

@FunctionalInterface
public interface StrStrFunc extends MethodDeclare {

    Object invoke(String a, String b);

    @Override
    default Object invoke(InternalContext context, Object[] args) {
        return invoke(string0(args), string1(args));
    }
}
