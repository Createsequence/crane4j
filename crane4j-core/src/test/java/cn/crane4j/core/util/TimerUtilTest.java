package cn.crane4j.core.util;

import org.junit.Test;

/**
 * test for {@link TimerUtil}
 *
 * @author huangchengxing
 */
public class TimerUtilTest {

    @Test
    public void getExecutionTime() {
        TimerUtil.getExecutionTime(false, System.out::println, () -> {});
    }
}
