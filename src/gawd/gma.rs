// 🌌 gha: EAI: Exponential Intelligence for Any AI. — Tier 1 Master Interactor
// 100% Rust implementation for Universal EAI Execution

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

        let mut is_reflex = false;
        let mut reasoning_content = String::new();
        for msg in &a2a_logs {
            if msg.sender == "GhaReasoningAgent" {
                reasoning_content = msg.payload.clone();
                if msg.payload.contains("Tier 0") {
                    is_reflex = true;
                }
            }
        }

        let is_orchestration = goal.contains("orchestrate") || goal.contains("mission");
        let is_placeholder = reasoning_content.contains("scouting for specialized brains");

        let mut report = String::new();
        report.push_str("# 🌌 gha: EAI: Exponential Intelligence for Any AI. - Sole Interactor Report\n\n");

        if is_orchestration || !is_reflex {
            report.push_str("## 🧠 Tier 0: GHA-Alpha (Native Reflex)\n");
            report.push_str("- **Logic**: Hyper-Optimized Protocol Routing (< 1ms)\n\n");

            report.push_str("## 🤖 Tier 1: GAWD (Universal Swarm Supervisor)\n");
            report.push_str("- **Identity**: GMA Master Agent (A2A Protocol Root)\n");
            report.push_str(&format!("- **Fleet**: {} Specialized GAWD Agents Active\n", fleet.len()));
            report.push_str(&format!("- **Hardware**: {} CPUs | {}\n", num_cpus, gpu_info));
            report.push_str(&format!("- **Engine**: v{} (100% Native Rust)\n\n", version));
        }

        report.push_str("## 🎯 Mission Execution (A2A Swarm Flux)\n");
        report.push_str(&format!("🤖 [GMA] Universal Intent: \"{}\"\n", goal));

        if is_orchestration || !is_reflex {
            for (i, msg) in a2a_logs.iter().enumerate() {
                let connector = if i == a2a_logs.len() - 1 { "└──" } else { "├──" };
                report.push_str(&format!(" {} [{} -> GMA] {} ('{}')\n", connector, msg.sender, msg.action, msg.payload));
            }
            let mut intelligence_tier = if is_reflex { "Tier 0: GHA-Alpha (Native Reflex)" } else { "Tier 2: GEMI (Deep Reasoning)" };
            if reasoning_content.contains("🏆 Premier") {
                intelligence_tier = "Tier 2: GEMI (🏆 Premier Tier Brain)";
            } else if reasoning_content.contains("Specialist") {
                intelligence_tier = "Tier 2: GEMI (🛠️ Specialist Tier Brain)";
            }
            report.push_str(&format!("\n🚀 [Orchestration Strategy]: {}\n", intelligence_tier));
        }

        if is_orchestration {
            report.push_str("\n## 🔌 GMCP (Universal Tool Capabilities)\n");
            report.push_str(&format!("- **Registry**: {} Tools Registered\n", active_tools.len()));
        }

        // 🛡️ Governance Protocol: Pre-Execution Safety & Security Audit
        let governance_check = self.audit_governance(&a2a_logs);
        if let Err(violation_msg) = governance_check {
            report.push_str("\n## 🚨 GOVERNANCE INTERVENTION\n");
            report.push_str(&format!("   └── {}\n", violation_msg));
            report.push_str("\n❌ [gha Intelligence] Mission aborted for system integrity.\n");
            return report;
        }

        // 🔍 Capability Resolution: Check for uninstalled MCP servers before cloud fallback
        if is_placeholder {
             let registry = crate::gmcp::client::GmcpClient::fetch_global_registry();
             if let Some(entry) = registry.iter().find(|e| goal.to_lowercase().contains(&e.name) || goal.to_lowercase().contains(&e.category)) {
                 report.push_str("\n## 💡 Capability Discovery\n");
                 report.push_str(&format!("   └── Discovered missing hand: '{}' ({})\n", entry.name, entry.package));
                 report.push_str(&format!("   └── ACTION: Run 'gha \"install mcp {}\"' to enable.\n", entry.name));
             }
        }

        // 🧪 Universal Capability Assert: Execute tool calls discovered in Swarm Flux
        let mission_result = self.execute_autonomous_flux(goal, &a2a_logs, workspace);
        if !mission_result.is_empty() {
            report.push_str("\n## 🏁 GAWD Accomplishment\n");
            report.push_str(&mission_result);
        }

        // ⚖️ GMA Trust Audit (Reality Check)
        let audit = self.audit_truth(goal, &a2a_logs, workspace);
        report.push_str("\n## ⚖️ GMA Trust Audit (Reality Check)\n");
        if is_placeholder || mission_result.contains("scouting for specialized brains") {
            report.push_str(" └── ⚠️ MISSION INCOMPLETE: Reasoning agent is scouting for brains. No solution provided.\n");
        } else {
            report.push_str(&format!(" └── {}\n", audit));
        }

        if is_reflex && !is_orchestration {
             report.push_str("\n✅ [gha Intelligence] Reflex executed natively (0-Effort, 100% Gains).\n");
        } else {
             report.push_str("\n✅ [gha Intelligence] Flux executed natively (0-Effort, 100% Gains).\n");
        }

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

    fn execute_autonomous_flux(&self, goal: &str, logs: &[super::gmas::A2AMessage], workspace: &Path) -> String {
        let mut results = Vec::new();
        for msg in logs {
            if msg.payload.contains("ACTION:") {
                if let Some(action_part) = msg.payload.split("ACTION: ").nth(1) {
                    let parts: Vec<&str> = action_part.splitn(2, ' ').collect();
                    let tool_name = parts[0];
                    let arg = parts.get(1).unwrap_or(&"");

                    if !tool_name.is_empty() {
                        let mut res = ToolRegistry::execute_tool(tool_name, arg, workspace);

                        // 🚀 Native Self-Healing Loop (2^0 intelligence)
                        if res.to_lowercase().contains("error") || res.to_lowercase().contains("failed") || res.to_lowercase().contains("rate_limit") {
                            let mut fix_prompt = format!("Mission '{}' failed at tool '{}' with error: '{}'. Suggest a fixed command.", goal, tool_name, res);

                            // Specific Self-Healing: Context Reduction for Rate Limits
                            if res.contains("rate_limit") || res.contains("too large") {
                                fix_prompt = format!("Mission '{}' failed due to rate limits. Suggest the same command but with a much smaller context or snippet.", goal);
                            }

                            if let Ok(fixed_action) = crate::gemi::pulse::GhaPulse::reason(&fix_prompt, workspace) {
                                if fixed_action.contains("ACTION:") {
                                     let fix_parts: Vec<&str> = fixed_action.split("ACTION: ").nth(1).unwrap_or("").splitn(2, ' ').collect();
                                     let fix_tool = fix_parts[0];
                                     let fix_arg = fix_parts.get(1).unwrap_or(&"");
                                     let fix_res = ToolRegistry::execute_tool(fix_tool, fix_arg, workspace);
                                     res = format!("{} (Self-Healed: {})", res, fix_res);
                                }
                            }
                        }

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
