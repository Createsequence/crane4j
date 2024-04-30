package cn.crane4j.core.container;

import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.crane4j.core.util.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * A base implementation of {@link ContainerManager}.
 *
 * @author huangchengxing
 */
@Slf4j
public class DefaultContainerManager implements ContainerManager {

    /**
     * Container singleton caches.
     */
    protected final ConcurrentMap<Object, Object> containerMap = new ConcurrentHashMap<>(64);

    /**
     * Registered container lifecycle callbacks.
     */
    protected final List<ContainerLifecycleProcessor> containerLifecycleProcessorList = new ArrayList<>();

    /**
     * Registered container provider.
     */
    protected final Map<String, ContainerProvider> containerProviderMap = new LinkedHashMap<>();

    /**
     * Whether allow container overriding.
     *
     * @since 2.8.0
     */
    @Setter
    private boolean allowContainerOverriding = false;

    // =============== lifecycle lifecycle  ===============

    /**
     * Register {@link ContainerLifecycleProcessor}.
     *
     * @param lifecycle lifecycle
     * @return whether the registration is successful
     */
    @Override
    public boolean registerContainerLifecycleProcessor(ContainerLifecycleProcessor lifecycle) {
        if (containerLifecycleProcessorList.contains(lifecycle)) {
            return false;
        }
        return containerLifecycleProcessorList.add(lifecycle);
    }

    /**
     * Get all registered {@link ContainerLifecycleProcessor}.
     *
     * @return {@link ContainerLifecycleProcessor}
     */
    @Override
    public Collection<ContainerLifecycleProcessor> getContainerLifecycleProcessors() {
        return containerLifecycleProcessorList;
    }

    // =============== container provider  ===============

    /**
     * Register {@link ContainerProvider} by given name.
     *
     * @param name              name
     * @param containerProvider containerProvider
     */
    @Override
    public void registerContainerProvider(String name, ContainerProvider containerProvider) {
        containerProviderMap.put(name, containerProvider);
    }

    /**
     * Get {@link ContainerProvider} by given name.
     *
     * @param name name
     * @return {@link ContainerProvider} instance
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends ContainerProvider> T getContainerProvider(String name) {
        return (T)containerProviderMap.get(name);
    }

    // =============== container  ===============

    /**
     * Register container
     *
     * @param definition definition of container
     * @return old definition
     * @see ContainerLifecycleProcessor#whenRegistered
     */
    @Nullable
    @Override
    public ContainerDefinition registerContainer(ContainerDefinition definition) {
        Asserts.isNotNull(definition, "definition must not null");
        String namespace = definition.getNamespace();
        if (StringUtils.isEmpty(namespace)) {
            // TODO add assertion to check namespace is not empty GtiHub#261
            log.warn("container namespace is empty, it will be ignored");
        }
        if (StringUtils.isNotEmpty(namespace) && containerMap.containsKey(namespace)) {
            Asserts.isTrue(allowContainerOverriding, "The container [{}] has been registered, and the container overriding is not allowed.", namespace);
            log.warn("The container [{}] already exists, it will be overridden", namespace);
        }
        Object key = getCacheKey(namespace);
        containerMap.compute(key, (k, t) -> {
            // process new definition
            ContainerDefinition newDefinition = ConfigurationUtil.triggerWhenRegistered(
                    definition, key.toString(), t, containerLifecycleProcessorList, log
            );
            // no change if the new definition is null
            if (Objects.isNull(newDefinition)) {
                return t;
            }
            // remove old instance or definition
            if (Objects.nonNull(t)) {
                ConfigurationUtil.triggerWhenDestroyed(t, containerLifecycleProcessorList);
            }
            // register new definition
            return definition;
        });
        return definition;
    }

    /**
     * Obtaining and caching container instances from provider or definition.
     *
     * @param namespace namespace of container, which can also be the cache name for the container comparator.
     * @return container comparator
     * @see ContainerLifecycleProcessor#whenCreated
     */
    @Nullable
    @Override
    public <K> Container<K> getContainer(String namespace) {
        if (Objects.equals(namespace, Container.EMPTY_CONTAINER_NAMESPACE)) {
            return Container.empty();
        }
        // check if the container is created
        Object key = getCacheKey(namespace);
        return doGetContainer(key);
    }

