package cn.crane4j.spring.boot.example.qucikstart;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.support.Crane4jTemplate;
import cn.hutool.core.map.MapBuilder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author huangchengxing
 */
@Configuration
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {QuickStartWithSpringBootTest.Service.class})
public class QuickStartWithSpringBootTest {

    @Autowired
    private Crane4jTemplate crane4jTemplate;
    @Autowired
    private Service service;

    @Test
    public void run() {
        // 创建并注册数据源
        Map<Integer, String> map = MapBuilder.<Integer, String>create()
            .put(1, "a").put(2, "b").put(3, "c")
            .build();
        crane4jTemplate.opsForContainer()
            .registerMapContainer("test", map);

        // 执行填充
        List<Foo> foos = Arrays.asList(new Foo(1), new Foo(2), new Foo(3));
        crane4jTemplate.execute(foos);
        System.out.println(foos);

        foos = service.getFoos();
        System.out.println(foos);
    }

    @Component
    public static class Service {
        @AutoOperate(type = Foo.class)
        public List<Foo> getFoos() {
            return Arrays.asList(new Foo(1), new Foo(2), new Foo(3));
        }
    }

    @Data  // 使用 lombok 生成构造器、getter/setter 方法
    @RequiredArgsConstructor
    public static class Foo {
        // 根据 id 填充 name
        @Assemble(container = "test", props = @Mapping(ref = "name"))
        private final Integer id;
        private String name;
    }
}
