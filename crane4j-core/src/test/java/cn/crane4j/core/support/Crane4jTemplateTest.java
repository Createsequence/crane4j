package cn.crane4j.core.support;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.Operator;
import cn.crane4j.core.cache.AbstractMapCacheManager;
import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.CacheableContainer;
import cn.crane4j.core.condition.Condition;
import cn.crane4j.core.condition.ConditionParser;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.container.PartitionContainerProvider;
import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.handler.AssembleKeyAnnotationHandler;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.container.MethodContainerFactory;
import cn.crane4j.core.support.operator.OperatorProxyMethodFactory;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link Crane4jTemplate}
 *
 * @author huangchengxing
 */
public abstract class Crane4jTemplateTest {

    private static final String TEST_ENUM = "test_enum";
    private static final String TEST_CONSTANT = "test_constant";
    private static final String TEST_METHOD_CONTAINERS = "test_method_containers";

    protected Crane4jTemplate crane4jTemplate;
    protected SimpleCrane4jGlobalConfiguration configuration;
    protected OperateTemplate operateTemplate;

    @Before
    public void init() {
        crane4jTemplate = Crane4jTemplate.withDefaultConfiguration();
        configuration = (SimpleCrane4jGlobalConfiguration) crane4jTemplate.configuration();
        operateTemplate = crane4jTemplate.operateTemplate();

        if (!configuration.containsContainer(Foo.CONTAINER_NAMESPACE)) {
            crane4jTemplate.opsForContainer().registerLambdaContainer(
                Foo.CONTAINER_NAMESPACE,
                ids -> ids.stream().collect(Collectors.toMap(Function.identity(), id -> "name" + id))
            );
        }
    }

    public static class OpsForExecuteTest extends Crane4jTemplateTest {

        @Test
        public void testGetBeanOperations() {
            BeanOperations ops = crane4jTemplate.getBeanOperations(Foo.class);
            Assert.assertNotNull(ops);
            Assert.assertEquals(1, ops.getAssembleOperations().size());
            Assert.assertTrue(ops.getDisassembleOperations().isEmpty());
        }

        @Test
        public void testForExecute() {
            Foo foo = new Foo("1");
            crane4jTemplate.execute(Collections.singletonList(foo));
            Assert.assertEquals("name1", foo.getName());

            foo = new Foo("1");
            BeanOperations ops = crane4jTemplate.getBeanOperations(Foo.class);
            crane4jTemplate.execute(Collections.singletonList(foo), ops);
            Assert.assertEquals("name1", foo.getName());

            foo = new Foo("1");
            crane4jTemplate.execute(Collections.singletonList(foo), DisorderedBeanOperationExecutor.class);
            Assert.assertEquals("name1", foo.getName());

            foo = new Foo("1");
            BeanOperationExecutor executor = configuration.getBeanOperationExecutor(DisorderedBeanOperationExecutor.class);
            Assert.assertNotNull(executor);
            crane4jTemplate.execute(Collections.singletonList(foo), executor);
            Assert.assertEquals("name1", foo.getName());

            foo = new Foo("1");
            crane4jTemplate.executeAsync(Collections.singletonList(foo));
            Assert.assertEquals("name1", foo.getName());

            foo = new Foo("1");
            crane4jTemplate.executeOrdered(Collections.singletonList(foo));
            Assert.assertEquals("name1", foo.getName());
        }
    }

    public static class OpsForContainerTest extends Crane4jTemplateTest {

