// 🔌 GMCP Native Server: JSON-RPC 2.0 MCP Host & Server over stdio & TCP Port 9090
// 100% Rust implementation supporting dynamic tool execution

use super::tools::ToolRegistry;
use std::io::{self, BufRead, Write};
use std::path::Path;

pub struct GmcpServer;

impl GmcpServer {
    pub fn run_stdio(workspace: &Path, version: &str) {
        eprintln!("🔌 [GMCP Server] Started (Listening on stdio for external AI clients like Cursor/Claude).");
        let stdin = io::stdin();
        let mut stdout = io::stdout();

        for line in stdin.lock().lines().map_while(Result::ok) {
            let trimmed = line.trim();
            if trimmed.is_empty() {
                continue;
            }

            if trimmed.contains("\"method\":\"initialize\"") {
                let id = extract_json_id(trimmed).unwrap_or(1);
                let resp = format!(
                    "{{\"jsonrpc\":\"2.0\",\"id\":{},\"result\":{{\"protocolVersion\":\"2024-11-05\",\"capabilities\":{{\"tools\":{{}}}},\"serverInfo\":{{\"name\":\"gmcp-native-server\",\"version\":\"{}\"}}}}}}\n",
                    id, version
                );
                let _ = stdout.write_all(resp.as_bytes());
                let _ = stdout.flush();
            } else if trimmed.contains("\"method\":\"tools/list\"") {
                let id = extract_json_id(trimmed).unwrap_or(2);
                let tools = ToolRegistry::list_tools();
                let tools_json: Vec<String> = tools
                    .iter()
                    .map(|t| format!("{{\"name\":\"{}\",\"description\":\"{}\"}}", t.name, t.description))
                    .collect();
                let resp = format!(
                    "{{\"jsonrpc\":\"2.0\",\"id\":{},\"result\":{{\"tools\":[{}]}}}}\n",
                    id, tools_json.join(",")
                );
                let _ = stdout.write_all(resp.as_bytes());
                let _ = stdout.flush();
            } else if trimmed.contains("\"method\":\"tools/call\"") {
                let id = extract_json_id(trimmed).unwrap_or(3);
                let tool_name = extract_tool_name(trimmed).unwrap_or_else(|| "status".to_string());
                let tool_arg = extract_tool_arg(trimmed).unwrap_or_default();

                let result_text = ToolRegistry::execute_tool(&tool_name, &tool_arg, workspace);
                let escaped_text = serde_json::to_string(&result_text).unwrap_or_default();

                let resp = format!(
                    "{{\"jsonrpc\":\"2.0\",\"id\":{},\"result\":{{\"content\":[{{\"type\":\"text\",\"text\":{}}}]}}}}\n",
                    id, escaped_text
                );
                let _ = stdout.write_all(resp.as_bytes());
                let _ = stdout.flush();
            }
        }
    }
}

pub fn extract_json_id(line: &str) -> Option<u64> {
    if let Ok(v) = serde_json::from_str::<serde_json::Value>(line) {
        if let Some(id) = v.get("id").and_then(|i| i.as_u64()) {
            return Some(id);
        }
    }
    None
}

pub fn extract_tool_name(line: &str) -> Option<String> {
    if let Ok(v) = serde_json::from_str::<serde_json::Value>(line) {
        if let Some(params) = v.get("params") {
            if let Some(name) = params.get("name").and_then(|n| n.as_str()) {
                return Some(name.to_string());
            }
        }
    }
    None
}

pub fn extract_tool_arg(line: &str) -> Option<String> {
    if let Ok(v) = serde_json::from_str::<serde_json::Value>(line) {
        if let Some(params) = v.get("params") {
            if let Some(arguments) = params.get("arguments") {
                return if let Some(s) = arguments.as_str() {
                    Some(s.to_string())
                } else if let Some(command) = arguments.get("command").and_then(|c| c.as_str()) {
                    Some(command.to_string())
                } else if let Some(path) = arguments.get("path").and_then(|p| p.as_str()) {
                    Some(path.to_string())
                } else {
                    Some(arguments.to_string())
                };
            }
        }
    }
    None
}
