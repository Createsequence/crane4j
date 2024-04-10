package cn.crane4j.extension.jackson;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.auto.AutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.auto.ClassBasedAutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.converter.SimpleConverterManager;
import cn.crane4j.core.support.expression.OgnlExpressionContext;
import cn.crane4j.core.support.expression.OgnlExpressionEvaluator;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * test for {@link JsonNodeAutoOperateSerializerModifier}.
 *
 * @author huangchengxing
 */
public class JsonNodeAutoOperateSerializerModifierTest {

    @SneakyThrows
    @Test
    public void test() {
        // prepare operator
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        JsonNodeAssistant<JsonNode> jsonNodeAssistant = new JacksonJsonNodeAssistant(objectMapper);
        JsonNodePropertyOperator propertyOperator = new JsonNodePropertyOperator(jsonNodeAssistant, new ReflectivePropertyOperator());

        // prepare context
        AnnotationFinder annotationFinder = SimpleAnnotationFinder.INSTANCE;
        SimpleCrane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create(
            SimpleAnnotationFinder.INSTANCE, new SimpleConverterManager(), propertyOperator
        );
        AutoOperateAnnotatedElementResolver elementResolver = new ClassBasedAutoOperateAnnotatedElementResolver(configuration, new OgnlExpressionEvaluator(), OgnlExpressionContext::new);

        // register module of modifier
        JsonNodeAutoOperateModule autoOperateModule = new JsonNodeAutoOperateModule(elementResolver, objectMapper, annotationFinder);
        objectMapper.registerModule(autoOperateModule);

        // inti data source container
        Map<String, Object> data = new HashMap<>();
        data.put("1", "name1");
        data.put("2", "name2");
        configuration.registerContainer(Containers.forMap("test", data));

        Foo foo = new Foo(1);
        String json = objectMapper.writeValueAsString(foo);
        System.out.println(json);
        Assert.assertEquals("{\"user_id\":1,\"user_name\":\"name1\"}", json);
    }

    @AllArgsConstructor
    @Data
    @AutoOperate
    private static class Foo {
        @Assemble(
            container = "test",
            props = @Mapping(ref = "userName")
        )
        private Integer userId;
    }
}
