package org.febit.rectify.function;

import org.febit.wit.InternalContext;
import org.febit.wit.lang.MethodDeclare;

import static org.febit.rectify.util.Args.*;

@FunctionalInterface
public interface StrStrStrFunc extends MethodDeclare {

    Object invoke(String a, String b, String c);

    @Override
    default Object invoke(InternalContext context, Object[] args) {
        return invoke(string0(args), string1(args), string2(args));
    }
}
