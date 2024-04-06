package cn.crane4j.core.executor.handler.key;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.SimpleAssembleOperation;
import cn.crane4j.core.support.converter.SimpleConverterManager;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link ReflectiveAliasPropertyKeyResolver}
 *
 * @author huangchengxing
 */
public class ReflectiveAliasPropertyKeyResolverTest {

    @Test
    public void test() {
        ReflectiveAliasPropertyKeyResolver resolver = new ReflectiveAliasPropertyKeyResolver(ReflectivePropertyOperator.INSTANCE, SimpleConverterManager.INSTANCE);
        AssembleOperation operation = SimpleAssembleOperation.builder()
            .keyDescription("id, userId, userKey")
            .keyResolver(resolver)
            .build();
        resolver.setAssembleOperation(operation);

        Foo foo = new Foo();
        foo.setUserKey("test1");
        Assert.assertEquals("test1", resolver.resolve(foo, operation));
        foo.setUserId("test2");
        Assert.assertEquals("test2", resolver.resolve(foo, operation));
        foo.setId("test3");
        Assert.assertEquals("test3", resolver.resolve(foo, operation));
    }


    @Data
    private static class Foo {
        @Assemble(
            container = "user",
            props = @Mapping("name"),
            keyResolver = ReflectiveAliasPropertyKeyResolver.class, // 指定键值解析器
            keyDesc = "id, userId, userKey" // 指定关联三个属性
        )
        private String id;
        private String userId;
        private String userKey;
        private String name;
    }
}
