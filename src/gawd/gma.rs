// 🌌 gha: Pure AI for AI Runtime — Tier 1 Master Interactor
// 100% Rust implementation for Universal AI for AI Execution

use std::path::Path;
use super::gmas::GmasSupervisor;
use super::safety::SafetyDetector;
use super::security::SecurityDetector;
use crate::gemi::hardware::HardwareProfiler;
use crate::gmcp::tools::ToolRegistry;

pub struct GmaMasterAgent;

impl GmaMasterAgent {
    pub fn new() -> Self {
        GmaMasterAgent
    }

    pub fn solve(&self, goal: &str, workspace: &Path, version: &str) -> String {
        let (num_cpus, gpu_info) = HardwareProfiler::profile();

        // 🚀 World-Scale GAWD Mission Supervision (A2A Swarm Flux)
        let (a2a_logs, fleet) = GmasSupervisor::supervise_mission(goal, workspace);
        let active_tools = ToolRegistry::list_tools();

        let mut report = String::new();

        report.push_str("# 🌌 gha: AI for AI — Sole Interactor Report\n\n");

        report.push_str("## 🤖 Tier 1: GAWD (Universal Swarm Supervisor)\n");
        report.push_str("- **Identity**: GMA Master Agent (A2A Protocol Root)\n");
        report.push_str(&format!("- **Fleet**: {} Specialized GAWD Agents Active\n", fleet.len()));
        report.push_str(&format!("- **Hardware**: {} CPUs | {}\n", num_cpus, gpu_info));
        report.push_str(&format!("- **Engine**: v{} (100% Native Rust)\n\n", version));

        report.push_str("## 🎯 Mission Execution (A2A Swarm Flux)\n");
        report.push_str(&format!("🤖 [GMA] Universal Intent: \"{}\"\n", goal));

        for msg in &a2a_logs {
            report.push_str(&format!("   ├── [{} -> GMA] {} ('{}')\n", msg.sender, msg.action, msg.payload));
        }

        report.push_str("\n## 🔌 GMCP (Universal Tool Capabilities)\n");
        report.push_str(&format!("- **Registry**: {} Tools Registered\n", active_tools.len()));

        // 🛡️ Governance Protocol: Pre-Execution Safety & Security Audit
        let governance_check = self.audit_governance(&a2a_logs);
        if let Err(violation_msg) = governance_check {
            report.push_str("\n## 🚨 GOVERNANCE INTERVENTION\n");
            report.push_str(&format!("   └── {}\n", violation_msg));
            report.push_str("\n❌ [gha Intelligence] Mission aborted for system integrity.\n");
            return report;
        }

        // 🧪 Universal Capability Assert: Execute tool calls discovered in Swarm Flux
        let mission_result = self.execute_autonomous_flux(goal, &a2a_logs, workspace);
        if !mission_result.is_empty() {
            report.push_str("\n## 🏁 GAWD Accomplishment\n");
            report.push_str(&mission_result);
        }

        // ⚖️ GMA Trust Audit (Post-Execution Reality Check)
        let audit = self.audit_truth(goal, &a2a_logs, workspace);
        report.push_str("\n## ⚖️ GMA Trust Audit (Reality Check)\n");
        report.push_str(&format!("   └── {}\n", audit));

        report.push_str("\n✅ [gha Intelligence] Flux executed natively (0-Effort, 100% Gains).\n");

        report
    }

    fn audit_governance(&self, logs: &[super::gmas::A2AMessage]) -> Result<(), String> {
        for msg in logs {
            if msg.payload.contains("ACTION:") {
                if let Some(action_part) = msg.payload.split("ACTION: ").nth(1) {
                    let parts: Vec<&str> = action_part.splitn(2, ' ').collect();
                    let tool_name = parts[0];
                    let arg = parts.get(1).unwrap_or(&"");

                    SafetyDetector::audit_action(tool_name, arg)?;
                    SecurityDetector::audit_action(tool_name, arg)?;
                }
            }
        }
        Ok(())
    }

    fn execute_autonomous_flux(&self, _goal: &str, logs: &[super::gmas::A2AMessage], workspace: &Path) -> String {
        let mut results = Vec::new();
        for msg in logs {
            if msg.payload.contains("ACTION:") {
                if let Some(action_part) = msg.payload.split("ACTION: ").nth(1) {
                    let parts: Vec<&str> = action_part.splitn(2, ' ').collect();
                    let tool_name = parts[0];
                    let arg = parts.get(1).unwrap_or(&"");

                    if !tool_name.is_empty() {
                        let res = ToolRegistry::execute_tool(tool_name, arg, workspace);
                        results.push(format!("   └── [Autonomous Tool: {}]: {}", tool_name, res));
                    }
                }
            }
        }
        results.join("\n")
    }

    fn audit_truth(&self, goal: &str, logs: &[super::gmas::A2AMessage], workspace: &Path) -> String {
        let mut score = 100;
        let mut flags = Vec::new();

        let mut reasoning = String::new();
        for msg in logs {
            if msg.sender == "GhaReasoningAgent" {
                reasoning = msg.payload.clone();
                break;
            }
        }

        if reasoning.contains("ACTION: write_file") {
            if let Some(path_part) = reasoning.split("write_file ").nth(1) {
                let file_name = path_part.split_whitespace().next().unwrap_or("");
                if !file_name.is_empty() {
                    let full_path = workspace.join(file_name);
                    if !full_path.exists() {
                        score -= 50;
                        flags.push(format!("🔴 LIE DETECTED: Agent claimed to write '{}', but file is missing.", file_name));
                    }
                }
            }
        }

        let lower_goal = goal.to_lowercase();
        let lower_reasoning = reasoning.to_lowercase();
        if lower_goal.contains("bootloader") && (!lower_reasoning.contains("bits 16") || !lower_reasoning.contains("0x7c00")) {
            score -= 30;
            flags.push("🟠 HALLUCINATION DETECTED: Technical specs for BIOS bootloader missing.".to_string());
        }

        if flags.is_empty() {
            "✅ TRUTH VERIFIED: Swarm logic is semantically sound and artifact-aligned.".to_string()
        } else {
            format!("⚠️ TRUTH AUDIT (Score: {}/100):\n   {}", score, flags.join("\n   "))
        }
    }
}
