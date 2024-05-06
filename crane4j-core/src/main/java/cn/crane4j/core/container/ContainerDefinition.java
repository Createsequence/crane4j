package cn.crane4j.core.container;

import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Supplier;

/**
 * Definition of container.
 *
 * @author huangchengxing
 * @see ContainerLifecycleProcessor
 * @since 1.3.0
 */
public interface  ContainerDefinition {

    /**
     * Create a simple container definition.
     *
     * @param namespace namespace
     * @param providerName provider name
     * @param containerFactory container factory
     * @return container definition
     */
    static SimpleContainerDefinition create(
            String namespace, @Nullable String providerName, @Nullable Supplier<Container<Object>> containerFactory) {
        return new SimpleContainerDefinition(namespace, providerName, containerFactory);
    }

    /**
     * Get the namespace of container
     *
     * @return namespace
     */
    String getNamespace();

    /**
     * Get container provider name.
     *
     * @return provider name
     */
    String getProviderName();

    /**
     * Get container factory.
     *
     * @return container factory method
     */
    Supplier<Container<Object>> getContainerFactory();

    /**
     * Set container factory.
     *
     * @param factory factory
     */
    void setContainerFactory(Supplier<Container<Object>> factory);

    /**
     * Whether the container is {@link LimitedContainer}.
     *
     * @return true if the container is {@link LimitedContainer}, otherwise false
     * @since 2.3.0
     */
    boolean isLimited();

    /**
     * Set whether the container is {@link LimitedContainer}.
     *
     * @param limited true if the container is {@link LimitedContainer}, otherwise false
     * @since 2.3.0
     */
    void setLimited(boolean limited);

    /**
     * Create {@link Container} by given {@code factory}.
     *
     * @return {@link Container} instance.
     */
    @SuppressWarnings("unchecked")
    default <K> Container<K> createContainer() {
        return (Container<K>) getContainerFactory().get();
    }

    /**
     * Container definition.
     *
     * @author huangchengxing
     * @see ContainerLifecycleProcessor
     */
    @Getter
    class SimpleContainerDefinition implements ContainerDefinition {
    
        /**
         * Namespace of container.
         */
        private final String namespace;
    
        /**
         * Name of container provider. If not create by provider, it will be {@code null}.
         */
        @Nullable
        private final String providerName;
    
        /**
         * Container factory.
         */
        @Setter
        @Nullable
        private Supplier<Container<Object>> containerFactory;

        /**
         * Whether the container is {@link LimitedContainer}.
         */
        @Setter
        private boolean limited = false;

        /**
         * Create an instance.
         *
         * @param namespace namespace
         * @param providerName provider name
         * @param containerFactory container factory
         */
        SimpleContainerDefinition(
                String namespace, @Nullable String providerName, @Nullable Supplier<Container<Object>> containerFactory) {
            this.namespace = namespace;
            this.providerName = providerName;
            this.containerFactory = containerFactory;
        }
    }
}
