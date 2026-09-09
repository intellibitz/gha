// GAWD Agent Fleet: Dynamic Intelligence Substrate
// RULE 11: Agents must add functionality directly to the gha engine.

use std::path::{Path, PathBuf};
use std::sync::mpsc::channel;
use std::thread;
use std::sync::{Arc, RwLock, OnceLock};
use std::collections::HashMap;
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

/// Collaborative mission state shared among all swarm agents
pub type SwarmBlackboard = Arc<RwLock<HashMap<String, String>>>;

/// Core Intelligence Trait for GHA Swarm Agents
pub trait GawdAgent: Send + Sync {
    fn name(&self) -> String;
    fn role(&self) -> String;
    fn protocol(&self) -> String { "A2A".to_string() }
    fn keywords(&self) -> Vec<&'static str>;
    fn execute(&self, goal: &str, workspace: &Path, blackboard: &SwarmBlackboard) -> String;
}

pub struct AgentRegistry {
    agents: RwLock<Vec<Arc<dyn GawdAgent>>>,
}

impl AgentRegistry {
    pub fn global() -> &'static Self {
        static REGISTRY: OnceLock<AgentRegistry> = OnceLock::new();
        REGISTRY.get_or_init(|| {
            let registry = AgentRegistry {
                agents: RwLock::new(Vec::new()),
            };
            registry.bootstrap();
            registry
        })
    }

    fn bootstrap(&self) {
        let mut agents = self.agents.write().unwrap();
        agents.push(Arc::new(GhaUserAgent));
        agents.push(Arc::new(GhaContextAgent));
        agents.push(Arc::new(GhaReasoningAgent));
        agents.push(Arc::new(GhaKernelAgent));
        agents.push(Arc::new(GhaEconomicAgent));
        agents.push(Arc::new(GhaLinguistAgent));
        agents.push(Arc::new(GhaAgronomyAgent));
        agents.push(Arc::new(GhaMedicalAgent));
        agents.push(Arc::new(GhaLegalAgent));
        agents.push(Arc::new(GhaEducationAgent));
        agents.push(Arc::new(GhaEnergyAgent));
        agents.push(Arc::new(GhaTradesAgent));
        agents.push(Arc::new(GhaHouseholdAgent));
        agents.push(Arc::new(GhaCreativeAgent));
        agents.push(Arc::new(GhaPublicSafetyAgent));
        agents.push(Arc::new(GhaEnterpriseAgent));
        agents.push(Arc::new(GhaSafetyAgent));
        agents.push(Arc::new(GhaTruthAgent));
    }

    #[allow(dead_code)]
    pub fn list_all(&self) -> Vec<GawdAgentInfo> {
        let agents = self.agents.read().unwrap();
        agents.iter().map(|a| GawdAgentInfo {
            name: a.name(),
            role: a.role(),
            protocol: a.protocol(),
        }).collect()
    }

    pub fn synthesize_fleet(&self, goal: &str) -> Vec<Arc<dyn GawdAgent>> {
        let agents = self.agents.read().unwrap();
        let mut fleet = Vec::new();

        // 1. Always include Core Agents
        for agent in agents.iter() {
            let name = agent.name();
            if name == "GhaUserAgent" || name == "GhaContextAgent" || name == "GhaReasoningAgent" || name == "GhaSafetyAgent" || name == "GhaTruthAgent" {
                fleet.push(Arc::clone(agent));
            }
        }

        // 2. Semantic Synthesis: Score specialists based on role description and keywords
        use crate::gawd::pkb::PkbSynthesizer;
        for agent in agents.iter() {
            let name = agent.name();
            if name == "GhaUserAgent" || name == "GhaContextAgent" || name == "GhaReasoningAgent" || name == "GhaSafetyAgent" || name == "GhaTruthAgent" {
                continue;
            }

            let mut best_score = PkbSynthesizer::calculate_semantic_score(goal, &agent.role());
            for kw in agent.keywords() {
                 let kw_score = PkbSynthesizer::calculate_semantic_score(goal, kw);
                 if kw_score > best_score {
                     best_score = kw_score;
                 }
            }

            if best_score >= 1.0 {
                fleet.push(Arc::clone(agent));
            }
        }

        // 3. Fallback to Dynamic Generic Specialist if fleet is sparse
        if fleet.len() <= 5 {
             let topic = goal.split_whitespace().find(|w| w.len() > 3).unwrap_or("Domain");
             fleet.push(Arc::new(DynamicSpecialist {
                 topic: topic.to_string(),
                 goal: goal.to_string(),
             }));
        }

        fleet
    }
}

