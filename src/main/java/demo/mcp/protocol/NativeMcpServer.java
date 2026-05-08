package demo.mcp.protocol;

import com.fasterxml.jackson.databind.JsonNode;

public interface NativeMcpServer {

    String id();

    JsonNode handle(JsonNode request);
}
