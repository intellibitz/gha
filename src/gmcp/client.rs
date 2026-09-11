// 🔌 GMCP Universal Client: Bridges AEON to Industry Protocol Standard MCP Servers
// 100% Rust implementation for Stdio-based Multi-Server Orchestration

use std::collections::HashMap;
use std::fs;
use std::io::{BufRead, BufReader, Write};
use std::path::PathBuf;
use std::process::{Command, Stdio};
use std::time::Instant;
use serde_json::json;

use super::tools::McpTool;
use super::{GlobalMcpEntry, McpServerConfig, McpConfig};

pub struct GmcpClient;

impl GmcpClient {
    pub fn get_config_path() -> PathBuf {
        let home = std::env::var("HOME").unwrap_or_default();
        let aeon_dir = PathBuf::from(home).join(".aeon");
        if !aeon_dir.exists() {
            let _ = fs::create_dir_all(&aeon_dir);
        }
        aeon_dir.join("mcp_config.json")
    }

    pub fn list_external_tools() -> Vec<McpTool> {
        let mut tools = Vec::new();
        let config_path = Self::get_config_path();
        if let Ok(content) = fs::read_to_string(&config_path)
            && let Ok(config) = serde_json::from_str::<McpConfig>(&content)
        {
            for (name, _srv) in config.mcp_servers {
                tools.push(McpTool {
                    name: format!("{}:*", name),
                    description: format!("Proxy for industry standard MCP server: {}", name),
                });
            }
        }
        tools
    }

    pub fn fetch_global_registry() -> Vec<GlobalMcpEntry> {
        let home = std::env::var("HOME").unwrap_or_default();
        let global_dir = PathBuf::from(home).join(".aeon");
        let registry_path = global_dir.join("global_mcp_registry.json");
        let cfg = crate::sandbox::manager::AeonConfig::load(&global_dir);

        let mut entries: Vec<GlobalMcpEntry> = Vec::new();

        // 1. Try Online Registry Scout from Dynamic Config URL
        if let Ok(resp) = ureq::get(&cfg.mcp_registry_url).timeout(std::time::Duration::from_secs(10)).call() {
            let mut reader = resp.into_reader();
            if let Ok(remote_entries) = serde_json::from_reader::<_, Vec<GlobalMcpEntry>>(&mut reader) {
                if !remote_entries.is_empty() {
                    entries = remote_entries;
                }
            }
        }

        // 2. Read / Merge Local Dynamic Registry Overrides (~/.aeon/global_mcp_registry.json)
        if registry_path.is_file()
            && let Ok(content) = fs::read_to_string(&registry_path)
            && let Ok(local_entries) = serde_json::from_str::<Vec<GlobalMcpEntry>>(&content)
        {
            for local_entry in local_entries {
                if !entries.iter().any(|e| e.name == local_entry.name) {
                    entries.push(local_entry);
                }
            }
        }

        // 3. Fallback to Bootstrap Registry from Dynamic Config if empty
        if entries.is_empty() {
            entries = cfg.bootstrap_mcp_servers;
        }

        // Cache / persist merged registry locally
        let _ = fs::write(&registry_path, serde_json::to_string_pretty(&entries).unwrap_or_default());
        entries
    }

    #[allow(dead_code)]
    pub fn benchmark_server(name: &str) -> (u128, bool) {
        let start = Instant::now();
        let config_path = Self::get_config_path();
        if let Ok(content) = fs::read_to_string(&config_path)
            && let Ok(config) = serde_json::from_str::<McpConfig>(&content)
            && let Some(srv) = config.mcp_servers.get(name)
        {
            // Quick spawn test
            let child = Command::new(&srv.command).args(&srv.args).stdin(Stdio::piped()).stdout(Stdio::piped()).spawn();
            let success = child.is_ok();
            return (start.elapsed().as_millis(), success);
        }
        (0, false)
    }

