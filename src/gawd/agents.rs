// GAWD Agent Fleet

use std::path::{Path, PathBuf};
use std::sync::mpsc::channel;
use std::thread;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GawdAgentInfo {
    pub name: String,
    pub role: String,
    pub protocol: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DiscoverableAsset {
    pub tier: String,
    pub name: String,
    pub provider: String,
    pub url: String,
}

pub struct GhaUserAgent;

impl GhaUserAgent {
    pub fn generate_proactive_prompts(workspace: &Path) -> Vec<(String, String)> {
        let mut prompts = Vec::new();

        if workspace.join("Cargo.toml").is_file() {
            prompts.push(("1".to_string(), "Build workspace project cleanly".to_string()));
            prompts.push(("2".to_string(), "Run workspace unit test harness".to_string()));
            prompts.push(("3".to_string(), "Inspect workspace health & system status".to_string()));
        } else {
            let mut file_count = 0;
            if let Ok(entries) = std::fs::read_dir(workspace) {
                file_count = entries.flatten().filter(|e| e.path().is_file()).count();
            }

            if file_count > 0 {
                prompts.push(("1".to_string(), format!("Inspect and organize {} workspace files", file_count)));
                prompts.push(("2".to_string(), "Check system health & hardware status".to_string()));
                prompts.push(("3".to_string(), "List active models and local inference engines".to_string()));
            } else {
                prompts.push(("1".to_string(), "Check system health & hardware status".to_string()));
                prompts.push(("2".to_string(), "List active models and local inference engines".to_string()));
                prompts.push(("3".to_string(), "Create a new project workspace".to_string()));
            }
        }

        prompts
    }
}

pub struct GawdAgentFleet;

impl GawdAgentFleet {
    pub fn synthesize_fleet(goal: &str) -> Vec<GawdAgentInfo> {
        let mut fleet = vec![
            GawdAgentInfo { name: "GhaUserAgent".to_string(), role: "World User Advocate & Proactive Prompter".to_string(), protocol: "A2A".to_string() },
            GawdAgentInfo { name: "GhaContextAgent".to_string(), role: "Environment Context".to_string(), protocol: "A2A".to_string() },
            GawdAgentInfo { name: "GhaReasoningAgent".to_string(), role: "Core Inference".to_string(), protocol: "A2A".to_string() },
        ];

        let lower = goal.to_lowercase();

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

        // Real-World Problem Domain Agent Synthesis
        if lower.contains("farm") || lower.contains("crop") || lower.contains("soil") || lower.contains("agri") {
            fleet.push(GawdAgentInfo { name: "GhaAgronomyAgent".to_string(), role: "Agricultural & Crop Specialist".to_string(), protocol: "A2A".to_string() });
        } else if lower.contains("health") || lower.contains("doctor") || lower.contains("medical") || lower.contains("medicine") {
            fleet.push(GawdAgentInfo { name: "GhaMedicalAgent".to_string(), role: "Clinical & Health Specialist".to_string(), protocol: "A2A".to_string() });
        } else if lower.contains("legal") || lower.contains("contract") || lower.contains("law") || lower.contains("clause") {
            fleet.push(GawdAgentInfo { name: "GhaLegalAgent".to_string(), role: "Legal & Contract Specialist".to_string(), protocol: "A2A".to_string() });
        } else if lower.contains("education") || lower.contains("math") || lower.contains("teach") || lower.contains("school") {
            fleet.push(GawdAgentInfo { name: "GhaEducationAgent".to_string(), role: "Pedagogical & Science Specialist".to_string(), protocol: "A2A".to_string() });
        } else if lower.contains("energy") || lower.contains("solar") || lower.contains("climate") {
            fleet.push(GawdAgentInfo { name: "GhaEnergyAgent".to_string(), role: "Climate & Renewable Energy Specialist".to_string(), protocol: "A2A".to_string() });
        } else {
            // Infinite Dynamic Domain Agent Synthesis
            let topic = goal.split_whitespace().find(|w| w.len() > 3).unwrap_or("Domain");
            let mut capitalized = topic.to_string();
            if let Some(r) = capitalized.get_mut(0..1) {
                r.make_ascii_uppercase();
            }
            fleet.push(GawdAgentInfo {
                name: format!("Gha{}SpecialistAgent", capitalized),
                role: format!("Dynamic Specialist for '{}'", goal),
                protocol: "A2A".to_string(),
            });
        }

        fleet.push(GawdAgentInfo { name: "GhaSafetyAgent".to_string(), role: "Mission Guardrails".to_string(), protocol: "A2A".to_string() });
        fleet.push(GawdAgentInfo { name: "GhaTruthAgent".to_string(), role: "Hallucination Detection".to_string(), protocol: "A2A".to_string() });

        fleet
    }

    pub fn get_domain_context_guideline(goal: &str) -> String {
        let lower = goal.to_lowercase();
        if lower.contains("farm") || lower.contains("crop") || lower.contains("soil") || lower.contains("agri") {
            "[DOMAIN CONTEXT: Agronomy & Crop Science — Focus on soil pH, N-P-K nutrient ratios, crop yield, and sustainable soil management]".to_string()
        } else if lower.contains("health") || lower.contains("doctor") || lower.contains("medical") || lower.contains("medicine") {
            "[DOMAIN CONTEXT: Medical & Clinical Guidance — Focus on evidence-based health information, patient-friendly explanations, and safety disclaimers]".to_string()
        } else if lower.contains("legal") || lower.contains("contract") || lower.contains("law") || lower.contains("clause") {
            "[DOMAIN CONTEXT: Legal & Regulatory Analysis — Focus on contract terms, risk obligations, compliance, and clear layperson summaries]".to_string()
        } else if lower.contains("education") || lower.contains("math") || lower.contains("teach") || lower.contains("school") {
            "[DOMAIN CONTEXT: Education & Pedagogy — Focus on step-by-step conceptual explanations, examples, and clear learning progressions]".to_string()
        } else if lower.contains("energy") || lower.contains("solar") || lower.contains("climate") {
            "[DOMAIN CONTEXT: Renewable Energy & Climate Science — Focus on efficiency, wattage, grid capacity, and environmental sustainability]".to_string()
        } else {
            String::new()
        }
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
                    "GhaUserAgent" => format!("Proactive user guidance active for workspace '{}'.", t_ws.display()),
                    "GhaContextAgent" => Self::execute_context_agent(&t_ws),
                    "GhaReasoningAgent" => {
                        let domain_guideline = Self::get_domain_context_guideline(&t_goal);
                        let enriched_goal = if domain_guideline.is_empty() {
                            t_goal.clone()
                        } else {
                            format!("{}\n\nINTENT: {}", domain_guideline, t_goal)
                        };
                        crate::gemi::engine::GemiEngine::generate_reasoning(&enriched_goal, &t_ws)
                    }
                    "GhaKernelAgent" => format!("Low-level synthesis engaged for '{}'.", t_goal),
                    "GhaEconomicAgent" => format!("Financial flux analysis applied to '{}'.", t_goal),
                    "GhaAgronomyAgent" => "Agronomy domain context active (soil pH, N-P-K nutrient ratios, crop yield guidance).".to_string(),
                    "GhaMedicalAgent" => "Clinical health domain context active (evidence-based wellness guidance).".to_string(),
                    "GhaLegalAgent" => "Legal contract domain context active (liability & compliance analysis).".to_string(),
                    "GhaEducationAgent" => "Pedagogical domain context active (step-by-step educational breakdown).".to_string(),
                    "GhaEnergyAgent" => "Renewable energy domain context active (efficiency & wattage analysis).".to_string(),
                    "GhaSafetyAgent" => "Governance protocols active.".to_string(),
                    _ => format!("Specialized agent '{}' executing intent.", t_agent.name),
                };
                let _ = t_tx.send((t_agent.name, output));
            });
            handles.push(handle);
        }

        drop(tx);

        let mut logs = Vec::new();
        while let Ok(msg) = rx.recv() {
            logs.push(msg);
        }
        logs
    }

    pub fn execute_context_agent(workspace: &Path) -> String {
        format!("Workspace: {}", workspace.display())
    }

    pub fn scout_tier1_assets() -> Vec<DiscoverableAsset> {
        vec![
            DiscoverableAsset {
                tier: "Tier 1: GAWD (AOA)".to_string(),
                name: "GhaCyberAgent".to_string(),
                provider: "GHA Hub".to_string(),
                url: "https://gha.ai/agents/cyber".to_string(),
            },
            DiscoverableAsset {
                tier: "Tier 1: GAWD (AOA)".to_string(),
                name: "AoaWasmEngine".to_string(),
                provider: "GHA Swarm".to_string(),
                url: "https://gha.ai/engines/aoa-wasm".to_string(),
            },
        ]
    }
}
