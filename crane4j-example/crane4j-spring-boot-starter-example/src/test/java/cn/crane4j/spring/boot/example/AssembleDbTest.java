package cn.crane4j.spring.boot.example;

import cn.crane4j.annotation.AssembleDb;
import cn.crane4j.core.support.Crane4jTemplate;
import cn.crane4j.extension.query.AssembleDbAnnotationHandler;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

/**
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    Crane4jSpringBootStarterExampleApplication.class,
    TestDataSourceConfig.class,
})
public class AssembleDbTest {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Crane4jTemplate crane4jTemplate;

    @Test
    public void test() {
        Assert.assertFalse(applicationContext.getBeansOfType(AssembleDbAnnotationHandler.class).isEmpty());
        crane4jTemplate.opsForComponent()
            .configureOperationAnnotationHandler(AssembleDbAnnotationHandler.class, (c, h) -> {
                Assert.assertNotNull(h);
            });

        Foo foo = new Foo(1);
        crane4jTemplate.execute(Collections.singletonList(foo));
        Assert.assertEquals("小明", foo.getName());
        Assert.assertEquals(18, foo.getAge().intValue());
        Assert.assertEquals(1, foo.getSex().intValue());
    }

    @Data
    @RequiredArgsConstructor
    public static class Foo {
        @AssembleDb(
            sort = 2,
            selectColumns = {"name", "age", "sex"}, fromTable = "foo", whereColumn = "id",
            prop = {"name", "age", "sex"}
        )
        private final Integer id;
        private String name;
        private Integer age;
        private Integer sex;
    }
}
