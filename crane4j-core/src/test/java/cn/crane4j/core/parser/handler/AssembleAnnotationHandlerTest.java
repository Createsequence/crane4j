package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimpleBeanOperations;
import cn.crane4j.core.parser.handler.strategy.OverwriteMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.parser.handler.strategy.SimplePropertyMappingStrategyManager;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.util.CollectionUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/**
 * test for {@link AssembleAnnotationHandler}
 *
 * @author huangchengxing
 */
public class AssembleAnnotationHandlerTest {

    private AssembleAnnotationHandler annotationHandler;
    private Crane4jGlobalConfiguration configuration;

    @Before
    public void init() {
        configuration = SimpleCrane4jGlobalConfiguration.create();
        PropertyMappingStrategyManager propertyMappingStrategyManager = new SimplePropertyMappingStrategyManager();
        propertyMappingStrategyManager.addPropertyMappingStrategy(OverwriteMappingStrategy.INSTANCE);
        this.annotationHandler = new AssembleAnnotationHandler(
            new SimpleAnnotationFinder(), configuration, propertyMappingStrategyManager
        );
        this.annotationHandler.setOperationComparator(Comparator.comparing(KeyTriggerOperation::getSort));
    }

    @Test
    public void resolve() {
        BeanOperations beanOperations = new SimpleBeanOperations(Foo.class);
        annotationHandler.resolve(null, beanOperations);

        Collection<AssembleOperation> operations = beanOperations.getAssembleOperations();
        Assert.assertEquals(2, operations.size());

        AssembleOperation genderOperation = CollectionUtils.get(operations, 0);
        Assert.assertNotNull(genderOperation);
        Assert.assertEquals("gender", genderOperation.getKey());
        Assert.assertEquals("test", genderOperation.getContainer());
        Assert.assertNull(genderOperation.getKeyResolver());
        Assert.assertEquals("", genderOperation.getKeyDescription());
        Assert.assertEquals(Integer.class, genderOperation.getKeyType());
        Set<PropertyMapping> propertyMappings = genderOperation.getPropertyMappings();
        Assert.assertEquals(1, propertyMappings.size());
        PropertyMapping propertyMapping = CollectionUtils.get(propertyMappings, 0);
        Assert.assertNotNull(propertyMapping);
        Assert.assertFalse(propertyMapping.hasSource());
        Assert.assertEquals("value", propertyMapping.getReference());

        AssembleOperation codeOperation = CollectionUtils.get(operations, 1);
        Assert.assertNotNull(codeOperation);
        Assert.assertEquals("code", codeOperation.getKey());
        Assert.assertEquals("test", codeOperation.getContainer());
        Assert.assertNull(codeOperation.getKeyResolver());
        Assert.assertEquals("", codeOperation.getKeyDescription());
        Assert.assertEquals(Integer.class, codeOperation.getKeyType());
        propertyMappings = codeOperation.getPropertyMappings();
        Assert.assertEquals(1, propertyMappings.size());
        propertyMapping = CollectionUtils.get(propertyMappings, 0);
        Assert.assertNotNull(propertyMapping);
        Assert.assertEquals("value", propertyMapping.getSource());
        Assert.assertEquals("value", propertyMapping.getReference());
    }

    @Assemble(key = "gender", container = "test", prop = ":value", sort = 0, keyType = Integer.class)
    @Data
    @RequiredArgsConstructor
    private static class Foo {
        @Assemble(container = "test", prop = "value:value", sort = 1, keyType = Integer.class)
        private Integer code;
        private Integer gender;
        private String value;
    }
}
