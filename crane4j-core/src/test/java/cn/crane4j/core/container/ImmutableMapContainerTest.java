package cn.crane4j.core.container;

import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * test for {@link ImmutableMapContainer}
 *
 * @author tangcent
 */
public class ImmutableMapContainerTest {

    @Test
    public void forMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("1", 1);
        map.put("2", 2);
        LimitedContainer<String> container = ImmutableMapContainer.forMap("test", map);
        assertEquals("test", container.getNamespace());
        assertEquals(map, container.get(Arrays.asList("1", "2")));
        assertEquals(map, container.getAll());
        //always return all data
        assertEquals(map, container.get(Collections.singletonList("1")));

        container.refresh(Collections.emptyMap());
        Assert.assertNotSame(map, container.getAll());
        Assert.assertTrue(container.getAll().isEmpty());
    }

    @Test
    public void destroy() {
        // map is modifiable
        Map<String, Object> map = new HashMap<>();
        map.put("1", new Object());
        ImmutableMapContainer.forMap("test", map).destroy();
        Assert.assertTrue(map.isEmpty());

        // map is unmodifiable
        map = new HashMap<>();
        map.put("1", new Object());
        map = Collections.unmodifiableMap(map);
        ImmutableMapContainer.forMap("test", map).destroy();
        Assert.assertFalse(map.isEmpty());
    }
}