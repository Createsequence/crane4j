package cn.crane4j.spring.boot.example;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.ImmutableMapContainer;
import cn.crane4j.core.executor.handler.ManyToManyAssembleOperationHandler;
import cn.crane4j.core.support.OperateTemplate;
import cn.crane4j.extension.spring.Crane4jApplicationContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 演示如何使用属性映射，包括一对一装配情况下的属性映射，以及批量装配情况下的属性映射
 *
 * @author huangchengxing
 * @since 1.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Crane4jSpringBootStarterExampleApplication.class})
public class PropMappingExampleTest {

    @Autowired
    private Crane4jApplicationContext context;
    @Autowired
    private OperateTemplate operateTemplate;

    @Before
    public void init() {
        Map<Integer, Object> sources = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            Map<String, Object> source = new HashMap<>();
            source.put("id", i);
            source.put("name", "student" + i);
            source.put("age", 18 + i);
            sources.put(i, source);
        }
        if (!context.containsContainer("student")) {
            context.registerContainer(ImmutableMapContainer.forMap("student", sources));
        }
    }

    /**
     * 将数据源对象的属性映射到目标对象的属性上
     */
    @Test
    public void testPropsMapping() {
        List<StudentVO> students = getStudentList(3);
        operateTemplate.execute(students, StudentVO.class);
        for (int i = 0; i < students.size(); i++) {
            StudentVO studentVO = students.get(i);
            Assert.assertEquals("student" + i, studentVO.getName());
            Assert.assertEquals((Integer) (18 + i), studentVO.getAge());
            System.out.println(studentVO);
        }
    }

    /**
     * 将数据源对象直接映射到目标对象的属性上
     */
    @Test
    public void testSourceObjectMapping() {
        List<StudentVO> students = getStudentList(3);
        operateTemplate.execute(students, StudentVO.class);
        for (int i = 0; i < students.size(); i++) {
            StudentVO studentVO = students.get(i);
            Map<String, Object> map = studentVO.getSource();
            Assert.assertEquals("student" + i, map.get("name"));
            Assert.assertEquals(18 + i, map.get("age"));
            System.out.println(studentVO);
        }
    }

    /**
     * 将数据源对象的属性映射到目标对象的key上
     */
    @Test
    public void testKeyMapping() {
        List<StudentVO> students = getStudentList(3);
        operateTemplate.execute(students, StudentVO.class);
        for (StudentVO studentVO : students) {
            Assert.assertEquals(studentVO.getKey(), studentVO.getAge());
            System.out.println(studentVO);
        }
    }

    /**
     * 批量映射将数据源对象的属性映射到目标对象的key上
     */
    @Test
    public void testMultiMapping() {
        List<Classroom> classrooms = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            List<Integer> studentIds = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                studentIds.add(j);
            }
            Classroom classroom = new Classroom(studentIds);
            classrooms.add(classroom);
        }

        operateTemplate.execute(classrooms, Classroom.class);
        for (Classroom classroom : classrooms) {
            List<Integer> studentIds = classroom.getStudentIds();
            List<String> studentNames = classroom.getStudentNames();
            List<Map<String, Object>> students = classroom.getStudents();

            for (int i = 0; i < studentIds.size(); i++) {
                Integer studentId = studentIds.get(i);
                Assert.assertEquals("student" + studentId, studentNames.get(i));
                Map<String, Object> map = students.get(studentId);
                Assert.assertEquals("student" + studentId, map.get("name"));
                Assert.assertEquals(18 + studentId, map.get("age"));
            }
            System.out.println(classroom);
        }
    }

    private static List<StudentVO> getStudentList(int range) {
        return IntStream.rangeClosed(0, range)
            .mapToObj(StudentVO::new)
            .collect(Collectors.toList());
    }

    @RequiredArgsConstructor
    @Data
    private static class StudentVO {
        public StudentVO(Integer key) {
            this.key = key;
        }

        // 默认为一对一装配
        @Assemble(container = "student", props = {
            @Mapping(src = "name", ref = "name"), // s.name -> t.name
            @Mapping(src = "age", ref = "age"),   // s.age -> t.age
            @Mapping(ref = "source"),             // s -> t.source
            @Mapping(src = "age")                 // s.age -> t.key
        })
        private Integer key;
        private String name;
        private Integer age;
        private Map<String, Object> source;
    }

    @RequiredArgsConstructor
    @Data
    private static class Classroom {
        // 批量装配，这里选择的是多对多装配
        @Assemble(
            container = "student", handlerType = ManyToManyAssembleOperationHandler.class,
            props = {
                @Mapping(src = "name", ref = "studentNames"), // [s, s, s] -> [s.name, s.name, s.name] -> t.studentNames
                @Mapping(ref = "students")                    // [s, s, s] -> [s, s, s] -> t.students
            }
        )
        private final List<Integer> studentIds;
        private List<String> studentNames;
        private List<Map<String, Object>> students;
    }
}
