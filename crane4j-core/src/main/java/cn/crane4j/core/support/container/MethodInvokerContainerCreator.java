package cn.crane4j.core.support.container;

import cn.crane4j.annotation.DuplicateStrategy;
import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.container.MethodInvokerContainer;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.converter.ParameterConvertibleMethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectiveMethodInvoker;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Support class for {@link MethodInvokerContainer} creation.
 *
 * @author huangchengxing
 * @see ParameterConvertibleMethodInvoker
 * @see ReflectiveMethodInvoker
 * @since 1.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class MethodInvokerContainerCreator {

    protected final PropertyOperator propertyOperator;
    protected final ConverterManager converterManager;

    /**
     * Create a {@link MethodInvokerContainer} from the given method.
     *
     * @param target method's calling object, if the method is static, it can be null
     * @param method method
     * @param mappingType mapping type
     * @param namespace namespace, if null, use method name as namespace
     * @param resultType result type
     * @param resultKey result key
     * @param duplicateStrategy duplicate strategy
     * @return {@link MethodInvokerContainer}
     */
    public MethodInvokerContainer createContainer(
        @Nullable Object target, Method method, MappingType mappingType,
        @Nullable String namespace, Class<?> resultType, String resultKey, DuplicateStrategy duplicateStrategy) {
        log.debug("create method container from [{}]", method);
        MethodInvokerContainerCreation containerCreation = MethodInvokerContainerCreation.builder()
            .target(target)
            .method(method)
            .methodInvoker(getMethodInvoker(target, method))
            .mappingType(mappingType)
            .namespace(namespace)
            .resultType(resultType)
            .resultKey(resultKey)
            .duplicateStrategy(duplicateStrategy)
            .build();
        return doCreateContainer(containerCreation);
    }

    /**
     * Create a {@link MethodInvokerContainer} from the given method.
     *
     * @param target method's calling object, if the method is static, it can be null
     * @param methodInvoker method invoker
     * @param mappingType mapping type
     * @param namespace namespace, if null, use method name as namespace
     * @param resultType result type
     * @param resultKey result key
     * @param duplicateStrategy duplicate strategy
     * @return {@link MethodInvokerContainer}
     */
    public MethodInvokerContainer createContainer(
        @Nullable Object target, MethodInvoker methodInvoker, MappingType mappingType,
        @Nullable String namespace, Class<?> resultType, String resultKey, DuplicateStrategy duplicateStrategy) {
        MethodInvokerContainerCreation containerCreation = MethodInvokerContainerCreation.builder()
            .target(target)
            .methodInvoker(methodInvoker)
            .mappingType(mappingType)
            .namespace(namespace)
            .resultType(resultType)
            .resultKey(resultKey)
            .duplicateStrategy(duplicateStrategy)
            .build();
        return doCreateContainer(containerCreation);
    }

    private MethodInvokerContainer doCreateContainer(MethodInvokerContainerCreation containerCreation) {
        Method method = containerCreation.getMethod();
        String namespace = getNamespace(method, containerCreation.getNamespace());
        MappingType mappingType = containerCreation.getMappingType();
        MethodInvoker methodInvoker = containerCreation.getMethodInvoker();
        Object target = containerCreation.getTarget();

        MethodInvokerContainer container;
        if (mappingType == MappingType.NO_MAPPING) {
            container = doCreateNoMappingContainer(target, methodInvoker, method, namespace);
        } else if (mappingType == MappingType.ORDER_OF_KEYS) {
            Asserts.isNotNull(method, "method must not be null when mapping type is [{}]", mappingType);
            // fix https://gitee.com/opengoofy/crane4j/issues/I97R7E
            container = isSingleParameterMethod(method) ?
                doCreateSingleKeyContainer(target, methodInvoker, namespace) :
                doCreateOrderOfKeysContainer(target, methodInvoker, method, namespace);
        } else if (mappingType == MappingType.ONE_TO_ONE) {
            container = doCreateOneToOneContainer(
                target, methodInvoker, method, namespace,
                containerCreation.getResultType(), containerCreation.getResultKey(),
                containerCreation.getDuplicateStrategy()
            );
        } else if (mappingType == MappingType.ONE_TO_MANY) {
            container = doCreateOneToManyContainer(
                target, methodInvoker, method, namespace,
                containerCreation.getResultType(), containerCreation.getResultKey(),
                containerCreation.getDuplicateStrategy()
            );
        } else {
            throw new Crane4jException("unsupported mapping type: " + mappingType);
        }

        if (Objects.isNull(method)) {
            log.debug("create method invoker container [{}], mapping type is [{}]", container.getNamespace(), mappingType);
        } else {
            log.debug("create method invoker container [{}] for method [{}], mapping type is [{}]", container.getNamespace(), method, mappingType);
        }
        return container;
    }

    private static boolean isSingleParameterMethod(@NonNull Method method) {
        return method.getParameterCount() == 1
            && !Collection.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

    protected MethodInvokerContainer doCreateSingleKeyContainer(
        @Nullable Object target, MethodInvoker methodInvoker, String namespace) {
        return MethodInvokerContainer.singleKey(namespace, methodInvoker, target);
    }

    protected MethodInvokerContainer doCreateNoMappingContainer(
        @Nullable Object target, MethodInvoker methodInvoker, @Nullable Method method, String namespace) {
        if (Objects.nonNull(method)) {
            Asserts.isTrue(
                Map.class.isAssignableFrom(method.getReturnType()),
                "method [{}] must return a map type when mapping type is [{}]", method, MappingType.NO_MAPPING
            );
        }
        return MethodInvokerContainer.create(namespace, methodInvoker, target, true);
    }

    @SuppressWarnings("unused")
    protected MethodInvokerContainer doCreateOrderOfKeysContainer(
        @Nullable Object target, MethodInvoker methodInvoker, @Nullable Method method, String namespace) {
        return MethodInvokerContainer.create(namespace, methodInvoker, target, false);
    }

    @SuppressWarnings("unused")
    protected MethodInvokerContainer doCreateOneToOneContainer(
        @Nullable Object target, MethodInvoker methodInvoker, @Nullable Method method, String namespace,
        Class<?> resultType, String resultKey, DuplicateStrategy duplicateStrategy) {
        MethodInvokerContainer.KeyExtractor keyExtractor = getKeyExtractor(resultType, resultKey);
        return MethodInvokerContainer.oneToOne(namespace, methodInvoker, target, keyExtractor, duplicateStrategy);
    }

    @SuppressWarnings("unused")
    protected MethodInvokerContainer doCreateOneToManyContainer(
        @Nullable Object target, MethodInvoker methodInvoker, @Nullable Method method, String namespace,
        Class<?> resultType, String resultKey, DuplicateStrategy duplicateStrategy) {
        MethodInvokerContainer.KeyExtractor keyExtractor = getKeyExtractor(resultType, resultKey);
        return MethodInvokerContainer.oneToMany(namespace, methodInvoker, target, keyExtractor);
    }

    /**
     * Get the namespace of method container.
     *
     * @param target target, if the method is static, it can be null
     * @param method method
     * @return namespace
     * @implNote if target is <b>proxy object</b>, invoke method on proxy object,
     * otherwise invoke method on target object
     */
    @NonNull
    protected MethodInvoker getMethodInvoker(Object target, Method method) {
        MethodInvoker invoker = ReflectiveMethodInvoker.create(target, method, false);
        return ParameterConvertibleMethodInvoker.create(invoker, converterManager, method.getParameterTypes());
    }

    /**
     * Get key extractor of result object if necessary.
     *
     * @param resultType  result type
     * @param resultKey   result key
     * @return key extractor
     */
    protected MethodInvokerContainer.@Nullable KeyExtractor getKeyExtractor(
        Class<?> resultType, String resultKey) {
        MethodInvokerContainer.KeyExtractor keyExtractor;
        // fix https://gitee.com/opengoofy/crane4j/issues/I8UZH4
        // if the result type is a primitive type(or wrapper type), the key extractor is not required
        if (canExtractKey(resultType, resultKey)) {
            MethodInvoker keyGetter = findKeyGetter(resultType, resultKey);
            keyExtractor = keyGetter::invoke;
            return keyExtractor;
        }
        throw new Crane4jException(
            "Cannot resolve key extractor from key [{}] on result type [{}],"
                + "the result type must be a java bean or map collection, "
                + "and the key must be a name of valid property.",
            resultKey, resultType
        );
    }

    private static boolean canExtractKey(Class<?> resultType, String resultKey) {
        return !ClassUtils.isPrimitiveTypeOrWrapperType(resultType)
            && !Objects.equals(String.class, resultType)
            && StringUtils.isNotEmpty(resultKey);
    }

    /**
     * Get the namespace from method.
     *
     * @param method     method
     * @param namespace  namespace
     * @return namespace
     */
    @NonNull
    protected static String getNamespace(@Nullable Method method, String namespace) {
        if (StringUtils.isEmpty(namespace)) {
            Objects.requireNonNull(method, "method must not be null");
            return method.getName();
        }
        return namespace;
    }

    /**
     * Find key getter method of result object.
     *
     * @param resultType result type
     * @param resultKey  result key
     * @return key getter method
     */
    protected MethodInvoker findKeyGetter(Class<?> resultType, String resultKey) {
        MethodInvoker keyGetter = propertyOperator.findGetter(resultType, resultKey);
        Asserts.isNotNull(keyGetter, "cannot find getter method [{}] on [{}]", resultKey, resultType);
        return keyGetter;
    }

    @Getter
    @Builder
    private static class MethodInvokerContainerCreation {
        @Nullable
        private final Object target;
        @Nullable
        private final Method method;
        private final MethodInvoker methodInvoker;
        private final MappingType mappingType;
        @Nullable
        private final String namespace;
        private final Class<?> resultType;
        private final String resultKey;
        private final DuplicateStrategy duplicateStrategy;
    }
}
