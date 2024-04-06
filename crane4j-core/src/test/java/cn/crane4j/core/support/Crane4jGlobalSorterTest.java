package cn.crane4j.core.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link Crane4jGlobalSorter}
 *
 * @author huangchengxing
 */
public class Crane4jGlobalSorterTest {
    @SuppressWarnings("all")
    @Test
    public void test() {
        // test value extractor
        Crane4jGlobalSorter sorter = Crane4jGlobalSorter.INSTANCE;

        // get sort value
        SortedBean sortedBean = new SortedBean(1);
        Assert.assertEquals(1, sorter.getSortValue(sortedBean, -1));
        OrderedBean orderedBean = new OrderedBean(2);
        Assert.assertEquals(-1, sorter.getSortValue(orderedBean, -1));

        // add extractor
        sorter.addCompareValueExtractor(t -> (t instanceof OrderedBean) ? ((OrderedBean)t).getOrder() : null);
        Assert.assertEquals(2, sorter.getSortValue(orderedBean, -1));

        // compare
        Assert.assertEquals(0, sorter.compare(sortedBean, sortedBean));
        Assert.assertEquals(-1, sorter.compare(sortedBean, orderedBean));
        Assert.assertEquals(1, sorter.compare(orderedBean, sortedBean));
        Assert.assertEquals(0, sortedBean.compareTo(sortedBean));
    }

    @Getter
    @RequiredArgsConstructor
    private static class SortedBean implements Sorted {
        private final int sort;
    }

    @Getter
    @RequiredArgsConstructor
    private static class OrderedBean {
        private final Integer order;
    }
}
