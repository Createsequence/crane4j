package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.util.CollectionUtils;
import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link PropertyOperator} implementation based on {@link com.esotericsoftware.reflectasm}.
 *
 * @author huangchengxing
 * @author tangcent
 */
@NoArgsConstructor
public class AsmReflectivePropertyOperator extends ReflectivePropertyOperator {

    /**
     * Create an {@link AsmReflectivePropertyOperator} instance
     *
     * @param converterManager converter register
     */
    public AsmReflectivePropertyOperator(@Nullable ConverterManager converterManager) {
        super(converterManager);
    }

    /**
     * Get property descriptor.
     *
     * @param targetType target type
     * @return property descriptor
     * @since 2.7.0
     */
    @Override
    public @NonNull PropDesc getPropertyDescriptor(Class<?> targetType) {
        return new AsmReflectivePropDesc(targetType, converterManager, throwIfNoAnyMatched);
    }

    /**
     * {@link PropDesc} implementation based on {@link com.esotericsoftware.reflectasm}.
     *
     * @author huangchengxing
     * @since 2.7.0
     */
    protected static class AsmReflectivePropDesc extends ReflectivePropDesc {

        private final Map<Class<?>, MethodAccess> methodAccessCaches = new ConcurrentHashMap<>();
        private final Map<Class<?>, FieldAccess> fieldAccessCaches = new ConcurrentHashMap<>();

        public AsmReflectivePropDesc(
            Class<?> beanType, @Nullable ConverterManager converterManager, boolean throwIfNoAnyMatched) {
            super(beanType, converterManager, throwIfNoAnyMatched);
        }

        @Override
        protected MethodInvoker createSetterInvokerForField(String propertyName, Field field) {
            if (Modifier.isPrivate(field.getModifiers())) {
                return super.createSetterInvokerForField(propertyName, field);
            }
            FieldAccess access = CollectionUtils.computeIfAbsent(fieldAccessCaches, beanType, FieldAccess::get);
            try {
                int fieldIndex = access.getIndex(field.getName());
                return new ReflectAsmFieldInvoker.Setter(access, fieldIndex);
            } catch (IllegalArgumentException e) {
                return super.createSetterInvokerForField(propertyName, field);
            }
        }

        @Override
        protected MethodInvoker createGetterInvokerForField(String propertyName, Field field) {
            if (Modifier.isPrivate(field.getModifiers())) {
                return super.createGetterInvokerForField(propertyName, field);
            }
            FieldAccess access = CollectionUtils.computeIfAbsent(fieldAccessCaches, beanType, FieldAccess::get);
            try {
                int fieldIndex = access.getIndex(field.getName());
                return new ReflectAsmFieldInvoker.Getter(access, fieldIndex);
            } catch (IllegalArgumentException e) {
                return super.createGetterInvokerForField(propertyName, field);
            }
        }

        /**
         * Create {@link MethodInvoker} according to the specified method
         *
         * @param propertyName property name
         * @param method getter method or setter method
         * @return {@link MethodInvoker}
         */
        @Override
        protected MethodInvoker createInvokerForMethod(String propertyName, Method method) {
            MethodAccess access = CollectionUtils.computeIfAbsent(methodAccessCaches, beanType, MethodAccess::get);
            int methodIndex = access.getIndex(method.getName(), method.getParameterTypes());
            return new ReflectAsmMethodInvoker(methodIndex, access);
        }
    }

    /**
     * {@link MethodInvoker} implementation based on {@link MethodAccess}
     *
     * @author huangchengxing
     * @since 2.0.0
     */
    @RequiredArgsConstructor
    public static class ReflectAsmMethodInvoker implements MethodInvoker {

        private final int methodIndex;
        private final MethodAccess methodAccess;

        /**
         * Invoke method.
         *
         * @param target target
         * @param args args
         * @return result of invoke
         */
        @Override
        public Object invoke(@Nullable Object target, @Nullable Object... args) {
            return methodAccess.invoke(target, methodIndex, args);
        }
    }

    /**
     * {@link MethodInvoker} implementation based on {@link FieldAccess}
     *
     * @author tangcent
     * @since 2.0.0
     */
    public abstract static class ReflectAsmFieldInvoker implements MethodInvoker {

        private final FieldAccess fieldAccess;
        private final int fieldIndex;

        protected ReflectAsmFieldInvoker(FieldAccess fieldAccess, int fieldIndex) {
            this.fieldAccess = fieldAccess;
            this.fieldIndex = fieldIndex;
        }

        @Override
        public Object invoke(@Nullable Object target, @Nullable Object... args) {
            return invoke(fieldAccess, fieldIndex, target, args);
        }

        /**
         * Invoke the field access.
         *
         * @param fieldAccess field access
         * @param fieldIndex field index
         * @param target target
         * @param args args
         * @return result of invoke
         */
        protected abstract Object invoke(FieldAccess fieldAccess, int fieldIndex, @Nullable Object target, @Nullable Object... args);

        /**
         * An implementation of the {@link ReflectAsmFieldInvoker} for getter.
         *
         * @author tangcent
         * @since 2.0.0
         */
        public static class Getter extends ReflectAsmFieldInvoker {
            public Getter(FieldAccess fieldAccess, int fieldIndex) {
                super(fieldAccess, fieldIndex);
            }
            @Override
            protected Object invoke(FieldAccess fieldAccess, int fieldIndex, @Nullable Object target, @Nullable Object... args) {
                return fieldAccess.get(target, fieldIndex);
            }
        }

        /**
         * An implementation of the {@link ReflectAsmFieldInvoker} for setter.
         *
         * @author tangcent
         * @since 2.0.0
         */
        public static class Setter extends ReflectAsmFieldInvoker {
            public Setter(FieldAccess fieldAccess, int fieldIndex) {
                super(fieldAccess, fieldIndex);
            }
            @Override
            protected Object invoke(FieldAccess fieldAccess, int fieldIndex, @Nullable Object target, @Nullable Object... args) {
                fieldAccess.set(target, fieldIndex, args[0]);
                return null;
            }
        }
    }

}
