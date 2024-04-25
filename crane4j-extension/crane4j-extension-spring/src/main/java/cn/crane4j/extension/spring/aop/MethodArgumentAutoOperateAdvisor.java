package cn.crane4j.extension.spring.aop;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.support.aop.MethodArgumentAutoOperateSupport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Method input parameter automatic filling Aspect based on Spring AOP implementation.
 *
 * @author huangchengxing
 * @see ArgAutoOperate
 * @see AutoOperate
 * @see MethodArgumentAutoOperateSupport
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class MethodArgumentAutoOperateAdvisor
    implements PointcutAdvisor, MethodInterceptor, DisposableBean {

    /**
     * <p>Support class for auto operate input arguments.<br/>
     * Use {@link ObjectProvider} to avoid early initialization.
     *
     * @see <a href="https://github.com/opengoofy/crane4j/issues/269">GitHub#269</a>
     */
    private final ObjectProvider<MethodArgumentAutoOperateSupport> methodArgumentAutoOperateSupport;

    /**
     * Pointcut for method input parameter automatic filling.
     */
    private final Pointcut pointcut = AutoOperatePointcut.forAnnotatedMethod(
        (m, c) -> m.getParameterCount() > 0
            && (AnnotatedElementUtils.isAnnotated(m, ArgAutoOperate.class)
            || Arrays.stream(m.getParameters()).anyMatch(p -> p.isAnnotationPresent(AutoOperate.class)))
    );

    /**
     * Clear resources when destroying the bean.
     */
    @Override
    public void destroy() {
        methodArgumentAutoOperateSupport.ifAvailable(MethodArgumentAutoOperateSupport::destroy);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        try {
            methodArgumentAutoOperateSupport.getObject()
                .beforeMethodInvoke(method, methodInvocation.getArguments());
        } catch (Exception ex) {
            log.error("cannot auto operate input arguments for method [{}]", method, ex);
            throw ex;
        }
        return methodInvocation.proceed();
    }

    @NonNull
    @Override
    public Advice getAdvice() {
        return this;
    }

    @Override
    public boolean isPerInstance() {
        return false;
    }
}
