package demo.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.Locale;

@Service
public class OnlineTools {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    public OnlineTools(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @McpTool(
            name = "online_current_time",
            description = "Return the current server time in ISO-8601 format.",
            annotations = @McpTool.McpAnnotations(
                    title = "Current server time",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public String currentTime() {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("serverTime", OffsetDateTime.now().toString());
        return pretty(result);
    }

    @McpTool(
            name = "online_fetch_url",
            description = "Fetch a small text response from an http or https URL. This is intended for local demos only.",
            annotations = @McpTool.McpAnnotations(
                    title = "Fetch URL",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = false,
                    openWorldHint = true))
    public String fetchUrl(
            @McpToolParam(description = "HTTP or HTTPS URL to fetch.")
            String url,
            @McpToolParam(required = false, description = "Maximum response characters. Defaults to 1000, max 4000.")
            Integer max_chars) {

        return pretty(fetch(requireText(url, "url"), boundedMaxChars(max_chars == null ? 1000 : max_chars)));
    }

    private ObjectNode fetch(String urlText, int maxChars) {
        String lower = urlText.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            throw new IllegalArgumentException("Only http and https URLs are supported");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(urlText))
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", "spring-multi-mcp-server/0.1.0")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body() == null ? "" : truncate(response.body(), maxChars);

            ObjectNode result = objectMapper.createObjectNode();
            result.put("url", urlText);
            result.put("status", response.statusCode());
            response.headers().firstValue("content-type").ifPresent(value -> result.put("contentType", value));
            result.put("body", body);
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Fetch failed: " + e.getMessage(), e);
        }
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required argument: " + field);
        }
        return value.trim();
    }

    private String truncate(String body, int maxChars) {
        if (body.length() <= maxChars) {
            return body;
        }
        return body.substring(0, maxChars);
    }

    private String pretty(ObjectNode node) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render tool result", e);
        }
    }

    private int boundedMaxChars(int maxChars) {
        return Math.max(1, Math.min(maxChars, 4000));
    }
}
