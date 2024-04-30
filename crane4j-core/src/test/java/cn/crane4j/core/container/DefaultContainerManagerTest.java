package cn.crane4j.core.container;

import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.exception.Crane4jException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

/**
 * test for {@link DefaultContainerManager}
 *
 * @author huangchengxing
 */
public class DefaultContainerManagerTest {

    private DefaultContainerManager containerManager;

    @Before
    public void init() {
        containerManager = new DefaultContainerManager();
    }

    @Test
    public void containerLifecycleProcessorTest() {
        ContainerLifecycleProcessor processor = new ContainerLifecycleProcessor() {};
        containerManager.registerContainerLifecycleProcessor(processor);
        Assert.assertEquals(1, containerManager.getContainerLifecycleProcessors().size());
    }

    @Test
    public void containerProviderTest() {
        containerManager.registerContainerLifecycleProcessor(new ContainerLifecycleProcessor() {});
        Container<Object> container = Container.empty();
        ContainerProvider provider = new ContainerProvider() {
            @SuppressWarnings("unchecked")
            @Override
            public @Nullable <K> Container<K> getContainer(String namespace) {
                return (Container<K>) container;
            }
        };

        // register provider
        containerManager.registerContainerProvider("test", provider);
        Assert.assertSame(provider, containerManager.getContainerProvider("test"));

        // get container by provider
        Assert.assertSame(container, containerManager.getContainer("test", container.getNamespace()));
        String canonicalNamespace = ContainerManager.canonicalNamespace(container.getNamespace(), "test");
        Assert.assertTrue(containerManager.containsContainer(canonicalNamespace));
        Assert.assertSame(container, containerManager.getContainer(canonicalNamespace));

        // the provider is not registered, we get null
        Assert.assertNull(containerManager.getContainer("test2", container.getNamespace()));
    }

    @Test
    public void containerTest() {
        Container<Object> container = LambdaContainer.forLambda("test", ids -> Collections.emptyMap());
        ContainerDefinition definition = ContainerDefinition.create("test", "test", () -> container);

        // register container by definition
        Assert.assertSame(definition, containerManager.registerContainer(definition));
        Assert.assertTrue(containerManager.containsContainer(definition.getNamespace()));
        Assert.assertSame(container, containerManager.getContainer(definition.getNamespace()));

        // register container by factory method
        Assert.assertThrows(Crane4jException.class, () -> containerManager.registerContainer("test", () -> container));
        containerManager.setAllowContainerOverriding(true);
        definition = containerManager.registerContainer("test", () -> container);
        Assert.assertNotNull(definition);
        Assert.assertSame(container, definition.createContainer());
        Assert.assertTrue(containerManager.containsContainer(definition.getNamespace()));
        Assert.assertSame(container, containerManager.getContainer(definition.getNamespace()));

        // register container by comparator
        Assert.assertSame(definition, containerManager.registerContainer(definition));
        Assert.assertTrue(containerManager.containsContainer(definition.getNamespace()));
        Assert.assertSame(container, containerManager.getContainer(definition.getNamespace()));
    }

    @Test
    public void getAllLimitedContainers() {
        Container<Object> container1 = Containers.forLambda("test1", ids -> Collections.emptyMap());
        containerManager.registerContainer(container1);
        Container<Object> container2 = Containers.forMap("test2", Collections.emptyMap());
        containerManager.registerContainer(container2);
        Container<Object> container3 = Containers.forConstantClass(DefaultContainerManagerTest.class);
        containerManager.registerContainer(container3);
        Container<Object> container4 = Containers.forEnum(Enum.class);
        containerManager.registerContainer(container4);

        Collection<Container<Object>> containers = containerManager.getAllLimitedContainers();
        Assert.assertEquals(3, containers.size());
        Assert.assertTrue(containers.contains(container2));
        Assert.assertTrue(containers.contains(container3));
        Assert.assertTrue(containers.contains(container4));
    }

    @Test
    public void clear() {
        // register some container、provider、processor
        Container<Object> container = Container.empty();
        ContainerProvider provider = new ContainerProvider() {
            @SuppressWarnings("unchecked")
            @Override
            public @Nullable <K> Container<K> getContainer(String namespace) {
                return (Container<K>) container;
            }
        };
        ContainerLifecycleProcessor processor = new ContainerLifecycleProcessor() {};
        containerManager.registerContainerLifecycleProcessor(processor);
        containerManager.registerContainerProvider("test", provider);
        containerManager.registerContainer(container);

        // clear all
        containerManager.clear();
        Assert.assertTrue(containerManager.getContainerLifecycleProcessors().isEmpty());
        Assert.assertNull(containerManager.getContainerProvider("test"));
        Assert.assertFalse(containerManager.containsContainer(container.getNamespace()));
    }

    private enum Enum {}
}
