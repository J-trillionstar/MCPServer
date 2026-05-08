package demo.mcp.config;

import demo.mcp.service.DatasourceTools;
import demo.mcp.service.OnlineTools;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcStatelessServerTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.ai.mcp.annotation.spring.SyncMcpAnnotationProviders;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class McpServerConfig {

    @Bean
    public WebMvcStatelessServerTransport datasourceMcpTransport() {
        return WebMvcStatelessServerTransport.builder()
                .messageEndpoint("/mcp/datasource")
                .build();
    }

    @Bean
    public McpStatelessSyncServer datasourceMcpServer(
            @Qualifier("datasourceMcpTransport") WebMvcStatelessServerTransport transport,
            DatasourceTools datasourceTools) {

        return McpServer.sync(transport)
                .serverInfo("demo-datasource-mcp-server", "0.1.0")
                .instructions("Datasource MCP server. Use these tools for local demo database queries.")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(false).build())
                .tools(SyncMcpAnnotationProviders.statelessToolSpecifications(List.of(datasourceTools)))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> datasourceMcpRouter(
            @Qualifier("datasourceMcpTransport") WebMvcStatelessServerTransport transport,
            @Qualifier("datasourceMcpServer") McpStatelessSyncServer server) {

        return transport.getRouterFunction();
    }

    @Bean
    public WebMvcStatelessServerTransport onlineMcpTransport() {
        return WebMvcStatelessServerTransport.builder()
                .messageEndpoint("/mcp/online")
                .build();
    }

    @Bean
    public McpStatelessSyncServer onlineMcpServer(
            @Qualifier("onlineMcpTransport") WebMvcStatelessServerTransport transport,
            OnlineTools onlineTools) {

        return McpServer.sync(transport)
                .serverInfo("demo-online-mcp-server", "0.1.0")
                .instructions("Online MCP server. Use these tools for simple HTTP fetch checks.")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(false).build())
                .tools(SyncMcpAnnotationProviders.statelessToolSpecifications(List.of(onlineTools)))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> onlineMcpRouter(
            @Qualifier("onlineMcpTransport") WebMvcStatelessServerTransport transport,
            @Qualifier("onlineMcpServer") McpStatelessSyncServer server) {

        return transport.getRouterFunction();
    }
}
