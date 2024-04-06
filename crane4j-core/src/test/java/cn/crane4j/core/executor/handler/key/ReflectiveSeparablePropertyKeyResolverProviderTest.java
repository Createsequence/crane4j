package cn.crane4j.core.executor.handler.key;

import cn.crane4j.core.executor.handler.ManyToManyAssembleOperationHandler;
import cn.crane4j.core.parser.operation.SimpleAssembleOperation;
import cn.crane4j.core.support.converter.SimpleConverterManager;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * test for {@link ReflectiveSeparablePropertyKeyResolver}
 *
 * @author huangchengxing
 * @since 2.7.0
 */
public class ReflectiveSeparablePropertyKeyResolverProviderTest {

    private ManyToManyAssembleOperationHandler handler;

    @Before
    public void init() {
        this.handler = new ManyToManyAssembleOperationHandler(
            ReflectivePropertyOperator.INSTANCE, SimpleConverterManager.INSTANCE
        );
    }

    @Test
    public void testIfNotNullOrEmpty() {
        String[] array = new String[] {"1", "2", "3"};
        List<Object> coll = Arrays.asList(4, 5, "6");
        String str = "7, 8, 9";
        String singleStr = "10";
        Object target = 10;
        Source source = new Source(array, coll, str, target);

        // array
        SimpleAssembleOperation operation = SimpleAssembleOperation.builder()
            .key("idArray")
            .keyDescription(", ")
            .build();
        KeyResolver resolver = handler.determineKeyResolver(operation);
        Assert.assertNotNull(resolver);
        Object key = resolver.resolve(source, operation);
        Assert.assertEquals(Arrays.asList(array), key);

        // collection
        operation = SimpleAssembleOperation.builder()
            .key("idColl")
            .keyType(String.class)
            .keyDescription(", ")
            .build();
        resolver = handler.determineKeyResolver(operation);
        Assert.assertNotNull(resolver);
        key = resolver.resolve(source, operation);
        Assert.assertTrue(key instanceof Collection);
        Assert.assertEquals(coll, key);

        // string
        operation = SimpleAssembleOperation.builder()
            .key("idStr")
            .keyDescription(", ")
            .build();
        resolver = handler.determineKeyResolver(operation);
        Assert.assertNotNull(resolver);
        key = resolver.resolve(source, operation);
        Assert.assertEquals(Arrays.asList("7", "8", "9"), key);

        // single string
        operation = SimpleAssembleOperation.builder()
            .key("idStr")
            .keyType(String.class)
            .build();
        resolver = handler.determineKeyResolver(operation);
        Assert.assertNotNull(resolver);
        source.setIdStr(singleStr);
        key = resolver.resolve(source, operation);
        Assert.assertEquals(Collections.singletonList(singleStr), key);

        // target
        operation = SimpleAssembleOperation.builder()
            .key("idTarget")
            .keyType(String.class)
            .keyDescription(", ")
            .build();
        resolver = handler.determineKeyResolver(operation);
        Assert.assertNotNull(resolver);
        key = resolver.resolve(source, operation);
        Assert.assertEquals(Collections.singletonList(target), key);
    }

    @Test
    public void testNullAndEmpty() {
        Source source = new Source(null, Collections.emptyList(), "", null);

        // array
        SimpleAssembleOperation operation = SimpleAssembleOperation.builder()
            .key("idArray")
            .keyDescription(", ")
            .build();
        KeyResolver resolver = handler.determineKeyResolver(operation);
        Assert.assertNotNull(resolver);
        Object key = resolver.resolve(source, operation);
        Assert.assertTrue(key instanceof Collection);
        Assert.assertTrue(((Collection<?>) key).isEmpty());

        // collection
        operation = SimpleAssembleOperation.builder()
            .key("idColl")
            .keyDescription(", ")
            .build();
        resolver = handler.determineKeyResolver(operation);
        Assert.assertNotNull(resolver);
        key = resolver.resolve(source, operation);
        Assert.assertTrue(key instanceof Collection);
        Assert.assertTrue(((Collection<?>) key).isEmpty());

        // string
        operation = SimpleAssembleOperation.builder()
            .key("idStr")
            .keyDescription(", ")
            .build();
        resolver = handler.determineKeyResolver(operation);
        Assert.assertNotNull(resolver);
        key = resolver.resolve(source, operation);
        Assert.assertTrue(key instanceof Collection);
        Assert.assertTrue(((Collection<?>) key).isEmpty());

        // target
        operation = SimpleAssembleOperation.builder()
            .key("idTarget")
            .keyDescription(", ")
            .build();
        resolver = handler.determineKeyResolver(operation);
        Assert.assertNotNull(resolver);
        key = resolver.resolve(source, operation);
        Assert.assertTrue(key instanceof Collection);
        Assert.assertTrue(((Collection<?>) key).isEmpty());
    }

    @SuppressWarnings("unused")
    @Setter
    @AllArgsConstructor
    private static class Source {
        private String[] idArray;
        private Collection<Object> idColl;
        private String idStr;
        private Object idTarget;
    }
}
