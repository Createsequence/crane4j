package cn.crane4j.core.support;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.ManyToManyAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectiveDisassembleOperationHandler;
import cn.crane4j.core.executor.handler.key.KeyResolver;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy;
import cn.crane4j.core.parser.operation.AssembleOperation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * test for {@link SimpleCrane4jGlobalConfiguration}
 *
 * @author huangchengxing
 */
public class SimpleCrane4jGlobalConfigurationTest {

    private SimpleCrane4jGlobalConfiguration configuration;

    @Before
    public void init() {
        configuration =  SimpleCrane4jGlobalConfiguration.create();
    }

    @Test
    public void operatePropertyMappingStrategy() {
        Assert.assertNotNull(configuration.getPropertyMappingStrategyManager());
        Collection<PropertyMappingStrategy> strategies = new ArrayList<>(configuration.getAllPropertyMappingStrategies());
        Assert.assertFalse(strategies.isEmpty());
        strategies.stream().map(PropertyMappingStrategy::getName).forEach(configuration::removePropertyMappingStrategy);
        Assert.assertTrue(configuration.getAllPropertyMappingStrategies().isEmpty());
        strategies.forEach(configuration::addPropertyMappingStrategy);
        Assert.assertEquals(new HashSet<>(strategies), new HashSet<>(configuration.getAllPropertyMappingStrategies()));
    }

    @Test
    public void registerContainerLifecycleProcessor() {
        Collection<ContainerLifecycleProcessor> awareList = configuration.getContainerLifecycleProcessors();
        int size = awareList.size();
        configuration.registerContainerLifecycleProcessor(new ContainerLifecycleProcessor() { });
        Assert.assertEquals(size + 1, awareList.size());
        configuration.registerContainerLifecycleProcessor(new ContainerLifecycleProcessor() { });
        Assert.assertEquals(size + 2, awareList.size());
    }

    @Test
    public void getContainerProvider() {
        Assert.assertSame(configuration, configuration.getContainerProvider(configuration.getClass().getSimpleName()));
    }

    @Test
    public void getPropertyOperator() {
        Assert.assertNotNull(configuration.getPropertyOperator());
    }

    @Test
    public void getTypeResolver() {
        Assert.assertNotNull(configuration.getTypeResolver());
    }

    @Test
    public void getContainer() {
        Container<?> container = LambdaContainer.forLambda("test", DataProvider.empty());
        ContainerDefinition cd = configuration.registerContainer(container);
        Assert.assertSame(cd.createContainer(), container);
        Assert.assertNotNull(configuration.getContainer("test"));
    }

    @Test
    public void getBeanOperationsParser() {
        Assert.assertNotNull(configuration.getBeanOperationsParser(TypeHierarchyBeanOperationParser.class));
        Assert.assertNotNull(configuration.getBeanOperationsParser(TypeHierarchyBeanOperationParser.class.getSimpleName()));
    }

    @Test
    public void getAssembleOperationHandler() {
        Assert.assertNotNull(configuration.getAssembleOperationHandler(ManyToManyAssembleOperationHandler.class));
        Assert.assertNotNull(configuration.getAssembleOperationHandler(ManyToManyAssembleOperationHandler.class.getSimpleName()));
    }

    @Test
    public void getGetBeanOperationExecutor() {
        Assert.assertNotNull(configuration.getBeanOperationExecutor(DisorderedBeanOperationExecutor.class));
        Assert.assertNotNull(configuration.getBeanOperationExecutor(DisorderedBeanOperationExecutor.class.getSimpleName()));
    }

    @Test
    public void getDisassembleOperationHandler() {
        Assert.assertNotNull(configuration.getDisassembleOperationHandler(ReflectiveDisassembleOperationHandler.class));
        Assert.assertNotNull(configuration.getDisassembleOperationHandler(ReflectiveDisassembleOperationHandler.class.getSimpleName()));
    }

    @Test
    public void getKeyResolver() {
        Assert.assertTrue(configuration.getKeyResolver(TestKeyResolver.class) instanceof TestKeyResolver);
    }

    public static class TestKeyResolver implements KeyResolver {
        @Override
        public Object resolve(Object target, AssembleOperation operation) {
            return null;
        }
    }
}
