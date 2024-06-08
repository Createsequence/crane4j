package cn.crane4j.extension.query;

import cn.crane4j.annotation.AssembleDb;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.MethodInvokerContainer;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.SimpleBeanOperations;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.Crane4jTemplate;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ConfigurationUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * test for {@link AssembleDbAnnotationHandler}
 *
 * @author huangchengxing
 */
public class AssembleDbAnnotationHandlerTest extends DataSourceTestSupport {

    private AssembleDbAnnotationHandler handler;
    private SimpleCrane4jGlobalConfiguration configuration;
    private Crane4jTemplate crane4jTemplate;

    @Override
    public void afterDataSourceInitialized(DataSource dataSource) {
        // init components
        crane4jTemplate = Crane4jTemplate.withDefaultConfiguration();
        configuration = (SimpleCrane4jGlobalConfiguration) crane4jTemplate.configuration();
        handler = new AssembleDbAnnotationHandler(
            configuration.getAnnotationFinder(), configuration,
            ConfigurationUtil.createMethodInvokerContainerCreator(configuration),
            DataSourceProvider.of(dataSource)
        );
        crane4jTemplate.opsForComponent()
            .registerOperationAnnotationHandler(handler);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResolveOperations() {
        BeanOperations operations = new SimpleBeanOperations(Foo.class);
        handler.resolve(null, operations);

        Assert.assertThrows(UnsupportedOperationException.class,
            () -> handler.setRepositoryTargetProvider(null));

        Collection<AssembleOperation> assembleOperations = operations.getAssembleOperations();
        Assert.assertEquals(2, assembleOperations.size());

        // check operation
        AssembleOperation classLevelOperation = CollectionUtils.get(assembleOperations, 0);
        Assert.assertNotNull(classLevelOperation);
        Assert.assertEquals("id", classLevelOperation.getKey());
        Assert.assertEquals(3, classLevelOperation.getPropertyMappings().size());

        // check container
        Container<Object> classLevelOperationContainer = configuration.getContainer(classLevelOperation.getContainer());
        Assert.assertNotNull(classLevelOperationContainer);
        Assert.assertTrue(classLevelOperationContainer instanceof MethodInvokerContainer);

        // check query
        Map<Object, ?> resultOfContainer = classLevelOperationContainer.get(Collections.singletonList(1));
        Assert.assertEquals(1, resultOfContainer.size());
        Map<String, Object> data = (Map<String, Object>)resultOfContainer.get(1);
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.get("id"));
        Assert.assertEquals("小明", data.get("name"));
        Assert.assertEquals(18, data.get("age"));
        Assert.assertEquals(1, data.get("sex"));

        Map<Object, ?> emptyResult = classLevelOperationContainer.get(null);
        Assert.assertTrue(emptyResult.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResolveOperations2() {
        BeanOperations operations = new SimpleBeanOperations(Foo.class);
        handler.resolve(null, operations);

        Assert.assertThrows(UnsupportedOperationException.class,
            () -> handler.setRepositoryTargetProvider(null));

        Collection<AssembleOperation> assembleOperations = operations.getAssembleOperations();
        Assert.assertEquals(2, assembleOperations.size());

        // check operation
        AssembleOperation fieldLevelOperation = CollectionUtils.get(assembleOperations, 1);
        Assert.assertNotNull(fieldLevelOperation);
        Assert.assertEquals("id", fieldLevelOperation.getKey());
        Assert.assertEquals(3, fieldLevelOperation.getPropertyMappings().size());

        // check container
        Container<Object> fieldLevelOperationContainer = configuration.getContainer(fieldLevelOperation.getContainer());
        Assert.assertNotNull(fieldLevelOperationContainer);
        Assert.assertTrue(fieldLevelOperationContainer instanceof MethodInvokerContainer);

        // check query
        Map<Object, ?> resultOfContainer = fieldLevelOperationContainer.get(Collections.singletonList("小明"));
        Assert.assertEquals(1, resultOfContainer.size());
        Map<String, Object> data = (Map<String, Object>)resultOfContainer.get("小明");
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.get("id"));
        Assert.assertEquals("小明", data.get("name"));
        Assert.assertEquals(18, data.get("age"));
        Assert.assertNull(data.get("sex"));

        Map<Object, ?> emptyResult = fieldLevelOperationContainer.get(null);
        Assert.assertTrue(emptyResult.isEmpty());
    }

    @Test
    public void testAssemble() {
        Foo foo = new Foo(1);
        crane4jTemplate.execute(Collections.singletonList(foo));
        Assert.assertEquals("小明", foo.getName());
        Assert.assertEquals(18, foo.getAge().intValue());
        Assert.assertEquals(1, foo.getSex().intValue());
    }

    @AssembleDb(
        key = "id", sort = 1,
        fromTable = "foo", whereColumn = "id",
        prop = {"name", "age", "sex"}
    )
    @Data
    @RequiredArgsConstructor
    public static class Foo {
        @AssembleDb(
            sort = 2,
            selectColumns = "id, age", fromTable = "foo", whereColumn = "name",
            prop = {"name", "age", "sex"}
        )
        private final Integer id;
        private String name;
        private Integer age;
        private Integer sex;
    }
}
