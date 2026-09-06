// 🤖 GAWD Worker Agent Fleet & Specialized Sub-Agents
// 100% Rust implementation for real Agent-to-Agent (A2A) async channel execution

use std::path::{Path, PathBuf};
use std::process::Command;
use std::sync::mpsc::channel;
use std::thread;
use serde::{Deserialize, Serialize};

use crate::gemi::models::ModelManager;
use crate::gmcp::tools::ToolRegistry;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GawdAgentInfo {
    pub name: String,
    pub role: String,
    pub protocol: String,
}

pub struct GawdAgentFleet;

impl GawdAgentFleet {
    pub fn list_agents() -> Vec<GawdAgentInfo> {
        vec![
            GawdAgentInfo {
                name: "GhaContextAgent".to_string(),
                role: "Workspace Context & Code Base Analysis".to_string(),
                protocol: "A2A".to_string(),
            },
            GawdAgentInfo {
                name: "GhaWebResearchAgent".to_string(),
                role: "Web Research & Hugging Face Hub Integration".to_string(),
                protocol: "A2A".to_string(),
            },
            GawdAgentInfo {
                name: "GhaReasoningAgent".to_string(),
                role: "Deep Intelligence & Strategic Reasoning".to_string(),
                protocol: "A2A".to_string(),
            },
            GawdAgentInfo {
                name: "GhaSystemExecutionAgent".to_string(),
                role: "Native Execution & System Capability Orchestration".to_string(),
                protocol: "A2A".to_string(),
            },
            GawdAgentInfo {
                name: "GhaAutonomousAgent".to_string(),
                role: "Autonomous Mission Execution & Refinement".to_string(),
                protocol: "A2A".to_string(),
            },
        ]
    }

    pub fn dispatch_parallel_fleet(goal: String, workspace: PathBuf) -> (String, String, String, String, String) {
        let (tx1, rx1) = channel();
        let (tx2, rx2) = channel();
        let (tx3, rx3) = channel();
        let (tx4, rx4) = channel();

        // Worker 1: Context Agent Thread (with Git Auto-Branching check)
        let ws1 = workspace.clone();
        thread::spawn(move || {
            let out = Self::execute_context_agent(&ws1);
            let _ = tx1.send(out);
        });

        // Worker 2: Research Agent Thread
        let ws2 = workspace.clone();
        thread::spawn(move || {
            let out = Self::execute_research_agent(&ws2);
            let _ = tx2.send(out);
        });

        // Worker 3: Reasoning Agent Thread
        let g3 = goal.clone();
        let ws3 = workspace.clone();
        thread::spawn(move || {
            let out = crate::gemi::engine::GemiEngine::generate_reasoning(&g3, &ws3);
            let _ = tx3.send(out);
        });

        // Worker 4: System Execution Thread
        let g4 = goal.clone();
        let ws4 = workspace.clone();
        thread::spawn(move || {
            let out = Self::execute_system_agent(&g4, &ws4);
            let _ = tx4.send(out);
        });

        let ctx_out = rx1.recv().unwrap_or_else(|_| "Context Agent error".to_string());
        let research_out = rx2.recv().unwrap_or_else(|_| "Research Agent error".to_string());
        let reasoning_out = rx3.recv().unwrap_or_else(|_| "Reasoning Agent error".to_string());
        let sys_out = rx4.recv().unwrap_or_else(|_| "System Agent error".to_string());

        let auto_out = Self::execute_autonomous_agent(&goal, &workspace);

        (ctx_out, research_out, reasoning_out, sys_out, auto_out)
    }

    pub fn execute_context_agent(workspace: &Path) -> String {
        let mut context = Vec::new();
        if workspace.join("Cargo.toml").exists() {
            context.push("Rust (Cargo.toml)");
        }
        if workspace.join("build.gradle").exists() || workspace.join("build.gradle.kts").exists() {
            context.push("Android/Gradle");
        }
        if workspace.join("package.json").exists() {
            context.push("Node.js");
        }

        let git_branch_info = ToolRegistry::git_auto_branch(workspace);

        let git_status = Command::new("git")
            .args(["status", "--short"])
            .current_dir(workspace)
            .output()
            .ok()
            .and_then(|o| String::from_utf8(o.stdout).ok())
            .unwrap_or_else(|| "Git repo inactive".to_string());

        let ctx_summary = if context.is_empty() {
            "Generic Workspace".to_string()
        } else {
            context.join(", ")
        };

        format!(
            "Workspace Context: {} | {} | Git Status Summary: {}",
            ctx_summary,
            git_branch_info,
            if git_status.trim().is_empty() { "Clean Tree" } else { git_status.trim() }
        )
    }

    pub fn execute_research_agent(workspace: &Path) -> String {
        let models = ModelManager::list_models(workspace);
        let names: Vec<String> = models.iter().map(|m| format!("{} ({})", m.name, m.registry)).collect();
        format!("Discovered AI Model Vaults ({} Models): {}", models.len(), names.join(", "))
    }

    pub fn execute_system_agent(goal: &str, workspace: &Path) -> String {
        let lower = goal.to_lowercase();
        if lower.contains("dir") || lower.contains("ls") || lower.contains("files") {
            ToolRegistry::execute_tool("list_directory", "", workspace)
        } else if lower.contains("disk") || lower.contains("df") || lower.contains("space") {
            ToolRegistry::execute_tool("get_disk_usage", "", workspace)
        } else if lower.contains("status") {
            ToolRegistry::execute_tool("status", "", workspace)
        } else {
            format!("System Capability Ready: Executed goal scope in `{}`", workspace.display())
        }
    }

    pub fn execute_autonomous_agent(goal: &str, workspace: &Path) -> String {
        let lower = goal.to_lowercase();
        if lower.contains("test") {
            ToolRegistry::run_test_harness(workspace)
        } else if lower.contains("build") || lower.contains("check") || lower.contains("heal") {
            ToolRegistry::self_heal_build(workspace)
        } else {
            let build_check = ToolRegistry::self_heal_build(workspace);
            let test_check = ToolRegistry::run_test_harness(workspace);
            format!("Autonomous Agent Verification: {} | {}", build_check, test_check)
        }
    }
}
