package cn.crane4j.extension.spring.scanner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

/**
 * test for {@link ClassScanner}
 *
 * @author huangchengxing
 */
public class ClassScannerTest {

    private ClassScanner classScanner;

    @Before
    public void init() {
        classScanner = new ClassScanner();
    }

    @Test
    public void scanAllClasses() {
        Set<Class<?>> classes = classScanner.scan("cn.crane4j.extension.spring.scanner");
        Assert.assertEquals(18, classes.size());

        classes = classScanner.scan("cn.crane4j.extension.spring.scanner.**.*");
        Assert.assertEquals(18, classes.size());

        classes = classScanner.scan("cn.crane4j.extension.spring.scanner.**");
        Assert.assertEquals(18, classes.size());

        classes = classScanner.scan("cn.crane4j.extension.spring.scanner.*");
        Assert.assertEquals(13, classes.size());

        classes = classScanner.scan("cn.crane4j.extension.spring.scanner.dir");
        Assert.assertEquals(5, classes.size());

        classes = classScanner.scan("cn.crane4j.extension.spring.scanner.**.*2.class");
        Assert.assertEquals(2, classes.size());

        classes = classScanner.scan("cn.crane4j.extension.spring.scanner.**.*1.class");
        Assert.assertEquals(3, classes.size());

        classes = classScanner.scan("cn.crane4j.extension.spring.scanner.ClassScannerTest.class");
        Assert.assertEquals(1, classes.size());
        classes = classScanner.scan("cn.crane4j.extension.spring.scanner.*ClassScanner*.class");
        Assert.assertEquals(2, classes.size());
    }
}