// --- Agent Implementations ---

pub struct GhaUserAgent;
impl GawdAgent for GhaUserAgent {
    fn name(&self) -> String { "GhaUserAgent".to_string() }
    fn role(&self) -> String { "World User Advocate & Proactive Prompter".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec![] }
    fn execute(&self, _goal: &str, workspace: &Path, blackboard: &SwarmBlackboard) -> String {
        let msg = format!("Proactive user guidance active for workspace '{}'.", workspace.display());
        blackboard.write().unwrap().insert("USER_ADVOCATE_STATUS".to_string(), "ACTIVE".to_string());
        msg
    }
}

impl GhaUserAgent {
    pub fn generate_proactive_prompts(workspace: &Path) -> Vec<(String, String)> {
        let mut prompts = Vec::new();
        let is_dev = workspace.join("Cargo.toml").exists() || workspace.join(".git").exists();
        if is_dev {
            prompts.push(("1".to_string(), "Build workspace project cleanly".to_string()));
            prompts.push(("2".to_string(), "Run workspace unit test harness".to_string()));
            prompts.push(("3".to_string(), "Inspect workspace health & system status".to_string()));
        } else {
            prompts.push(("1".to_string(), "Plan a healthy dinner recipe".to_string()));
            prompts.push(("2".to_string(), "Explain a school or homework concept".to_string()));
            prompts.push(("3".to_string(), "Organize family budget or documents".to_string()));
        }
        prompts
    }

    pub fn detect_domain_badge(goal: &str) -> (&'static str, &'static str) {
        let lower = goal.to_lowercase();
        if lower.contains("farm") || lower.contains("crop") || lower.contains("soil") { ("Agronomy", "Agricultural & Crop Intelligence") }
        else if lower.contains("health") || lower.contains("medical") { ("Medical", "Clinical & Health Intelligence") }
        else if lower.contains("legal") || lower.contains("law") { ("Legal", "Legal & Regulatory Compliance") }
        else if lower.contains("education") || lower.contains("math") || lower.contains("school") { ("Education", "Pedagogical & Science Learning") }
        else if lower.contains("energy") || lower.contains("solar") || lower.contains("climate") { ("Energy", "Renewable Energy & Climate Science") }
        else if lower.contains("plumb") || lower.contains("pipe") || lower.contains("electric") { ("Skilled Trades", "Field Engineering & Building Codes") }
        else if lower.contains("story") || lower.contains("script") || lower.contains("video") { ("Creative & Media", "Content & Visual Storytelling") }
        else if lower.contains("recipe") || lower.contains("cook") || lower.contains("home") { ("Home & Family", "Household, Budget & Family Life") }
        else if lower.contains("fire") || lower.contains("police") || lower.contains("emergency") { ("Public Safety", "Emergency Response & Infrastructure") }
        else if lower.contains("ceo") || lower.contains("product") || lower.contains("business") { ("Enterprise", "Business & Corporate Operations") }
        else if lower.contains("code") || lower.contains("build") || lower.contains("rust") || lower.contains("api") { ("Engineering", "Software & Systems Architecture") }
        else { ("Universal", "Intelligence Reflex & Execution Substrate") }
    }
}

struct GhaContextAgent;
impl GawdAgent for GhaContextAgent {
    fn name(&self) -> String { "GhaContextAgent".to_string() }
    fn role(&self) -> String { "Environment Context".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec![] }
    fn execute(&self, _goal: &str, workspace: &Path, blackboard: &SwarmBlackboard) -> String {
        let ctx = format!("Workspace: {}", workspace.display());
        blackboard.write().unwrap().insert("MISSION_CONTEXT".to_string(), ctx.clone());
        ctx
    }
}

