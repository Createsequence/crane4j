package cn.crane4j.extension.spring;

import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.cache.CacheableContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.MethodInvokerContainer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link BeanMethodContainerRegistrar}
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DefaultCrane4jSpringConfiguration.class, BeanMethodContainerRegistrarTest.Service.class})
public class BeanMethodContainerRegistrarTest {

    @Autowired
    private Crane4jApplicationContext context;
    @Autowired
    private BeanMethodContainerRegistrar beanMethodContainerRegistrar;
    @Autowired
    private Service service;

    @Test
    public void testAutoRegister() {
        Assert.assertFalse(context.containsContainer("noneResultMethod"));

        // mappedMethod
        Container<?> mappedMethod = context.getContainer("mappedMethod");
        Assert.assertTrue(mappedMethod instanceof MethodInvokerContainer);
        Assert.assertEquals("mappedMethod", mappedMethod.getNamespace());

        // onoToOneMethod
        Container<?> onoToOneMethod = context.getContainer("onoToOneMethod");
        Assert.assertTrue(onoToOneMethod instanceof MethodInvokerContainer);
        Assert.assertEquals("onoToOneMethod", onoToOneMethod.getNamespace());

        // oneToManyMethod
        Container<?> oneToManyMethod = context.getContainer("oneToManyMethod");
        Assert.assertTrue(oneToManyMethod instanceof CacheableContainer);
        Assert.assertEquals("oneToManyMethod", oneToManyMethod.getNamespace());

        beanMethodContainerRegistrar.destroy();
    }

    @Test
    public void testRegister() {
        context.setAllowContainerOverriding(true);
        beanMethodContainerRegistrar.register(service, Service.class);

        // mappedMethod
        Container<?> mappedMethod = context.getContainer("mappedMethod");
        Assert.assertTrue(mappedMethod instanceof MethodInvokerContainer);
        Assert.assertEquals("mappedMethod", mappedMethod.getNamespace());

        // onoToOneMethod
        Container<?> onoToOneMethod = context.getContainer("onoToOneMethod");
        Assert.assertTrue(onoToOneMethod instanceof MethodInvokerContainer);
        Assert.assertEquals("onoToOneMethod", onoToOneMethod.getNamespace());

        // oneToManyMethod
        Container<?> oneToManyMethod = context.getContainer("oneToManyMethod");
        Assert.assertTrue(oneToManyMethod instanceof CacheableContainer);
        Assert.assertEquals("oneToManyMethod", oneToManyMethod.getNamespace());
    }

    @SuppressWarnings("unused")
    protected static class BaseService {
        public void noneResultMethod() { }
        public Map<String, Foo> mappedMethod(List<String> args) {
            return args.stream()
                .map(key -> new Foo(key, key))
                .collect(Collectors.toMap(Foo::getId, Function.identity()));
        }
    }

    @SuppressWarnings("all")
    // 若不指定bind则无法正确找到对应方法
    @ContainerMethod(namespace = "noneResultMethod", type = MappingType.NO_MAPPING, resultType = Foo.class)
    // 通过类注解声明父类中的容器方法
    @ContainerMethod(
        namespace = "noneResultMethod", type = MappingType.NO_MAPPING, resultType = Foo.class, bindMethod = "noneResultMethod"
    )
    @ContainerMethod(
        namespace = "mappedMethod", type = MappingType.NO_MAPPING, resultType = Foo.class
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
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    private static class Foo {
        private String id;
        private String name;
    }
}
