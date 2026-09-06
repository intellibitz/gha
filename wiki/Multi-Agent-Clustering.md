# 🌐 Multi-Node A2A Agent Clustering

`gha` has evolved from a local process into a **World-Scale AI Agent Network**. It can pool computational resources across local LAN peers and global cloud nodes.

---

## 📡 UDP LAN Auto-Discovery (Port 9092)

The `GMA Daemon` background process listens on UDP port `9092`. 
*   When a new GHA node enters the network, it broadcasts a `GHA_LAN_PING`.
*   Active peers respond with a `GHA_LAN_PONG` and their capability profile.
*   GMA automatically registers these peers for parallel task dispatch.

---

## 🗺️ Cluster Registry (`~/.gha/peers.json`)

You can manually pin peer nodes or inspect auto-discovered nodes:

```json
{
  "peers": [
    {
      "node_id": "gha-desktop-gpu",
      "address": "192.168.1.15:9090",
      "node_type": "LAN_PEER",
      "is_active": true,
      "capabilities": ["REASONING", "CUDA"]
    }
  ]
}
```

---

## 🌌 Swarm Synchronization

GMA uses **Autonomous Swarm Orchestration** to synchronize mission context across all active nodes.
*   **Segmented Reasoning**: A complex mission is decomposed; segments are dispatched to specialized nodes (e.g., Vision tasks to a GPU node, Code analysis to the local master).
*   **Collective Intelligence**: Results are synthesized into a single Unified Executive Report for the user.

---

## 🧪 CLI Commands
```bash
# Inspect active cluster nodes:
ghai ai cluster

# Broadcast discovery ping to local peers:
ghai ai ping-peers

# Synchronize mission context across global swarm:
ghai ai swarm
```
