package cn.crane4j.core.executor.handler.key;

import cn.crane4j.core.executor.handler.OneToOneAssembleOperationHandler;
import cn.crane4j.core.parser.operation.SimpleAssembleOperation;
import cn.crane4j.core.support.converter.SimpleConverterManager;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * test for {@link ReflectivePropertyKeyResolver}
 *
 * @author huangchengxing
 * @since 2.7.0
 */
public class ReflectivePropertyKeyResolverProviderTest {

    private OneToOneAssembleOperationHandler handler;

    @Before
    public void inti() {
        this.handler = new OneToOneAssembleOperationHandler(
            ReflectivePropertyOperator.INSTANCE, SimpleConverterManager.INSTANCE
        );
    }

    @Test
    public void test() {
        SimpleAssembleOperation operation = SimpleAssembleOperation.builder()
            .key("key")
            .build();
        Foo foo = new Foo("test");
        KeyResolver resolver = handler.determineKeyResolver(operation);
        Assert.assertNotNull(resolver);
        Object key = resolver.resolve(foo, operation);
        Assert.assertEquals(foo.getKey(), key);

        operation = SimpleAssembleOperation.builder()
            .key("key")
            .keyType(String.class)
            .build();
        resolver = handler.determineKeyResolver(operation);
        Assert.assertNotNull(resolver);
        key = resolver.resolve(foo, operation);
        Assert.assertEquals(foo.getKey(), key);
    }

    @AllArgsConstructor
    @Data
    private static class Foo {
        private String key;
    }
}