        @Test
        public void testConfigureContainer() {
            Crane4jTemplate.OpsForContainer ops = crane4jTemplate.opsForContainer();

            Container<?> container = Containers.empty("test_empty");
            Assert.assertSame(ops, ops.registerContainer(container));
            Assert.assertTrue(configuration.containsContainer("test_empty"));
            Assert.assertSame(container, ops.getContainer("test_empty"));

            Assert.assertSame(ops, ops.registerMapContainer("test_map1", Collections.emptyMap()));
            Assert.assertTrue(configuration.containsContainer("test_map1"));

            Assert.assertSame(ops, ops.registerMapContainer("test_map2", Arrays.asList(1, 2, 3), String::valueOf, Function.identity()));
            Assert.assertTrue(configuration.containsContainer("test_map2"));

            Assert.assertSame(ops, ops.registerLambdaContainer("test_lambda", DataProvider.empty()));
            Assert.assertTrue(configuration.containsContainer("test_lambda"));

            Assert.assertSame(ops, ops.registerConstantContainer(TestConstant.class));
            Assert.assertTrue(configuration.containsContainer(TEST_CONSTANT));

            Assert.assertSame(ops, ops.registerEnumContainer(TestEnum.class));
            Assert.assertTrue(configuration.containsContainer(TEST_ENUM));

            Assert.assertSame(ops, ops.registerEnumContainer("test_enum2", TestEnum.class, TestEnum::getCode));
            Assert.assertTrue(configuration.containsContainer("test_enum2"));

            Assert.assertSame(ops, ops.registerEnumContainer("test_enum3", TestEnum.class, TestEnum::getCode, TestEnum::getValue));
            Assert.assertTrue(configuration.containsContainer("test_enum3"));

            Assert.assertSame(ops, ops.registerMethodContainers(new TestMethodContainers()));
            Assert.assertTrue(configuration.containsContainer("test_enum3"));
        }

        @Test
        public void testConfigureComponentOfContainer() {
            Crane4jTemplate.OpsForContainer ops = crane4jTemplate.opsForContainer();

            ContainerProvider containerProvider = new PartitionContainerProvider();
            Assert.assertSame(ops, ops.registerContainerProvider("test_provider", containerProvider));
            Assert.assertSame(containerProvider, ops.getContainerProvider("test_provider"));
            Assert.assertSame(ops, ops.configureContainerProvider(
                "test_provider", PartitionContainerProvider.class, (ct, p) -> Assert.assertSame(containerProvider, p)
            ));

            Container<?> container = Containers.empty("test_cache_wrap");
            Container<?> cachedContainer = ops.wrapContainerWithCache(container, CacheManager.DEFAULT_MAP_CACHE_MANAGER_NAME, 1000L);
            Assert.assertTrue(cachedContainer instanceof CacheableContainer);
            Assert.assertNotSame(cachedContainer, container);
        }

        @Test
        public void testChainConfigure() {
            Crane4jTemplate.OpsForContainer ops = crane4jTemplate.opsForContainer();
            Assert.assertSame(ops.opsForComponent(), crane4jTemplate.opsForComponent());
            Assert.assertSame(ops.opsForExecute(), crane4jTemplate);
            Assert.assertSame(ops.opsForProxy(), crane4jTemplate.opsForProxy());
        }

        public static class TestMethodContainers {
            @ContainerMethod(namespace = TEST_METHOD_CONTAINERS, resultType = Map.class)
            public List<Map<String, Object>> listByIds(Collection<String> ids) {
                return ids.stream().map(id -> {
                    Map<String, Object> map = new HashMap<>(2);
                    map.put("id", id);
                    map.put("name", "name" + id);
                    return map;
                }).collect(Collectors.toList());
            }
        }

        @ContainerConstant(namespace = TEST_CONSTANT, reverse = true)
        public static class TestConstant {
            private static final String A = "a";
            private static final String B = "b";
            private static final String C = "c";
        }

        @ContainerEnum(namespace = TEST_ENUM, key = "code", value = "value")
        @Getter
        @RequiredArgsConstructor
        public enum TestEnum {
            A("a", "A"),
            B("b", "B"),
            C("c", "C");
            private final String code;
            private final String value;
        }
    }

    public static class OpsForComponentTest extends Crane4jTemplateTest {

        @Test
        public void testComponentRegister() {
            Crane4jTemplate.OpsForComponent ops = crane4jTemplate.opsForComponent();

            CacheManager cacheManager = new AbstractMapCacheManager() {
                @Override
                public String getName() {
                    return "test_cache_manager";
                }
                @Override
                protected <K> Map<K, Object> createMap() {
                    return new LinkedHashMap<>();
                }
            };
            ;
            Assert.assertSame(ops, ops.registerCacheManager(cacheManager));
            Assert.assertSame(cacheManager, ops.getCacheManager("test_cache_manager"));

            Assert.assertSame(ops, ops.registerConditionParser(new ConditionParser() {
                @Override
                public @NonNull List<Condition> parse(AnnotatedElement element, KeyTriggerOperation operation) {
                    return Collections.emptyList();
                }
            }));

            Assert.assertSame(ops, ops.registerMethodContainerFactory(new MethodContainerFactory() {
                @Override
                public boolean support(@Nullable Object source, Method method, Collection<ContainerMethod> annotations) {
                    return false;
                }
                @Override
                public List<Container<Object>> get(@Nullable Object source, Method method, Collection<ContainerMethod> annotations) {
                    return Collections.emptyList();
                }
            }));

            Assert.assertSame(ops, ops.registerContainerLifecycleProcessor(new ContainerLifecycleProcessor() { }));
            Assert.assertSame(ops, ops.registerOperationAnnotationHandler((parser, beanOperations) -> { }));
            Assert.assertSame(ops, ops.registerPropertyMappingStrategy((target, source, sourceValue, propertyMapping, mapping) -> { }));
        }

