// 🔌 GMCP Native Client & Local Tool Dispatcher
// 100% Rust implementation for calling MCP servers and executing tools

use std::path::Path;
use super::tools::{McpTool, ToolRegistry};

pub struct GmcpClient;

impl GmcpClient {
    pub fn list_tools() -> Vec<McpTool> {
        ToolRegistry::list_tools()
    }

    #[allow(dead_code)]
    pub fn execute_tool(name: &str, arg: &str, workspace: &Path) -> String {
        ToolRegistry::execute_tool(name, arg, workspace)
    }
}
