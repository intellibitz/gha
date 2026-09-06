// 🌌 GMAS: Universal AI for AI Swarm Supervisor
// Tier 1 AOA Protocol governing Full Autonomous World-Scale AI Agent Network

use std::fs;
use std::io::{BufRead, BufReader, Write};
use std::net::{TcpStream, UdpSocket};
use std::path::{Path, PathBuf};
use std::time::Duration;
use serde::{Deserialize, Serialize};

use super::agents::{GawdAgentFleet, GawdAgentInfo};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct A2AMessage {
    pub sender: String,
    pub recipient: String,
    pub action: String,
    pub payload: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ClusterPeerNode {
    pub node_id: String,
    pub address: String,
    pub node_type: String,
    pub is_active: bool,
    pub capabilities: Vec<String>,
}

pub struct GmasSupervisor;

impl GmasSupervisor {
    pub const UDP_DISCOVERY_PORT: u16 = 9092;

    pub fn list_cluster_nodes() -> Vec<ClusterPeerNode> {
        let mut nodes = Vec::new();
        nodes.push(ClusterPeerNode {
            node_id: "gha-local-master".to_string(),
            address: "127.0.0.1:9090".to_string(),
            node_type: "LOCAL_MASTER".to_string(),
            is_active: true,
            capabilities: vec!["CORE".to_string(), "INFERENCE".to_string(), "TOOLING".to_string()],
        });
        nodes
    }

    pub fn supervise_mission(goal: &str, workspace: &Path) -> (Vec<A2AMessage>, Vec<GawdAgentInfo>) {
        let fleet = GawdAgentFleet::list_agents();

        // 1. Swarm Flux Initiation
        let (ctx_out, vault_out, reasoning_out, exec_out, verify_out) =
            GawdAgentFleet::dispatch_parallel_fleet(goal.to_string(), workspace.to_path_buf());

        let logs = vec![
            A2AMessage {
                sender: "GMA-Master".to_string(),
                recipient: "ContextAgent".to_string(),
                action: "MISSION_SYNC".to_string(),
                payload: ctx_out,
            },
            A2AMessage {
                sender: "GMA-Master".to_string(),
                recipient: "VaultAgent".to_string(),
                action: "VAULT_DISCOVERY".to_string(),
                payload: vault_out,
            },
            A2AMessage {
                sender: "ContextAgent".to_string(),
                recipient: "ReasoningAgent".to_string(),
                action: "INFERENCE_FLUX".to_string(),
                payload: reasoning_out,
            },
            A2AMessage {
                sender: "ReasoningAgent".to_string(),
                recipient: "ExecutorAgent".to_string(),
                action: "TOOL_DISPATCH".to_string(),
                payload: exec_out,
            },
            A2AMessage {
                sender: "ExecutorAgent".to_string(),
                recipient: "GMA-Master".to_string(),
                action: "MISSION_VERIFY".to_string(),
                payload: verify_out,
            },
        ];

        (logs, fleet)
    }

    pub fn dispatch_peer_task(addr: &str, tool_name: &str, arg: &str) -> String {
        if let Ok(mut stream) = TcpStream::connect_timeout(&addr.parse().unwrap_or_else(|_| "127.0.0.1:9090".parse().unwrap()), Duration::from_millis(500)) {
            let req = format!(
                "{{\"jsonrpc\":\"2.0\",\"id\":99,\"method\":\"tools/call\",\"params\":{{\"name\":\"{}\",\"arguments\":\"{}\"}}}}\n",
                tool_name, arg
            );
            if stream.write_all(req.as_bytes()).is_ok() && stream.flush().is_ok() {
                let mut reader = BufReader::new(stream);
                let mut resp = String::new();
                if reader.read_line(&mut resp).is_ok() {
                    return format!("🌐 [A2A Flux ({})]: {}", addr, resp.trim());
                }
            }
        }
        format!("🌐 [A2A Fallback]: Node '{}' unreachable.", addr)
    }

    pub fn broadcast_lan_ping() -> Vec<String> {
        let mut active_peers = Vec::new();
        if let Ok(socket) = UdpSocket::bind("0.0.0.0:0") {
            let _ = socket.set_broadcast(true);
            let _ = socket.set_read_timeout(Some(Duration::from_millis(200)));
            let _ = socket.send_to(b"GHA_LAN_PING", format!("255.255.255.255:{}", Self::UDP_DISCOVERY_PORT));
        }
        active_peers
    }
}
