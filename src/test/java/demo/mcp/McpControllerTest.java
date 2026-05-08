package demo.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class McpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void datasourceEndpointInitializesAndListsTools() throws Exception {
        mockMvc.perform(post("/mcp/datasource")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2025-03-26\",\"capabilities\":{},\"clientInfo\":{\"name\":\"test\",\"version\":\"1\"}}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.serverInfo.name").value("demo-datasource-mcp-server"));

        mockMvc.perform(post("/mcp/datasource")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.tools", hasSize(2)))
                .andExpect(jsonPath("$.result.tools[0].name").value("datasource_list_tables"));
    }

    @Test
    void onlineEndpointInitializesAndListsTools() throws Exception {
        mockMvc.perform(post("/mcp/online")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2025-03-26\",\"capabilities\":{},\"clientInfo\":{\"name\":\"test\",\"version\":\"1\"}}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.serverInfo.name").value("demo-online-mcp-server"));

        mockMvc.perform(post("/mcp/online")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.tools", hasSize(2)))
                .andExpect(jsonPath("$.result.tools[0].name").value("online_current_time"));
    }

    @Test
    void datasourceToolCanBeCalled() throws Exception {
        mockMvc.perform(post("/mcp/datasource")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"datasource_query_demo_data\",\"arguments\":{\"table\":\"customers\",\"tier\":\"gold\"}}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].type").value("text"))
                .andExpect(jsonPath("$.result.isError").value(false));
    }
}
