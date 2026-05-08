package demo.mcp.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultNativeMcpServer implements NativeMcpServer {

    private static final String PROTOCOL_VERSION = "2025-03-26";

    private final ObjectMapper objectMapper;
    private final String id;
    private final String serverName;
    private final String serverVersion;
    private final String instructions;
    private final Map<String, McpToolDefinition> tools;

    public DefaultNativeMcpServer(
            ObjectMapper objectMapper,
            String id,
            String serverName,
            String serverVersion,
            String instructions,
            List<McpToolDefinition> tools) {
        this.objectMapper = objectMapper;
        this.id = id;
        this.serverName = serverName;
        this.serverVersion = serverVersion;
        this.instructions = instructions;
        this.tools = new LinkedHashMap<String, McpToolDefinition>();
        for (McpToolDefinition tool : tools) {
            this.tools.put(tool.name(), tool);
        }
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public JsonNode handle(JsonNode request) {
        if (request == null || request.isNull()) {
            return error(NullNode.getInstance(), -32600, "Invalid JSON-RPC request");
        }
        if (request.isArray()) {
            return handleBatch(request);
        }
        if (!request.isObject()) {
            return error(NullNode.getInstance(), -32600, "Invalid JSON-RPC request");
        }

        JsonNode idNode = request.get("id");
        String method = request.path("method").asText(null);
        if (method == null || method.isBlank()) {
            return error(idNode, -32600, "Missing JSON-RPC method");
        }

        boolean notification = idNode == null;
        if (notification) {
            return null;
        }

        return switch (method) {
            case "initialize" -> response(idNode, initializeResult());
            case "ping" -> response(idNode, objectMapper.createObjectNode());
            case "tools/list" -> response(idNode, toolsListResult());
            case "tools/call" -> handleToolCall(idNode, request.path("params"));
            default -> error(idNode, -32601, "Method not found: " + method);
        };
    }

    private JsonNode handleBatch(JsonNode batch) {
        ArrayNode responses = objectMapper.createArrayNode();
        for (JsonNode item : batch) {
            JsonNode response = handle(item);
            if (response != null) {
                responses.add(response);
            }
        }
        if (responses.isEmpty()) {
            return null;
        }
        return responses;
    }

    private ObjectNode initializeResult() {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("protocolVersion", PROTOCOL_VERSION);

        ObjectNode capabilities = result.putObject("capabilities");
        capabilities.putObject("tools").put("listChanged", false);

        ObjectNode serverInfo = result.putObject("serverInfo");
        serverInfo.put("name", serverName);
        serverInfo.put("version", serverVersion);

        result.put("instructions", instructions);
        return result;
    }

    private ObjectNode toolsListResult() {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode toolNodes = result.putArray("tools");
        for (McpToolDefinition tool : tools.values()) {
            toolNodes.add(tool.toJson(objectMapper));
        }
        return result;
    }

    private JsonNode handleToolCall(JsonNode idNode, JsonNode params) {
        if (!params.isObject()) {
            return error(idNode, -32602, "tools/call params must be an object");
        }

        String toolName = params.path("name").asText(null);
        if (toolName == null || toolName.isBlank()) {
            return error(idNode, -32602, "Missing tool name");
        }

        McpToolDefinition tool = tools.get(toolName);
        if (tool == null) {
            return error(idNode, -32602, "Unknown tool: " + toolName);
        }

        JsonNode arguments = params.path("arguments");
        if (!arguments.isObject()) {
            arguments = objectMapper.createObjectNode();
        }

        try {
            return response(idNode, toolResult(tool.handler().call(arguments), false));
        } catch (Exception e) {
            return response(idNode, toolResult(e.getMessage(), true));
        }
    }

    private ObjectNode toolResult(String text, boolean isError) {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode content = result.putArray("content");
        ObjectNode item = content.addObject();
        item.put("type", "text");
        item.put("text", text == null ? "" : text);
        result.put("isError", isError);
        return result;
    }

    private ObjectNode response(JsonNode idNode, JsonNode resultNode) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", copyId(idNode));
        response.set("result", resultNode);
        return response;
    }

    private ObjectNode error(JsonNode idNode, int code, String message) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", copyId(idNode));

        ObjectNode error = response.putObject("error");
        error.put("code", code);
        error.put("message", message);
        return response;
    }

    private JsonNode copyId(JsonNode idNode) {
        if (idNode == null || idNode.isMissingNode()) {
            return NullNode.getInstance();
        }
        return idNode.deepCopy();
    }
}
