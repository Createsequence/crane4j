package cn.crane4j.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Objects;

/**
 * test for {@link EmptyMultiMap}
 *
 * @author huangchengxing
 */
public class EmptyMultiMapTest {

    @Test
    public void test() {
        MultiMap<Object, Objects> emptyMultiMap = MultiMap.emptyMultiMap();
        Assert.assertTrue(emptyMultiMap.isEmpty());
        Assert.assertEquals(0, emptyMultiMap.size());
        Assert.assertEquals(Collections.emptyList(), emptyMultiMap.get(null));
        Assert.assertFalse(emptyMultiMap.containsKey(null));

        Assert.assertEquals(Collections.emptySet(), emptyMultiMap.keySet());
        Assert.assertEquals(Collections.emptyMap(), emptyMultiMap.asMap());
        Assert.assertEquals(Collections.emptyList(), emptyMultiMap.values());
        Assert.assertEquals(Collections.emptyList(), emptyMultiMap.entries());

        Assert.assertThrows(UnsupportedOperationException.class, () -> emptyMultiMap.put(null, null));
        Assert.assertThrows(UnsupportedOperationException.class, () -> emptyMultiMap.removeAll(null));
        Assert.assertThrows(UnsupportedOperationException.class, emptyMultiMap::clear);
        Assert.assertThrows(UnsupportedOperationException.class, () -> emptyMultiMap.putAll(emptyMultiMap));
        Assert.assertThrows(UnsupportedOperationException.class, () -> emptyMultiMap.putAll(null, Collections.emptyList()));
    }
}
