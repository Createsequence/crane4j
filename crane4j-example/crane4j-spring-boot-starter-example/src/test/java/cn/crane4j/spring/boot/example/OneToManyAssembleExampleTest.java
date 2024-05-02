package cn.crane4j.spring.boot.example;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.executor.handler.OneToManyAssembleOperationHandler;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.OperateTemplate;
import lombok.AllArgsConstructor;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 演示如何使用一对多装配处理器：
 * 1.准备一个数据源容器，向该容器的输入一个key值将会获得对应的一个数据源集合/数组;
 * 2.配置装配操作，并指定装配处理器为OneToManyReflexAssembleOperationHandler;
 * 3.指定字段映射，获取数据源集合中的每一个指定字段值，转为集合后赋值给目标对象；
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Crane4jSpringBootStarterExampleApplication.class})
public class OneToManyAssembleExampleTest {

    @Autowired
    private Crane4jGlobalConfiguration configuration;
    @Autowired
    private OperateTemplate operateTemplate;

    @Before
    public void init() {
        Container<Integer> container = LambdaContainer.forLambda("customer-group", groupIds -> {
            Map<Integer, Object> result = new HashMap<>();
            groupIds.forEach(gid -> {
                List<Customer> customers = new ArrayList<>(groupIds.size());
                for (int i = 0; i < gid; i++) {
                    customers.add(new Customer("customer" + i, gid));
                }
                result.put(gid, customers);
            });
            return result;
        });
        if (!configuration.containsContainer("customer-group")) {
            configuration.registerContainer(container);
        }
    }

    @Test
    public void test() {
        List<CustomerGroup> customerGroups = Arrays.asList(
            new CustomerGroup(1), new CustomerGroup(2), new CustomerGroup(3)
        );
        operateTemplate.execute(customerGroups);
        for (CustomerGroup customerGroup : customerGroups) {
            List<String> customerNames = customerGroup.getCustomerNames();
            for (int i = 0; i < customerGroup.getGroupId(); i++) {
                Assert.assertEquals("customer" + i, customerNames.get(i));
            }
        }
    }

    @RequiredArgsConstructor
    @Data
    private static class CustomerGroup {
        @Assemble(
            container = "customer-group",
            props = @Mapping(src = "name", ref = "customerNames"),
            handler = "oneToManyAssembleOperationHandler",
            handlerType = OneToManyAssembleOperationHandler.class
        )
        private final Integer groupId;
        private List<String> customerNames;
    }

    @AllArgsConstructor
    @Data
    private static class Customer {
        private String name;
        private Integer groupId;
    }
}
