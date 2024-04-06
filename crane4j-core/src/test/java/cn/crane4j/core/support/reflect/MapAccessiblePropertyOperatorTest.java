package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * test for {@link MapAccessiblePropertyOperator}
 *
 * @author huangchengxing
 */
public class MapAccessiblePropertyOperatorTest {

    private PropertyOperator operator;
    private Map<String, Object> map;
    private Foo foo;

    @Before
    public void init() {
        operator = new MapAccessiblePropertyOperator(new ReflectivePropertyOperator(new HutoolConverterManager()));
        map = new HashMap<>();
        map.put("name", "name");
        foo = new Foo(1, false);
    }

    @Test
    public void testGetPropDesc() {
        PropDesc propDesc = operator.getPropertyDescriptor(map.getClass());
        Assert.assertEquals(Map.class, propDesc.getBeanType());
    }

    @Test
    public void readProperty() {
        Assert.assertEquals("name", operator.readProperty(map.getClass(), map, "name"));
        Assert.assertEquals(1, operator.readProperty(foo.getClass(), foo, "id"));
    }

    @Test
    public void findGetter() {
        MethodInvoker getter = operator.findGetter(map.getClass(), "name");
        Assert.assertEquals("name", getter.invoke(map));

        getter = operator.findGetter(foo.getClass(), "id");
        Assert.assertEquals(1, getter.invoke(foo));
    }

    @Test
    public void writeProperty() {
        operator.writeProperty(map.getClass(), map, "age", 18);
        Assert.assertEquals(18, map.get("age"));

        operator.writeProperty(foo.getClass(), foo, "flag", true);
        Assert.assertTrue(foo.isFlag());
    }

    @Test
    public void findSetter() {
        MethodInvoker setter = operator.findSetter(map.getClass(), "age");
        setter.invoke(map, 18);
        Assert.assertEquals(18, map.get("age"));

        setter = operator.findSetter(foo.getClass(), "id");
        setter.invoke(foo, 10);
        Assert.assertEquals((Integer)10, foo.getId());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Foo {
        private Integer id;
        private boolean flag;
    }
}
