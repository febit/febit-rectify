package org.febit.rectify.function;

import org.febit.wit.InternalContext;
import org.febit.wit.lang.MethodDeclare;

import static org.febit.rectify.util.Args.*;

@FunctionalInterface
public interface StrIntIntFunc extends MethodDeclare {

    Object invoke(String text, Integer i, Integer j);

    @Override
    default Object invoke(InternalContext context, Object[] args) {
        return invoke(string0(args), int1(args), int2(args));
    }
}
