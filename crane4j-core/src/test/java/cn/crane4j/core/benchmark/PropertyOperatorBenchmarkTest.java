package cn.crane4j.core.benchmark;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.AsmReflectivePropertyOperator;
import cn.crane4j.core.support.reflect.MethodHandlePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import lombok.Setter;
import org.junit.Ignore;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * {@link PropertyOperator}基准测试
 *
 * 检查每次调用的平均耗时，单位为毫秒
 * 三个进程
 * 预热5轮，每轮10秒
 * 测试5轮，每轮20秒
 *
 * @author huangchengxing
 */
@Ignore
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(3)
@State(Scope.Thread)
public class PropertyOperatorBenchmarkTest {

    private static final String VALUE = "value";

    @Setter
    private String value = "value";

    private MethodInvoker reflectGetter;
    private MethodInvoker reflectAsmGetter;
    private MethodInvoker methodHandleGetter;

    private MethodInvoker reflectSetter;
    private MethodInvoker reflectAsmSetter;
    private MethodInvoker methodHandleSetter;

    public static void main(String[] args) throws Exception {
        Options opts = new OptionsBuilder()
            .include(PropertyOperatorBenchmarkTest.class.getSimpleName())
            .resultFormat(ResultFormatType.JSON)
            .build();
        new Runner(opts).run();
    }

    @Setup
    public void setup() {
        ReflectivePropertyOperator reflectivePropertyOperator = ReflectivePropertyOperator.INSTANCE;
        reflectGetter = reflectivePropertyOperator.findGetter(getClass(), VALUE);
        reflectSetter = reflectivePropertyOperator.findSetter(getClass(), VALUE);

        AsmReflectivePropertyOperator asmReflectivePropertyOperator = new AsmReflectivePropertyOperator();
        reflectAsmGetter = asmReflectivePropertyOperator.findGetter(getClass(), VALUE);
        reflectAsmSetter = asmReflectivePropertyOperator.findSetter(getClass(), VALUE);

        MethodHandlePropertyOperator methodHandlePropertyOperator = new MethodHandlePropertyOperator();
        methodHandleGetter = methodHandlePropertyOperator.findGetter(getClass(), VALUE);
        methodHandleSetter = methodHandlePropertyOperator.findSetter(getClass(), VALUE);
    }

    @Benchmark
    public Object directGet() {
        return value;
    }

    @Benchmark
    public Object reflectGet() {
        return reflectGetter.invoke(this);
    }

    @Benchmark
    public Object reflectAsmGet() {
        return reflectAsmGetter.invoke(this);
    }

    @Benchmark
    public Object methodHandleGet() {
        return methodHandleGetter.invoke(this);
    }

    @Benchmark
    public void directSet() {
        value = VALUE;
    }

    @Benchmark
    public void reflectSet() {
        reflectSetter.invoke(this, VALUE);
    }

    @Benchmark
    public void reflectAsmSet() {
        reflectAsmSetter.invoke(this, VALUE);
    }

    @Benchmark
    public void methodHandleSet() {
        methodHandleSetter.invoke(this, VALUE);
    }
}
