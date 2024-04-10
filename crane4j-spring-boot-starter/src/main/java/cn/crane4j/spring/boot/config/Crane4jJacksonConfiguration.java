package cn.crane4j.spring.boot.config;

import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.auto.AutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.reflect.DecoratedPropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.extension.jackson.JacksonJsonNodeAssistant;
import cn.crane4j.extension.jackson.JsonNodeAssistant;
import cn.crane4j.extension.jackson.JsonNodeAutoOperateModule;
import cn.crane4j.extension.jackson.JsonNodePropertyOperator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Crane4j jackson configuration.
 *
 * @author huangchengxing
 * @see cn.crane4j.extension.jackson
 */
@Slf4j
@AutoConfiguration(after = { Crane4jAutoConfiguration.class })
@ConditionalOnClass({ObjectMapper.class, JsonNodeAssistant.class})
public class Crane4jJacksonConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @ConditionalOnMissingBean
    @Bean
    public JsonNodeAssistant<JsonNode> jacksonJsonNodeAssistant(ObjectMapper objectMapper) {
        return new JacksonJsonNodeAssistant(objectMapper);
    }

    @Bean
    public JsonNodeAutoOperateModule jsonNodeAutoOperateModule(
        AutoOperateAnnotatedElementResolver elementResolver, AnnotationFinder annotationFinder, ObjectMapper objectMapper) {
        return new JsonNodeAutoOperateModule(elementResolver, objectMapper, annotationFinder);
    }

    @Bean
    public Initializer initializer(
        ApplicationContext applicationContext, JsonNodeAssistant<JsonNode> assistant,
        JsonNodeAutoOperateModule jsonNodeAutoOperateModule) {
        return new Initializer(applicationContext, jsonNodeAutoOperateModule, assistant);
    }

    @RequiredArgsConstructor
    public static class Initializer implements ApplicationRunner {

        private final ApplicationContext applicationContext;
        private final JsonNodeAutoOperateModule jsonNodeAutoOperateModule;
        private final JsonNodeAssistant<JsonNode> assistant;

        /**
         * Callback used to run the bean.
         *
         * @param args incoming application arguments
         */
        @Override
        public void run(ApplicationArguments args) {
            registerModule();
            wrapPropertyOperatorIfNecessary();
            log.info("crane4j jackson extension component initialization completed.");
        }

        private void wrapPropertyOperatorIfNecessary() {
            PropertyOperator propertyOperator = applicationContext.getBean(PropertyOperator.class);
            if (!(propertyOperator instanceof DecoratedPropertyOperator)) {
                log.warn(
                    "cannot wrap an existing PropertyOperator as a {}, "
                        + "make sure the instance that exists in the spring container is {}",
                    JsonNodePropertyOperator.class.getSimpleName(), DecoratedPropertyOperator.class.getSimpleName()
                );
                return;
            }
            DecoratedPropertyOperator decorated = (DecoratedPropertyOperator)propertyOperator;
            decorated.setPropertyOperator(new JsonNodePropertyOperator(
                assistant, decorated.getPropertyOperator()
            ));
        }

        private void registerModule() {
            try {
                ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
                if (!objectMapper.getRegisteredModuleIds().contains(JsonNodeAutoOperateModule.MODULE_NAME)) {
                    objectMapper.registerModule(jsonNodeAutoOperateModule);
                    log.info("register module [{}] from ObjectMapper", JsonNodeAutoOperateModule.MODULE_NAME);
                }
            } catch(Exception e) {
                log.warn("unable to automatically register module [{}] because ObjectMapper could not be found,"
                        + "or there are multiple ObjectMapper but primary bean is not specified,"
                        + "please manually register xx to the specified instance",
                    JsonNodeAutoOperateModule.class.getSimpleName()
                );
            }
        }
    }
}
