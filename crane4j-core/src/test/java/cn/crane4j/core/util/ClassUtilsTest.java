package cn.crane4j.core.util;

import cn.crane4j.core.exception.Crane4jException;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link ClassUtils}
 *
 * @author huangchengxing
 */
public class ClassUtilsTest {

    @Test
    public void isPrimitiveTypeOrWrapperType() {
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Boolean.TYPE));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Boolean.class));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Byte.TYPE));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Byte.class));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Character.TYPE));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Character.class));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Double.TYPE));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Double.class));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Float.TYPE));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Float.class));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Integer.TYPE));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Integer.class));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Long.TYPE));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Long.class));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Short.TYPE));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Short.class));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Void.TYPE));
        Assert.assertTrue(ClassUtils.isPrimitiveTypeOrWrapperType(Void.class));
        Assert.assertFalse(ClassUtils.isPrimitiveTypeOrWrapperType(String.class));
        Assert.assertFalse(ClassUtils.isPrimitiveTypeOrWrapperType(ClassUtilsTest.class));
    }

    @Test
    public void testIsObjectOrVoid() {
        Assert.assertFalse(ClassUtils.isObjectOrVoid(String.class));
        Assert.assertTrue(ClassUtils.isObjectOrVoid(Object.class));
        Assert.assertTrue(ClassUtils.isObjectOrVoid(Void.TYPE));
        Assert.assertTrue(ClassUtils.isObjectOrVoid(void.class));
    }

    @Test
    public void isJdkClass() {
        Assert.assertTrue(ClassUtils.isJdkClass(String.class));
        Assert.assertFalse(ClassUtils.isJdkClass(Nullable.class));
        Assert.assertFalse(ClassUtils.isJdkClass(ClassUtilsTest.class));
        Assert.assertThrows(NullPointerException.class, () -> ClassUtils.isJdkClass(null));
    }

    @Test
    public void forName() {
        Assert.assertEquals(String.class, ClassUtils.forName("java.lang.String"));
        Assert.assertEquals(ClassUtilsTest.class, ClassUtils.forName("cn.crane4j.core.util.ClassUtilsTest"));
        Assert.assertThrows(Crane4jException.class, () -> ClassUtils.forName("not.found.class"));
        Assert.assertThrows(NullPointerException.class, () -> ClassUtils.forName(null));

        Assert.assertEquals(ClassUtilsTest.class, ClassUtils.forName("", ClassUtilsTest.class));
        Assert.assertEquals(String.class, ClassUtils.forName("java.lang.String", ClassUtilsTest.class));
        Assert.assertThrows(Crane4jException.class, () -> ClassUtils.forName("not.found.class", ClassUtilsTest.class));
    }

    @Test
    public void newInstance() {
        Object object = ClassUtils.newInstance(String.class);
        Assert.assertNotNull(object);
        // if class has no default constructor, it will throw exception
        Assert.assertThrows(Crane4jException.class, () -> ClassUtils.newInstance(Foo.class));
    }

    @Test
    public void isInstantiable() {
        Assert.assertFalse(
            ClassUtils.isInstantiable(Foo.class, Assert::assertNotNull)
        );
        Assert.assertTrue(ClassUtils.isInstantiable(Foo2.class, null));
    }

    @Test
    public void packageToPath() {
        Assert.assertEquals("cn/crane4j/core/util/ClassUtils", ClassUtils.packageToPath("cn.crane4j.core.util.ClassUtils"));
        Assert.assertThrows(NullPointerException.class, () -> ClassUtils.packageToPath(null));
    }

    @SuppressWarnings("unused")
    @RequiredArgsConstructor
    private static class Foo {
        private final String name;
    }

    @SuppressWarnings("unused")
    @NoArgsConstructor
    private static class Foo2 {
    }
}
