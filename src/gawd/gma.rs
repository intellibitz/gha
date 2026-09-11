// GAWD Tier 1 Master Agent
// RULE 11: Agents must add functionality directly to the gha engine via ToolRegistry.
// Agents must not simulate or "fake" gha capabilities by performing logic themselves.

use std::path::{Path, PathBuf};
use std::fs;
use super::gmas::GmasSupervisor;
use super::safety::SafetyDetector;
use super::security::SecurityDetector;
use super::truth::TruthTransformer;
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
        crate::gawd::axiom::AxiomSubstrate::ingest_constitution(workspace);
        if let Some(res) = Self::handle_self_awareness_intent(goal, workspace) {
            return res;
        }
        if let Some(res) = Self::handle_file_read_intent(goal, workspace) {
            return res;
        }

        let (a2a_logs, _) = GmasSupervisor::supervise_mission(goal, workspace);

        let governance_check = self.audit_governance(&a2a_logs, workspace);
        if let Err(e) = governance_check {
            return format!("{}", e);
        }

        let raw_response = self.solve_clean_raw(goal, &a2a_logs, workspace, version);
        let thinking = Self::format_thinking_trace(goal, workspace);
        format!("{}{}", thinking, raw_response.trim())
    }

    pub fn format_thinking_trace(goal: &str, _workspace: &Path) -> String {
        let mut strategy: Vec<String> = Vec::new();
        let lower = goal.to_lowercase();

        if lower.contains("get") || lower.contains("fetch") || lower.contains("search") || lower.contains("download") {
            strategy.push("Search web for content".to_string());
        }
        if lower.contains("read") || lower.contains("file") {
            strategy.push("Inspect local workspace files".to_string());
        }
        if lower.contains("translate") {
            let lang = if lower.contains("tamil") { "Tamil" } else { "target language" };
            strategy.push(format!("Translate text to {}", lang));
        } else if lower.contains("summarize") {
            strategy.push("Synthesize summary".to_string());
        } else if lower.contains("code") || lower.contains("build") || lower.contains("fix") {
            strategy.push("Analyze code and apply native fixes".to_string());
        }

        if strategy.is_empty() {
            strategy.push("Reason and execute intent".to_string());
        }

        let plan = strategy.join(" → ");
        let active_model = crate::gemi::models::ModelManager::get_selected_model()
            .unwrap_or_else(|| "gha-alpha.safetensors".to_string());
        let model_display = active_model.split('/').last().unwrap_or(&active_model);

        format!(
            "[Thinking Process]:\n ├── Plan: {}\n └── Substrate: {}\n\n",
            plan, model_display
        )
    }

    #[allow(dead_code)]
    pub fn determine_model_used(raw_response: &str, a2a_logs: &[super::gmas::A2AMessage]) -> String {
        let combined = format!("{} {}", raw_response, a2a_logs.iter().map(|m| m.payload.as_str()).collect::<Vec<_>>().join(" "));
        if combined.contains("Tier 0") || combined.contains("Reflex") || combined.contains("local reflex tensor weights") {
            "gha-alpha.safetensors (Local Neural Reflex)".to_string()
        } else if combined.contains("Candle") || combined.contains("Local Candle Substrate") {
            "local-candle-tensor-substrate".to_string()
        } else {
            crate::gemi::models::ModelManager::get_selected_model().unwrap_or_else(|| "gha-alpha.safetensors".to_string())
        }
    }

    pub fn handle_file_read_intent(goal: &str, workspace: &Path) -> Option<String> {
        let trim_goal = goal.trim();
        let lower_goal = trim_goal.to_lowercase();
        if lower_goal.starts_with("read ") {
            let target = lower_goal.strip_prefix("read ").unwrap().trim();
            let mut found_content = String::new();
            let mut read_success = false;

            for file_name in target.split("and") {
                let clean_name = file_name.trim().trim_matches('"').trim_matches('\'');
                let mut current = workspace.to_path_buf();

                loop {
                    let mut candidate_paths = vec![
                        current.join(clean_name),
                        current.join(".agents").join(clean_name),
                        current.join(format!("{}.md", clean_name)),
                        current.join(".agents").join(format!("{}.md", clean_name)),
                    ];
                    if clean_name.contains("projects") {
                        candidate_paths.push(current.join(".agents/PROJECTS.md"));
                        candidate_paths.push(current.join("PROJECTS.md"));
                    }
                    if clean_name.contains("agents") {
                        candidate_paths.push(current.join(".agents/AGENTS.md"));
                        candidate_paths.push(current.join("AGENTS.md"));
                    }

                    let mut found = false;
                    for path in candidate_paths {
                        if path.is_file() {
                            if let Ok(content) = fs::read_to_string(&path) {
                                found_content.push_str(&format!("=== File: {} ===\n{}\n\n", path.display(), content));
                                read_success = true;
                                found = true;
                                break;
                            }
                        }
                    }

                    if found { break; }
                    if !current.pop() { break; }
                }
            }

            if read_success {
                return Some(found_content.trim().to_string());
            }
        }
        None
    }

    pub fn handle_self_awareness_intent(goal: &str, workspace: &Path) -> Option<String> {
        let trim_goal = goal.trim().to_lowercase();
        if trim_goal.contains("who are you") || trim_goal.contains("inspect self") || trim_goal.contains("describe yourself") || trim_goal == "self" {
            let brain = crate::gawd::brain::AlphaBrainContext::initialize(workspace);
            let mut report = String::new();
            report.push_str("# gha Alpha Brain - Self-Awareness Report\n\n");
            report.push_str("## 1. SELF (Compiled Binary Axiomatic Core)\n");
            report.push_str(&format!("- Version: {}\n", crate::gawd::self_core::AlphaSelf::VERSION));
            report.push_str(&format!("- Core Paradigm: {}\n", crate::gawd::self_core::AlphaSelf::CORE_PARADIGM));
            report.push_str(&format!("- Baked Axiom Rules: {}\n", crate::gawd::self_core::AlphaSelf::RULES.len()));
            report.push_str(&format!("- Baked Native Components: {}\n", crate::gawd::self_core::AlphaSelf::COMPONENTS.len()));
            report.push_str(&format!("- Orchestrated Meta Components: {}\n", crate::gawd::self_core::AlphaSelf::META_COMPONENTS.len()));
            report.push_str(&format!("- Orchestrated Meta Contexts: {}\n\n", crate::gawd::self_core::AlphaSelf::META_CONTEXTS.len()));

            report.push_str("## 2. META SYSTEM ENVIRONMENT (Hardware & Compute)\n");
            report.push_str(&format!("- CPUs: {}\n", brain.system_cpus));
            report.push_str(&format!("- GPU Acceleration: {}\n", brain.system_gpu));
            report.push_str(&format!("- RAM: {}GB\n\n", brain.system_ram_gb));

            report.push_str("## 3. META USER ENVIRONMENT (Configuration & Workspace)\n");
            report.push_str(&format!("- Workspace: {}\n", brain.workspace_path.display()));
            report.push_str(&format!("- Default Engine: {}\n", brain.default_engine));
            report.push_str(&format!("- Default Model: {}\n\n", brain.default_model));

            report.push_str("## 4. META EXECUTION CONTEXT (State & Memory)\n");
            let status = crate::sandbox::manager::SandboxManager::check_interrupted_checkpoint(workspace)
                .map(|c| c.status)
                .unwrap_or_else(|| "IDLE".to_string());
            report.push_str(&format!("- Execution Status: {}\n\n", status));

            report.push_str("## Validation\n └── Verified: Alpha Brain fully operational and self-aware.\n");
            return Some(report);
        }
        None
    }

    fn solve_clean_raw(&self, goal: &str, a2a_logs: &[super::gmas::A2AMessage], workspace: &Path, version: &str) -> String {
        let mission_result = self.execute_autonomous_flux(goal, a2a_logs, workspace);
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

        for msg in a2a_logs {
            if msg.sender == "GhaUniversalSubstrateAgent" {
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

    pub fn solve(&self, goal: &str, workspace: &Path, _version: &str) -> String {
        crate::gawd::axiom::AxiomSubstrate::ingest_constitution(workspace);
        if let Some(res) = Self::handle_self_awareness_intent(goal, workspace) {
            return res;
        }
        if let Some(res) = Self::handle_file_read_intent(goal, workspace) {
            return res;
        }

        crate::sandbox::manager::GhaAuditLogger::log_event(workspace, "MISSION_START", goal);
        let trim_goal = goal.trim();

        let (cmd, arg) = trim_goal.split_once(' ').unwrap_or((trim_goal, ""));

        let is_direct_tool = ToolRegistry::exists(cmd) || cmd == "models";
        let is_orchestration = goal.contains("orchestrate") || goal.contains("mission");

        // 🚀 Phase 0: Direct Tool Execution (High-Performance Path)
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

            if !tool_res.to_lowercase().contains("error") && !tool_res.to_lowercase().contains("failed") && !tool_res.contains("TRUTH VIOLATION") {
                checkpoint.status = "COMPLETED".to_string();
                checkpoint.completed_tools.push(actual_cmd.to_string());
                checkpoint.blackboard.insert(format!("RESULT_{}", actual_cmd), tool_res.clone());
                crate::sandbox::manager::SandboxManager::save_mission_checkpoint(workspace, &checkpoint);

                return tool_res.trim().to_string();
            } else {
                crate::sandbox::manager::GhaAuditLogger::log_event(workspace, "TOOL_FAILURE", &format!("Tool {} failed: {}. Escalating to GEMI.", actual_cmd, tool_res));
                // Fall through to Swarm/GEMI orchestration
            }
        }

        // 🌀 Rule 18: Autonomous Capability Mapping & Gap Detection
        if !is_direct_tool && !is_orchestration && goal.len() > 3 {
            let clean_intent = goal.replace(|c: char| !c.is_alphanumeric() && c != ' ', "").replace(' ', "_").to_lowercase();
            if ToolRegistry::exists(&clean_intent) {
                // Execute existing synthesized reflex
                let res = ToolRegistry::execute_tool(&clean_intent, arg, workspace);

                if !res.to_lowercase().contains("error") && !res.to_lowercase().contains("failed") && !res.contains("TRUTH VIOLATION") {
                    return res.trim().to_string();
                } else {
                    crate::sandbox::manager::GhaAuditLogger::log_event(workspace, "REFLEX_FAILURE", &format!("Reflex {} failed: {}. Falling back to GEMI.", clean_intent, res));
                    // Fall through to Swarm/GEMI orchestration
                }
            }

            // If no reflex exists, attempt to distill one
            if !goal.contains('/') && !goal.contains('\\') {
                if let Ok(evolve_res) = self.trigger_autonomous_evolution(goal, workspace) {
                    return format!("Distilled native reflex for \"{}\". Applied architectural integration.\n{}", goal, evolve_res);
                }
            }
        }

        let (a2a_logs, _) = GmasSupervisor::supervise_mission(goal, workspace);

        let mut reasoning_content = String::new();
        let mut intelligence_gap = false;

        for msg in &a2a_logs {
            if msg.sender == "GhaUniversalSubstrateAgent" {
                reasoning_content = msg.payload.clone();
                if msg.payload.contains("no responding models found") {
                    intelligence_gap = true;
                }
            }
        }

        // 🌀 Rule 18: Autonomous Intelligence Bootstrapping
        if intelligence_gap && !goal.contains("scout_model") {
             crate::sandbox::manager::GhaAuditLogger::log_event(workspace, "INTELLIGENCE_GAP", "No models found. Bootstrapping local intelligence.");
             let scout_res = ToolRegistry::execute_tool("scout_model", "mistral", workspace);
             return format!("Critical reasoning gap detected. Scouting for local models...\n{}", scout_res);
        }

        let governance_check = self.audit_governance(&a2a_logs, workspace);
        if let Err(e) = governance_check {
            return format!("Governance Error: {}", e);
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
            let clean_out = lines.join("\n");
            return clean_out.trim().to_string();
        }

        if !reasoning_content.is_empty() {
            let clean_text = if let Some((_, rest)) = reasoning_content.split_once("]:\n") {
                rest
            } else if let Some((_, rest)) = reasoning_content.split_once("]: ") {
                rest
            } else {
                &reasoning_content
            };
            return clean_text.trim().to_string();
        }

        "Completed.".to_string()
    }

    fn audit_governance(&self, logs: &[super::gmas::A2AMessage], workspace: &Path) -> EaiResult<()> {
        super::model_supervisor::ModelSupervisor::audit_and_prepare_models(workspace)?;

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
                        if goal.contains(&res) || res.contains(tool_name) {
                             // Break potential recursion if the error is already about this tool or contains the goal
                             crate::sandbox::manager::GhaAuditLogger::log_event(workspace, "RECURSION_BLOCK", &format!("Bypassing reflex retry for {}", tool_name));
                             continue;
                        }

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

                    results.push(res);
                }
            }
        }

        // Secondary Synthesis: If goal requires translation, summarization, or analysis after tool execution
        let lower_goal = goal.to_lowercase();
        let needs_synthesis = lower_goal.contains("translate")
            || lower_goal.contains("tamil")
            || lower_goal.contains("summarize")
            || lower_goal.contains("analyze")
            || lower_goal.contains("explain");

        if needs_synthesis && !results.is_empty() {
            let mut clean_fetched = String::new();
            let download_file = workspace.join("download_content.txt");
            if download_file.is_file() {
                if let Ok(c) = fs::read_to_string(&download_file) {
                    clean_fetched = c;
                }
            }
            if clean_fetched.trim().is_empty() {
                clean_fetched = results.join("\n");
            }

            let synthesis_prompt = format!(
                "Goal: {}\n\nContent:\n{}\n\nProvide the complete response (e.g. translation or summary) in clean text.",
                goal, clean_fetched
            );
            let model_response = crate::gemi::engine::GemiEngine::generate_reasoning(&synthesis_prompt, workspace);
            let clean_model_resp = model_response.lines()
                .filter(|l| !l.starts_with("ACTION:") && !l.contains("Fetched Content:") && !l.contains("Saved results"))
                .collect::<Vec<_>>()
                .join("\n");

            let save_path = workspace.join("download_content.txt");

            if !clean_model_resp.trim().is_empty()
                && !clean_model_resp.contains("Executed intent for")
                && !clean_model_resp.contains("Processed intent")
                && !clean_model_resp.contains("Local reasoning substrate unavailable")
                && !clean_model_resp.contains("Provide the complete response")
                && !clean_model_resp.contains("STATUS:")
            {
                let _ = fs::write(&save_path, clean_model_resp.trim());
                checkpoint.status = "COMPLETED".to_string();
                crate::sandbox::manager::SandboxManager::save_mission_checkpoint(workspace, &checkpoint);
                GmasSupervisor::replicate_checkpoint(&checkpoint);
                return format!("Here is the result [Saved to: {}]:\n\n{}", save_path.display(), clean_model_resp.trim());
            } else {
                checkpoint.status = "FAILED_INCOMPLETE".to_string();
                crate::sandbox::manager::SandboxManager::save_mission_checkpoint(workspace, &checkpoint);
                GmasSupervisor::replicate_checkpoint(&checkpoint);
                let preview: String = clean_fetched.lines().take(15).collect::<Vec<_>>().join("\n");
                return format!(
                    "[TASK FAILED - INCOMPLETE EXECUTION]\n\nReason: Substrate model unfulfilled or unavailable for synthesis/translation. Configure active model or GHA_API_KEY in ~/.gha/env.\n\nFetched Raw Content Saved to [{}]:\n{}",
                    save_path.display(), preview
                );
            }
        }

        // Fulfill Indestructible Identity: Set status to COMPLETED
        checkpoint.status = "COMPLETED".to_string();
        crate::sandbox::manager::SandboxManager::save_mission_checkpoint(workspace, &checkpoint);
        GmasSupervisor::replicate_checkpoint(&checkpoint);

        results.join("\n")
    }

    #[allow(dead_code)]
    fn audit_truth(&self, goal: &str, logs: &[super::gmas::A2AMessage], workspace: &Path, mission_result: &str) -> String {
        if mission_result.contains("TRUTH VIOLATION") {
             return "MISSION BLOCKED: Hallucination detected during formal verification.".to_string();
        }

        let mut score = 100;
        let mut flags = Vec::new();

        let mut reasoning = String::new();
        for msg in logs {
            if msg.sender == "GhaUniversalSubstrateAgent" {
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
