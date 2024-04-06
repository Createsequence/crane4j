package cn.crane4j.core.executor.handler.key;

import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link IntrospectionKeyResolver}
 *
 * @author huangchengxing
 */
public class IntrospectionKeyResolverTest {

    @Test
    public void test() {
        Object object = new Object();
        IntrospectionKeyResolver resolver = IntrospectionKeyResolver.INSTANCE;
        Assert.assertSame(object, resolver.resolve(object, null));
    }
}
