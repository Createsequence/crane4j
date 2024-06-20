package cn.crane4j.core.container;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link FilterableKeyContainer}
 *
 * @author huangchengxing
 */
public class FilterableKeyContainerTest {

    private static final Map<String, Object> NULL_MAP = Collections.singletonMap(null, null);

    @Test
    public void test() {
        Container<String> container = Containers.forLambda("test", ks -> Objects.isNull(ks) ?
            NULL_MAP : ks.stream().collect(Collectors.toMap(Function.identity(), Function.identity())));

        FilterableKeyContainer<String> wrapper = new FilterableKeyContainer<>(container);
        Assert.assertSame(container, wrapper.getContainer());
        Assert.assertSame(NULL_MAP, container.get(null));

        wrapper.setFilterNullKey(true);
        Assert.assertSame(NULL_MAP, container.get(null));
        Assert.assertEquals(1, wrapper.get(Arrays.asList(null, "test1", null)).size());
        Assert.assertTrue(wrapper.get(Arrays.asList(null, null, null)).isEmpty());

        wrapper.setSkipQueryIfKeyCollIsEmpty(true);
        Assert.assertSame(Collections.emptyMap(), wrapper.get(null));
        Assert.assertSame(Collections.emptyMap(), wrapper.get(Arrays.asList(null, null, null)));
    }
}
