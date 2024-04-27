package cn.crane4j.core.support.reflect;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.converter.SimpleConverterManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link AsmReflectivePropertyOperator}
 *
 * @author huangchengxing
 */
public class AsmReflectivePropertyOperatorTest {

    private final AsmReflectivePropertyOperator operator = new AsmReflectivePropertyOperator(new SimpleConverterManager());

    @Test
    public void readProperty() {
        Foo foo = new Foo(12, true, "intact", "open", "shade");
        Foo.shared = "shared";
        Assert.assertNull(operator.readProperty(Foo.class, foo, "noneField"));
        Assert.assertEquals(12, operator.readProperty(Foo.class, foo, "id"));
        Assert.assertEquals(true, operator.readProperty(Foo.class, foo, "flag"));
        Assert.assertEquals("intact", operator.readProperty(Foo.class, foo, "intact"));
        Assert.assertEquals("open", operator.readProperty(Foo.class, foo, "open"));
        Assert.assertEquals("shade", operator.readProperty(Foo.class, foo, "shade"));
        Assert.assertEquals("shared", operator.readProperty(Foo.class, foo, "shared"));
    }

    @Test
    public void findGetter() {
        Assert.assertNotNull(operator.findGetter(Foo.class, "id"));
        Assert.assertNotNull(operator.findGetter(Foo.class, "open"));
        Assert.assertNotNull(operator.findGetter(Foo.class, "shared"));
        Assert.assertNull(operator.findGetter(Foo.class, "none"));

        operator.setThrowIfNoAnyMatched(true);
        Assert.assertThrows(Crane4jException.class, () -> operator.findGetter(Foo.class, "none"));
        operator.setThrowIfNoAnyMatched(false);
    }

    @Test
    public void writeProperty() {
        Foo foo = new Foo(1, true, "intact", "open", "shade");
        operator.writeProperty(Foo.class, foo, "noneField", null);
        operator.writeProperty(Foo.class, foo, "id", 2);
        Assert.assertEquals((Integer)2, foo.getId());
        operator.writeProperty(Foo.class, foo, "flag", false);
        Assert.assertFalse(foo.flag);
        operator.writeProperty(Foo.class, foo, "intact", "other");
        Assert.assertEquals("other", foo.getIntact());
        operator.writeProperty(Foo.class, foo, "shade", "another");
        Assert.assertEquals("another", foo.getShade());
        operator.writeProperty(Foo.class, foo, "open", "closed");
        Assert.assertEquals("closed", foo.open);
        operator.writeProperty(Foo.class, foo, "shared", "changed");
        Assert.assertEquals("changed", Foo.shared);

    }

    @Test
    public void findSetter() {
        Assert.assertNotNull(operator.findSetter(Foo.class, "id"));
        Assert.assertNotNull(operator.findSetter(Foo.class, "open"));
        Assert.assertNotNull(operator.findSetter(Foo.class, "shared"));
        Assert.assertNull(operator.findSetter(Foo.class, "none"));

        operator.setThrowIfNoAnyMatched(true);
        Assert.assertThrows(Crane4jException.class, () -> operator.findSetter(Foo.class, "none"));
        operator.setThrowIfNoAnyMatched(false);
    }

    @AllArgsConstructor
    protected static class Foo {

        @Getter
        private Integer id;

        @Setter
        private boolean flag;

        @Setter
        @Getter
        private String intact;

        public String open;

        private String _shade;

        public static String shared;

        public String getShade() {
            return _shade;
        }

        public void setShade(String shade) {
            _shade = shade;
        }
    }

}
