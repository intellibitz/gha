// 🤖 GAWD Worker Agent Fleet & Specialized Sub-Agents
// 100% Rust implementation for Agent-to-Agent (A2A) tasks

use serde::{Deserialize, Serialize};

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
}
