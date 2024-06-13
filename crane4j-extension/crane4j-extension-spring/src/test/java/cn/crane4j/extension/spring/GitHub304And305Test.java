package cn.crane4j.extension.spring;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.cache.CacheDefinition;
import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.CacheableContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.extension.spring.annotation.compose.GuavaContainerCache;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author huangchengxing
 * @see <a href="https://github.com/opengoofy/crane4j/issues/304">GitHub#304</a>
 * @see <a href="https://github.com/opengoofy/crane4j/issues/305">GitHub#305</a>
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { DefaultCrane4jSpringConfiguration.class, GitHub304And305Test.ServiceImpl.class })
public class GitHub304And305Test {

    @Autowired
    private Crane4jGlobalConfiguration configuration;
    @Autowired
    private ServiceImpl serviceImpl;

    @Test
    public void test() {
        Container<?> container = configuration.getContainer("test");
        Assert.assertTrue(container instanceof CacheableContainer);
        CacheableContainer<?> cacheableContainer = (CacheableContainer<?>) container;
        CacheDefinition cacheDefinition = cacheableContainer.getCacheDefinition();
        Assert.assertEquals(CacheManager.DEFAULT_GUAVA_CACHE_MANAGER_NAME, cacheDefinition.getCacheManager());

        Map<Integer, Foo> data1 = (Map<Integer, Foo>) ((Container<Integer>)container).get(Collections.singletonList(10086));
        Assert.assertEquals(1, data1.size());
        Foo foo1 = data1.get(10086);
        Assert.assertNotNull(foo1);
        Assert.assertEquals(1, serviceImpl.getCounter().get());

        Map<Integer, Foo> data2 = (Map<Integer, Foo>) ((Container<Integer>)container).get(Collections.singletonList(10086));
        Assert.assertEquals(1, data2.size());
        Foo foo2 = data2.get(10086);
        Assert.assertNotNull(foo2);

        Assert.assertSame(foo1, foo2);
        Assert.assertEquals(1, serviceImpl.getCounter().get());
    }

    @Component
    public static class ServiceImpl {
        @Getter
        private AtomicInteger counter = new AtomicInteger(0);
        @GuavaContainerCache
        @ContainerMethod(namespace = "test", resultType = Foo.class)
        public List<Foo> getByIds(Collection<Integer> ids) {
            counter.incrementAndGet();
            return ids.stream()
                .map(Foo::new)
                .collect(Collectors.toList());
        }
    }

    @Data
    @RequiredArgsConstructor
    public static class Foo {
        private final Integer id;
    }
}
