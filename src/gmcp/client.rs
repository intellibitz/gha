// 🔌 GMCP Universal Client: Bridges GHA to Industry Protocol Standard MCP Servers
// 100% Rust implementation for Stdio-based Multi-Server Orchestration

use std::collections::HashMap;
use std::fs;
use std::io::{BufRead, BufReader, Write};
use std::path::{Path, PathBuf};
use std::process::{Child, Command, Stdio};
use serde::{Deserialize, Serialize};
use serde_json::json;

use super::tools::McpTool;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct McpServerConfig {
    pub command: String,
    pub args: Vec<String>,
    pub env: Option<HashMap<String, String>>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct McpConfig {
    pub mcp_servers: HashMap<String, McpServerConfig>,
}

pub struct GmcpClient;

impl GmcpClient {
    pub fn get_config_path() -> PathBuf {
        let home = std::env::var("HOME").unwrap_or_default();
        PathBuf::from(home).join(".gha/mcp_config.json")
    }

    pub fn list_external_tools() -> Vec<McpTool> {
        let mut tools = Vec::new();
        let config_path = Self::get_config_path();

        if let Ok(content) = fs::read_to_string(&config_path) {
            if let Ok(config) = serde_json::from_str::<McpConfig>(&content) {
                for (name, srv) in config.mcp_servers {
                    // In a full implementation, we would spawn and call 'tools/list'
                    // For now, we register a prefix-based proxy tool
                    tools.push(McpTool {
                        name: format!("{}:*", name),
                        description: format!("Proxy for industry standard MCP server: {}", name),
                    });
                }
            }
        }
        tools
    }

    pub fn execute_external_tool(server_name: &str, tool_name: &str, args: &str) -> String {
        let config_path = Self::get_config_path();
        let config_content = match fs::read_to_string(&config_path) {
            Ok(c) => c,
            Err(_) => return format!("❌ MCP Error: Config not found at {}", config_path.display()),
        };

        let config: McpConfig = match serde_json::from_str(&config_content) {
            Ok(c) => c,
            Err(e) => return format!("❌ MCP Error: Config parse failed: {}", e),
        };

        let srv = match config.mcp_servers.get(server_name) {
            Some(s) => s,
            None => return format!("❌ MCP Error: Server '{}' not found in config.", server_name),
        };

        Self::proxy_call(srv, tool_name, args)
    }

    fn proxy_call(srv: &McpServerConfig, tool_name: &str, args_json: &str) -> String {
        let mut child = match Command::new(&srv.command)
            .args(&srv.args)
            .stdin(Stdio::piped())
            .stdout(Stdio::piped())
            .stderr(Stdio::null())
            .spawn() {
                Ok(c) => c,
                Err(e) => return format!("❌ MCP Error: Failed to spawn '{}': {}", srv.command, e),
            };

        let stdin = child.stdin.as_mut().unwrap();
        let stdout = child.stdout.as_mut().unwrap();
        let mut reader = BufReader::new(stdout);

        // 1. Initialize
        let init_req = json!({
            "jsonrpc": "2.0",
            "id": 1,
            "method": "initialize",
            "params": {
                "protocolVersion": "2024-11-05",
                "capabilities": {},
                "clientInfo": { "name": "gha-master", "version": "0.1.112" }
            }
        });
        let _ = writeln!(stdin, "{}", init_req.to_string());
        let mut line = String::new();
        let _ = reader.read_line(&mut line);

        // 2. Call Tool
        let params = match serde_json::from_str::<serde_json::Value>(args_json) {
            Ok(v) => v,
            Err(_) => json!({ "input": args_json }) // Fallback for raw string args
        };

        let call_req = json!({
            "jsonrpc": "2.0",
            "id": 2,
            "method": "tools/call",
            "params": {
                "name": tool_name,
                "arguments": params
            }
        });

        line.clear();
        let _ = writeln!(stdin, "{}", call_req.to_string());
        if reader.read_line(&mut line).is_ok() {
            let resp: serde_json::Value = serde_json::from_str(&line).unwrap_or(json!({}));
            if let Some(content) = resp.get("result").and_then(|r| r.get("content")).and_then(|c| c.get(0)).and_then(|i| i.get("text")).and_then(|t| t.as_str()) {
                return content.to_string();
            }
            return format!("🔌 [MCP Proxy Response]: {}", line.trim());
        }

        "❌ MCP Error: No response from server.".to_string()
    }
}
