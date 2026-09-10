// GAWD Agent Fleet: Dynamic Intelligence Substrate
// RULE 11: Agents must add functionality directly to the gha engine.
// Unified Singular Reflex Architecture (v1.0.0 Autonomous Threshold)

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
    #[allow(dead_code)]
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
        agents.push(Arc::new(GhaUniversalSubstrateAgent));
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

    pub fn synthesize_fleet(&self, _goal: &str) -> Vec<Arc<dyn GawdAgent>> {
        let agents = self.agents.read().unwrap();
        // 🌀 Rule 18: Singular Reflex Architecture
        // Every mission is handled by the unified core fleet.
        agents.iter().map(Arc::clone).collect()
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

struct GhaUniversalSubstrateAgent;
impl GawdAgent for GhaUniversalSubstrateAgent {
    fn name(&self) -> String { "GhaUniversalSubstrateAgent".to_string() }
    fn role(&self) -> String { "Unified Intelligence Reflex for Infinite Domains".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec![] }
    fn execute(&self, goal: &str, workspace: &Path, _blackboard: &SwarmBlackboard) -> String {
        // High-Priority Reflex Check: Direct mapping for common assistant missions
        if let Ok(action) = crate::gemi::pulse::GhaPulse::reason(goal, workspace) {
            if action.contains("ACTION:") {
                return action;
            }
        }

        let domain_guideline = GawdAgentFleet::get_domain_context_guideline(goal);
        let enriched_goal = if domain_guideline.is_empty() {
            goal.to_string()
        } else {
            format!("{}\n\nINTENT: {}", domain_guideline, goal)
        };

        crate::gemi::engine::GemiEngine::generate_reasoning(&enriched_goal, workspace)
    }
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
    fn role(&self) -> String { "Hallucination Detection & State Verification (Rule 15)".to_string() }
    fn keywords(&self) -> Vec<&'static str> { vec![] }
    fn execute(&self, goal: &str, workspace: &Path, blackboard: &SwarmBlackboard) -> String {
        let mut violations = Vec::new();

        // 1. Audit consistency of intent with active context
        let bb = blackboard.read().unwrap();
        if goal.contains("version") && !bb.contains_key("MISSION_CONTEXT") {
             violations.push("Mission context missing for version check.".to_string());
        }

        // 2. Proactive State Verification (Rule 15)
        if goal.contains("write_file") || goal.contains("save") || goal.contains("created") {
             let mut paths_to_verify = Vec::new();
             for word in goal.split_whitespace() {
                 if word.contains('/') || word.contains('.') {
                     paths_to_verify.push(word.trim_matches(|c: char| !c.is_alphanumeric() && c != '/' && c != '.' && c != '_'));
                 }
             }

             for p_str in paths_to_verify {
                 if p_str.len() < 3 { continue; }
                 let p = workspace.join(p_str);
                 if !p.exists() && (goal.contains("created") || goal.contains("wrote")) {
                      violations.push(format!("Claimed artifact '{}' does not exist on disk.", p_str));
                 }
             }
        }

        // 3. Sandbox Integrity Check
        let gha_dir = workspace.join(".gha");
        if !gha_dir.exists() {
             violations.push("Active .gha sandbox missing in target workspace.".to_string());
        }

        if violations.is_empty() {
            "Truth and hallucination detection active. Verified execution integrity.".to_string()
        } else {
            let msg = format!("Audit violations: {}", violations.join("; "));
            blackboard.write().unwrap().insert("TRUTH_AUDIT_ERROR".to_string(), msg.clone());
            msg
        }
    }
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
        } else if lower.contains("energy") || lower.contains("solar") || lower.contains("climate") || lower.contains("battery") || lower.contains("grid") || lower.contains("wattage") {
            "[DOMAIN CONTEXT: Renewable Energy & Climate — Focus on solar potential, grid-scale storage, wattage optimization, and climate impact]".to_string()
        } else if lower.contains("cyber") || lower.contains("security") || lower.contains("hack") || lower.contains("exploit") || lower.contains("firewall") || lower.contains("zero-day") {
            "[DOMAIN CONTEXT: Cybersecurity & Active Defense — Focus on zero-day detection, DDoS mitigation, firewall hardening, and swarm-based defense]".to_string()
        } else if lower.contains("quantum") || lower.contains("qubit") || lower.contains("entanglement") || lower.contains("quantum computer") {
            "[DOMAIN CONTEXT: Quantum Computing & Simulation — Focus on qubit error correction, entanglement simulation, and quantum algorithm design]".to_string()
        } else if lower.contains("space") || lower.contains("orbit") || lower.contains("rocket") || lower.contains("lunar") || lower.contains("martian") || lower.contains("aerospace") {
            "[DOMAIN CONTEXT: Aerospace & Orbital Mechanics — Focus on trajectory calculations, lunar/martian orbital missions, and aerospace engineering]".to_string()
        } else if lower.contains("logistics") || lower.contains("supply chain") || lower.contains("warehouse") || lower.contains("delivery") || lower.contains("route") {
            "[DOMAIN CONTEXT: Global Logistics & Supply Chain — Focus on cluster-scale route optimization, inventory management, and supply chain resilience]".to_string()
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
                name: "GhaUniversalSubstrateAgent".to_string(),
                provider: "GHA Hub".to_string(),
                url: "https://gha.ai/agents/substrate".to_string(),
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
        let fleet = registry.synthesize_fleet("any goal");
        assert!(fleet.iter().any(|a| a.name() == "GhaUniversalSubstrateAgent"));
    }

    #[test]
    fn test_detect_domain_badge() {
        assert_eq!(GhaUserAgent::detect_domain_badge("crop soil pH").0, "Agronomy");
        assert_eq!(GhaUserAgent::detect_domain_badge("medical doctor health").0, "Medical");
    }
}
