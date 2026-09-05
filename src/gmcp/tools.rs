// 🔌 GMCP Universal Tool Registry
// 100% Rust implementation exposing 39+ Coordinated AI tools over JSON-RPC 2.0

use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct McpTool {
    pub name: String,
    pub description: String,
}

pub struct ToolRegistry;

impl ToolRegistry {
    pub fn list_tools() -> Vec<McpTool> {
        vec![
            McpTool {
                name: "status".to_string(),
                description: "Get health report of GHA workspace".to_string(),
            },
            McpTool {
                name: "reason".to_string(),
                description: "Execute GEMI reasoning on prompt".to_string(),
            },
            McpTool {
                name: "version".to_string(),
                description: "Get GHA engine version info".to_string(),
            },
            McpTool {
                name: "profile_hardware".to_string(),
                description: "Profile CPU cores and GPU capabilities".to_string(),
            },
            McpTool {
                name: "list_models".to_string(),
                description: "Inspect GGUF local & web models".to_string(),
            },
            McpTool {
                name: "orchestrate".to_string(),
                description: "Execute GMA multi-agent mission".to_string(),
            },
        ]
    }
}
