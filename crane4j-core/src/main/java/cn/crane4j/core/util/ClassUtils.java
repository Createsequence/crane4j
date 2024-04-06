package cn.crane4j.core.util;

import cn.crane4j.core.exception.Crane4jException;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * <p>{@link Class} utils.
 *
 * @author huangchengxing
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ClassUtils {

    /**
     * primitive type and wrapper type mapping
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TYPE_TO_WRAPPER_TYPE = new HashMap<>(16);
    /**
     * wrapper type and primitive type mapping
     */
    private static final Map<Class<?>, Class<?>> WRAPPER_TYPE_TO_PRIMITIVE_TYPE = new HashMap<>(16);

    static {
        PRIMITIVE_TYPE_TO_WRAPPER_TYPE.put(Boolean.TYPE, Boolean.class);
        PRIMITIVE_TYPE_TO_WRAPPER_TYPE.put(Byte.TYPE, Byte.class);
        PRIMITIVE_TYPE_TO_WRAPPER_TYPE.put(Character.TYPE, Character.class);
        PRIMITIVE_TYPE_TO_WRAPPER_TYPE.put(Double.TYPE, Double.class);
        PRIMITIVE_TYPE_TO_WRAPPER_TYPE.put(Float.TYPE, Float.class);
        PRIMITIVE_TYPE_TO_WRAPPER_TYPE.put(Integer.TYPE, Integer.class);
        PRIMITIVE_TYPE_TO_WRAPPER_TYPE.put(Long.TYPE, Long.class);
        PRIMITIVE_TYPE_TO_WRAPPER_TYPE.put(Short.TYPE, Short.class);
        PRIMITIVE_TYPE_TO_WRAPPER_TYPE.put(Void.TYPE, Void.class);
        PRIMITIVE_TYPE_TO_WRAPPER_TYPE.forEach((key, value) -> WRAPPER_TYPE_TO_PRIMITIVE_TYPE.put(value, key));
    }

    /**
     * Whether the given class is primitive type or wrapper type.
     *
     * @param type type
     * @return boolean
     */
    public static boolean isPrimitiveTypeOrWrapperType(Class<?> type) {
        return PRIMITIVE_TYPE_TO_WRAPPER_TYPE.containsKey(type)
            || WRAPPER_TYPE_TO_PRIMITIVE_TYPE.containsKey(type);
    }

    /**
     * Whether the given class is {@code Object} or {@code Void}.
     *
     * @param clazz clazz
     * @return boolean
     */
    public static boolean isObjectOrVoid(Class<?> clazz) {
        return Objects.equals(Object.class, clazz)
            || Objects.equals(Void.TYPE, clazz);
    }

    /**
     * <p>Whether the given class is from packages
     * which package name is started with "java." or "javax.".
     *
     * @param clazz class
     * @return is jdk class
     */
    public static boolean isJdkClass(Class<?> clazz) {
        Objects.requireNonNull(clazz, "class name must not null");
        final Package objectPackage = clazz.getPackage();
        // unable to determine the package in which it is located, maybe is a proxy class？
        if (Objects.isNull(objectPackage)) {
            return false;
        }
        final String objectPackageName = objectPackage.getName();
        return objectPackageName.startsWith("java.")
            || objectPackageName.startsWith("javax.")
            || clazz.getClassLoader() == null;
    }

    /**
     * <p>Get class by class name.
     *
     * @param className class name
     * @return class instance
     * @throws Crane4jException if class not found
     */
    public static Class<?> forName(String className) throws Crane4jException {
        Objects.requireNonNull(className, "class name must not null");
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new Crane4jException(e);
        }
    }

    /**
     * <p>Get class by class name, if class not found, return default class.
     *
     * @param className class name, it may be null or empty
     * @param defaultClass default class
     * @return class instance or default class
     * @throws Crane4jException if class which specified by className not found
     */
    public static Class<?> forName(@Nullable String className, Class<?> defaultClass) {
        if (StringUtils.isNotEmpty(className)) {
            return forName(className);
        }
        return defaultClass;
    }

    /**
     * <p>Create new instance of given type.
     *
     * @param type type
     * @param <T> type
     * @return new instance
     * @throws Crane4jException if create instance failed
     */
    @SuppressWarnings({"unchecked", "java:S3011"})
    public static <T> T newInstance(@NonNull Class<?> type, Object... args) {
        Objects.requireNonNull(type, "type must not null");
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return (T) constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException
                 | InvocationTargetException | NoSuchMethodException e) {
            throw new Crane4jException(e);
        }
    }

    /**
     * <p>Whether the given class is instantiable.
     *
     * @param type type
     * @param fallback fallback when create instance failed, usually used for logging
     * @return true if instantiable, otherwise false
     */
    @SuppressWarnings("java:S3011")
    public static boolean isInstantiable(Class<?> type, @Nullable Consumer<Throwable> fallback) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            constructor.newInstance();
            return true;
        } catch (Exception e) {
            if (Objects.nonNull(fallback)) {
                fallback.accept(e);
            }
            return false;
        }
    }

    /**
     * <p>Convert package path to resource path.<br />
     * eg: {@code cn.crane4j.core.util.ClassUtils -> cn/crane4j/core/util/ClassUtils}
     *
     * @param packagePath class path
     * @return resource path
     */
    public static String packageToPath(String packagePath) {
        Objects.requireNonNull(packagePath, "packagePath must not null");
        return packagePath.replace(".", "/");
    }
}
