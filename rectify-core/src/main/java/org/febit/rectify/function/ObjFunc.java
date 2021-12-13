package org.febit.rectify.function;

import org.febit.wit.InternalContext;
import org.febit.wit.lang.MethodDeclare;

import static org.febit.rectify.util.Args.arg0;

@FunctionalInterface
public interface ObjFunc extends MethodDeclare {

    Object invoke(Object obj);

    @Override
    default Object invoke(InternalContext context, Object[] args) {
        return invoke(arg0(args));
    }
}
