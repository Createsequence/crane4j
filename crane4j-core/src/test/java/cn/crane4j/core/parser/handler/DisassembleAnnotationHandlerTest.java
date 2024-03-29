package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.Disassemble;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.DisassembleOperation;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.util.CollectionUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

/**
 * test for {@link DisassembleAnnotationHandler}
 *
 * @author huangchengxing
 */
public class DisassembleAnnotationHandlerTest {

    private Crane4jGlobalConfiguration configuration;

    @Before
    public void init() {
        configuration = SimpleCrane4jGlobalConfiguration.create();
    }

    @Test
    public void resolve() {
        BeanOperationParser parser = configuration.getBeanOperationsParser(BeanOperationParser.class);
        BeanOperations beanOperations = parser.parse(Foo.class);

        Collection<DisassembleOperation> operations = beanOperations.getDisassembleOperations();
        Assert.assertEquals(2, operations.size());

        DisassembleOperation nested1Operation = CollectionUtils.get(operations, 0);
        Assert.assertNotNull(nested1Operation);
        Assert.assertEquals("nested1", nested1Operation.getKey());
        Assert.assertEquals(0, nested1Operation.getSort());
        Assert.assertEquals(Foo.class, nested1Operation.getSourceType());
        Assert.assertSame(nested1Operation.getInternalBeanOperations(new Foo()), beanOperations);

        DisassembleOperation nested2Operation = CollectionUtils.get(operations, 1);
        Assert.assertNotNull(nested2Operation);
        Assert.assertEquals("nested2", nested2Operation.getKey());
        Assert.assertEquals(1, nested2Operation.getSort());
        Assert.assertEquals(Foo.class, nested2Operation.getSourceType());
        Assert.assertSame(nested2Operation.getInternalBeanOperations(new Foo()), beanOperations);
    }

    @Disassemble(key = "nested1", sort = 0, type = Foo.class)
    @Data
    @RequiredArgsConstructor
    private static class Foo {
        private Foo nested1;
        @Disassemble(sort = 1)
        private Foo nested2;
    }
}
