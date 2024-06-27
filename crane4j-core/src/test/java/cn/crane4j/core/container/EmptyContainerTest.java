package cn.crane4j.core.container;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * test for {@link EmptyContainer}
 *
 * @author huangchengxing
 */
public class EmptyContainerTest {

    @Test
    public void get() {
        Container<Object> container = Container.empty();
        Assert.assertSame(container, Container.empty());
        Assert.assertEquals(Container.EMPTY_CONTAINER_NAMESPACE, container.getNamespace());
        Assert.assertTrue(container.get(null).isEmpty());

        Assert.assertTrue(container instanceof LimitedContainer);
        LimitedContainer<Object> limitedContainer = (LimitedContainer<Object>)container;
        Assert.assertTrue(limitedContainer.getAll().isEmpty());
        limitedContainer.refresh(Collections.emptyMap());
    }

}
