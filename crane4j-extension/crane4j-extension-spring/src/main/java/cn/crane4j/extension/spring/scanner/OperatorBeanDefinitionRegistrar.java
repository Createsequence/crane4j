package cn.crane4j.extension.spring.scanner;

import cn.crane4j.annotation.Operator;
import cn.crane4j.core.support.operator.OperatorProxyFactory;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.extension.spring.annotation.OperatorScan;
import cn.crane4j.extension.spring.util.ContainerResolveUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

/**
 * Registrar for proxy object bean of operator interfaces.
 *
 * @author huangchengxing
 * @see OperatorProxyFactory
 * @see Operator
 * @see OperatorScan
 * @since 1.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class OperatorBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(
        @NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        Class<OperatorScan> annotationType = OperatorScan.class;
        Set<Class<?>> types = ContainerResolveUtils.resolveComponentTypesFromMetadata(
            AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(annotationType.getName())),
            ClassScanner.INSTANCE, str -> str
        );
        if (CollectionUtils.isEmpty(types)) {
            log.warn("cannot find any class by scan configuration for annotation: [{}]", annotationType.getName());
            return;
        }
        doRegisterBeanDefinitions(types, registry);
    }

    /**
     * Register bean definitions.
     *
     * @param classes classes which resolve from annotation attributes
     * @param registry bean definition registry
     */
    protected void doRegisterBeanDefinitions(Set<Class<?>> classes, BeanDefinitionRegistry registry) {
        classes.stream()
            .filter(Class::isInterface)
            .filter(operatorType -> AnnotatedElementUtils.isAnnotated(operatorType, Operator.class))
            .forEach(operator -> registerOperatorBeanDefinition(registry, operator));
    }

    private static void registerOperatorBeanDefinition(BeanDefinitionRegistry registry, Class<?> operatorType) {
        log.debug("register operator bean definition for [{}]", operatorType);
        // register factory bean
        BeanDefinition factoryBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(OperatorProxyFactoryBean.class)
            .addAutowiredProperty("operatorProxyFactory")
            .setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE)
            .setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
            .addPropertyValue("operatorType", operatorType)
            .getBeanDefinition();
        String factoryBeanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(factoryBeanDefinition, registry);
        factoryBeanName += "#" + operatorType.getName();
        registry.registerBeanDefinition(factoryBeanName, factoryBeanDefinition);

        // register operator bean
        BeanDefinition operatorBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(operatorType)
            .setFactoryMethodOnBean("getObject", factoryBeanName)
            .setLazyInit(true)
            .getBeanDefinition();
        String operatorBeanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(operatorBeanDefinition, registry);
        registry.registerBeanDefinition(operatorBeanName, operatorBeanDefinition);
    }

    /**
     * {@link FactoryBean} of operator interface proxy object.
     *
     * @author huangchengxing
     */
    @Setter
    public static class OperatorProxyFactoryBean<T> implements FactoryBean<T> {

        private ObjectProvider<OperatorProxyFactory> operatorProxyFactory;
        private Class<T> operatorType;

        @Override
        public T getObject() {
            // fix https://github.com/opengoofy/crane4j/issues/269
            // use lazy injection to avoid early initialization
            // when the DefaultListableBeanFactory#getBeanNamesForType method is called
            return operatorProxyFactory.getObject().get(operatorType);
        }

        @Override
        public Class<?> getObjectType() {
            return operatorType;
        }
    }
}
