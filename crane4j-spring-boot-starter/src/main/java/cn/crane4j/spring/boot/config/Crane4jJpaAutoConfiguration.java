package cn.crane4j.spring.boot.config;

import cn.crane4j.core.container.MethodInvokerContainer;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.support.query.QueryDefinition;
import cn.crane4j.core.support.query.RepositoryTargetProvider;
import cn.crane4j.core.util.Try;
import cn.crane4j.extension.jpa.AssembleJpaAnnotationHandler;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.Repository;

import javax.persistence.EntityManager;
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
@EnableConfigurationProperties(Crane4jJpaAutoConfiguration.Properties.class)
@ConditionalOnClass({JpaRepositoriesAutoConfiguration.class, AssembleJpaAnnotationHandler.class})
@AutoConfiguration(after = { JpaRepositoriesAutoConfiguration.class, Crane4jAutoConfiguration.class })
public class Crane4jJpaAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public AssembleJpaAnnotationHandler assembleJpaAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        MethodInvokerContainerCreator methodInvokerContainerCreator, BeanFactory beanFactory, EntityManager entityManager) {
        AssembleJpaAnnotationHandler handler = new AssembleJpaAnnotationHandler(
            annotationFinder, globalConfiguration, methodInvokerContainerCreator, entityManager
        );
        handler.setRepositoryTargetProvider(new RepositoryLazyLoader(beanFactory));
        return handler;
    }

    @ConditionalOnProperty(
        prefix = Properties.CRANE4J_JPA_EXTENSION_PREFIX,
        name = "auto-register-repository",
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
    @ConfigurationProperties(prefix = Properties.CRANE4J_JPA_EXTENSION_PREFIX)
    @Data
    public static class Properties {

        public static final String CRANE4J_JPA_EXTENSION_PREFIX = Crane4jAutoConfiguration.CRANE_PREFIX + ".jpa";

        /**
         * repository allowed to be scanned and registered.
         */
        private Set<String> includes = new HashSet<>();

        /**
         * repository isn't allowed to be scanned and registered.
         */
        private Set<String> excludes = new HashSet<>();

        /**
         * whether to register repository automatically
         */
        private boolean autoRegisterRepository = false;
    }

    /**
     * Mapper lazy loader.
     *
     * @author huangchengxing
     * @since 2.9.0
     */
    @RequiredArgsConstructor
    public static class RepositoryLazyLoader implements RepositoryTargetProvider<Repository<?, ?>> {

        private final BeanFactory beanFactory;

        /**
         * Get repository by given id.
         *
         * @param queryDefinition query definition
         * @return repository
         */
        @Override
        public Repository<?, ?> get(QueryDefinition queryDefinition) {
            return beanFactory.getBean(Repository.class, queryDefinition.getRepositoryId());
        }
    }

    /**
     * Auto registrar of container based on {@link Repository}.
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
         * obtain and parse the beans that implement the {@link Repository} interface,
         * and then adapt them to {@link MethodInvokerContainer} and register them.
         *
         * @param args incoming application arguments
         */
        @SuppressWarnings("rawtypes")
        @Override
        public void run(ApplicationArguments args) {
            if (!properties.isAutoRegisterRepository()) {
                return;
            }
            Set<String> includes = properties.getIncludes();
            Set<String> excludes = properties.getExcludes();
            includes.removeAll(excludes);
            BiPredicate<String, Repository<?, ?>> repositoryFilter = includes.isEmpty() ?
                (n, m) -> !excludes.contains(n) : (n, m) -> includes.contains(n) && !excludes.contains(n);
            Map<String, Repository> repositories = applicationContext.getBeansOfType(Repository.class);
            AssembleJpaAnnotationHandler handler = applicationContext.getBean(AssembleJpaAnnotationHandler.class);
            repositories.entrySet().stream()
                .filter(e -> repositoryFilter.test(e.getKey(), e.getValue()))
                .forEach(e -> Try.of(() -> handler.registerRepository(e.getKey(), e.getValue()))
                    .subscribeFailure(ex -> log.error("Failed to register repository: {}", e.getKey(), ex))
                    .perform()
                );
            log.info("crane4j jpa extension component initialization completed.");
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
            log.info("crane4j jpa extension initialization completed!");
        }
    }
}
