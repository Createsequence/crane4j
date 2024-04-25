package cn.crane4j.extension.spring.aop;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.support.aop.MethodResultAutoOperateSupport;
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
import java.util.Objects;

/**
 * Automatic filling of an aspect with method return value based on Spring AOP implementation
 *
 * @author huangchengxing
 * @see AutoOperate
 * @see MethodResultAutoOperateSupport
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class MethodResultAutoOperateAdvisor
    implements PointcutAdvisor, MethodInterceptor, DisposableBean {

    /**
     * <p>Support class for auto operate input arguments.<br/>
     * Use {@link ObjectProvider} to avoid early initialization.
     *
     * @see <a href="https://github.com/opengoofy/crane4j/issues/269">GitHub#269</a>
     */
    private final ObjectProvider<MethodResultAutoOperateSupport> methodResultAutoOperateSupport;

    /**
     * Pointcut for method return value automatic filling.
     */
    private final Pointcut pointcut = AutoOperatePointcut.forAnnotatedMethod(
        (m, c) -> !Objects.equals(m.getReturnType(), Void.TYPE)
            && AnnotatedElementUtils.isAnnotated(m, AutoOperate.class)
    );


    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        AutoOperate annotation = AnnotatedElementUtils.findMergedAnnotation(method, AutoOperate.class);
        Object result = methodInvocation.proceed();
        try {
            methodResultAutoOperateSupport.getObject()
                .afterMethodInvoke(annotation, method, result, methodInvocation.getArguments());
        } catch (Exception ex) {
            log.error("cannot auto operate result for method [{}]", method, ex);
            throw ex;
        }
        return result;
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

    /**
     * Clear resources when destroying the bean.
     */
    @Override
    public void destroy() {
        methodResultAutoOperateSupport.ifAvailable(MethodResultAutoOperateSupport::destroy);
    }
}
