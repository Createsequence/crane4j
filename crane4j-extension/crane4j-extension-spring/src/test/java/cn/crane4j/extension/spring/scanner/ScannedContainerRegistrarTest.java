package cn.crane4j.extension.spring.scanner;

import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.extension.spring.DefaultCrane4jSpringConfiguration;
import cn.crane4j.extension.spring.annotation.ContainerConstantScan;
import cn.crane4j.extension.spring.annotation.ContainerEnumScan;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * test for {@link OperatorBeanDefinitionRegistrar}
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DefaultCrane4jSpringConfiguration.class, ScannedContainerRegistrarTest.Config.class})
public class ScannedContainerRegistrarTest {

    @Autowired
    private Crane4jGlobalConfiguration configuration;

    @Test
    public void test() {
        Assert.assertTrue(configuration.containsContainer("constant1"));
        Assert.assertTrue(configuration.containsContainer("constant2"));
        Assert.assertTrue(configuration.containsContainer("constant3"));

        Assert.assertTrue(configuration.containsContainer("enum1"));
        Assert.assertTrue(configuration.containsContainer("enum2"));
        Assert.assertTrue(configuration.containsContainer("enum3"));
    }

    @ContainerConstantScan(includePackages = "cn.crane4j.extension.spring.scanner")
    @ContainerEnumScan(includePackages = "cn.crane4j.extension.spring.scanner")
    @Configuration
    protected static class Config {
    }
}
