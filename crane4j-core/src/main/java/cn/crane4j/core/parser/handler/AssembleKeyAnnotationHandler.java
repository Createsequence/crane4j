package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.AssembleKey;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimplePropertyMapping;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * An {@link AbstractStandardOperationAnnotationHandler} implementation for {@link AssembleKey} annotation.
 *
 * @author huangchengxing
 * @see AssembleKey
 */
public class AssembleKeyAnnotationHandler extends InternalProviderAssembleAnnotationHandler<AssembleKey> {

    /**
     * Registered value mapper providers.
     */
    private final Map<String, ValueMapperProvider> valueMapperProviders = new HashMap<>();

    public AssembleKeyAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        super(AssembleKey.class, annotationFinder, Crane4jGlobalSorter.comparator(), configuration, propertyMappingStrategyManager);
        valueMapperProviders.put(AssembleKey.IDENTITY_HANDLER_MAPPER, k -> UnaryOperator.identity());
    }

    /**
     * Register provider provider.
     *
     * @param name provider provider name
     * @param provider provider provider
     */
    public void registerValueMapperProvider(String name, ValueMapperProvider provider) {
        valueMapperProviders.put(name, provider);
    }

    /**
     * Create container by given annotation and namespace.
     *
     * @param standardAnnotation standard annotation
     * @param namespace  namespace
     * @return {@link Container} instant
     */
    @NonNull
    @Override
    protected Container<Object> createContainer(
        StandardAssembleAnnotation<AssembleKey> standardAnnotation, String namespace) {
        AssembleKey annotation = standardAnnotation.getAnnotation();
        AnnotatedElement element = standardAnnotation.getAnnotatedElement();
        ValueMapperProvider provider = valueMapperProviders.get(annotation.mapper());
        Asserts.isNotNull(provider, "No value mapper found for [{}]", annotation.mapper());
        return new HandlerContainerAdapter(namespace, provider.get(element));
    }

    /**
     * Determine namespace by given annotation.
     *
     * @param standardAnnotation standard annotation
     * @return namespace
     */
    @Override
    protected String determineNamespace(StandardAssembleAnnotation<AssembleKey> standardAnnotation) {
        return standardAnnotation.getAnnotation().mapper();
    }

    /**
     * Get property mapping from given {@link StandardAssembleAnnotation}.
     *
     * @param standardAnnotation standard annotation
     * @param key                key
     * @return assemble operation groups
     */
    @Override
    protected Set<PropertyMapping> parsePropertyMappings(
        StandardAssembleAnnotation<AssembleKey> standardAnnotation, String key) {
        Set<PropertyMapping> propertyMappings = super.parsePropertyMappings(standardAnnotation, key);
        AssembleKey annotation = standardAnnotation.getAnnotation();
        if (StringUtils.isNotEmpty(annotation.ref())) {
            propertyMappings.add(new SimplePropertyMapping("", annotation.ref()));
        }
        return propertyMappings;
    }

    /**
     * Get {@link StandardAssembleAnnotation}.
     *
     * @param beanOperations bean operations
     * @param element        element
     * @param annotation     annotation
     * @return {@link StandardAssembleAnnotation} instance
     */
    @Override
    protected StandardAssembleAnnotation<AssembleKey> getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, AssembleKey annotation) {
        return StandardAssembleAnnotationAdapter.<AssembleKey>builder()
            .annotatedElement(element)
            .annotation(annotation)
            .id(annotation.id())
            .key(annotation.key())
            .sort(annotation.sort())
            .groups(annotation.groups())
            .mappingTemplates(annotation.propTemplates())
            .props(annotation.props())
            .prop(annotation.prop())
            .propertyMappingStrategy(annotation.propertyMappingStrategy())
            .build();
    }

    @RequiredArgsConstructor
    private static class HandlerContainerAdapter implements Container<Object> {
        @Getter
        private final String namespace;
        private final UnaryOperator<Object> handler;
        @Override
        public Map<Object, ?> get(Collection<Object> keys) {
            return keys.stream()
                .collect(Collectors.toMap(Function.identity(), handler));
        }
    }

    /**
     * Handler provider.
     */
    @FunctionalInterface
    public interface ValueMapperProvider {

        /**
         * Get provider
         *
         * @param element element
         * @return provider
         */
        @NonNull
        UnaryOperator<Object> get(AnnotatedElement element);
    }
}
