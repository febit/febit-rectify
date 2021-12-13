package org.febit.rectify.function;

import org.febit.wit.InternalContext;
import org.febit.wit.lang.MethodDeclare;

import static org.febit.rectify.util.Args.arg0;
import static org.febit.rectify.util.Args.arg1;

@FunctionalInterface
public interface ObjObjFunc extends MethodDeclare {

    Object invoke(Object a, Object b);

    @Override
    default Object invoke(InternalContext context, Object[] args) {
        return invoke(arg0(args), arg1(args));
    }
}
