package cn.crane4j.spring.boot.config;

import cn.crane4j.core.container.MethodInvokerContainer;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.extension.mybatis.plus.AssembleMpAnnotationHandler;
import cn.crane4j.extension.mybatis.plus.MybatisPlusQueryContainerProvider;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Crane4j mybatis plus configuration.
 *
 * @author huangchengxing
 * @see cn.crane4j.extension.mybatis.plus
 */
@EnableConfigurationProperties(Crane4jMybatisPlusAutoConfiguration.Properties.class)
@ConditionalOnClass({MybatisPlusAutoConfiguration.class, AssembleMpAnnotationHandler.class})
@AutoConfiguration(after = { MybatisPlusAutoConfiguration.class, Crane4jAutoConfiguration.class })
public class Crane4jMybatisPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusQueryContainerProvider mybatisPlusQueryContainerProvider(
        MethodInvokerContainerCreator methodInvokerContainerCreator,
        Crane4jGlobalConfiguration globalConfiguration, ApplicationContext applicationContext) {
        return new MybatisPlusQueryContainerProvider(
            methodInvokerContainerCreator, globalConfiguration,
            mapperName -> applicationContext.getBean(mapperName, BaseMapper.class)
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public AssembleMpAnnotationHandler assembleMpAnnotationResolver(
        AnnotationFinder annotationFinder, MybatisPlusQueryContainerProvider mybatisPlusQueryContainerProvider,
        Crane4jGlobalConfiguration globalConfiguration,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        return new AssembleMpAnnotationHandler(
            annotationFinder, mybatisPlusQueryContainerProvider, globalConfiguration, propertyMappingStrategyManager
        );
    }

    @ConditionalOnProperty(
        prefix = Properties.CRANE4J_MP_EXTENSION_PREFIX,
        name = "auto-register-mapper",
        havingValue = "true"
    )
    @Bean
    @ConditionalOnMissingBean
    public BaseMapperAutoRegistrar baseMapperAutoRegistrar(
        ApplicationContext applicationContext, Properties crane4jMybatisPlusProperties) {
        return new BaseMapperAutoRegistrar(applicationContext, crane4jMybatisPlusProperties);
    }

    @Bean
    public InitializationLogger initializationLogger() {
        return new InitializationLogger();
    }

    /**
     * Crane4j mybatis plus properties.
     *
     * @author huangchengxing
     */
    @ConfigurationProperties(prefix = Properties.CRANE4J_MP_EXTENSION_PREFIX)
    @Data
    public static class Properties {

        public static final String CRANE4J_MP_EXTENSION_PREFIX = Crane4jAutoConfiguration.CRANE_PREFIX + ".mybatis-plus";

        /**
         * mapper allowed to be scanned and registered.
         */
        private Set<String> includes = new HashSet<>();

        /**
         * mapper not allowed to be scanned and registered.
         */
        private Set<String> excludes = new HashSet<>();

        /**
         * whether to register mapper automatically
         */
        private boolean autoRegisterMapper = false;
    }

    /**
     * Auto registrar of container based on {@link BaseMapper}.
     *
     * @author huangchengxing
     */
    @Slf4j
    @Accessors(chain = true)
    @RequiredArgsConstructor
    public static class BaseMapperAutoRegistrar implements ApplicationRunner {

        private final ApplicationContext applicationContext;
        private final Properties properties;

        /**
         * After initializing all singleton beans in the Spring context,
         * obtain and parse the beans that implement the {@link BaseMapper} interface,
         * and then adapt them to {@link MethodInvokerContainer} and register them.
         *
         * @param args incoming application arguments
         */
        @SuppressWarnings("rawtypes")
        @Override
        public void run(ApplicationArguments args) {
            if (!properties.isAutoRegisterMapper()) {
                return;
            }
            Set<String> includes = properties.getIncludes();
            Set<String> excludes = properties.getExcludes();
            includes.removeAll(excludes);
            BiPredicate<String, BaseMapper<?>> mapperFilter = includes.isEmpty() ?
                (n, m) -> !excludes.contains(n) : (n, m) -> includes.contains(n) && !excludes.contains(n);
            Map<String, BaseMapper> mappers = applicationContext.getBeansOfType(BaseMapper.class);
            MybatisPlusQueryContainerProvider register = applicationContext.getBean(MybatisPlusQueryContainerProvider.class);
            mappers.entrySet().stream()
                .filter(e -> mapperFilter.test(e.getKey(), e.getValue()))
                .forEach(e -> register.registerRepository(e.getKey(), e.getValue()));
            log.info("crane4j mybatis-plus extension component initialization completed.");
        }
    }

    /**
     * Initialization logger.
     *
     * @author huangchengxing
     */
    @Slf4j
    public static class InitializationLogger implements ApplicationRunner {
        @Override
        public void run(ApplicationArguments args) {
            log.info("crane4j mybatis-plus extension initialization completed!");
        }
    }
}
