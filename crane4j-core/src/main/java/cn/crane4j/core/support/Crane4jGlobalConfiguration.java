package cn.crane4j.core.support;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.executor.handler.key.KeyResolver;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.ClassUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Framework global configuration.
 *
 * @author huangchengxing
 */
public interface Crane4jGlobalConfiguration
    extends ContainerManager, PropertyMappingStrategyManager {

    /**
     * Get {@link ConverterManager}
     *
     * @return {@link ConverterManager}
     */
    ConverterManager getConverterManager();

    /**
     * Get property operator.
     *
     * @return property operator
     */
    PropertyOperator getPropertyOperator();

    /**
     * Get type handler.
     *
     * @return type handler
     */
    TypeResolver getTypeResolver();

    /**
     * Get bean operation executor.
     *
     * @param executorName executor name
     * @param executorType executor type
     * @return executor
     */
    @NonNull
    BeanOperationExecutor getBeanOperationExecutor(@Nullable String executorName, Class<?> executorType);

    /**
     * Get bean operation executor.
     *
     * @param executorName executor name
     * @return executor
     */
    @NonNull
    default BeanOperationExecutor getBeanOperationExecutor(String executorName) {
        return getBeanOperationExecutor(executorName, BeanOperationExecutor.class);
    }

    /**
     * Get bean operation executor.
     *
     * @param executorType executor type
     * @return executor
     */
    @NonNull
    default BeanOperationExecutor getBeanOperationExecutor(Class<?> executorType) {
        return getBeanOperationExecutor(null, executorType);
    }

    /**
     * Get bean operation parser.
     *
     * @param parserName parser name
     * @param parserType parser type
     * @return parser
     */
    @NonNull
    BeanOperationParser getBeanOperationsParser(@Nullable String parserName, Class<?> parserType);

    /**
     * Get bean operation parser.
     *
     * @param parserName parser name
     * @return parser
     */
    @NonNull
    default BeanOperationParser getBeanOperationsParser(String parserName) {
        return getBeanOperationsParser(parserName, BeanOperationParser.class);
    }

    /**
     * Get bean operation parser.
     *
     * @param parserType parser type
     * @return parser
     */
    @NonNull
    default BeanOperationParser getBeanOperationsParser(Class<?> parserType) {
        return getBeanOperationsParser(null, parserType);
    }

    /**
     * Get assemble operation handler.
     *
     * @param  handlerName handler name
     * @param handlerType handler type
     * @return handler
     */
    @NonNull
    AssembleOperationHandler getAssembleOperationHandler(@Nullable String handlerName, Class<?> handlerType);

    /**
     * Get assemble operation handler.
     *
     * @param  handlerName handler name
     * @return handler
     */
    @NonNull
    default AssembleOperationHandler getAssembleOperationHandler(String handlerName) {
        return getAssembleOperationHandler(handlerName, AssembleOperationHandler.class);
    }

    /**
     * Get assemble operation handler.
     *
     * @param handlerType handler type
     * @return handler
     */
    @NonNull
    default AssembleOperationHandler getAssembleOperationHandler(Class<?> handlerType) {
        return getAssembleOperationHandler(null, handlerType);
    }

    /**
     * Get disassemble operation handler.
     *
     * @param handlerName handler name
     * @param handlerType handler type
     * @return handler
     */
    @NonNull
    DisassembleOperationHandler getDisassembleOperationHandler(@Nullable String handlerName, Class<?> handlerType);

    /**
     * Get disassemble operation handler.
     *
     * @param handlerName handler name
     * @return handler
     */
    @NonNull
    default DisassembleOperationHandler getDisassembleOperationHandler(String handlerName) {
        return getDisassembleOperationHandler(handlerName, DisassembleOperationHandler.class);
    }

    /**
     * Get disassemble operation handler.
     *
     * @param handlerType handler type
     * @return handler
     */
    @NonNull
    default DisassembleOperationHandler getDisassembleOperationHandler(Class<?> handlerType) {
        return getDisassembleOperationHandler(null, handlerType);
    }

    /**
     * Get cache manager.
     *
     * @param name cache factory name
     * @return cache factory
     * @throws IllegalArgumentException if name does not find cache factory
     * @since 2.4.0
     */
    @NonNull
    CacheManager getCacheManager(String name);

    /**
     * Register cache manager.
     *
     * @param cacheManager cache manager
     * @since 2.8.0
     */
    void registerCacheManager(CacheManager cacheManager);

    /**
     * Get key resolver.
     *
     * @param resolverType resolver type
     * @return key resolver
     * @since 2.7.0
     */
    default KeyResolver getKeyResolver(Class<? extends KeyResolver> resolverType) {
        return ClassUtils.newInstance(resolverType);
    }
}
