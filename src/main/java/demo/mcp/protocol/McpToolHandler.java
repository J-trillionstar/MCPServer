package demo.mcp.protocol;

import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface McpToolHandler {

    String call(JsonNode arguments);
}
