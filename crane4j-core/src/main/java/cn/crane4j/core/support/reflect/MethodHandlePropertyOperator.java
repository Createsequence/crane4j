package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.Try;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * An {@link PropertyOperator} implementation that uses {@link java.lang.invoke.MethodHandle} to access properties.
 *
 * @author huangchengxing
 * @see MethodHandles
 * @see MethodHandle
 * @see 2.2.0
 */
@NoArgsConstructor
@Slf4j
public class MethodHandlePropertyOperator extends ReflectivePropertyOperator {

    /**
     * Create a property operator.
     *
     * @param converterManager converter manager
     */
    public MethodHandlePropertyOperator(@Nullable ConverterManager converterManager) {
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
        return new MethodHandlePropDesc(targetType, converterManager, throwIfNoAnyMatched);
    }

    /**
     * The method handle based property descriptor.
     *
     * @author huangchengxing
     * @since 2.7.0
     */
    private static class MethodHandlePropDesc extends ReflectivePropDesc {

        public MethodHandlePropDesc(
            Class<?> beanType, @Nullable ConverterManager converterManager, boolean throwIfNoAnyMatched) {
            super(beanType, converterManager, throwIfNoAnyMatched);
        }

        /**
         * Creates a {@link MethodInvoker} for setting the value of the specified field.
         *
         * @param propertyName property name
         * @param field        field to be set.
         * @return The {@link MethodInvoker} instance for setting the value of the specified field.
         */
        @SneakyThrows
        @Override
        protected MethodInvoker createSetterInvokerForField(String propertyName, Field field) {
            if (Modifier.isStatic(field.getModifiers())) {
                return super.createSetterInvokerForField(propertyName, field);
            }
            return Try.<MethodInvoker>of(() -> {
                ReflectUtils.setAccessible(field);
                MethodHandle handle = MethodHandles.lookup().unreflectSetter(field);
                return new MethodHandleSetter(handle);
            }).getOrElseGet(e -> {
                log.warn("cannot find method handle of setter for field: {}", field, e);
                return super.createSetterInvokerForField(propertyName, field);
            });
        }

        /**
         * Creates a {@link MethodInvoker} for getting the value of the specified field.
         *
         * @param propertyName property name
         * @param field        field to be got.
         * @return The {@link MethodInvoker} instance for getting the value of the specified field.
         */
        @Override
        protected MethodInvoker createGetterInvokerForField(String propertyName, Field field) {
            if (Modifier.isStatic(field.getModifiers())) {
                return super.createGetterInvokerForField(propertyName, field);
            }
            return Try.<MethodInvoker>of(() -> {
                ReflectUtils.setAccessible(field);
                MethodHandle handle = MethodHandles.lookup().unreflectGetter(field);
                return new MethodHandleGetter(handle);
            }).getOrElseGet(e -> {
                log.debug("cannot find method handle of getter for field: {}", field, e);
                return super.createGetterInvokerForField(propertyName, field);
            });
        }

        /**
         * Create {@link MethodInvoker} according to the specified method.
         *
         * @param propertyName property name
         * @param method       getter method or setter method
         * @return {@link MethodInvoker}
         */
        @Override
        protected @Nullable MethodInvoker createInvokerForMethod(String propertyName, Method method) {
            return Try.of(() -> {
                ReflectUtils.setAccessible(method);
                MethodHandle methodHandle = MethodHandles.lookup().unreflect(method);
                return method.getParameterCount() > 0 ?
                    new MethodHandleSetter(methodHandle) : new MethodHandleGetter(methodHandle);
            }).getOrElseGet(e -> {
                log.debug("cannot find method handle of getter method: {}", method, e);
                return super.createInvokerForMethod(propertyName, method);
            });
        }
    }

    /**
     * Setter based on {@link MethodHandle}.
     *
     * @author huangchengxing
     * @since 2.2.0
     */
    @RequiredArgsConstructor
    public static class MethodHandleSetter implements MethodInvoker {

        /**
         * method handle.
         */
        private final MethodHandle methodHandle;

        /**
         * Invoke method.
         *
         * @param target target
         * @param args   args
         * @return result of invoke
         */
        @SneakyThrows
        @Override
        public Object invoke(Object target, Object... args) {
            return methodHandle.bindTo(target).invokeWithArguments(args);
        }
    }

    /**
     * Getter based on {@link MethodHandle}.
     *
     * @author huangchengxing
     * @since 2.2.0
     */
    @RequiredArgsConstructor
    public static class MethodHandleGetter implements MethodInvoker {

        /**
         * method handle.
         */
        private final MethodHandle methodHandle;

        /**
         * Invoke method.
         *
         * @param target target
         * @param args   args
         * @return result of invoke
         */
        @SneakyThrows
        @Override
        public Object invoke(Object target, Object... args) {
            return methodHandle.bindTo(target).invoke();
        }
    }
}
