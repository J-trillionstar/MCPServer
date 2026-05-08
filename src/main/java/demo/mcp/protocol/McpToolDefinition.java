package demo.mcp.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record McpToolDefinition(
        String name,
        String description,
        ObjectNode inputSchema,
        ObjectNode annotations,
        McpToolHandler handler) {

    public ObjectNode toJson(ObjectMapper objectMapper) {
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("name", name);
        tool.put("description", description);
        tool.set("inputSchema", inputSchema.deepCopy());
        tool.set("annotations", annotations.deepCopy());
        return tool;
    }
}
