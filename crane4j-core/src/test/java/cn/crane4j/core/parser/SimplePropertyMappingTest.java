package cn.crane4j.core.parser;

import cn.crane4j.core.util.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

/**
 * test for {@link SimplePropertyMapping}
 *
 * @author huangchengxing
 */
public class SimplePropertyMappingTest {

    private SimplePropertyMapping mapping;

    @Before
    public void init() {
        mapping = new SimplePropertyMapping("name", "userName");
    }

    @Test
    public void getSource() {
        Assert.assertEquals("name", mapping.getSource());
    }

    @Test
    public void hasSource() {
        Assert.assertTrue(mapping.hasSource());
    }

    @Test
    public void getReference() {
        Assert.assertEquals("userName", mapping.getReference());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("s.name -> t.userName", mapping.toString());
    }

    @Test
    public void from() {
        Assert.assertEquals(Collections.emptySet(), SimplePropertyMapping.from(""));
        Set<PropertyMapping> mappingSet = SimplePropertyMapping.from("a, a:b");
        Assert.assertEquals(2, mappingSet.size());

        PropertyMapping mapping1 = CollectionUtils.get(mappingSet, 0);
        Assert.assertNotNull(mapping1);
        Assert.assertEquals("a", mapping1.getSource());
        Assert.assertEquals("a", mapping1.getReference());

        PropertyMapping mapping2 = CollectionUtils.get(mappingSet, 1);
        Assert.assertNotNull(mapping2);
        Assert.assertEquals("a", mapping2.getSource());
        Assert.assertEquals("b", mapping2.getReference());
    }
}
