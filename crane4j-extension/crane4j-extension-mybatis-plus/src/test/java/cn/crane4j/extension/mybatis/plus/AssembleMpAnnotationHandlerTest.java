package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.annotation.AssembleMp;
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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * test for {@link AssembleMpAnnotationHandler}
 *
 * @author huangchengxing
 */
public class AssembleMpAnnotationHandlerTest extends MpBaseTest {

    private AssembleMpAnnotationHandler annotationHandler;
    private Crane4jGlobalConfiguration configuration;

    @Before
    public void afterInit() {
        configuration = SimpleCrane4jGlobalConfiguration.builder().build();
        annotationHandler = new AssembleMpAnnotationHandler(
            SimpleAnnotationFinder.INSTANCE, configuration,
            ConfigurationUtil.createMethodInvokerContainerCreator(configuration)
        );
        annotationHandler.registerRepository("fooMapper", fooMapper);
        annotationHandler.setDataSourceSwitcher(AssembleMpAnnotationHandler.DataSourceSwitcher.DO_NOTING);
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

    @AssembleMp(
        key = "name",
        mapper = "fooMapper", where = "userName",
        prop = {"name", "age"},
        sort = 1
    )
    private static class Example {
        @AssembleMp(
            datasource = "foo",
            mapper = "fooMapper", selects = "name as userName", where = "id",
            prop = "userName:name",
            sort = 2
        )
        private Integer id;
        private String name;
        private String age;
    }
}
