# Spring MVC Native Multi MCP Server Demo

这个 demo 不使用 Spring AI，也不使用 MCP Java SDK。项目只依赖 Spring Boot Web，用 Spring MVC 接收 HTTP 请求，并手写 MCP 的 JSON-RPC 协议处理逻辑，在一个微服务中暴露两个独立 MCP server endpoint：

- `POST http://127.0.0.1:8080/mcp/datasource`
- `POST http://127.0.0.1:8080/mcp/online`

两个 endpoint 有独立的 `serverInfo`、`instructions`、工具列表和工具调用逻辑。核心分发逻辑在 `NativeMcpController`，协议实现逻辑在 `DefaultNativeMcpServer`。

## 实现范围

当前 demo 实现了 Claude / 常见 MCP 客户端连接时会用到的基础方法：

- `initialize`
- `notifications/initialized`，按 JSON-RPC notification 处理，不返回 body
- `ping`
- `tools/list`
- `tools/call`

工具返回 MCP 标准的 `content: [{ "type": "text", "text": "..." }]` 结构。这个 demo 是 stateless HTTP JSON-RPC 版本，未实现 OAuth、resource、prompt 和 server-initiated SSE。

## 核心代码

- `src/main/java/demo/mcp/protocol/NativeMcpController.java`
- `src/main/java/demo/mcp/protocol/DefaultNativeMcpServer.java`
- `src/main/java/demo/mcp/config/McpServerConfig.java`
- `src/main/java/demo/mcp/service/DatasourceTools.java`
- `src/main/java/demo/mcp/service/OnlineTools.java`

## 环境

- JDK 21+
- Maven 3.9+
- Node.js/npm，只有用 Claude Desktop + `mcp-remote` 本地桥接时需要

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

## 手工验证

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

调用 datasource 工具：

```powershell
curl.exe -s http://127.0.0.1:8080/mcp/datasource `
  -H "Content-Type: application/json" `
  -H "Accept: application/json, text/event-stream" `
  -d '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"datasource_query_demo_data","arguments":{"table":"customers","tier":"gold","limit":5}}}'
```

查看 online 工具：

```powershell
curl.exe -s http://127.0.0.1:8080/mcp/online `
  -H "Content-Type: application/json" `
  -H "Accept: application/json, text/event-stream" `
  -d '{"jsonrpc":"2.0","id":4,"method":"tools/list","params":{}}'
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

如果客户端支持 HTTP MCP transport，可以直接使用 `.mcp-http.example.json`。

## 已实现工具

Datasource server:

- `datasource_list_tables`
- `datasource_query_demo_data`

Online server:

- `online_current_time`
- `online_fetch_url`