struct GhaReasoningAgent;
impl GawdAgent for GhaReasoningAgent {
    fn name(&self) -> String { "GhaReasoningAgent".to_string() }
    fn role(&self) -> String { "Core Inference".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec![] }
    fn execute(&self, goal: &str, workspace: &Path, _blackboard: &SwarmBlackboard) -> String {
        let domain_guideline = GawdAgentFleet::get_domain_context_guideline(goal);
        let enriched_goal = if domain_guideline.is_empty() {
            goal.to_string()
        } else {
            format!("{}\n\nINTENT: {}", domain_guideline, goal)
        };

        // High-Priority Reflex Check: Direct mapping for common assistant missions
        match crate::gemi::pulse::GhaPulse::reason(&enriched_goal, workspace) {
            Ok(action) => {
                if action.contains("ACTION:") {
                    return action;
                }
                format!("ACTION: {}", action)
            },
            Err(_) => {
                // 🚀 Piped Mission Substrate: Autonomous transformation loop
                if goal.contains("[INPUT DATA]:") {
                     if let Some(data) = goal.split("[INPUT DATA]:\n").nth(1) {
                         let lower_goal = goal.to_lowercase();
                         if lower_goal.contains("uppercase") {
                              return format!("ACTION: exec_command echo \"{}\" | tr '[:lower:]' '[:upper:]'", data.replace("\"", "\\\""));
                         } else if lower_goal.contains("lowercase") {
                              return format!("ACTION: exec_command echo \"{}\" | tr '[:upper:]' '[:lower:]'", data.replace("\"", "\\\""));
                         } else if lower_goal.contains("save") || lower_goal.contains("write") {
                              if let Some(to_pos) = lower_goal.find(" to ") {
                                  let path = goal[to_pos + 4..].trim().trim_end_matches('.');
                                  return format!("ACTION: write_file {} {}", path, data);
                              }
                         }
                     }
                }
                crate::gemi::engine::GemiEngine::generate_reasoning(&enriched_goal, workspace)
            }
        }
    }
}

struct GhaKernelAgent;
impl GawdAgent for GhaKernelAgent {
    fn name(&self) -> String { "GhaKernelAgent".to_string() }
    fn role(&self) -> String { "Low-Level Engineering".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec!["os", "kernel", "bootloader", "driver", "assembly", "firmware"] }
    fn execute(&self, goal: &str, _workspace: &Path, _blackboard: &SwarmBlackboard) -> String { format!("Low-level synthesis engaged for '{}'.", goal) }
}

struct GhaEconomicAgent;
impl GawdAgent for GhaEconomicAgent {
    fn name(&self) -> String { "GhaEconomicAgent".to_string() }
    fn role(&self) -> String { "Financial Intelligence".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec!["business", "stock", "money", "economic", "finance", "market", "roi"] }
    fn execute(&self, goal: &str, _workspace: &Path, _blackboard: &SwarmBlackboard) -> String { format!("Financial flux analysis applied to '{}'.", goal) }
}

struct GhaLinguistAgent;
impl GawdAgent for GhaLinguistAgent {
    fn name(&self) -> String { "GhaLinguistAgent".to_string() }
    fn role(&self) -> String { "Universal Translation".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec!["japanese", "tamil", "translate", "language", "linguist"] }
    fn execute(&self, _goal: &str, _workspace: &Path, _blackboard: &SwarmBlackboard) -> String { "Universal linguist substrate active.".to_string() }
}

struct GhaAgronomyAgent;
impl GawdAgent for GhaAgronomyAgent {
    fn name(&self) -> String { "GhaAgronomyAgent".to_string() }
    fn role(&self) -> String { "Agricultural & Crop Specialist".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec!["farm", "crop", "soil", "agri", "harvest", "planting", "ph"] }
    fn execute(&self, _goal: &str, _workspace: &Path, _blackboard: &SwarmBlackboard) -> String { "Agronomy domain context active (soil pH, N-P-K nutrient ratios, crop yield guidance).".to_string() }
}

