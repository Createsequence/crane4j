package cn.crane4j.spring.boot.config;

import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.extension.query.AssembleDbAnnotationHandler;
import cn.crane4j.extension.query.DataSourceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Crane4j data source auto configuration.
 *
 * @author huangchengxing
 */
@Slf4j
@AutoConfiguration(after = { Crane4jAutoConfiguration.class, DataSourceAutoConfiguration.class})
@ConditionalOnClass(AssembleDbAnnotationHandler.class)
@RequiredArgsConstructor
public class Crane4jDataSourceAutoConfiguration  {

    @ConditionalOnMissingBean(AssembleDbAnnotationHandler.class)
    @ConditionalOnBean(DataSource.class)
    @Bean
    public AssembleDbAnnotationHandler assembleDbAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        MethodInvokerContainerCreator methodInvokerContainerCreator, Map<String, DataSource> dataSourceMap) {
        log.info("enable datasource for @AssembleDb annotation: {}", String.join(", ", dataSourceMap.keySet()));
        DataSourceProvider dataSourceProvider = dataSourceMap.size() == 1 ?
            DataSourceProvider.of(dataSourceMap.values().iterator().next()) :
            // TODO if datasource is specified primary?
            DataSourceProvider.of(dataSourceMap);
        return new AssembleDbAnnotationHandler(
            annotationFinder, globalConfiguration, methodInvokerContainerCreator, dataSourceProvider
        );
    }
}
