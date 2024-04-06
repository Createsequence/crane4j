package cn.crane4j.core.support.container;

import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.cache.CacheableContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.MethodInvokerContainer;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.converter.ConverterManager;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link ContainerMethodAnnotationProcessor}
 *
 * @author huangchengxing
 */
public class ContainerMethodAnnotationProcessorTest {

    private ContainerMethodAnnotationProcessor processor;

    @Before
    public void init() {
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        ConverterManager converterManager = configuration.getConverterManager();
        MethodInvokerContainerCreator containerCreator = new MethodInvokerContainerCreator(
            configuration.getPropertyOperator(), converterManager
        );
        AnnotationFinder annotationFinder = SimpleAnnotationFinder.INSTANCE;
        Collection<MethodContainerFactory> factories = Collections.singletonList(
            new CacheableMethodContainerFactory(containerCreator, annotationFinder, configuration)
        );
        processor = new ContainerMethodAnnotationProcessor(new ArrayList<>(), new SimpleAnnotationFinder());
        factories.forEach(processor::registerMethodContainerFactory);
    }

    @Test
    public void test() {
        Assert.assertThrows(Crane4jException.class, () -> processor.process(new NoneMethodClass(), NoneMethodClass.class));

        Service target = new Service();
        Map<String, Container<?>> containerMap = processor.process(target, target.getClass()).stream()
            .collect(Collectors.toMap(Container::getNamespace, Function.identity()));
        Assert.assertEquals(3, containerMap.size());
        Assert.assertFalse(containerMap.containsKey("noneResultMethod"));

        // mappedMethod
        Container<?> mappedMethod = containerMap.get("mappedMethod");
        Assert.assertTrue(mappedMethod instanceof MethodInvokerContainer);
        Assert.assertEquals("mappedMethod", mappedMethod.getNamespace());

        // onoToOneMethod
        Container<?> onoToOneMethod = containerMap.get("onoToOneMethod");
        Assert.assertTrue(onoToOneMethod instanceof MethodInvokerContainer);
        Assert.assertEquals("onoToOneMethod", onoToOneMethod.getNamespace());

        // oneToManyMethod
        Container<?> oneToManyMethod = containerMap.get("oneToManyMethod");
        Assert.assertTrue(oneToManyMethod instanceof CacheableContainer);
        Assert.assertEquals("oneToManyMethod", oneToManyMethod.getNamespace());

        // unnecessary type
        Assert.assertTrue(processor.process(new Object(), Object.class).isEmpty());
        Assert.assertTrue(processor.process(new Object(), Object.class).isEmpty());
    }

    protected static class BaseService {
        public void noneResultMethod() { }
        public Map<String, Foo> mappedMethod(List<String> args) {
            return args.stream()
                .map(key -> new Foo(key, key))
                .collect(Collectors.toMap(Foo::getId, Function.identity()));
        }
        public Map<String, Foo> mappedMethod(List<String> args, Object other) {
            return args.stream()
                .map(key -> new Foo(key, key))
                .collect(Collectors.toMap(Foo::getId, Function.identity()));
        }
    }


    @ContainerMethod(bindMethod = "none", type = MappingType.NO_MAPPING, resultType = Foo.class)
    private static class NoneMethodClass {}

    @ContainerMethod(namespace = "noneResultMethod", type = MappingType.NO_MAPPING, resultType = Foo.class)
    // 通过类注解声明父类中的容器方法
    @ContainerMethod(
        namespace = "noneResultMethod", type = MappingType.NO_MAPPING, resultType = Foo.class,
        bindMethod = "noneResultMethod"
    )
    @ContainerMethod(
        namespace = "mappedMethod", type = MappingType.NO_MAPPING, resultType = Foo.class,
        bindMethod = "mappedMethod", bindMethodParamTypes = List.class
    )
    protected static class Service extends BaseService {
        @ContainerMethod(namespace = "onoToOneMethod", type = MappingType.ONE_TO_ONE, resultType = Foo.class)
        public Set<Foo> onoToOneMethod(List<String> args) {
            return args.stream().map(key -> new Foo(key, key)).collect(Collectors.toSet());
        }
        // 声明该方法容器为可缓存容器
        @ContainerCache
        @ContainerMethod(namespace = "oneToManyMethod", type = MappingType.ONE_TO_MANY, resultType = Foo.class, resultKey = "name")
        public List<Foo> oneToManyMethod(List<String> args) {
            return args.stream().map(key -> new Foo(key, key)).collect(Collectors.toList());
        }
        public List<String> groupedByIndex(List<String> args) {
            return args;
        }
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    protected static class Foo {
        private String id;
        private String name;
    }
}
