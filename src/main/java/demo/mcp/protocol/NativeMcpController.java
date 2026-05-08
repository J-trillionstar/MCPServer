package demo.mcp.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class NativeMcpController {

    private final Map<String, NativeMcpServer> servers;

    public NativeMcpController(List<NativeMcpServer> servers) {
        this.servers = new LinkedHashMap<String, NativeMcpServer>();
        for (NativeMcpServer server : servers) {
            this.servers.put(server.id(), server);
        }
    }

    @PostMapping(
            value = "/mcp/{serverId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> handle(@PathVariable String serverId, @RequestBody JsonNode request) {
        NativeMcpServer server = servers.get(serverId);
        if (server == null) {
            return ResponseEntity.notFound().build();
        }

        JsonNode response = server.handle(request);
        if (response == null) {
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.ok(response);
    }
}
