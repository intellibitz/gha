// 🌌 GMAS: Universal EAI Swarm Supervisor
// Tier 1 AOA Protocol governing Exponential Explosive Intelligence Swarms

use std::io::{BufRead, BufReader, Write};
use std::net::{TcpStream, UdpSocket};
use std::path::Path;
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
        let fleet_info = GawdAgentFleet::synthesize_fleet(goal);

        // 🚀 Exponential Swarm Execution
        let swarm_logs = GawdAgentFleet::dispatch_explosive_swarm(goal.to_string(), workspace.to_path_buf());

        let mut a2a_logs = Vec::new();
        for (name, output) in swarm_logs {
            a2a_logs.push(A2AMessage {
                sender: name,
                recipient: "GMA-Master".to_string(),
                action: "MISSION_FLUX".to_string(),
                payload: output,
            });
        }

        (a2a_logs, fleet_info)
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
        let active_peers = Vec::new();
        if let Ok(socket) = UdpSocket::bind("0.0.0.0:0") {
            let _ = socket.set_broadcast(true);
            let _ = socket.set_read_timeout(Some(Duration::from_millis(200)));
            let _ = socket.send_to(b"GHA_LAN_PING", format!("255.255.255.255:{}", Self::UDP_DISCOVERY_PORT));
        }
        active_peers
    }
}
