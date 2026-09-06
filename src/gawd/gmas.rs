// 🏛️ GMAS: GMA Supervisor
// Tier 1 AOA Protocol Supervisor governing GAWD agent fleet execution

use std::path::Path;
use super::agents::{GawdAgentFleet, GawdAgentInfo};
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct A2AMessage {
    pub sender: String,
    pub recipient: String,
    pub action: String,
    pub payload: String,
}

pub struct GmasSupervisor;

impl GmasSupervisor {
    pub fn supervise_mission(goal: &str, workspace: &Path) -> (Vec<A2AMessage>, Vec<GawdAgentInfo>) {
        let fleet = GawdAgentFleet::list_agents();

        // Dispatch parallel worker threads over channels
        let (ctx_out, research_out, reasoning_out, sys_out, auto_out) =
            GawdAgentFleet::dispatch_parallel_fleet(goal.to_string(), workspace.to_path_buf());

        let logs = vec![
            A2AMessage {
                sender: "GMA-Master".to_string(),
                recipient: "GhaContextAgent".to_string(),
                action: "PARALLEL_CONTEXT_INSPECT".to_string(),
                payload: ctx_out,
            },
            A2AMessage {
                sender: "GMAS-Supervisor".to_string(),
                recipient: "GhaWebResearchAgent".to_string(),
                action: "PARALLEL_VAULT_DISCOVER".to_string(),
                payload: research_out,
            },
            A2AMessage {
                sender: "GhaContextAgent".to_string(),
                recipient: "GhaReasoningAgent".to_string(),
                action: "PARALLEL_GEMI_REASONING".to_string(),
                payload: reasoning_out,
            },
            A2AMessage {
                sender: "GhaReasoningAgent".to_string(),
                recipient: "GhaSystemExecutionAgent".to_string(),
                action: "PARALLEL_TOOL_EXECUTE".to_string(),
                payload: sys_out,
            },
            A2AMessage {
                sender: "GhaSystemExecutionAgent".to_string(),
                recipient: "GhaAutonomousAgent".to_string(),
                action: "PARALLEL_HEALTH_VERIFY".to_string(),
                payload: auto_out,
            },
        ];

        (logs, fleet)
    }
}
