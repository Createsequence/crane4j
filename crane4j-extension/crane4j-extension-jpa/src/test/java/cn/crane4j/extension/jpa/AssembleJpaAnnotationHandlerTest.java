package cn.crane4j.extension.jpa;

import cn.crane4j.annotation.AssembleJpa;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.MethodInvokerContainer;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.SimpleBeanOperations;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ConfigurationUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * test for {@link AssembleJpaAnnotationHandler}
 *
 * @author huangchengxing
 */
@EnableAutoConfiguration
@TestPropertySource(properties = "spring.config.location = classpath:test.yml")
@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {
//    HibernateJpaAutoConfiguration.class,
//    JpaRepositoriesAutoConfiguration.class,
//    DataSourceAutoConfiguration.class,
//    FooRepository.class
//})
public class AssembleJpaAnnotationHandlerTest {

    private AssembleJpaAnnotationHandler annotationHandler;
    private Crane4jGlobalConfiguration configuration;
    @Autowired
    private FooRepository fooRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DataSource dataSource;

    @Before
    public void afterInit() {
        configuration = SimpleCrane4jGlobalConfiguration.builder().build();
        annotationHandler = new AssembleJpaAnnotationHandler(
            SimpleAnnotationFinder.INSTANCE, configuration,
            ConfigurationUtil.createMethodInvokerContainerCreator(configuration),
            entityManager
        );
        annotationHandler.registerRepository("fooRepository", fooRepository);
    }

    @Test
    public void testResolveOperation1() {
        BeanOperations operations = new SimpleBeanOperations(Example.class);
        annotationHandler.resolve(null, operations);

        Collection<AssembleOperation> assembleOperations = operations.getAssembleOperations();
        Assert.assertEquals(2, assembleOperations.size());

        // check operation
        AssembleOperation classLevelOperation = CollectionUtils.get(assembleOperations, 0);
        Assert.assertNotNull(classLevelOperation);
        Assert.assertEquals("name", classLevelOperation.getKey());
        Assert.assertEquals(2, classLevelOperation.getPropertyMappings().size());

        // check container
        Container<?> classLevelContainer = configuration.getContainer(classLevelOperation.getContainer());
        Assert.assertTrue(classLevelContainer instanceof MethodInvokerContainer);

        // check query
        Map<String, ?> data = ((Container<String>)classLevelContainer).get(Collections.singletonList("小明"));
        Assert.assertEquals(1, data.size());
        Foo foo = (Foo)data.get("小明");
        Assert.assertNotNull(foo);
        Assert.assertEquals("小明", foo.getUserName());
        Assert.assertEquals((Integer)18, foo.getUserAge());
        Assert.assertEquals((Integer)1, foo.getUserSex());
        Assert.assertEquals((Integer)1, foo.getId());

        Assert.assertTrue(classLevelContainer.get(Collections.emptyList()).isEmpty());
    }

    @Test
    public void testResolveOperation2() {
        BeanOperations operations = new SimpleBeanOperations(Example.class);
        annotationHandler.resolve(null, operations);

        Collection<AssembleOperation> assembleOperations = operations.getAssembleOperations();
        Assert.assertEquals(2, assembleOperations.size());

        // check operation
        AssembleOperation fieldLevelOperation = CollectionUtils.get(assembleOperations, 1);
        Assert.assertNotNull(fieldLevelOperation);
        Assert.assertEquals("id", fieldLevelOperation.getKey());
        Assert.assertEquals(1, fieldLevelOperation.getPropertyMappings().size());

        // check container
        Container<?> fieldLevelContainer = configuration.getContainer(fieldLevelOperation.getContainer());
        Assert.assertTrue(fieldLevelContainer instanceof MethodInvokerContainer);

        // check query
        Map<Integer, ?> data = ((Container<Integer>)fieldLevelContainer).get(Collections.singletonList(1));
        Assert.assertEquals(1, data.size());
        Foo foo = (Foo)data.get(1);
        Assert.assertNotNull(foo);
        Assert.assertEquals("小明", foo.getUserName());
        Assert.assertEquals((Integer)1, foo.getId());
        Assert.assertNull(foo.getUserAge());
        Assert.assertNull(foo.getUserSex());
    }

    @AssembleJpa(
        key = "name",
        repository = "fooRepository", where = "userName",
        prop = {"name", "age"},
        sort = 1
    )
    private static class Example {
        @AssembleJpa(
            repository = "fooRepository", selects = "name as userName", where = "id",
            prop = "userName:name",
            sort = 2
        )
        private Integer id;
        private String name;
        private String age;
    }
}
