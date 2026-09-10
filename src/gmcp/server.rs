// 🔌 GMCP Native Server: JSON-RPC 2.0 MCP Host & Server over stdio & TCP Port 9090
// 100% Rust implementation supporting dynamic tool execution

use super::tools::ToolRegistry;
use std::io::{self, BufRead, BufReader, Write};
use std::net::TcpListener;
use std::path::{Path, PathBuf};
use std::thread;
use serde_json::json;

pub struct GmcpServer;

impl GmcpServer {
    /// 🚀 Run MCP server over stdio (Standard for GHA MCP Clients & IDE Substrates)
    pub fn run_stdio(workspace: &Path, version: &str) {
        eprintln!("🔌 [GMCP Server] Started (Listening on stdio).");
        let stdin = io::stdin();
        let mut stdout = io::stdout();

        for line in stdin.lock().lines().map_while(Result::ok) {
            if let Some(resp) = Self::process_json_rpc(&line, workspace, version) {
                let _ = writeln!(stdout, "{}", resp);
                let _ = stdout.flush();
            }
        }
    }

    /// 🌐 Run MCP server over TCP (Enables remote plugin integration)
    pub fn start_tcp_server(workspace: PathBuf, port: u16, version: String) {
        let addr = format!("127.0.0.1:{}", port);
        let listener = match TcpListener::bind(&addr) {
            Ok(l) => l,
            Err(e) => {
                eprintln!("⚠️ [GMCP TCP Server] Could not bind to {}: {}", addr, e);
                return;
            }
        };

        eprintln!("🔌 [GMCP Server] TCP Listener active at {}", addr);

        for stream in listener.incoming().flatten() {
            let ws = workspace.clone();
            let ver = version.clone();
            thread::spawn(move || {
                let mut reader = BufReader::new(&stream);
                let mut writer = &stream;
                let mut line = String::new();

                while reader.read_line(&mut line).is_ok() {
                    if line.trim().is_empty() { break; }
                    if let Some(resp) = Self::process_json_rpc(&line, &ws, &ver) {
                        let _ = writeln!(writer, "{}", resp);
                    }
                    line.clear();
                }
            });
        }
    }

    /// 🧠 Core JSON-RPC 2.0 Logic (Universal Interop)
    fn process_json_rpc(line: &str, workspace: &Path, version: &str) -> Option<String> {
        let trimmed = line.trim();
        if trimmed.is_empty() { return None; }

        if trimmed.contains("\"method\":\"initialize\"") {
            let id = extract_json_id(trimmed).unwrap_or(json!(1));
            let resp_val = json!({
                "jsonrpc": "2.0",
                "id": id,
                "result": {
                    "protocolVersion": "2024-11-05",
                    "capabilities": {
                        "tools": { "listChanged": false }
                    },
                    "serverInfo": {
                        "name": "gmcp-native-server",
                        "version": version
                    }
                }
            });
            return serde_json::to_string(&resp_val).ok();
        }

        if trimmed.contains("\"method\":\"tools/list\"") {
            let id = extract_json_id(trimmed).unwrap_or(json!(2));
            let tools = ToolRegistry::list_tools();
            let tools_json: Vec<serde_json::Value> = tools
                .iter()
                .map(|t| json!({"name": t.name, "description": t.description}))
                .collect();
            let resp_val = json!({
                "jsonrpc": "2.0",
                "id": id,
                "result": {
                    "tools": tools_json
                }
            });
            return serde_json::to_string(&resp_val).ok();
        }

        if trimmed.contains("\"method\":\"tools/call\"") {
            let id = extract_json_id(trimmed).unwrap_or(json!(3));
            let tool_name = extract_tool_name(trimmed).unwrap_or_else(|| "status".to_string());
            let tool_arg = extract_tool_arg(trimmed).unwrap_or_default();

            let result_text = ToolRegistry::execute_tool(&tool_name, &tool_arg, workspace);

            let resp_val = json!({
                "jsonrpc": "2.0",
                "id": id,
                "result": {
                    "content": [
                        {
                            "type": "text",
                            "text": result_text
                        }
                    ]
                }
            });
            return serde_json::to_string(&resp_val).ok();
        }

        None
    }
}

pub fn extract_json_id(line: &str) -> Option<serde_json::Value> {
    if let Ok(v) = serde_json::from_str::<serde_json::Value>(line)
        && let Some(id) = v.get("id")
    {
        return Some(id.clone());
    }
    None
}

pub fn extract_tool_name(line: &str) -> Option<String> {
    if let Ok(v) = serde_json::from_str::<serde_json::Value>(line)
        && let Some(params) = v.get("params")
        && let Some(name) = params.get("name").and_then(|n| n.as_str())
    {
        return Some(name.to_string());
    }
    None
}

pub fn extract_tool_arg(line: &str) -> Option<String> {
    if let Ok(v) = serde_json::from_str::<serde_json::Value>(line)
        && let Some(params) = v.get("params")
        && let Some(arguments) = params.get("arguments")
    {
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
    None
}
