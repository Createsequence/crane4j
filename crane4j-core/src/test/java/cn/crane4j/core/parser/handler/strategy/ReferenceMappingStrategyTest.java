package cn.crane4j.core.parser.handler.strategy;

import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimplePropertyMapping;
import cn.crane4j.core.parser.operation.SimpleAssembleOperation;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link ReferenceMappingStrategy}
 *
 * @author huangchengxing
 */
public class ReferenceMappingStrategyTest {

    @Test
    public void test() {
        PropertyOperator propertyOperator = new ReflectivePropertyOperator();
        ReferenceMappingStrategy strategy = new ReferenceMappingStrategy(propertyOperator);
        Foo source = new Foo("test");
        Foo target = new Foo();
        PropertyMapping mapping = new SimplePropertyMapping("name", "name");
        strategy.doMapping(
            SimpleAssembleOperation.builder().build(),
            target, source, source.getName(), mapping, t -> target.setName((String)t)
        );
        Assert.assertEquals(source.getName(), target.getName());

        source.setName("test2");
        strategy.doMapping(
            SimpleAssembleOperation.builder().build(),
            target, source, source.getName(), mapping, t -> target.setName((String)t)
        );
        Assert.assertNotEquals(source.getName(), target.getName());
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    private static final class Foo {
        private String name;
    }
}
