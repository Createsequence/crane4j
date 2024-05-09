package cn.crane4j.benchmark;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.OperateTemplate;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author huangchengxing
 */
public class PerformanceAnalysis {

    private static final int size = 100000;
    private static OperateTemplate operateTemplate;

    public static void main(String[] args) {
        init();
        while (true) {
            List<Foo> target = IntStream.rangeClosed(1, size)
                .mapToObj(Foo::new)
                .collect(Collectors.toList());
            operateTemplate.execute(target);
        }
    }

    public static void init() {
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        operateTemplate = new OperateTemplate(
            configuration.getBeanOperationsParser(BeanOperationParser.class),
            configuration.getBeanOperationExecutor(BeanOperationExecutor.class),
            configuration.getTypeResolver()
        );

        // 初始化数据源
        Container<Integer> container = Containers.forLambda("test", ids -> ids.stream()
            .map(id -> new Foo(id, "name" + id, id % 100))
            .collect(Collectors.toMap(Foo::getId, Function.identity())));
        configuration.registerContainer(container);
    }

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class Foo {
        @Assemble(container = "test", props = {
            @Mapping("name"), @Mapping("age")
        })
        private final Integer id;
        private String name;
        private Integer age;
    }

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class Nested {

        @Assemble(container = "test", props = {
            @Mapping("name"), @Mapping("age")
        })
        private final Integer id;
        private String name;
        private Integer age;

        @Disassemble(type = Foo.class)
        private final Foo foo;
    }
}
