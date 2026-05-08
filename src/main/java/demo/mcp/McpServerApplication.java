package demo.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = {
        "org.springframework.ai.mcp.server.autoconfigure.McpServerSseWebMvcAutoConfiguration",
        "org.springframework.ai.mcp.server.autoconfigure.McpServerStreamableHttpWebMvcAutoConfiguration",
        "org.springframework.ai.mcp.server.autoconfigure.McpServerStatelessWebMvcAutoConfiguration",
        "org.springframework.ai.mcp.server.common.autoconfigure.McpServerAutoConfiguration",
        "org.springframework.ai.mcp.server.common.autoconfigure.McpServerStatelessAutoConfiguration",
        "org.springframework.ai.mcp.server.common.autoconfigure.ToolCallbackConverterAutoConfiguration",
        "org.springframework.ai.mcp.server.common.autoconfigure.StatelessToolCallbackConverterAutoConfiguration",
        "org.springframework.ai.mcp.server.common.autoconfigure.annotations.McpServerAnnotationScannerAutoConfiguration",
        "org.springframework.ai.mcp.server.common.autoconfigure.annotations.McpServerSpecificationFactoryAutoConfiguration",
        "org.springframework.ai.mcp.server.common.autoconfigure.annotations.StatelessServerSpecificationFactoryAutoConfiguration"
})
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
