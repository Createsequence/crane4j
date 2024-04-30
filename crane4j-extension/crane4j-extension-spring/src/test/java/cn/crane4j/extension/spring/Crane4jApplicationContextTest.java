package cn.crane4j.extension.spring;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.OneToOneAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectiveDisassembleOperationHandler;
import cn.crane4j.core.executor.handler.key.KeyResolver;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.util.ReflectUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Map;

/**
 * test for {@link Crane4jApplicationContext}
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DefaultCrane4jSpringConfiguration.class, Crane4jApplicationContextTest.TestConfig.class})
public class Crane4jApplicationContextTest {

    @Autowired
    private Crane4jApplicationContext context;
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void test() {
        Assert.assertNotNull(context.getTypeResolver());
        Assert.assertNotNull(context.getPropertyOperator());
        Assert.assertNotNull(context.getBeanOperationsParser(TypeHierarchyBeanOperationParser.class));
        Assert.assertNotNull(context.getBeanOperationsParser("ConditionalTypeHierarchyBeanOperationParser"));
        Assert.assertNotNull(context.getBeanOperationExecutor(DisorderedBeanOperationExecutor.class));
        Assert.assertNotNull(context.getBeanOperationExecutor("disorderedBeanOperationExecutor"));
        Assert.assertNotNull(context.getAssembleOperationHandler(OneToOneAssembleOperationHandler.class));
        Assert.assertNotNull(context.getAssembleOperationHandler("oneToOneAssembleOperationHandler"));
        Assert.assertNotNull(context.getDisassembleOperationHandler(ReflectiveDisassembleOperationHandler.class));
        Assert.assertNotNull(context.getDisassembleOperationHandler("reflectiveDisassembleOperationHandler"));
        Assert.assertNotNull(context.getContainer("test"));
        Assert.assertNotNull(context.getContainer("testBean"));
        Assert.assertNotNull(context.getContainer("test2"));
        Assert.assertNotNull(context.getContainer("testBean2"));

        int size = context.getContainerLifecycleProcessors().size();
        context.registerContainerLifecycleProcessor(new ContainerLifecycleProcessor() { });
        Assert.assertEquals(size + 1, context.getContainerLifecycleProcessors().size());
        context.registerContainerLifecycleProcessor(new ContainerLifecycleProcessor() { });
        Assert.assertEquals(size + 2, context.getContainerLifecycleProcessors().size());

        Map<String, Container<?>> containerMap = ReflectUtils.getFieldValue(context, "containerMap");
        Assert.assertNotNull(containerMap);
        Assert.assertFalse(containerMap.isEmpty());

        // get by bean name
        Assert.assertEquals(
            applicationContext.getBean("testBean"), context.getContainer("testBean")
        );
        Assert.assertEquals(
            applicationContext.getBean("testBean"), context.getContainer("test")
        );
        Assert.assertEquals(
            applicationContext.getBean("testProvider"), context.getContainerProvider("testProvider")
        );
        Assert.assertEquals(
            applicationContext.getBean(ConverterManager.class), context.getConverterManager()
        );

        context.destroy();
        Assert.assertTrue(containerMap.isEmpty());
    }

    @Test
    public void replaceContainer() {
        Container<?> container1 = LambdaContainer.forLambda("replaceContainer", ids -> Collections.emptyMap());
        ContainerDefinition cd1 = context.registerContainer(container1);
        Assert.assertSame(cd1.createContainer(), container1);
        Container<Object> container = context.getContainer("replaceContainer");
        Assert.assertSame(container, context.getContainer("replaceContainer"));

        Container<?> container2 = LambdaContainer.forLambda("replaceContainer", ids -> Collections.emptyMap());
        Assert.assertThrows(Crane4jException.class, () -> context.registerContainer(container2));
        context.setAllowContainerOverriding(true);
        ContainerDefinition cd2 = context.registerContainer(container2);
        Assert.assertSame(container2, cd2.createContainer());
        Assert.assertNotSame(container, context.getContainer("replaceContainer"));
    }

    @Test
    public void testBeanNameAndNamespace() {
        String beanName = context.getBeanNameByNamespace("test");
        Assert.assertEquals("testBean", beanName);
        beanName = context.getBeanNameByNamespace("testBean");
        Assert.assertNull(beanName);

        String namespace = context.getNamespaceByBeanName("testBean");
        Assert.assertEquals("test", namespace);
        namespace = context.getNamespaceByBeanName("test");
        Assert.assertNull(namespace);
    }

    @Test
    public void getKeyResolver() {
        KeyResolver keyResolver = context.getKeyResolver(TestKeyResolver.class);
        Assert.assertNotNull(keyResolver);
        Assert.assertTrue(keyResolver instanceof TestKeyResolver);
        ((DefaultListableBeanFactory)applicationContext.getAutowireCapableBeanFactory()).registerSingleton("testKeyResolver", keyResolver);
        Assert.assertSame(keyResolver, context.getKeyResolver(TestKeyResolver.class));
    }

    public static class TestKeyResolver implements KeyResolver {
        @Override
        public Object resolve(Object target, AssembleOperation operation) {
            return null;
        }
    }

    protected static class TestConfig {
        @Bean("testBean")
        public Container<String> container() {
            return Containers.forMap("test", Collections.singletonMap("key", "value"));
        }

        @Bean("testBean2")
        public ContainerDefinition containerDefinition() {
            return ContainerDefinition.create("test2", null, Container::empty);
        }

        @Bean("testProvider")
        public ContainerProvider testContainerProvider() {
            return new ContainerProvider() {
                @Override
                public @Nullable <K> Container<K> getContainer(String namespace) {
                    return null;
                }
            };
        }
    }
}