struct GhaMedicalAgent;
impl GawdAgent for GhaMedicalAgent {
    fn name(&self) -> String { "GhaMedicalAgent".to_string() }
    fn role(&self) -> String { "Clinical & Health Specialist".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec!["health", "doctor", "medical", "medicine", "clinic", "patient", "fever", "pain"] }
    fn execute(&self, _goal: &str, _workspace: &Path, _blackboard: &SwarmBlackboard) -> String { "Clinical health domain context active (evidence-based wellness guidance).".to_string() }
}

struct GhaLegalAgent;
impl GawdAgent for GhaLegalAgent {
    fn name(&self) -> String { "GhaLegalAgent".to_string() }
    fn role(&self) -> String { "Legal & Contract Specialist".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec!["legal", "contract", "law", "clause", "court", "attorney", "liability"] }
    fn execute(&self, _goal: &str, _workspace: &Path, _blackboard: &SwarmBlackboard) -> String { "Legal contract domain context active (liability & compliance analysis).".to_string() }
}

struct GhaEducationAgent;
impl GawdAgent for GhaEducationAgent {
    fn name(&self) -> String { "GhaEducationAgent".to_string() }
    fn role(&self) -> String { "Pedagogical & Science Specialist".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec!["education", "math", "teach", "school", "learn", "homework", "essay"] }
    fn execute(&self, _goal: &str, _workspace: &Path, _blackboard: &SwarmBlackboard) -> String { "Pedagogical domain context active (step-by-step educational breakdown).".to_string() }
}

struct GhaEnergyAgent;
impl GawdAgent for GhaEnergyAgent {
    fn name(&self) -> String { "GhaEnergyAgent".to_string() }
    fn role(&self) -> String { "Climate & Renewable Energy Specialist".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec!["energy", "solar", "climate", "battery", "grid", "wattage"] }
    fn execute(&self, _goal: &str, _workspace: &Path, _blackboard: &SwarmBlackboard) -> String { "Renewable energy domain context active (efficiency & wattage analysis).".to_string() }
}

struct GhaTradesAgent;
impl GawdAgent for GhaTradesAgent {
    fn name(&self) -> String { "GhaTradesAgent".to_string() }
    fn role(&self) -> String { "Skilled Trades & Building Codes Specialist".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec!["plumb", "pipe", "electric", "hvac", "wire", "carpenter", "mechanic"] }
    fn execute(&self, _goal: &str, _workspace: &Path, _blackboard: &SwarmBlackboard) -> String { "Skilled trades domain context active (NEC/UPC/IMC building code compliance & field diagnostic).".to_string() }
}

struct GhaHouseholdAgent;
impl GawdAgent for GhaHouseholdAgent {
    fn name(&self) -> String { "GhaHouseholdAgent".to_string() }
    fn role(&self) -> String { "Home & Family Operations Specialist".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec!["recipe", "cook", "dinner", "family", "mom", "diy", "chore", "home"] }
    fn execute(&self, _goal: &str, _workspace: &Path, _blackboard: &SwarmBlackboard) -> String { "Home & family operations domain context active (budget, recipes, household care).".to_string() }
}

struct GhaCreativeAgent;
impl GawdAgent for GhaCreativeAgent {
    fn name(&self) -> String { "GhaCreativeAgent".to_string() }
    fn role(&self) -> String { "Narrative & Media Specialist".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec!["story", "script", "video", "design", "music", "content", "movie", "write"] }
    fn execute(&self, _goal: &str, _workspace: &Path, _blackboard: &SwarmBlackboard) -> String { "Narrative & media specialist engaged.".to_string() }
}

struct GhaPublicSafetyAgent;
impl GawdAgent for GhaPublicSafetyAgent {
    fn name(&self) -> String { "GhaPublicSafetyAgent".to_string() }
    fn role(&self) -> String { "Emergency & Public Safety Specialist".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec!["fire", "police", "emergency", "disaster", "safety", "civil", "triage"] }
    fn execute(&self, _goal: &str, _workspace: &Path, _blackboard: &SwarmBlackboard) -> String { "Public safety domain context active (emergency response & crisis coordination).".to_string() }
}

