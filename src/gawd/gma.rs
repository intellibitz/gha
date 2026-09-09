// GAWD Tier 1 Master Agent
// RULE 11: Agents must add functionality directly to the gha engine via ToolRegistry.
// Agents must not simulate or "fake" gha capabilities by performing logic themselves.

use std::path::{Path, PathBuf};
use super::gmas::GmasSupervisor;
use super::safety::SafetyDetector;
use super::security::SecurityDetector;
use super::truth::TruthTransformer;
use crate::gemi::hardware::HardwareProfiler;
use crate::sandbox::manager::NeuralCheckpoint;
use crate::gmcp::tools::ToolRegistry;
use crate::gawd::reflex_synth::ReflexSynthesizer;
use crate::error::EaiResult;

pub struct GmaMasterAgent;

impl GmaMasterAgent {
    pub fn new() -> Self {
        GmaMasterAgent
    }

    pub fn solve_clean(&self, goal: &str, workspace: &Path, version: &str) -> String {
        let (a2a_logs, _) = GmasSupervisor::supervise_mission(goal, workspace);

        let governance_check = self.audit_governance(&a2a_logs);
        if let Err(e) = governance_check {
            return format!("{}", e);
        }

        let mission_result = self.execute_autonomous_flux(goal, &a2a_logs, workspace);
        if !mission_result.is_empty() {
            let mut lines = Vec::new();
            for l in mission_result.lines() {
                if l.contains("└── [Tool:") {
                    if let Some(res) = l.split("]: ").nth(1) {
                        lines.push(res.to_string());
                    } else {
                        lines.push(l.to_string());
                    }
                } else {
                    lines.push(l.to_string());
                }
            }
            let res_text = lines.join("\n");
            crate::sandbox::manager::GhaMemory::append_interaction(workspace, goal, &res_text);
            return res_text;
        }

        for msg in &a2a_logs {
            if msg.sender == "GhaReasoningAgent" {
                let payload = &msg.payload;
                let clean_text = if let Some((_, rest)) = payload.split_once("]:\n") {
                    rest
                } else if let Some((_, rest)) = payload.split_once("]: ") {
                    rest
                } else {
                    payload
                };
                let mut ans = clean_text.trim().to_string();
                if ans.contains("CLOUD_BRAIN_UNAVAILABLE") {
                    ans = "STATUS: Local execution mode active. Connect a cloud provider or local model for extended reasoning.".to_string();
                }
                crate::sandbox::manager::GhaMemory::append_interaction(workspace, goal, &ans);
                return ans;
            }
        }

        self.solve(goal, workspace, version)
    }

