// 🌌 GMAS: Universal EAI Swarm Supervisor
// Tier 1 AOA Protocol governing Exponential Explosive Intelligence Swarms

use std::io::{BufRead, BufReader, Write};
use std::net::{TcpStream, UdpSocket};
use std::path::Path;
use std::time::Duration;
use serde::{Deserialize, Serialize};

use super::agents::{GawdAgentFleet, GawdAgentInfo};
use crate::error::{EaiError, EaiResult};

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
        vec![ClusterPeerNode {
            node_id: "gha-local-master".to_string(),
            address: "127.0.0.1:9090".to_string(),
            node_type: "LOCAL_MASTER".to_string(),
            is_active: true,
            capabilities: vec!["CORE".to_string(), "INFERENCE".to_string(), "TOOLING".to_string()],
        }]
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
        let mut active_peers = Vec::new();
        if let Ok(socket) = UdpSocket::bind("0.0.0.0:0") {
            let _ = socket.set_broadcast(true);
            let _ = socket.set_read_timeout(Some(Duration::from_millis(200)));
            let _ = socket.send_to(b"GHA_LAN_PING", format!("255.255.255.255:{}", Self::UDP_DISCOVERY_PORT));

            let mut buf = [0u8; 512];
            while let Ok((amt, src)) = socket.recv_from(&mut buf) {
                let msg = String::from_utf8_lossy(&buf[..amt]);
                if msg.contains("GHA_LAN_ACK") || msg.contains("GHA") {
                    active_peers.push(src.to_string());
                }
            }
        }
        if active_peers.is_empty() {
            active_peers.push("127.0.0.1:9090 (local)".to_string());
        }
        active_peers
    }

    #[allow(dead_code)]
    pub fn sync_cluster_state(workspace: &Path, payload: &str) -> String {
        let nodes = Self::list_cluster_nodes();
        let mut synced = 0;
        for node in &nodes {
            if Self::dispatch_peer_task(&node.address, "swarm_sync", payload).contains("Sync complete") {
                synced += 1;
            }
        }
        let sync_file = workspace.join(".gha/cluster_sync.json");
        let now = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).map(|d| d.as_secs()).unwrap_or(0);
        let _ = std::fs::write(&sync_file, format!("{{\"timestamp\": {}, \"synced_nodes\": {}, \"payload_size\": {}}}", now, synced, payload.len()));
        format!("Synchronized state across {} nodes (saved to {})", synced, sync_file.display())
    }
}
