package org.febit.rectify.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.febit.rectify.function.IFunctions;
import org.febit.wit.exceptions.UncheckedException;
import org.febit.wit.util.ClassUtil;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@UtilityClass
public class FunctionUtils {

    public static void scanConstFields(BiConsumer<String, Object> consumer, Class<?> cls) {
        Stream.of(cls.getFields())
                .filter(ClassUtil::isStatic)
                .filter(ClassUtil::isFinal)
                .forEach(field -> scanConst(consumer, field));
    }

    public static void scanConst(BiConsumer<String, Object> consumer, Field field) {
        Object fieldValue;
        try {
            fieldValue = field.get(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new UncheckedException(e);
        }
        val originName = field.getName();
        val aliasAnno = field.getAnnotation(IFunctions.Alias.class);
        if (aliasAnno == null) {
            consumer.accept(originName, fieldValue);
            return;
        }
        if (aliasAnno.keepOriginName()) {
            consumer.accept(originName, fieldValue);
        }
        for (val alias : aliasAnno.value()) {
            consumer.accept(alias, fieldValue);
        }
    }
}
