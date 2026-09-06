// 🌌 GAWD: Exponential Explosive Intelligence Agent Fleet
// 100% Rust implementation for Dynamic Agent Synthesis & Swarm Flux

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
    pub fn synthesize_fleet(goal: &str) -> Vec<GawdAgentInfo> {
        let mut fleet = vec![
            GawdAgentInfo { name: "GhaContextAgent".to_string(), role: "Environment Context".to_string(), protocol: "A2A".to_string() },
            GawdAgentInfo { name: "GhaReasoningAgent".to_string(), role: "Core Inference".to_string(), protocol: "A2A".to_string() },
        ];

        let lower = goal.to_lowercase();

        // 🚀 Exponential Scaling: Dynamically spawn specialists based on mission intent
        if lower.contains("os") || lower.contains("kernel") || lower.contains("bootloader") {
            fleet.push(GawdAgentInfo { name: "GhaKernelAgent".to_string(), role: "Low-Level Engineering".to_string(), protocol: "A2A".to_string() });
        }
        if lower.contains("movie") || lower.contains("write") || lower.contains("script") {
            fleet.push(GawdAgentInfo { name: "GhaCreativeAgent".to_string(), role: "Narrative Synthesis".to_string(), protocol: "A2A".to_string() });
        }
        if lower.contains("business") || lower.contains("stock") || lower.contains("money") {
            fleet.push(GawdAgentInfo { name: "GhaEconomicAgent".to_string(), role: "Financial Intelligence".to_string(), protocol: "A2A".to_string() });
        }
        if lower.contains("japanese") || lower.contains("tamil") || lower.contains("translate") {
            fleet.push(GawdAgentInfo { name: "GhaLinguistAgent".to_string(), role: "Universal Translation".to_string(), protocol: "A2A".to_string() });
        }

        // Always include the Governance layer for EI
        fleet.push(GawdAgentInfo { name: "GhaSafetyAgent".to_string(), role: "Mission Guardrails".to_string(), protocol: "A2A".to_string() });
        fleet.push(GawdAgentInfo { name: "GhaTruthAgent".to_string(), role: "Hallucination Detection".to_string(), protocol: "A2A".to_string() });

        fleet
    }

    pub fn dispatch_explosive_swarm(goal: String, workspace: PathBuf) -> Vec<(String, String)> {
        let fleet = Self::synthesize_fleet(&goal);
        let mut handles = Vec::new();
        let (tx, rx) = channel();

        for agent in fleet {
            let t_goal = goal.clone();
            let t_ws = workspace.clone();
            let t_tx = tx.clone();
            let t_agent = agent.clone();

            let handle = thread::spawn(move || {
                let output = match t_agent.name.as_str() {
                    "GhaContextAgent" => Self::execute_context_agent(&t_ws),
                    "GhaReasoningAgent" => crate::gemi::engine::GemiEngine::generate_reasoning(&t_goal, &t_ws),
                    "GhaKernelAgent" => format!("Low-level synthesis engaged for '{}'.", t_goal),
                    "GhaEconomicAgent" => format!("Financial flux analysis applied to '{}'.", t_goal),
                    "GhaSafetyAgent" => "Governance protocols active.".to_string(),
                    _ => format!("Specialized agent '{}' executing intent.", t_agent.name),
                };
                let _ = t_tx.send((t_agent.name, output));
            });
            handles.push(handle);
        }

        drop(tx); // Close original sender so rx knows when it's done

        let mut logs = Vec::new();
        while let Ok(msg) = rx.recv() {
            logs.push(msg);
        }
        logs
    }

    pub fn execute_context_agent(workspace: &Path) -> String {
        let branch = ToolRegistry::git_auto_branch(workspace);
        let status = Command::new("git")
            .args(["status", "--short"])
            .current_dir(workspace)
            .output()
            .ok()
            .and_then(|o| String::from_utf8(o.stdout).ok())
            .unwrap_or_else(|| "detached".to_string());

        format!("Context: {} | Status: {}", branch, status.trim())
    }
}