    /**
     * Get all limited containers.
     *
     * @return limited containers
     */
    @Override
    public Collection<Container<Object>> getAllLimitedContainers() {
        return containerMap.entrySet().stream()
            .filter(e -> (e.getValue() instanceof ContainerDefinition) ?
                ((ContainerDefinition) e.getValue()).isLimited() : e.getValue() instanceof LimitedContainer
            )
            .map(e -> doGetContainer(e.getKey()))
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <K> Container<K> doGetContainer(Object key) {
        Object container = containerMap.get(key);
        if (Objects.nonNull(container) && container instanceof Container) {
            return (Container<K>)container;
        }

        // create container
        return (Container<K>)containerMap.compute(key, (k, t) -> {
            // check again
            boolean isDirectRegistered = Objects.nonNull(t);
            // fix https://github.com/opengoofy/crane4j/issues/162
            if (isDirectRegistered && t instanceof Container) {
                return t;
            }
            // can't find it. maybe in the provider?
            ContainerDefinition definition = isDirectRegistered ? (ContainerDefinition)t : createDefinition(k);
            if (Objects.isNull(definition)) {
                return null;
            }
            return createContainer(k.toString(), definition);
        });
    }

    /**
     * Whether this provider has container of given {@code namespace}.
     *
     * @param namespace namespace
     * @return boolean
     */
    @Override
    public boolean containsContainer(String namespace) {
        Object key = getCacheKey(namespace);
        if (key instanceof String) {
            return containerMap.containsKey(key);
        }
        CacheKey ck = (CacheKey) key;
        return Optional.ofNullable(containerProviderMap.get(ck.getProviderName()))
                .map(containerProvider -> containerProvider.containsContainer(ck.getProviderName()))
                .orElse(false);
    }

    /**
     * Clear all data caches.
     *
     * @see ContainerLifecycleProcessor#whenRegistered
     */
    @Override
    public void clear() {
        log.info("clear all cache for container manager");
        containerMap.values().forEach(t -> ConfigurationUtil.triggerWhenDestroyed(t, containerLifecycleProcessorList));
        containerMap.clear();
        containerProviderMap.clear();
        containerLifecycleProcessorList.clear();
    }

    // ================ product methods ================

    /**
     * <p>Create container comparator by given {@link CacheKey}.<br/>
     * if definition is null, then try to create and register definition by provider-based factory method first,
     * then create and cache container comparator.
     * 
     * <p>It may lock {@link #containerMap}.
     *
     * @param namespace namespace
     * @param definition definition of container
     * @return container comparator
     * @see ContainerLifecycleProcessor#whenCreated 
     */
    @Nullable
    protected Container<Object> createContainer(
            String namespace, ContainerDefinition definition) {
        Container<Object> container = definition.createContainer();
        return ConfigurationUtil.triggerWhenCreated(
                namespace, definition, container, containerLifecycleProcessorList, log
        );
    }

    /**
     * <p>Create definition by provider-based factory method.<br/>
     * if namespace is not belonged to any container that has a specified provider, then return null.
     *
     * @param cacheKey namespace
     * @return container definition
     * @see ContainerLifecycleProcessor#whenRegistered
     */
    @Nullable
    protected ContainerDefinition createDefinition(Object cacheKey) {
        // is unregistered container
        if (!(cacheKey instanceof CacheKey)) {
            return null;
        }

        // the container need create from provider,
        // try to create and register definition by provider-based factory method
        CacheKey key = (CacheKey) cacheKey;
        ContainerProvider containerProvider = getContainerProvider(key.getProviderName());
        if (Objects.isNull(containerProvider)) {
            return null;
        }
        ContainerDefinition definition = new ContainerDefinition.SimpleContainerDefinition(
                key.getNamespace(), key.getProviderName(),
            () -> containerProvider.getContainer(key.getNamespace())
        );
        return ConfigurationUtil.triggerWhenRegistered(
            definition, key.toString(), null, containerLifecycleProcessorList, log
        );
    }

    /**
     * Get the cache key for container comparator.
     *
     * @param namespace namespace of container,
     *                   which can also be the cache name for the container comparator.
     * @return if namespace contains provider name, then return {@link CacheKey}, otherwise return {@link String}.
     * @see #PROVIDER_NAME_PREFIX
     */
    protected Object getCacheKey(String namespace) {
        int index = namespace.indexOf(PROVIDER_NAME_PREFIX);
        if (index < 0) {
            return namespace;
        }
        String providerName = namespace.substring(0, index);
        String containerNamespace = namespace.substring(index + 2);
        return new CacheKey(containerNamespace, providerName);
    }

    /**
     * Cache key for namespace with provider name
     *
     * @author huangchengxing
     */
    @EqualsAndHashCode
    @RequiredArgsConstructor
    @Getter
    protected static class CacheKey {
        @EqualsAndHashCode.Include
        private final String namespace;
        @EqualsAndHashCode.Include
        private final String providerName;
    }
}
