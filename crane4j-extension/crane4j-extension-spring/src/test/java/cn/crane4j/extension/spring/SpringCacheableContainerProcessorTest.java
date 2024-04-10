package cn.crane4j.extension.spring;

import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.core.cache.CacheDefinition;
import cn.crane4j.core.cache.CacheableContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.container.Containers;
import cn.crane4j.extension.spring.annotation.compose.GuavaContainerCache;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * test for {@link SpringCacheableContainerProcessor}
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DefaultCrane4jSpringConfiguration.class, SpringCacheableContainerProcessorTest.TestConfig.class})
public class SpringCacheableContainerProcessorTest {

    @Autowired
    private ContainerManager containerManager;
    @Autowired
    private SpringCacheableContainerProcessor processor;

    @Test
    public void testCacheDefinitionRetriever() {
        processor.setCacheDefinitionRetriever((d, c) -> d.getNamespace().equals("test") ?
            new CacheDefinition.Impl("test", null, -1L, TimeUnit.MILLISECONDS) : null
        );
        Container<Object> container = processor.whenCreated(ContainerDefinition.create("test", "test", Container::empty), Container.empty());
        Assert.assertTrue(container instanceof CacheableContainer);
        Assert.assertEquals("test", ((CacheableContainer<?>) container).getCacheDefinition().getName());
        // if namespace is not test, then container is not cacheable
        container = processor.whenCreated(ContainerDefinition.create("test2", "test2", Container::empty), Container.empty());
        Assert.assertFalse(container instanceof CacheableContainer);
    }

    @Test
    public void testWithMetaAnnotation() {
        TestContainerWithMetaAnnotation testContainerWithMetaAnnotation = new TestContainerWithMetaAnnotation();
        Container<Object> container = processor.whenCreated(
            ContainerDefinition.create("test", "test", () -> testContainerWithMetaAnnotation), testContainerWithMetaAnnotation
        );
        Assert.assertTrue(container instanceof CacheableContainer);
        Assert.assertEquals("test", ((CacheableContainer<?>) container).getCacheDefinition().getName());

        CacheDefinition definition = ((CacheableContainer<?>) container).getCacheDefinition();
        Assert.assertEquals("test", definition.getName());
        Assert.assertEquals(1000L, definition.getExpireTime().longValue());
        Assert.assertEquals(TimeUnit.SECONDS, definition.getTimeUnit());
    }

    @Test
    public void testWithAnnotation() {
        TestContainerWithAnnotation testContainerWithAnnotation = new TestContainerWithAnnotation();
        Container<Object> container = processor.whenCreated(
            ContainerDefinition.create("test", "test", () -> testContainerWithAnnotation), testContainerWithAnnotation
        );
        Assert.assertTrue(container instanceof CacheableContainer);
        Assert.assertEquals("test", ((CacheableContainer<?>) container).getCacheDefinition().getName());

        CacheDefinition definition = ((CacheableContainer<?>) container).getCacheDefinition();
        Assert.assertEquals("test", definition.getName());
        Assert.assertEquals(1000L, definition.getExpireTime().longValue());
        Assert.assertEquals(TimeUnit.SECONDS, definition.getTimeUnit());
    }

    @Test
    public void testWithAnnotationOnFactoryMethod() {
        Container<String> container = containerManager.getContainer("testBean");
        Assert.assertNotNull(container);
        Assert.assertTrue(container instanceof CacheableContainer);
        CacheDefinition definition = ((CacheableContainer<?>) container).getCacheDefinition();
        Assert.assertEquals(container.getNamespace(), definition.getName());
        Assert.assertEquals(1000L, definition.getExpireTime().longValue());
        Assert.assertEquals(TimeUnit.SECONDS, definition.getTimeUnit());
    }

    @Getter
    @Cached
    private static class TestContainerWithMetaAnnotation implements Container<Object> {
        private final String namespace = "test";
        @Override
        public Map<Object, ?> get(Collection<Object> keys) {
            return null;
        }
    }

    @Getter
    @ContainerCache(
        expirationTime = 1000L,
        timeUnit = TimeUnit.SECONDS
    )
    private static class TestContainerWithAnnotation implements Container<Object> {
        private final String namespace = "test";
        @Override
        public Map<Object, ?> get(Collection<Object> keys) {
            return null;
        }
    }

    @GuavaContainerCache(
        expirationTime = 1000L,
        timeUnit = TimeUnit.SECONDS
    )
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Cached {
    }

    protected static class TestConfig {
        @ContainerCache(
            expirationTime = 1000L,
            timeUnit = TimeUnit.SECONDS
        )
        @Bean("testBean")
        public Container<String> container() {
            return Containers.forMap("test", Collections.singletonMap("key", "value"));
        }
    }
}