        @Test
        public void testConfigureComponents() {
            Crane4jTemplate.OpsForComponent ops = crane4jTemplate.opsForComponent();
            Assert.assertSame(ops, ops.configureCacheManager(
                CacheManager.DEFAULT_MAP_CACHE_MANAGER_NAME, AbstractMapCacheManager.class,
                (ct, cm) -> Assert.assertNotNull(cm)
            ));
            Assert.assertSame(ops, ops.configureOperationAnnotationHandler(
                AssembleKeyAnnotationHandler.class, (ct, handler) -> Assert.assertNotNull(handler)
            ));
        }

        @Test
        public void testChainConfigure() {
            Crane4jTemplate.OpsForComponent ops = crane4jTemplate.opsForComponent();
            Assert.assertSame(ops.opsForContainer(), crane4jTemplate.opsForContainer());
            Assert.assertSame(ops.opsForExecute(), crane4jTemplate);
            Assert.assertSame(ops.opsForProxy(), crane4jTemplate.opsForProxy());
        }
    }

    public static class OpsForProxyTest extends Crane4jTemplateTest {

        @Test
        public void testCreateAutoOperateProxy() {
            Crane4jTemplate.OpsForProxy ops = crane4jTemplate.opsForProxy();
            TestAutoOperateProxy proxy = ops.createAutoOperateProxy(new TestAutoOperateProxy());
            Foo foo = new Foo("1");
            Assert.assertSame(foo, proxy.fill(foo));
            Assert.assertEquals("name" + foo.getId(), foo.getName());
        }

        @Test
        public void testRegisterOperatorProxyMethodFactory() {
            Crane4jTemplate.OpsForProxy ops = crane4jTemplate.opsForProxy();
            AtomicBoolean processed = new AtomicBoolean(false);
            Assert.assertSame(
                ops, ops.registerOperatorProxyMethodFactory(new OperatorProxyMethodFactory() {
                    @Override
                    public int getSort() {
                        return -1;
                    }
                    @Override
                    public @Nullable MethodInvoker get(
                        BeanOperations beanOperations, Method method, BeanOperationExecutor beanOperationExecutor) {
                        processed.set(true);
                        return null;
                    }
                })
            );
            ops.createOperatorProxy(TestOperatorProxy.class);
            Assert.assertTrue(processed.get());
        }

        @Test
        public void testCreateOperatorProxy() {
            Crane4jTemplate.OpsForProxy ops = crane4jTemplate.opsForProxy();
            TestOperatorProxy proxy = ops.createOperatorProxy(TestOperatorProxy.class);
            Assert.assertNotNull(proxy);
            Foo foo = new Foo("1");
            proxy.fill(foo);
            Assert.assertEquals("name" + foo.getId(), foo.getName());
        }

        @Test
        public void testChainConfigure() {
            Crane4jTemplate.OpsForProxy ops = crane4jTemplate.opsForProxy();
            Assert.assertSame(ops.opsForContainer(), crane4jTemplate.opsForContainer());
            Assert.assertSame(ops.opsForExecute(), crane4jTemplate);
            Assert.assertSame(ops.opsForComponent(), crane4jTemplate.opsForComponent());
        }

        public static class TestAutoOperateProxy {
            @AutoOperate(type = Foo.class)
            public Foo fill(Foo foo) {
                return foo;
            }
        }

        @Operator
        public interface TestOperatorProxy {
            @Assemble(key = "id", container = Foo.CONTAINER_NAMESPACE, prop = ":name")
            void fill(Foo foo);
        }
    }

    @Data
    @RequiredArgsConstructor
    public static class Foo {
        private static final String CONTAINER_NAMESPACE = "foo_container";
        @Assemble(container = CONTAINER_NAMESPACE, prop = ":name")
        private final String id;
        private String name;
    }
}
