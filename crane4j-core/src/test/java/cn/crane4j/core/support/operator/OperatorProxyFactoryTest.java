package cn.crane4j.core.support.operator;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.Operator;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.converter.SimpleConverterManager;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * test for {@link OperatorProxyFactory}.
 *
 * @author huangchengxing
 */
public class OperatorProxyFactoryTest {

    private OperatorProxyFactory operatorProxyFactory;

    @Before
    public void init() {
        Crane4jGlobalConfiguration globalConfiguration = SimpleCrane4jGlobalConfiguration.create();
        AnnotationFinder annotationFinder = new SimpleAnnotationFinder();
        this.operatorProxyFactory = OperatorProxyFactory.builder()
            .globalConfiguration(globalConfiguration)
            .annotationFinder(annotationFinder)
            .build();
        this.operatorProxyFactory.addProxyMethodFactory(new OperationAnnotationProxyMethodFactory(new SimpleConverterManager()));

        LambdaContainer<Integer> container = LambdaContainer.forLambda(
            "test", ids -> ids.stream().collect(Collectors.toMap(id -> id, id -> "name" + id))
        );
        globalConfiguration.registerContainer(container);
    }

    @Test
    public void checkInvalidOperator() {
        Assert.assertThrows(Crane4jException.class, () -> operatorProxyFactory.get(OperatorInterface2.class));
        Assert.assertThrows(Crane4jException.class, () -> operatorProxyFactory.get(OperatorInterface3.class));
        Assert.assertNull(operatorProxyFactory.get(OperatorInterface4.class));
    }

    @Test
    public void get() {
        OperatorInterface operator = operatorProxyFactory.get(OperatorInterface.class);
        Assert.assertNotNull(operator);

        Collection<Map<String, Object>> targets = IntStream.rangeClosed(0, 5).mapToObj(id -> {
            Map<String, Object> target = new HashMap<>();
            target.put("id", id);
            return target;
        }).collect(Collectors.toList());

        operator.fill(null);
        operator.fill(targets);
        targets.forEach(target -> assertEquals("name" + target.get("id"), target.get("name")));
    }

    @Test
    public void invokeObjectMethod() {
        OperatorInterface operator1 = operatorProxyFactory.get(OperatorInterface.class);
        Assert.assertNotNull(operator1);
        OperatorInterface operator2 = operatorProxyFactory.get(OperatorInterface.class);
        Assert.assertNotNull(operator2);
        Assert.assertEquals(operator1, operator2);
        Assert.assertEquals(operator1, Proxy.getInvocationHandler(operator2));
        Assert.assertEquals(operator1, Proxy.getInvocationHandler(operator1));
        Assert.assertNotEquals(operator1, null);
        Assert.assertEquals(operator1.hashCode(), operator2.hashCode());
        Assert.assertEquals(operator1.toString(), operator2.toString());
    }

    @Operator
    private interface OperatorInterface {
        @Assemble(key = "id", container = "test", props = @Mapping(ref = "name"))
        void fill(Collection<Map<String, Object>> targets);
    }

    @Operator
    private interface OperatorInterface2 extends OperatorInterface {
        void noneAnnotatedMethod(Collection<Map<String, Object>> targets);
    }

    @Operator
    private interface OperatorInterface3 extends OperatorInterface {
        @Assemble(key = "id", container = "test", props = @Mapping(ref = "name"))
        void noneArgMethod();
    }

    private interface OperatorInterface4 extends OperatorInterface {
    }
}
