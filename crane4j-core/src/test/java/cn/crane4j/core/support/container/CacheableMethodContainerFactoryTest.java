package cn.crane4j.core.support.container;

import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.cache.CacheableContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ReflectUtils;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * test for {@link CacheableMethodContainerFactory}
 *
 * @author huangchengxing
 */
public class CacheableMethodContainerFactoryTest {

    private CacheableMethodContainerFactory factory;
    private Method annotatedMethod;
    private Method noneAnnotatedMethod;
    private Service service;

    @Before
    public void init() {
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        ConverterManager converterManager = configuration.getConverterManager();
        MethodInvokerContainerCreator containerCreator = new MethodInvokerContainerCreator(
            configuration.getPropertyOperator(), converterManager
        );
        factory = new CacheableMethodContainerFactory(
            containerCreator, new SimpleAnnotationFinder(), configuration
        );
        service = new Service();
        annotatedMethod = ReflectUtils.getMethod(Service.class, "annotatedMethod", List.class);
        Assert.assertNotNull(annotatedMethod);
        noneAnnotatedMethod = ReflectUtils.getMethod(Service.class, "noneAnnotatedMethod", List.class);
        Assert.assertNotNull(noneAnnotatedMethod);
    }

    @Test
    public void getSort() {
        Assert.assertEquals(MethodContainerFactory.DEFAULT_METHOD_CONTAINER_FACTORY_ORDER, factory.getSort());
    }

    @Test
    public void support() {
        Assert.assertTrue(factory.support(service, annotatedMethod, findAnnotations(annotatedMethod)));
        Assert.assertTrue(factory.support(service, noneAnnotatedMethod, findAnnotations(noneAnnotatedMethod)));
    }

    @Test
    public void get() {
        List<Container<Object>> containers = factory.get(service, annotatedMethod, findAnnotations(annotatedMethod));
        Container<Object> container = CollectionUtils.get(containers, 0);
        Assert.assertTrue(container instanceof CacheableContainer);

        Object cachedA = container.get(Collections.singleton("a")).get("a");
        Assert.assertNotNull(cachedA);
        Object a = container.get(Collections.singleton("a")).get("a");
        Assert.assertSame(cachedA, a);
    }

    private static Collection<ContainerMethod> findAnnotations(Method method) {
        return Arrays.asList(method.getAnnotationsByType(ContainerMethod.class));
    }

    private static class Service {
        @ContainerCache
        @ContainerMethod(namespace = "annotatedMethod", resultType = Foo.class)
        public List<Foo> annotatedMethod(List<String> args) {
            return args.stream().map(key -> new Foo(key, key)).collect(Collectors.toList());
        }
        @ContainerMethod(namespace = "noneAnnotatedMethod", resultType = Foo.class)
        public List<Foo> noneAnnotatedMethod(List<String> args) {
            return args.stream().map(key -> new Foo(key, key)).collect(Collectors.toList());
        }
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    private static class Foo {
        private String id;
        private String name;
    }
}