struct GhaEnterpriseAgent;
impl GawdAgent for GhaEnterpriseAgent {
    fn name(&self) -> String { "GhaEnterpriseAgent".to_string() }
    fn role(&self) -> String { "Corporate & Enterprise Specialist".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec!["ceo", "product", "agile", "business", "corporate", "roadmap", "sales"] }
    fn execute(&self, _goal: &str, _workspace: &Path, _blackboard: &SwarmBlackboard) -> String { "Enterprise domain context active (product roadmaps, ROI & executive summary).".to_string() }
}

struct GhaSafetyAgent;
impl GawdAgent for GhaSafetyAgent {
    fn name(&self) -> String { "GhaSafetyAgent".to_string() }
    fn role(&self) -> String { "Mission Guardrails (Rules 4, 7, 11)".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec![] }
    fn execute(&self, goal: &str, _workspace: &Path, blackboard: &SwarmBlackboard) -> String {
        let mut bb = blackboard.write().unwrap();
        bb.insert("GOVERNANCE_ACTIVE".to_string(), "TRUE".to_string());

        let lower = goal.to_lowercase();
        if lower.contains("rm -rf") || lower.contains("mkfs") || lower.contains("dd if=") {
             bb.insert("SAFETY_ALERT".to_string(), "CRITICAL_DESTRUCTIVE_COMMAND".to_string());
             return "🚨 Safety Violation: Destructive command detected in intent.".to_string();
        }

        "Governance protocols active. Mission cleared for execution.".to_string()
    }
}

struct GhaTruthAgent;
impl GawdAgent for GhaTruthAgent {
    fn name(&self) -> String { "GhaTruthAgent".to_string() }
    fn role(&self) -> String { "Hallucination Detection (Rules 1, 2, 3, 10)".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec![] }
    fn execute(&self, goal: &str, workspace: &Path, blackboard: &SwarmBlackboard) -> String {
        let mut score = 100;
        let mut flags = Vec::new();

        let bb = blackboard.read().unwrap();

        // 1. Audit consistency of reasoning with goal
        if goal.contains("version") && !bb.contains_key("MISSION_CONTEXT") {
             score -= 10;
             flags.push("Mission context missing for version check.".to_string());
        }

        // 2. Real-time workspace verification
        if goal.contains("file") || goal.contains("read") || goal.contains("write") {
             let gha_dir = workspace.join(".gha");
             if !gha_dir.exists() {
                 score -= 20;
                 flags.push("Active .gha sandbox missing in target workspace.".to_string());
             }
        }

        if flags.is_empty() {
            "Truth and hallucination detection active. Verified.".to_string()
        } else {
            format!("Audit score: {}/100\n   {}", score, flags.join("\n   "))
        }
    }
}

struct DynamicSpecialist {
    topic: String,
    goal: String,
}
impl GawdAgent for DynamicSpecialist {
    fn name(&self) -> String { format!("Gha{}SpecialistAgent", self.topic) }
    fn role(&self) -> String { format!("Dynamic Specialist for '{}'", self.goal) }
    fn keywords(&self) -> Vec<&'static str> { vec![] }
    fn execute(&self, _goal: &str, _workspace: &Path, _blackboard: &SwarmBlackboard) -> String { format!("Specialized agent '{}' executing intent.", self.name()) }
}

pub struct GawdAgentFleet;

impl GawdAgentFleet {
    pub fn synthesize_fleet(goal: &str) -> Vec<GawdAgentInfo> {
        let registry = AgentRegistry::global();
        let fleet = registry.synthesize_fleet(goal);
        fleet.iter().map(|a| GawdAgentInfo {
            name: a.name(),
            role: a.role(),
            protocol: a.protocol(),
        }).collect()
    }

