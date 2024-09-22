package cn.crane4j.spring.boot.example;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.AssembleEnum;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.spring.boot.config.Crane4jAutoConfiguration;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 示例如何使用{@link AutoOperate}以及{@link ArgAutoOperate}使用自动填充
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Crane4jSpringBootStarterExampleApplication.class, EnumExampleTest.ServiceImpl.class, Crane4jAutoConfiguration.class})
@TestPropertySource(properties = {
        "crane4j.enable-method-result-auto-operate=true",
        "crane4j.enable-method-argument-auto-operate=true",
})
@EnableAspectJAutoProxy
public class EnumExampleTest {

    @Autowired
    private ServiceImpl service;

    @Test
    public void test() {
        List<Student> students = service.getStudents(Arrays.asList(1, 2));
        System.out.println(students);
    }

    @Component
    public static class ServiceImpl { // 你的 service 接口，确保 Spring 可以代理它

        @AutoOperate(type = Student.class) // 声明自动填充该方法的返回值
        public List<Student> getStudents(Collection<Integer> ids) {
            return ids.stream() // 模拟返回数据
                .map(id -> {
                    int genderCode = id % 2; // 随机设置一个性别编码
                    return new Student().setGenderCode(genderCode);
                })
                .collect(Collectors.toList());
        }
    }

    @Accessors(chain = true)
    @Data
    public static class Student {

        @AssembleEnum(
            type = Gender.class, // 指定数据源为 Gender 枚举类
            props = @Mapping(ref = "genderName") // 将 value 映射到 genderName 字段上
        )
        private Integer genderCode;
        private String genderName;
    }

    @ContainerEnum(
        key = "code", // key 为 Gender.code 字段值
        value = "value" // value 为 Gender.value 字段值
    )
    @Getter
    @RequiredArgsConstructor
    public enum Gender {
        FEMALE(0, "女"),
        MALE(1, "男");

        private final Integer code;
        private final String value;
    }
}
