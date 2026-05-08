package demo.mcp.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import demo.mcp.protocol.DefaultNativeMcpServer;
import demo.mcp.protocol.McpToolDefinition;
import demo.mcp.protocol.NativeMcpServer;
import demo.mcp.service.DatasourceTools;
import demo.mcp.service.OnlineTools;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class McpServerConfig {

    @Bean
    public NativeMcpServer datasourceMcpServer(ObjectMapper objectMapper, DatasourceTools datasourceTools) {
        return new DefaultNativeMcpServer(
                objectMapper,
                "datasource",
                "demo-datasource-mcp-server",
                "0.1.0",
                "Datasource MCP server. Use these tools for local demo database queries.",
                List.of(
                        new McpToolDefinition(
                                "datasource_list_tables",
                                "List available demo datasource tables and their columns.",
                                listTablesSchema(objectMapper),
                                annotations(objectMapper, "List demo tables", true, false, true, false),
                                arguments -> datasourceTools.listTables()),
                        new McpToolDefinition(
                                "datasource_query_demo_data",
                                "Query demo datasource rows. Supported tables: customers, orders. Optional filters: city, tier, status, customer_id, limit.",
                                queryDemoDataSchema(objectMapper),
                                annotations(objectMapper, "Query demo datasource", true, false, true, false),
                                arguments -> datasourceTools.queryDemoData(
                                        text(arguments, "table"),
                                        text(arguments, "city"),
                                        text(arguments, "tier"),
                                        text(arguments, "status"),
                                        integer(arguments, "customer_id"),
                                        integer(arguments, "limit")))));
    }

    @Bean
    public NativeMcpServer onlineMcpServer(ObjectMapper objectMapper, OnlineTools onlineTools) {
        return new DefaultNativeMcpServer(
                objectMapper,
                "online",
                "demo-online-mcp-server",
                "0.1.0",
                "Online MCP server. Use these tools for simple HTTP fetch checks.",
                List.of(
                        new McpToolDefinition(
                                "online_current_time",
                                "Return the current server time in ISO-8601 format.",
                                currentTimeSchema(objectMapper),
                                annotations(objectMapper, "Current server time", true, false, true, false),
                                arguments -> onlineTools.currentTime()),
                        new McpToolDefinition(
                                "online_fetch_url",
                                "Fetch a small text response from an http or https URL. This is intended for local demos only.",
                                fetchUrlSchema(objectMapper),
                                annotations(objectMapper, "Fetch URL", true, false, false, true),
                                arguments -> onlineTools.fetchUrl(
                                        text(arguments, "url"),
                                        integer(arguments, "max_chars")))));
    }

    private ObjectNode listTablesSchema(ObjectMapper objectMapper) {
        ObjectNode schema = objectSchema(objectMapper);
        schema.putObject("properties");
        return schema;
    }

    private ObjectNode queryDemoDataSchema(ObjectMapper objectMapper) {
        ObjectNode schema = objectSchema(objectMapper);
        ObjectNode properties = schema.putObject("properties");
        stringProperty(properties, "table", "Table name. Supported values: customers, orders.")
                .putArray("enum").add("customers").add("orders");
        stringProperty(properties, "city", "Filter customers by city.");
        stringProperty(properties, "tier", "Filter customers by tier.");
        stringProperty(properties, "status", "Filter orders by status.");
        integerProperty(properties, "customer_id", "Filter orders by customer id.");
        integerProperty(properties, "limit", "Maximum rows to return. Defaults to 5, max 20.");
        schema.putArray("required").add("table");
        return schema;
    }

    private ObjectNode currentTimeSchema(ObjectMapper objectMapper) {
        ObjectNode schema = objectSchema(objectMapper);
        schema.putObject("properties");
        return schema;
    }

    private ObjectNode fetchUrlSchema(ObjectMapper objectMapper) {
        ObjectNode schema = objectSchema(objectMapper);
        ObjectNode properties = schema.putObject("properties");
        stringProperty(properties, "url", "HTTP or HTTPS URL to fetch.");
        integerProperty(properties, "max_chars", "Maximum response characters. Defaults to 1000, max 4000.");
        schema.putArray("required").add("url");
        return schema;
    }

    private ObjectNode objectSchema(ObjectMapper objectMapper) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        return schema;
    }

    private ObjectNode stringProperty(ObjectNode properties, String name, String description) {
        ObjectNode property = properties.putObject(name);
        property.put("type", "string");
        property.put("description", description);
        return property;
    }

    private ObjectNode integerProperty(ObjectNode properties, String name, String description) {
        ObjectNode property = properties.putObject(name);
        property.put("type", "integer");
        property.put("description", description);
        return property;
    }

    private ObjectNode annotations(ObjectMapper objectMapper, String title, boolean readOnly, boolean destructive,
            boolean idempotent, boolean openWorld) {
        ObjectNode annotations = objectMapper.createObjectNode();
        annotations.put("title", title);
        annotations.put("readOnlyHint", readOnly);
        annotations.put("destructiveHint", destructive);
        annotations.put("idempotentHint", idempotent);
        annotations.put("openWorldHint", openWorld);
        return annotations;
    }

    private String text(JsonNode arguments, String field) {
        JsonNode value = arguments.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    private Integer integer(JsonNode arguments, String field) {
        JsonNode value = arguments.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.canConvertToInt()) {
            throw new IllegalArgumentException("Argument must be an integer: " + field);
        }
        return value.asInt();
    }
}
