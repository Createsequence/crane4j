package cn.crane4j.core.util;

import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.MappingTemplate;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.container.lifecycle.ContainerInstanceLifecycleProcessor;
import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.OperateTemplate;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.container.ContainerMethodAnnotationProcessor;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.support.operator.OperatorProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * test for {@link ConfigurationUtil}
 *
 * @author huangchengxing
 */
@Slf4j
public class ConfigurationUtilTest {

    @Test
    public void createOperatorProxyFactory() {
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        OperatorProxyFactory factory = ConfigurationUtil.createOperatorProxyFactory(configuration);
        Assert.assertNotNull(factory);
    }

    @Test
    public void createMethodInvokerContainerCreator() {
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        MethodInvokerContainerCreator containerCreator = ConfigurationUtil.createMethodInvokerContainerCreator(configuration);
        Assert.assertNotNull(containerCreator);
    }

    @Test
    public void createContainerMethodAnnotationProcessor() {
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        ContainerMethodAnnotationProcessor processor = ConfigurationUtil.createContainerMethodAnnotationProcessor(configuration);
        Assert.assertNotNull(processor);
    }

    @Test
    public void createOperateTemplate() {
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        OperateTemplate operateTemplate = ConfigurationUtil.createOperateTemplate(configuration);
        Assert.assertNotNull(operateTemplate);
    }

    @Test
    public void triggerWhenDestroyed() {
        List<ContainerLifecycleProcessor> processors = Arrays.asList(
            AlwaysNullContainerLifecycleProcessor.INSTANCE,
            DoNothingContainerLifecycleProcessor.INSTANCE,
            new ContainerInstanceLifecycleProcessor()
        );
        Map<String, Object> map = new HashMap<>();
        map.put("1", 1);
        Container<String> container = Containers.forMap("test", map);
        ConfigurationUtil.triggerWhenDestroyed(container, processors);
        Assert.assertTrue(map.isEmpty());
        ConfigurationUtil.triggerWhenDestroyed(null, processors);
    }

    @Test
    public void triggerWhenRegistered() {
        List<ContainerLifecycleProcessor> processors = Arrays.asList(
            AlwaysNullContainerLifecycleProcessor.INSTANCE, DoNothingContainerLifecycleProcessor.INSTANCE
        );
        ContainerDefinition definition = ContainerDefinition.create(Container.EMPTY_CONTAINER_NAMESPACE, null, Container::empty);
        definition = ConfigurationUtil.triggerWhenRegistered(definition, Container.EMPTY_CONTAINER_NAMESPACE, null, processors, log);
        Assert.assertNull(definition);
        definition = ConfigurationUtil.triggerWhenRegistered(null, Container.EMPTY_CONTAINER_NAMESPACE, null, processors, log);
        Assert.assertNull(definition);
    }

    @Test
    public void triggerWhenCreated() {
        List<ContainerLifecycleProcessor> processors = Arrays.asList(
            AlwaysNullContainerLifecycleProcessor.INSTANCE, DoNothingContainerLifecycleProcessor.INSTANCE
        );
        Container<Object> container = Container.empty();
        container = ConfigurationUtil.triggerWhenCreated(Container.EMPTY_CONTAINER_NAMESPACE, null, container, processors, log);
        Assert.assertNull(container);

        container = ConfigurationUtil.triggerWhenCreated(Container.EMPTY_CONTAINER_NAMESPACE, null, null, processors, log);
        Assert.assertNull(container);
    }

    @Test
    public void createPropertyMapping() {
        MappingTemplate mappingTemplate = AnnotatedElement.class.getAnnotation(MappingTemplate.class);

        PropertyMapping propertyMapping = ConfigurationUtil.createPropertyMapping(mappingTemplate.value()[0]);
        Assert.assertEquals("name", propertyMapping.getSource());
        Assert.assertEquals("name", propertyMapping.getReference());

        propertyMapping = ConfigurationUtil.createPropertyMapping(mappingTemplate.value()[1]);
        Assert.assertEquals("address", propertyMapping.getSource());
        Assert.assertEquals("", propertyMapping.getReference());
        Assert.assertTrue(propertyMapping.hasSource());

        propertyMapping = ConfigurationUtil.createPropertyMapping(mappingTemplate.value()[2]);
        Assert.assertEquals("", propertyMapping.getSource());
        Assert.assertEquals("age", propertyMapping.getReference());
        Assert.assertFalse(propertyMapping.hasSource());

        propertyMapping = ConfigurationUtil.createPropertyMapping(mappingTemplate.value()[3]);
        Assert.assertEquals("sex", propertyMapping.getSource());
        Assert.assertEquals("sex", propertyMapping.getReference());
    }

    @Test
    public void parsePropTemplate() {
        MappingTemplate mappingTemplate = AnnotatedElement.class.getAnnotation(MappingTemplate.class);
        List<PropertyMapping> mappings = ConfigurationUtil.parsePropTemplate(mappingTemplate);
        Assert.assertEquals(4, mappings.size());
    }

    @Test
    public void parsePropTemplateClasses() {
        List<PropertyMapping> mappings = ConfigurationUtil.parsePropTemplateClasses(
            new Class[]{ AnnotatedElement.class }, new SimpleAnnotationFinder()
        );
        Assert.assertEquals(4, mappings.size());
    }

    private static class AlwaysNullContainerLifecycleProcessor implements ContainerLifecycleProcessor {
        public static final AlwaysNullContainerLifecycleProcessor INSTANCE = new AlwaysNullContainerLifecycleProcessor();
        @Override
        public @Nullable Container<Object> whenCreated(ContainerDefinition definition, Container<Object> container) {
            return null;
        }
        @Override
        public ContainerDefinition whenRegistered(@Nullable Object old, ContainerDefinition newDefinition) {
            return null;
        }
    }

    private static class DoNothingContainerLifecycleProcessor implements ContainerLifecycleProcessor {
        public static final DoNothingContainerLifecycleProcessor INSTANCE = new DoNothingContainerLifecycleProcessor();
        @Override
        public @Nullable Container<Object> whenCreated(ContainerDefinition definition, Container<Object> container) {
            return null;
        }
    }

    @MappingTemplate({
        @Mapping(src = "name", ref = "name"),
        @Mapping(src = "address"),
        @Mapping(ref = "age"),
        @Mapping("sex"),
    })
    private static class AnnotatedElement { }
}
