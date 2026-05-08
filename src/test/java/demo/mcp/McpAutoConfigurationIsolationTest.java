package demo.mcp;

import io.modelcontextprotocol.server.transport.WebMvcStatelessServerTransport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.ai.mcp.server.enabled=true",
        "spring.ai.mcp.server.protocol=STATELESS"
})
class McpAutoConfigurationIsolationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void excludesDefaultMcpServerAutoConfiguration() {
        assertThat(applicationContext.getBeansOfType(WebMvcStatelessServerTransport.class))
                .containsOnlyKeys("datasourceMcpTransport", "onlineMcpTransport");

        assertThat(applicationContext.containsBean("webMvcStatelessServerTransport")).isFalse();
        assertThat(applicationContext.containsBean("mcpStatelessSyncServer")).isFalse();
    }
}