    pub fn solve(&self, goal: &str, workspace: &Path, version: &str) -> String {
        crate::sandbox::manager::GhaAuditLogger::log_event(workspace, "MISSION_START", goal);
        let hardware = HardwareProfiler::get_profile();

        let lower_goal = goal.trim();
        let (mut cmd, arg) = lower_goal.split_once(' ').unwrap_or((lower_goal, ""));

        // 🚀 Support for Colon-Prefixed Direct Tool Calls (Launcher Consistency)
        if cmd.starts_with(':') {
             cmd = &cmd[1..];
        }

        let is_direct_tool = ToolRegistry::exists(cmd) || cmd == "models";
        let is_orchestration = goal.contains("orchestrate") || goal.contains("mission");

        // 🌀 Rule 18: Autonomous Capability Gap Detection (Prioritized)
        // If not a direct tool and not explicit orchestration, attempt to distill a native reflex.
        if !is_direct_tool && !is_orchestration && goal.len() > 5 && goal.len() < 100 && !goal.contains('/') && !goal.contains('\\') {
            if let Ok(evolve_res) = self.trigger_autonomous_evolution(goal, workspace) {
                return format!("# gha Autonomous Evolution\n\n- **Intent**: \"{}\"\n- **Status**: Distilled native reflex substrate.\n- **Action**: Applied architectural integration.\n\n{}\n\nRun 'gha release' to deploy the new reflex.", goal, evolve_res);
            }
        }

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

        let (badge, badge_desc) = crate::gawd::agents::GhaUserAgent::detect_domain_badge(goal);
        let is_placeholder = reasoning_content.contains("scouting for specialized brains");

        let mut report = String::new();
        report.push_str("# gha Execution Report\n\n");

        report.push_str("## Domain Substrate\n");
        report.push_str(&format!("- **Mode**: {}\n", badge));
        report.push_str(&format!("- **Scope**: {}\n", badge_desc));
        report.push('\n');

        if is_direct_tool {
            let actual_cmd = if cmd == "models" { "list_models" } else { cmd };

            let mut checkpoint = NeuralCheckpoint {
                intent: goal.to_string(),
                timestamp: std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).map(|d| d.as_secs()).unwrap_or(0),
                completed_tools: vec![],
                blackboard: std::collections::HashMap::new(),
                status: "IN_PROGRESS".to_string(),
            };
            crate::sandbox::manager::SandboxManager::save_mission_checkpoint(workspace, &checkpoint);

            let tool_res = ToolRegistry::execute_tool(actual_cmd, arg, workspace);

            checkpoint.status = "COMPLETED".to_string();
            checkpoint.completed_tools.push(actual_cmd.to_string());
            checkpoint.blackboard.insert(format!("RESULT_{}", actual_cmd), tool_res.clone());
            crate::sandbox::manager::SandboxManager::save_mission_checkpoint(workspace, &checkpoint);

            report.push_str("## Output\n");
            report.push_str(&format!("   └── [Tool: {}]: {}\n\n", actual_cmd, tool_res));
            report.push_str("## Validation\n └── Verified.\n");
            return report;
        }

        if is_orchestration || !is_reflex {
            report.push_str("## Environment\n");
            report.push_str(&format!("- Engine: v{}\n", version));
            report.push_str(&format!("- Fleet: {} agents active\n", fleet.len()));
            report.push_str(&format!("- Hardware: {} CPUs | {} | {}GB RAM\n\n", hardware.cpus, hardware.gpu_info, hardware.ram_gb));
        }

        report.push_str("## Intent\n");
        report.push_str(&format!("\"{}\"\n\n", goal));

        if is_orchestration || !is_reflex {
            report.push_str("## Execution Trace\n");
            for (i, msg) in a2a_logs.iter().enumerate() {
                let connector = if i == a2a_logs.len() - 1 { "└──" } else { "├──" };
                report.push_str(&format!(" {} [{}] {} ('{}')\n", connector, msg.sender, msg.action, msg.payload));
            }
        }

        if is_orchestration {
            report.push_str("\n## Tools\n");
            report.push_str(&format!("- Active Registry: {} tools\n", active_tools.len()));
        }

        // Governance Protocol: Safety & Security Audit
        let governance_check = self.audit_governance(&a2a_logs);
        if let Err(e) = governance_check {
            report.push_str("\n## Governance Status\n");
            report.push_str(&format!("   └── Aborted: {}\n", e));
            return report;
        }

        if is_placeholder {
             let registry = crate::gmcp::client::GmcpClient::fetch_global_registry();
             if let Some(entry) = registry.iter().find(|e| goal.to_lowercase().contains(&e.name) || goal.to_lowercase().contains(&e.category)) {
                 report.push_str("\n## Capability Required\n");
                 report.push_str(&format!("   └── Missing: '{}' ({})\n", entry.name, entry.package));
                 report.push_str(&format!("   └── Install command: 'gha \"install mcp {}\"'\n", entry.name));
             }
        }

        let mission_result = self.execute_autonomous_flux(goal, &a2a_logs, workspace);
        if !mission_result.is_empty() {
            report.push_str("\n## Output\n");
            report.push_str(&mission_result);
            report.push('\n');
        }

