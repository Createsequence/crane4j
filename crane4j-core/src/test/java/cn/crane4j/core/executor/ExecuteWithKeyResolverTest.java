package cn.crane4j.core.executor;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.executor.handler.key.KeyResolver;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * test for {@link KeyResolver}
 *
 * @author huangchengxing
 */
public class ExecuteWithKeyResolverTest extends BaseExecutorTest {

    @Test
    public void test() {
        BeanOperationExecutor executor = new DisorderedBeanOperationExecutor(configuration);
        List<Object> beanKeys = new ArrayList<>();
        Container<Object> container = Containers.forLambda("test", keys -> {
            beanKeys.addAll(keys);
            return Collections.emptyMap();
        });
        configuration.registerContainer(container);

        Source source1 = new Source(1, "one");
        Source source2 = new Source(2, "two");
        executor.execute(Arrays.asList(source1, source2), parseOperations(Source.class));
        Assert.assertEquals(2, beanKeys.size());
        List<Target> targets = beanKeys.stream()
            .map(Target.class::cast)
            .sorted(Comparator.comparing(Target::getProp1))
            .collect(Collectors.toList());

        Target target1 = targets.get(0);
        Assert.assertNotNull(target1);
        Assert.assertEquals(source1.getId(), target1.getProp1());
        Assert.assertEquals(source1.getName(), target1.getProp2());

        Target target2 = targets.get(1);
        Assert.assertNotNull(target2);
        Assert.assertEquals(source2.getId(), target2.getProp1());
        Assert.assertEquals(source2.getName(), target2.getProp2());
    }

    @Assemble(
        container = "test",
        keyDesc = "id:prop1, name:prop2",
        keyType = Target.class
    )
    @AllArgsConstructor
    @Data
    private static class Source {
        private Integer id;
        private String name;
    }

    @Data
    private static class Target {
        private Integer prop1;
        private String prop2;
    }
}