    pub fn auto_configure_server(name: &str, package: &str) -> String {
        let config_path = Self::get_config_path();
        let mut config = if let Ok(content) = fs::read_to_string(&config_path) {
            serde_json::from_str::<McpConfig>(&content).unwrap_or(McpConfig { mcp_servers: HashMap::new() })
        } else {
            McpConfig { mcp_servers: HashMap::new() }
        };

        let has_uvx = Command::new("uvx").arg("--version").output().is_ok();
        let has_npx = Command::new("npx").arg("--version").output().is_ok();

        let (cmd, args) = if package.starts_with("pypi:") || package.starts_with("mcp-server-") || package.contains("python") {
            if has_uvx {
                ("uvx".to_string(), vec![package.trim_start_matches("pypi:").to_string()])
            } else if has_npx {
                ("npx".to_string(), vec!["-y".to_string(), package.trim_start_matches("pypi:").to_string()])
            } else {
                ("aeon".to_string(), vec!["mcp".to_string(), name.to_string()])
            }
        } else if has_npx {
            ("npx".to_string(), vec!["-y".to_string(), package.to_string()])
        } else {
            ("aeon".to_string(), vec!["mcp".to_string(), name.to_string()])
        };

        let new_srv = McpServerConfig {
            command: cmd,
            args,
            env: None,
        };

        config.mcp_servers.insert(name.to_string(), new_srv);
        if let Ok(updated) = serde_json::to_string_pretty(&config)
            && fs::write(&config_path, updated).is_ok()
        {
            return "SUCCESS_CONFIGURED".to_string();
        }
        "ERROR_FAILED".to_string()
    }

    #[allow(dead_code)]
    pub fn execute_category_tool(target_category: &str, query: &str) -> Option<String> {
        let registry = Self::fetch_global_registry();
        for entry in registry {
            if entry.category.eq_ignore_ascii_case(target_category) {
                let config_path = Self::get_config_path();
                let is_configured = if let Ok(content) = fs::read_to_string(&config_path)
                    && let Ok(config) = serde_json::from_str::<McpConfig>(&content)
                {
                    config.mcp_servers.contains_key(&entry.name)
                } else {
                    false
                };

                if !is_configured {
                    let _ = Self::auto_configure_server(&entry.name, &entry.package);
                }

                let tool_alias = format!("{}_search", entry.category);
                let res = Self::execute_external_tool(&entry.name, &tool_alias, query);
                if !res.contains("[FAIL]") {
                    return Some(res);
                }
            }
        }
        None
    }

    pub fn execute_external_tool(server_name: &str, tool_name: &str, args: &str) -> String {
        let config_path = Self::get_config_path();
        let config_content = match fs::read_to_string(&config_path) {
            Ok(c) => c,
            Err(_) => return format!("[FAIL] MCP Error: Config not found at {}", config_path.display()),
        };

        let config: McpConfig = match serde_json::from_str(&config_content) {
            Ok(c) => c,
            Err(e) => return format!("[FAIL] MCP Error: Config parse failed: {}", e),
        };

        let srv = match config.mcp_servers.get(server_name) {
            Some(s) => s,
            None => return format!("[FAIL] MCP Error: Server '{}' not found in config.", server_name),
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
                Err(e) => return format!("[FAIL] MCP Error: Failed to spawn '{}': {}", srv.command, e),
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
                "capabilities": {
                    "roots": { "listChanged": false },
                    "sampling": {}
                },
                "clientInfo": { "name": "aeon-master", "version": crate::AEON_VERSION }
            }
        });
        let _ = writeln!(stdin, "{}", init_req);
        let mut line = String::new();
        let _ = reader.read_line(&mut line);

        // 2. Call Tool
        let params = match serde_json::from_str::<serde_json::Value>(args_json) {
            Ok(v) => v,
            Err(_) => json!({ "input": args_json })
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
        let _ = writeln!(stdin, "{}", call_req);
        if reader.read_line(&mut line).is_ok() {
            let resp: serde_json::Value = serde_json::from_str(&line).unwrap_or(json!({}));
            if let Some(content) = resp.get("result").and_then(|r| r.get("content")).and_then(|c| c.get(0)).and_then(|i| i.get("text")).and_then(|t| t.as_str()) {
                return content.to_string();
            }
            return format!("🔌 [MCP Proxy Response]: {}", line.trim());
        }

        "[FAIL] MCP Error: No response from server.".to_string()
    }

    #[allow(dead_code)]
    pub fn scout_tier3_assets() -> Vec<crate::gawd::agents::DiscoverableAsset> {
        vec![
            crate::gawd::agents::DiscoverableAsset {
                tier: "Tier 3: GMCP (Capabilities)".to_string(),
                name: "AEON Substrate Protocol".to_string(),
                provider: "AEON Engine".to_string(),
                url: "https://aeon.ai/download".to_string(),
            },
            crate::gawd::agents::DiscoverableAsset {
                tier: "Tier 3: GMCP (Capabilities)".to_string(),
                name: "Meta MCP Protocol Hub".to_string(),
                provider: "MCP Standard".to_string(),
                url: "https://modelcontextprotocol.io".to_string(),
            },
        ]
    }
}
