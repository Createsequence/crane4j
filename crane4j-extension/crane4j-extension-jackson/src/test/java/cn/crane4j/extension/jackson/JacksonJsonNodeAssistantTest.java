package cn.crane4j.extension.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link JacksonJsonNodeAssistant}
 *
 * @author huangchengxing
 */
public class JacksonJsonNodeAssistantTest {

    @Test
    public void test() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        JacksonJsonNodeAssistant assistant = new JacksonJsonNodeAssistant(objectMapper);

        Foo foo = new Foo(1, "name");

        JsonNode node1 = objectMapper.valueToTree(foo);
        JsonNode node2 = assistant.convertTargetToJsonNode(foo);
        Assert.assertEquals(node2, node1);

        Assert.assertEquals("user_name", assistant.determinePropertyName("userName"));
        assistant.setNamingStrategy(null);
        Assert.assertEquals("userName", assistant.determinePropertyName("userName"));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private final static class Foo {
        private Integer userId;
        private String userName;
    }
}
