package cn.crane4j.core.parser.handler.strategy;

import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimplePropertyMapping;
import cn.crane4j.core.parser.operation.SimpleAssembleOperation;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * test for {@link CollJoinAsStringMappingStrategy}
 *
 * @author huangchengxing
 */
public class CollJoinAsStringMappingStrategyTest {

    @Test
    public void test() {
        CollJoinAsStringMappingStrategy strategy = CollJoinAsStringMappingStrategy.INSTANCE;
        PropertyMapping mapping = new SimplePropertyMapping("src", "ref");
        strategy.doMapping(
            SimpleAssembleOperation.builder().keyDescription(" | ").build(),
            new Object(), new Object(), Arrays.asList(1, 2, 3),
            mapping,
            values -> Assert.assertEquals(values, "1 | 2 | 3")
        );
    }
}
