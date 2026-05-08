# Spring Multi MCP Server Demo

这个 demo 使用 JDK 21、Spring Boot 3.5、Spring AI 1.1 的 MCP 注解能力，在一个 Spring Boot 微服务里暴露两个独立的 MCP server endpoint：

- `POST http://127.0.0.1:8080/mcp/datasource`
- `POST http://127.0.0.1:8080/mcp/online`

两个 endpoint 分别有自己的 `serverInfo`、`instructions` 和工具列表。工具方法用 `@McpTool` 标注，配置类手动把不同工具 bean 分组挂到不同 MCP server 上。

## 实现重点

- 工具注解：`org.springaicommunity.mcp.annotation.McpTool`
- 参数注解：`org.springaicommunity.mcp.annotation.McpToolParam`
- 注解转 tool specs：`SyncMcpAnnotationProviders.statelessToolSpecifications(...)`
- 两个 transport：`WebMvcStatelessServerTransport`
- 两个 server：`McpStatelessSyncServer`

核心代码：

- `src/main/java/demo/mcp/config/McpServerConfig.java`
- `src/main/java/demo/mcp/service/DatasourceTools.java`
- `src/main/java/demo/mcp/service/OnlineTools.java`

## 环境

- JDK 21+
- Maven 3.9+
- Node.js/npm（只有用 Claude Desktop + `mcp-remote` 本地桥接时需要）

本仓库已经把 Microsoft OpenJDK 21 下载到：

```powershell
D:\MCP\.tmp\jdk-21
```

如果不想改系统 Java，可以在当前 PowerShell 会话里临时使用它：

```powershell
$env:JAVA_HOME = "D:\MCP\.tmp\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version
```

## 启动

```powershell
$env:JAVA_HOME = "D:\MCP\.tmp\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\.tmp\apache-maven-3.9.15\bin\mvn.cmd spring-boot:run -Dspring-boot.run.arguments="--server.address=127.0.0.1 --server.port=8080"
```

也可以先打包再运行：

```powershell
$env:JAVA_HOME = "D:\MCP\.tmp\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\.tmp\apache-maven-3.9.15\bin\mvn.cmd clean package
java -jar target\spring-multi-mcp-server-0.0.1-SNAPSHOT.jar --server.address=127.0.0.1 --server.port=8080
```

`application.yml` 默认监听 `127.0.0.1:8080`。如果你的环境里设置了 `SERVER_PORT`，命令行里的 `--server.port=8080` 可以显式覆盖它。

## 手工验证

MCP Streamable HTTP 请求需要带上 `Accept: application/json, text/event-stream`。

初始化 datasource server：

```powershell
curl.exe -s http://127.0.0.1:8080/mcp/datasource `
  -H "Content-Type: application/json" `
  -H "Accept: application/json, text/event-stream" `
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"curl","version":"1.0.0"}}}'
```

查看 datasource 工具：

```powershell
curl.exe -s http://127.0.0.1:8080/mcp/datasource `
  -H "Content-Type: application/json" `
  -H "Accept: application/json, text/event-stream" `
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}'
```

查看 online 工具：

```powershell
curl.exe -s http://127.0.0.1:8080/mcp/online `
  -H "Content-Type: application/json" `
  -H "Accept: application/json, text/event-stream" `
  -d '{"jsonrpc":"2.0","id":3,"method":"tools/list","params":{}}'
```

调用 datasource 工具：

```powershell
curl.exe -s http://127.0.0.1:8080/mcp/datasource `
  -H "Content-Type: application/json" `
  -H "Accept: application/json, text/event-stream" `
  -d '{"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"datasource_query_demo_data","arguments":{"table":"customers","tier":"gold","limit":5}}}'
```

## 连接 Claude Desktop

Claude Desktop 本地 MCP 配置常见是 command/args 模式。这个项目暴露的是 HTTP MCP endpoint，所以本地开发时可以用 `mcp-remote` 做桥接。

1. 先启动 Spring Boot 项目。
2. 确认本机安装了 Node.js/npm。
3. 打开 Claude Desktop 配置文件：
   - Windows: `%APPDATA%\Claude\claude_desktop_config.json`
   - macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
4. 把 `.claude-desktop-config.example.json` 中的 `mcpServers` 合并进去。
5. 重启 Claude Desktop。

配置示例：

```json
{
  "mcpServers": {
    "datasource": {
      "command": "npx",
      "args": [
        "-y",
        "mcp-remote",
        "http://127.0.0.1:8080/mcp/datasource"
      ]
    },
    "online": {
      "command": "npx",
      "args": [
        "-y",
        "mcp-remote",
        "http://127.0.0.1:8080/mcp/online"
      ]
    }
  }
}
```

## 连接 Claude Code 或 HTTP MCP 客户端

如果客户端支持 HTTP MCP transport，可以直接使用 `.mcp-http.example.json`：

```json
{
  "mcpServers": {
    "datasource": {
      "type": "http",
      "url": "http://127.0.0.1:8080/mcp/datasource"
    },
    "online": {
      "type": "http",
      "url": "http://127.0.0.1:8080/mcp/online"
    }
  }
}
```

Claude Code 也可以用命令添加：

```bash
claude mcp add --transport http datasource http://127.0.0.1:8080/mcp/datasource
claude mcp add --transport http online http://127.0.0.1:8080/mcp/online
```

## 已实现的工具

Datasource server:

- `datasource_list_tables`
- `datasource_query_demo_data`

Online server:

- `online_current_time`
- `online_fetch_url`

这个 demo 是 stateless Streamable HTTP MCP，未实现 OAuth、resource、prompt 和 server-initiated SSE。
