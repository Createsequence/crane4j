package cn.crane4j.extension.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * A handler that supports reading and writing properties of {@link JsonNode}.
 *
 * @author huangchengxing
 * @see ObjectNode
 * @since 2.2.0
 */
@RequiredArgsConstructor
public class JsonNodeHandler {

    /**
     * Json node assistant.
     */
    private final JsonNodeAssistant<JsonNode> jsonNodeAssistant;

    /**
     * Read the specified property value.
     *
     * @param node         json node
     * @param propertyName property name
     * @return property value
     */
    public Object read(Object node, String propertyName) {
        JsonNode jsonNode = jsonNodeAssistant.convertTargetToJsonNode(node);
        String actualPropertyName = jsonNodeAssistant.determinePropertyName(propertyName);
        JsonNode result = jsonNode.get(actualPropertyName);
        // if the result is a value node, return the text value of it
        return Objects.nonNull(result) && result.isValueNode() ? result.asText() : result;
    }

    /**
     * Write the specified property value.
     *
     * @param target         json node
     * @param propertyName property name
     * @param value        property value
     */
    public Object write(Object target, String propertyName, Object value) {
        // it's only support writing to ObjectNode
        ObjectNode objectNode = (ObjectNode)target;
        String actualPropertyName = jsonNodeAssistant.determinePropertyName(propertyName);
        // write value even if it's not exist in the target fields
        JsonNode jsonNode = jsonNodeAssistant.convertTargetToJsonNode(value);
        return objectNode.set(actualPropertyName, jsonNode);
    }
}