    pub fn get_domain_context_guideline(goal: &str) -> String {
        let lower = goal.to_lowercase();
        if lower.contains("farm") || lower.contains("crop") || lower.contains("soil") || lower.contains("agri") {
            "[DOMAIN CONTEXT: Agronomy & Crop Science — Focus on soil pH, N-P-K nutrient ratios, crop yield, and sustainable soil management]".to_string()
        } else if lower.contains("health") || lower.contains("doctor") || lower.contains("medical") || lower.contains("medicine") || lower.contains("clinic") || lower.contains("patient") || lower.contains("fever") {
            "[DOMAIN CONTEXT: Medical & Clinical Guidance — Focus on evidence-based health information, patient-friendly explanations, and safety disclaimers]".to_string()
        } else if lower.contains("legal") || lower.contains("contract") || lower.contains("law") || lower.contains("clause") || lower.contains("court") {
            "[DOMAIN CONTEXT: Legal & Regulatory Analysis — Focus on contract terms, risk obligations, compliance, and clear layperson summaries]".to_string()
        } else if lower.contains("education") || lower.contains("math") || lower.contains("teach") || lower.contains("school") || lower.contains("learn") || lower.contains("homework") {
            "[DOMAIN CONTEXT: Education & Pedagogy — Focus on step-by-step conceptual explanations, examples, and clear learning progressions]".to_string()
        } else if lower.contains("energy") || lower.contains("solar") || lower.contains("climate") || lower.contains("battery") {
            "[DOMAIN CONTEXT: Renewable Energy & Climate Science — Focus on efficiency, wattage, grid capacity, and environmental sustainability]".to_string()
        } else if lower.contains("plumb") || lower.contains("pipe") || lower.contains("electric") || lower.contains("hvac") || lower.contains("wire") {
            "[DOMAIN CONTEXT: Skilled Trades & Field Services — Focus on building codes (NEC/UPC/IMC), safety compliance, diagnostic steps, and cost estimation]".to_string()
        } else if lower.contains("recipe") || lower.contains("cook") || lower.contains("dinner") || lower.contains("family") || lower.contains("mom") || lower.contains("diy") {
            "[DOMAIN CONTEXT: Home & Family Operations — Focus on quick preparation preparation steps, budget management, safety, and clear household guidance]".to_string()
        } else if lower.contains("story") || lower.contains("script") || lower.contains("video") || lower.contains("design") || lower.contains("music") {
            "[DOMAIN CONTEXT: Creative & Media Synthesis — Focus on narrative arcs, audience engagement, visual layout, and content branding]".to_string()
        } else if lower.contains("fire") || lower.contains("police") || lower.contains("emergency") || lower.contains("disaster") || lower.contains("safety") {
            "[DOMAIN CONTEXT: Public Safety & Emergency Response — Focus on emergency triage protocols, safety compliance, and crisis coordination]".to_string()
        } else if lower.contains("ceo") || lower.contains("product") || lower.contains("agile") || lower.contains("business") || lower.contains("corporate") {
            "[DOMAIN CONTEXT: Enterprise & Corporate Strategy — Focus on ROI, product roadmaps, operational efficiency, and executive summaries]".to_string()
        } else {
            String::new()
        }
    }

    pub fn dispatch_explosive_swarm(goal: String, workspace: PathBuf) -> Vec<(String, String)> {
        let registry = AgentRegistry::global();
        let fleet = registry.synthesize_fleet(&goal);
        let blackboard: SwarmBlackboard = Arc::new(RwLock::new(HashMap::new()));

        let mut handles = Vec::new();
        let (tx, rx) = channel();

        for agent in fleet {
            let t_goal = goal.clone();
            let t_ws = workspace.clone();
            let t_tx = tx.clone();
            let t_agent = Arc::clone(&agent);
            let t_bb = Arc::clone(&blackboard);

            let handle = thread::spawn(move || {
                let output = t_agent.execute(&t_goal, &t_ws, &t_bb);
                let _ = t_tx.send((t_agent.name(), output));
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

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_registry_lookup() {
        let registry = AgentRegistry::global();
        let fleet = registry.synthesize_fleet("crop soil pH");
        assert!(fleet.iter().any(|a| a.name() == "GhaAgronomyAgent"));
    }

    #[test]
    fn test_detect_domain_badge() {
        assert_eq!(GhaUserAgent::detect_domain_badge("crop soil pH").0, "Agronomy");
        assert_eq!(GhaUserAgent::detect_domain_badge("medical doctor health").0, "Medical");
    }
}
