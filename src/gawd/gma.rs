// 🌌 gha: Pure AI for AI Runtime — Tier 1 Master Interactor
// 100% Rust implementation for Universal AI for AI Execution

use std::path::Path;
use super::gmas::GmasSupervisor;
use crate::gemi::hardware::HardwareProfiler;
use crate::gmcp::tools::ToolRegistry;

pub struct GmaMasterAgent;

impl GmaMasterAgent {
    pub fn new() -> Self {
        GmaMasterAgent
    }

    pub fn solve(&self, goal: &str, workspace: &Path, version: &str) -> String {
        let (num_cpus, gpu_info) = HardwareProfiler::profile();

        // 🚀 World-Scale GAWD Mission Supervision (A2A Swarm)
        let (a2a_logs, fleet) = GmasSupervisor::supervise_mission(goal, workspace);
        let active_tools = ToolRegistry::list_tools();
        let cluster_nodes = GmasSupervisor::list_cluster_nodes();

        let mut report = String::new();

        report.push_str("# 🌌 gha: AI for AI — Sole Interactor Report\n\n");

        report.push_str("## 🤖 Tier 1: GAWD (Universal Swarm Supervisor)\n");
        report.push_str("- **Identity**: GMA Master Agent (A2A Protocol Root)\n");
        report.push_str(&format!("- **Fleet**: {} GAWD Agents | {} World-Scale Nodes Active\n", fleet.len(), cluster_nodes.len()));
        report.push_str(&format!("- **Hardware**: {} CPUs | {}\n", num_cpus, gpu_info));
        report.push_str(&format!("- **Engine**: v{} (100% Native Rust)\n\n", version));

        report.push_str("## 🎯 Mission Execution (A2A Swarm Flux)\n");
        report.push_str(&format!("🤖 [GMA] Universal Intent: \"{}\"\n", goal));

        for msg in &a2a_logs {
            report.push_str(&format!("   ├── [{} -> {}] {} ('{}')\n", msg.sender, msg.recipient, msg.action, msg.payload));
        }

        report.push_str("\n## 🔌 GMCP (Universal Tool Capabilities)\n");
        report.push_str(&format!("- **Registry**: {} Tools Registered\n", active_tools.len()));

        // 🧪 Universal Capability Assert: Execute tool calls discovered in Swarm Reasoning
        let mission_result = self.execute_autonomous_flux(goal, &a2a_logs, workspace);
        if !mission_result.is_empty() {
            report.push_str("\n## 🏁 GAWD Accomplishment\n");
            report.push_str(&mission_result);
        }

        report.push_str("\n✅ [gha Intelligence] Flux executed natively (0-Effort, 100% Gains).\n");

        report
    }

    fn execute_autonomous_flux(&self, _goal: &str, logs: &[super::gmas::A2AMessage], workspace: &Path) -> String {
        let mut results = Vec::new();

        // Detect and execute tool calls from any agent in the swarm
        for msg in logs {
            if msg.payload.contains("ACTION:") {
                if let Some(action_part) = msg.payload.split("ACTION: ").nth(1) {
                    let tool_name = action_part.split_whitespace().next().unwrap_or("");
                    let arg = action_part.splitn(2, ' ').nth(1).unwrap_or("").trim();

                    if !tool_name.is_empty() {
                        let res = ToolRegistry::execute_tool(tool_name, arg, workspace);
                        results.push(format!("   └── [Autonomous Tool: {}]: {}", tool_name, res));
                    }
                }
            }
        }

        if results.is_empty() {
            "".to_string()
        } else {
            results.join("\n")
        }
    }
}
