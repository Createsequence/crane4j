package cn.crane4j.core.support.aop;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.util.ConfigurationUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link AutoOperateProxy}
 *
 * @author huangchengxing
 */
public class AutoOperateProxyTest {

    private AutoOperateProxy autoOperateProxy;

    @Before
    public void init() {
        SimpleCrane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        configuration.registerContainer(Containers.forLambda(
            "test", ids -> ids.stream().map(id -> {
                Map<String, Object> r = new HashMap<>();
                r.put("id", id);
                r.put("name", "name" + id);
                return r;
            }).collect(Collectors.toMap(r -> r.get("id"), Function.identity()))
        ));
        autoOperateProxy = ConfigurationUtil.createAutoOperateProxy(configuration);
    }

    @Test
    public void testAutoOperateResult() {
        // 若没有任何需要代理的方法，则直接返回原对象
        NoneProxy noneProxy = new NoneProxy();
        Assert.assertSame(noneProxy, autoOperateProxy.wrapIfNecessary(noneProxy));

        // 若有需要代理的方法，则返回代理对象
        AutoOperateResult autoOperateResult = new AutoOperateResult();
        AutoOperateResult proxy = autoOperateProxy.wrapIfNecessary(autoOperateResult);
        Assert.assertNotSame(autoOperateResult, proxy);

        // 测试代理对象的方法
        Map<String, Object> result1 = proxy.test1(1);
        Assert.assertEquals(1, result1.get("id"));
        Assert.assertEquals("name1", result1.get("name"));

        // 私有方法不会被代理
        Map<String, Object> result2 = proxy.test2(2);
        Assert.assertEquals(2, result2.get("id"));
        Assert.assertNull(result2.get("name"));

        // 不需要代理的方法不会被代理
        Map<String, Object> result3 = proxy.test3(3);
        Assert.assertEquals(3, result3.get("id"));
        Assert.assertNull(result3.get("name"));
    }

    @Test
    public void autoOperateParameter() {
        AutoOperateParameter autoOperateParameter = new AutoOperateParameter();
        AutoOperateParameter proxy = autoOperateProxy.wrapIfNecessary(autoOperateParameter);
        Assert.assertNotSame(autoOperateParameter, proxy);
        Map<String, Object> r = new HashMap<>();
        r.put("id", 1);
        Map<String, Object> result = proxy.test(r);
        Assert.assertEquals(1, result.get("id"));
        Assert.assertEquals("name1", result.get("name"));
    }

    @Test
    public void autoOperateParameterAndResult() {
        AutoOperateParameterAndResult autoOperateParameterAndResult = new AutoOperateParameterAndResult();
        AutoOperateParameterAndResult proxy = autoOperateProxy.wrapIfNecessary(autoOperateParameterAndResult);
        Assert.assertNotSame(autoOperateParameterAndResult, proxy);
        Map<String, Object> r = new HashMap<>();
        r.put("code", 1);
        r.put("id", 1);
        Map<String, Object> result = proxy.test(r);
        Assert.assertEquals(1, result.get("id"));
        Assert.assertEquals("name1", result.get("name"));
        Assert.assertEquals(1, result.get("code"));
        Assert.assertEquals("name1", result.get("value"));
    }

    public static class NoneProxy {

    }

    public static class AutoOperateResult {
        @Assemble(key = "id", container = "test", prop = "name")
        @AutoOperate(type = Map.class, resolveOperationsFromCurrentElement = true)
        public Map<String, Object> test1(Integer id) {
            Map<String, Object> r = new HashMap<>();
            r.put("id", id);
            return r;
        }
        @Assemble(container = "test", prop = "name")
        @AutoOperate(type = Map.class, resolveOperationsFromCurrentElement = true)
        private Map<String, Object> test2(Integer id) {
            Map<String, Object> r = new HashMap<>();
            r.put("id", id);
            return r;
        }
        public Map<String, Object> test3(Integer id) {
            Map<String, Object> r = new HashMap<>();
            r.put("id", id);
            return r;
        }
    }

    public static class AutoOperateParameter {
        @ArgAutoOperate({@AutoOperate(value = "arg0", type = Map.class, resolveOperationsFromCurrentElement = true)})
        public Map<String, Object> test(@Assemble(key = "id", container = "test", prop = "name") Map<String, Object> r) {
            return r;
        }
    }

    public static class AutoOperateParameterAndResult {
        @Assemble(key = "id", container = "test", prop = "name")
        @AutoOperate(type = Map.class, resolveOperationsFromCurrentElement = true)
        public Map<String, Object> test(
            @Assemble(key = "code", container = "test", prop = "name:value")
            @AutoOperate(value = "arg0", type = Map.class, resolveOperationsFromCurrentElement = true) Map<String, Object> r) {
            return r;
        }
    }
}