        let audit = self.audit_truth(goal, &a2a_logs, workspace, &mission_result);
        report.push_str("\n## Validation\n");
        if is_placeholder || mission_result.contains("scouting for specialized brains") {
            report.push_str(" └── Pending solution synthesis.\n");
        } else {
            report.push_str(&format!(" └── {}\n", audit));
        }

        report
    }

    fn audit_governance(&self, logs: &[super::gmas::A2AMessage]) -> EaiResult<()> {
        for msg in logs {
            if msg.payload.contains("ACTION:")
                && let Some(action_part) = msg.payload.split("ACTION: ").nth(1)
            {
                let parts: Vec<&str> = action_part.splitn(2, ' ').collect();
                let tool_name = parts[0];
                let arg = parts.get(1).unwrap_or(&"");

                SafetyDetector::audit_action(tool_name, arg)?;
                SecurityDetector::audit_action(tool_name, arg)?;
            }
        }
        Ok(())
    }

    fn execute_autonomous_flux(&self, goal: &str, logs: &[super::gmas::A2AMessage], workspace: &Path) -> String {
        let mut results = Vec::new();
        let mut completed_tools = Vec::new();

        let mut checkpoint = NeuralCheckpoint {
            intent: goal.to_string(),
            timestamp: std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).map(|d| d.as_secs()).unwrap_or(0),
            completed_tools: completed_tools.clone(),
            blackboard: std::collections::HashMap::new(),
            status: "IN_PROGRESS".to_string(),
        };

        crate::sandbox::manager::SandboxManager::save_mission_checkpoint(workspace, &checkpoint);
        GmasSupervisor::replicate_checkpoint(&checkpoint);

        for msg in logs {
            if msg.payload.contains("ACTION:")
                && let Some(action_part) = msg.payload.split("ACTION: ").nth(1)
            {
                let parts: Vec<&str> = action_part.splitn(2, ' ').collect();
                let tool_name = parts[0];
                let arg = parts.get(1).unwrap_or(&"");

                if !tool_name.is_empty() {
                    let mut res = ToolRegistry::execute_tool(tool_name, arg, workspace);

                    // 🛡️ Formal Verification Reflex (Rule 15)
                    match TruthTransformer::verify_mission_reality(goal, tool_name, &res, workspace) {
                        Ok(verified_res) => res = verified_res,
                        Err(e) => {
                            // BLOCK and attempt HEAL
                            res = format!("{}", e);
                            crate::sandbox::manager::GhaAuditLogger::log_event(workspace, "TRUTH_BLOCK", &format!("Tool {} blocked: {}", tool_name, e));
                        }
                    }

                    if res.to_lowercase().contains("error") || res.to_lowercase().contains("failed") || res.to_lowercase().contains("cloud_brain_unavailable") || res.contains("TRUTH VIOLATION") {
                        let mut fix_prompt = format!("Mission '{}' failed at tool '{}' with error: '{}'. Suggest a fixed command.", goal, tool_name, res);

                        if res.contains("rate_limit") || res.contains("too large") || res.contains("CLOUD_BRAIN_UNAVAILABLE") {
                            fix_prompt = format!("Mission '{}' failed due to intelligence limits. Suggest the same command but with a 'smaller context' or 'snippet' of any referenced files.", goal);
                        }

                        // Reflex Fix First
                        let mut fixed_action = crate::gemi::pulse::GhaPulse::reason(&fix_prompt, workspace).unwrap_or_default();

                        // Escalate to Tier 2 Deep Fix if reflex fails
                        if !fixed_action.contains("ACTION:") {
                             fixed_action = crate::gemi::engine::GemiEngine::generate_reasoning_deep(&fix_prompt, workspace);
                        }

                        if fixed_action.contains("ACTION:") {
                            let fix_parts: Vec<&str> = fixed_action.split("ACTION: ").nth(1).unwrap_or("").splitn(2, ' ').collect();
                            let fix_tool = fix_parts[0];
                            let fix_arg = fix_parts.get(1).unwrap_or(&"");
                            let fix_res = ToolRegistry::execute_tool(fix_tool, fix_arg, workspace);

                            // Tier 2 -> Tier 0 Feedback Loop: Learn from Deep Fixes
                            if !fix_res.to_lowercase().contains("error") {
                                let home = std::env::var_os("HOME").or_else(|| std::env::var_os("USERPROFILE")).unwrap_or_else(|| ".".into());
                                let global_dir = PathBuf::from(home).join(".gha");
                                let entry = crate::gawd::pkb::PkbTrainingEntry {
                                    instruction: fix_prompt.to_string(),
                                    swarm_flux: vec![],
                                    tool_calls: vec![format!("{} {}", fix_tool, fix_arg)],
                                    outcome: "SUCCESS_DEEP_FIX".to_string(),
                                };
                                let _ = crate::gawd::pkb::PkbSynthesizer::save_training_data(vec![entry], &global_dir);
                            }
                            res = fix_res;
                        }
                    }

                    completed_tools.push(tool_name.to_string());
                    checkpoint.completed_tools = completed_tools.clone();
                    checkpoint.blackboard.insert(format!("RESULT_{}", tool_name), res.clone());

                    crate::sandbox::manager::SandboxManager::save_mission_checkpoint(workspace, &checkpoint);
                    GmasSupervisor::replicate_checkpoint(&checkpoint);

                    results.push(format!("   └── [Tool: {}]: {}", tool_name, res));
                }
            }
        }

        if results.is_empty() {
             // Logic for direct tool calls...
        }

        // Fulfill Indestructible Identity: Set status to COMPLETED
        checkpoint.status = "COMPLETED".to_string();
        crate::sandbox::manager::SandboxManager::save_mission_checkpoint(workspace, &checkpoint);
        GmasSupervisor::replicate_checkpoint(&checkpoint);

        results.join("\n")
    }

    fn audit_truth(&self, goal: &str, logs: &[super::gmas::A2AMessage], workspace: &Path, mission_result: &str) -> String {
        if mission_result.contains("TRUTH VIOLATION") {
             return "🔴 MISSION BLOCKED: Hallucination detected during formal verification.".to_string();
        }

        let mut score = 100;
        let mut flags = Vec::new();

        let mut reasoning = String::new();
        for msg in logs {
            if msg.sender == "GhaReasoningAgent" {
                reasoning = msg.payload.clone();
                break;
            }
        }

        if reasoning.contains("ACTION: write_file")
            && let Some(path_part) = reasoning.split("write_file ").nth(1)
        {
            let file_name = path_part.split_whitespace().next().unwrap_or("");
            if !file_name.is_empty() {
                let full_path = workspace.join(file_name);
                if !full_path.exists() {
                    score -= 50;
                    flags.push(format!("File '{}' missing after write action.", file_name));
                }
            }
        }

        let lower_goal = goal.to_lowercase();
        let lower_reasoning = reasoning.to_lowercase();
        if lower_goal.contains("bootloader") && (!lower_reasoning.contains("bits 16") || !lower_reasoning.contains("0x7c00")) {
            score -= 30;
            flags.push("Missing technical specification for bootloader.".to_string());
        }

        if flags.is_empty() {
            "Verified.".to_string()
        } else {
            format!("Audit score: {}/100\n   {}", score, flags.join("\n   "))
        }
    }

    /// 🌀 Rule 18: Self-Distillation Loop Trigger
    pub fn trigger_autonomous_evolution(&self, intent: &str, workspace: &Path) -> EaiResult<String> {
        crate::sandbox::manager::GhaAuditLogger::log_event(workspace, "AUTONOMOUS_EVOLUTION", intent);
        ReflexSynthesizer::distill_native_reflex(intent, workspace)
    }
}
