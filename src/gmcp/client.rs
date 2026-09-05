// 🔌 GMCP Native Client
// 100% Rust implementation for calling MCP servers

use super::tools::{McpTool, ToolRegistry};

pub struct GmcpClient;

impl GmcpClient {
    pub fn list_tools() -> Vec<McpTool> {
        ToolRegistry::list_tools()
    }
}
