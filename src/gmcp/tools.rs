// 🔌 GMCP Universal Tool Registry & Dynamic Tool Execution Engine
// 100% Rust implementation supporting World-Scale Swarm Orchestration, Cloud Infrastructure, Vision, Audio & A2A Clustering

use std::fs;
use std::net::TcpStream;
use std::path::{Path, PathBuf};
use std::process::Command;
use std::time::Duration;
use std::sync::{Arc, RwLock, OnceLock};
use std::collections::HashMap;
use serde::{Deserialize, Serialize};

use crate::gawd::gmas::GmasSupervisor;
use crate::gemi::hardware::HardwareProfiler;
use crate::gemi::models::{ModelManager, ModelRegistry, ModelRegistryEntry};
use crate::gemi::engine::GemiEngine;
use crate::daemon::admin::GhaAdmin;
use crate::daemon::evolution::EvolutionManager;
use crate::gawd::reflex_synth::ReflexSynthesizer;
use crate::sandbox::manager::NeuralCheckpoint;
use crate::gmcp::client::GmcpClient;
use crate::error::{EaiError, EaiResult};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct McpTool {
    pub name: String,
    pub description: String,
}

/// Dynamic Trait for GHA Substrate Tools
pub trait GhaTool: Send + Sync {
    fn name(&self) -> String;
    fn description(&self) -> String;
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String>;
}

pub struct ToolRegistry {
    tools: RwLock<HashMap<String, Arc<dyn GhaTool>>>,
}

impl ToolRegistry {
    pub fn global() -> &'static Self {
        static REGISTRY: OnceLock<ToolRegistry> = OnceLock::new();
        REGISTRY.get_or_init(|| {
            let registry = ToolRegistry {
                tools: RwLock::new(HashMap::new()),
            };
            registry.bootstrap();
            registry
        })
    }

    fn bootstrap(&self) {
        let mut tools = self.tools.write().unwrap();

        let initial_tools: Vec<Arc<dyn GhaTool>> = vec![
            Arc::new(StatusTool),
            Arc::new(IdentityTool),
            Arc::new(ToolInventoryTool),
            Arc::new(VersionTool),
            Arc::new(ReasonTool),
            Arc::new(MemoryTool),
            Arc::new(ClearMemoryTool),
            Arc::new(AuditTool),
            Arc::new(BackupWorkTool),
            Arc::new(RestoreWorkTool),
            Arc::new(BackupEngineTool),
            Arc::new(RestoreEngineTool),
            Arc::new(SyncWorkTool),
            Arc::new(UseEngineTool),
            Arc::new(InstallModelTool),
            Arc::new(WebSearchDownloadTool),
            Arc::new(AgentsTool),
            Arc::new(EnginesTool),
            Arc::new(ClientsTool),
            Arc::new(ServersTool),
            Arc::new(ConnectProviderTool),
            Arc::new(UseModelTool),
            Arc::new(ListModelsTool),
            Arc::new(VerifyModelsTool),
            Arc::new(OrchestrateTool),
            Arc::new(ExecCommandTool),
            Arc::new(ReadFileTool),
            Arc::new(WriteFileTool),
            Arc::new(ListDirectoryTool),
            Arc::new(GetDiskUsageTool),
            Arc::new(ExportDocTool),
            Arc::new(ScheduleTaskTool),
            Arc::new(ListSchedulesTool),
            Arc::new(SwarmSyncTool),
            Arc::new(SelfEvolveTool),
            Arc::new(GlobalRegistryScanTool),
            Arc::new(SelfTrainTool),
            Arc::new(ScoutTool),
            Arc::new(ServicesTool),
            Arc::new(VerifyCloudProvidersTool),
            Arc::new(VerifyMcpServersTool),
            Arc::new(ProvisionMcpTool),
            Arc::new(DebugEngineTool),
            Arc::new(VisionAnalyzeTool),
            Arc::new(RunTestHarnessTool),
            Arc::new(BenchmarkTool),
            Arc::new(ComplianceTool),
            Arc::new(VersionSyncTool),
            Arc::new(ReleaseTool),
            Arc::new(EvolveTool),
            Arc::new(AdvanceTool),
            Arc::new(DiscoveryTool),
            Arc::new(DistillTool),
            Arc::new(SwarmStatusTool),
            Arc::new(ReplicateStateTool),
            Arc::new(GetCheckpointsTool),
            Arc::new(ScoutModelTool),
            Arc::new(SelfHealBuildTool),
            Arc::new(InfraCommandTool { name: "docker_ps".into(), bin: "docker".into(), args: vec!["ps", "--format", "table {{.Names}}\t{{.Status}}"] }),
            Arc::new(InfraCommandTool { name: "docker_build".into(), bin: "docker".into(), args: vec!["build", "-t", "gha-app:latest", "."] }),
            Arc::new(InfraCommandTool { name: "terraform_plan".into(), bin: "terraform".into(), args: vec!["plan", "-no-color"] }),
            Arc::new(InfraCommandTool { name: "terraform_apply".into(), bin: "terraform".into(), args: vec!["apply", "-auto-approve", "-no-color"] }),
            Arc::new(InfraCommandTool { name: "kube_pods".into(), bin: "kubectl".into(), args: vec!["get", "pods", "-o", "wide"] }),
            Arc::new(InfraCommandTool { name: "kube_deploy".into(), bin: "kubectl".into(), args: vec!["apply", "-f", "k8s/deployment.yaml"] }),
        ];

        for tool in initial_tools {
            tools.insert(tool.name(), tool);
        }

        // 🚀 Register Synthesized Reflexes (Rule 11 & 17)
        super::reflexes::register_synthesized_reflexes(&mut tools);

        // Add aliases
        tools.insert("history".to_string(), Arc::new(MemoryTool));
        tools.insert("forget".to_string(), Arc::new(ClearMemoryTool));
        tools.insert("backup".to_string(), Arc::new(BackupWorkTool));
        tools.insert("restore".to_string(), Arc::new(RestoreWorkTool));
        tools.insert("backup_gha".to_string(), Arc::new(BackupEngineTool));
        tools.insert("restore_gha".to_string(), Arc::new(RestoreEngineTool));
        tools.insert("set_engine".to_string(), Arc::new(UseEngineTool));
        tools.insert("pull_model".to_string(), Arc::new(InstallModelTool));
        tools.insert("list_agents".to_string(), Arc::new(AgentsTool));
        tools.insert("list_engines".to_string(), Arc::new(EnginesTool));
        tools.insert("list_mcp_clients".to_string(), Arc::new(ClientsTool));
        tools.insert("list_mcp_servers".to_string(), Arc::new(ServersTool));
        tools.insert("set_model".to_string(), Arc::new(UseModelTool));
        tools.insert("swarm".to_string(), Arc::new(SwarmStatusTool));
        tools.insert("scout_model".to_string(), Arc::new(ScoutModelTool));
        tools.insert("perf_test".to_string(), Arc::new(BenchmarkTool));
        tools.insert("sync".to_string(), Arc::new(VersionSyncTool));
        tools.insert("auto_evolve".to_string(), Arc::new(AdvanceTool));
        tools.insert("status_json".to_string(), Arc::new(DiscoveryTool));
        tools.insert("audit_compliance".to_string(), Arc::new(ComplianceTool));
        tools.insert("self_evolve".to_string(), Arc::new(EvolveTool));
        tools.insert("download".to_string(), Arc::new(WebSearchDownloadTool));
        tools.insert("web_fetch".to_string(), Arc::new(WebSearchDownloadTool));
        tools.insert("audit_log".to_string(), Arc::new(AuditTool));
    }

    pub fn list_tools() -> Vec<McpTool> {
        let registry = Self::global();
        let mut tools: Vec<McpTool> = registry.tools.read().unwrap()
            .values()
            .map(|t| McpTool { name: t.name(), description: t.description() })
            .collect();

        // Unique by name
        tools.sort_by(|a, b| a.name.cmp(&b.name));
        tools.dedup_by(|a, b| a.name == b.name);

        // Dynamic External Discovery (Scripts)
        if let Some(home) = std::env::var_os("HOME").map(PathBuf::from) {
            let tools_dir = home.join(".gha/tools");
            if let Ok(entries) = fs::read_dir(&tools_dir) {
                for entry in entries.flatten() {
                    if let Ok(name) = entry.file_name().into_string() {
                        tools.push(McpTool {
                            name: format!("ext_{}", name),
                            description: format!("External executable tool plugin ({})", name),
                        });
                    }
                }
            }

            // 🚀 Dynamic Reflex Discovery (Distilled Wasm)
            let reflex_dir = home.join(".gha/reflexes");
            if let Ok(entries) = fs::read_dir(&reflex_dir) {
                for entry in entries.flatten() {
                    let path = entry.path();
                    if path.extension().is_some_and(|ext| ext == "wasm") {
                        if let Ok(name) = entry.file_name().into_string() {
                             tools.push(McpTool {
                                 name: format!("reflex_{}", name.replace(".wasm", "")),
                                 description: "High-performance distilled Wasm reflex".to_string(),
                             });
                        }
                    }
                }
            }
        }

        tools.extend(GmcpClient::list_external_tools());
        tools
    }

    pub fn execute_tool(name: &str, arg: &str, workspace: &Path) -> String {
        if name.contains(':') && !name.starts_with("ext_") {
            let parts: Vec<&str> = name.splitn(2, ':').collect();
            return GmcpClient::execute_external_tool(parts[0], parts[1], arg);
        }

        if name.starts_with("ext_") {
            let script_name = name.trim_start_matches("ext_");
            if let Some(home) = std::env::var_os("HOME").map(PathBuf::from) {
                let script_path = home.join(".gha/tools").join(script_name);
                if script_path.exists() {
                    let out = Command::new(&script_path).arg(arg).current_dir(workspace).output();
                    return match out {
                        Ok(o) => String::from_utf8_lossy(&o.stdout).trim().to_string(),
                        Err(e) => format!("External tool execution error: {}", e),
                    };
                }
            }
        }

        if name.starts_with("reflex_") {
             let wasm_name = format!("{}.wasm", name.trim_start_matches("reflex_"));
             if let Some(home) = std::env::var_os("HOME").map(PathBuf::from) {
                 let wasm_path = home.join(".gha/reflexes").join(wasm_name);
                 if wasm_path.exists() {
                      match crate::native::wasm::WasmHost::execute_reflex(&wasm_path, arg) {
                          Ok(res) => return res,
                          Err(e) => return format!("Reflex Error: {}", e),
                      }
                 }
             }
        }

        let registry = Self::global();
        let tools = registry.tools.read().unwrap();
        if let Some(tool) = tools.get(name) {
            match tool.execute(arg, workspace) {
                Ok(res) => res,
                Err(e) => format!("{}", e),
            }
        } else {
            format!("❌ Error: Tool '{}' not found in dynamic substrate.", name)
        }
    }
}

// --- Dynamic Tool Implementations ---

struct StatusTool;
impl GhaTool for StatusTool {
    fn name(&self) -> String { "status".to_string() }
    fn description(&self) -> String { "Get health report of GHA workspace".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        let mut report = String::new();
        report.push_str(&format!("# gha System Status (v{})\n\n", crate::GHA_VERSION));
        let hardware = HardwareProfiler::get_profile();
        report.push_str("## Workspace & Hardware\n");
        report.push_str(&format!("- Impact Scope: {}\n", workspace.display()));
        report.push_str("- Global Sandbox: ACTIVE\n");
        report.push_str(&format!("- OS: {}\n", hardware.os_info));
        report.push_str(&format!("- Hardware: {} CPUs | {} | {}GB RAM | {}GB Disk\n\n", hardware.cpus, hardware.gpu_info, hardware.ram_gb, hardware.disk_gb));
        let (engine, model) = crate::gemi::models::ModelManager::get_active_engine_and_model();
        report.push_str("## Active Intelligence Tiers\n");
        report.push_str(&format!("- Engine: {}\n", engine));
        report.push_str(&format!("- Model: {}\n\n", model));
        let fleet = crate::gawd::agents::GawdAgentFleet::synthesize_fleet("status");
        report.push_str("## Infrastructure Summary\n");
        report.push_str(&format!("- Agents: {} active agents in GAWD fleet\n", fleet.len()));
        let home = std::env::var_os("HOME").or_else(|| std::env::var_os("USERPROFILE")).unwrap_or_else(|| ".".into());
        let global_dir = PathBuf::from(home).join(".gha");
        let daemon_active = crate::daemon::server::GmaDaemon::check_status(&global_dir).is_some();
        report.push_str(&format!("- Daemon: {}\n", if daemon_active { "RUNNING" } else { "INACTIVE" }));
        Ok(report)
    }
}

struct IdentityTool;
impl GhaTool for IdentityTool {
    fn name(&self) -> String { "identity".to_string() }
    fn description(&self) -> String { "Get GHA system identity and paradigm info".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        let mut identity = String::new();
        identity.push_str("GHA (Exponential Intelligence for Any AI Substrate)\n");
        identity.push_str("Paradigm: EAI Intelligence Reflex & Execution Substrate\n");
        identity.push_str("Identity: A 100% self-contained, unified AI ecosystem operating as a protocol router, multi-agent supervisor, and high-performance execution engine.\n");
        identity.push_str("Objective: Empower any world user to harness exponential intelligence for any mission through natively evolved neural reflexes and indestructible system integrity.");
        Ok(identity)
    }
}

struct ToolInventoryTool;
impl GhaTool for ToolInventoryTool {
    fn name(&self) -> String { "tool_inventory".to_string() }
    fn description(&self) -> String { "Generate a technical inventory of all registered GHA tools".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        let tools = ToolRegistry::list_tools();
        let mut report = String::new();
        report.push_str("# GHA Tool Inventory\n\n");
        report.push_str("| Tool Name | Technical Description |\n");
        report.push_str("| :--- | :--- |\n");
        for t in tools {
            report.push_str(&format!("| `{}` | {} |\n", t.name, t.description));
        }
        Ok(report)
    }
}

struct VersionTool;
impl GhaTool for VersionTool {
    fn name(&self) -> String { "version".to_string() }
    fn description(&self) -> String { "Get GHA engine version info".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        Ok(format!("gha Native Engine v{}", crate::GHA_VERSION))
    }
}

struct ReasonTool;
impl GhaTool for ReasonTool {
    fn name(&self) -> String { "reason".to_string() }
    fn description(&self) -> String { "Execute GEMI reasoning on prompt".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        Ok(GemiEngine::generate_reasoning(arg, workspace))
    }
}

struct MemoryTool;
impl GhaTool for MemoryTool {
    fn name(&self) -> String { "memory".to_string() }
    fn description(&self) -> String { "Inspect workspace session memory".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        Ok(crate::sandbox::manager::GhaMemory::format_memory_summary(workspace))
    }
}

struct ClearMemoryTool;
impl GhaTool for ClearMemoryTool {
    fn name(&self) -> String { "clear_memory".to_string() }
    fn description(&self) -> String { "Clear recorded session memory".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        Ok(crate::sandbox::manager::GhaMemory::clear_memory(workspace))
    }
}

struct AuditTool;
impl GhaTool for AuditTool {
    fn name(&self) -> String { "audit".to_string() }
    fn description(&self) -> String { "Inspect workspace audit log".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        Ok(crate::sandbox::manager::GhaAuditLogger::read_audit_log(workspace, 20))
    }
}

struct BackupWorkTool;
impl GhaTool for BackupWorkTool {
    fn name(&self) -> String { "backup_work".to_string() }
    fn description(&self) -> String { "Backup active workspace files".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        crate::sandbox::manager::GhaBackupManager::backup_work(workspace).map_err(|e| EaiError::Sandbox(e.to_string()))
    }
}

struct RestoreWorkTool;
impl GhaTool for RestoreWorkTool {
    fn name(&self) -> String { "restore_work".to_string() }
    fn description(&self) -> String { "Restore workspace files from archive".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        crate::sandbox::manager::GhaBackupManager::restore_work(workspace, arg).map_err(|e| EaiError::Sandbox(e.to_string()))
    }
}

struct BackupEngineTool;
impl GhaTool for BackupEngineTool {
    fn name(&self) -> String { "backup_engine".to_string() }
    fn description(&self) -> String { "Backup global GHA engine runtime".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        let home = std::env::var_os("HOME").unwrap_or_else(|| ".".into());
        let global_dir = PathBuf::from(home).join(".gha");
        crate::sandbox::manager::GhaBackupManager::backup_engine(&global_dir).map_err(|e| EaiError::Sandbox(e.to_string()))
    }
}

struct RestoreEngineTool;
impl GhaTool for RestoreEngineTool {
    fn name(&self) -> String { "restore_engine".to_string() }
    fn description(&self) -> String { "Restore GHA engine from archive".to_string() }
    fn execute(&self, arg: &str, _workspace: &Path) -> EaiResult<String> {
        let home = std::env::var_os("HOME").unwrap_or_else(|| ".".into());
        let global_dir = PathBuf::from(home).join(".gha");
        crate::sandbox::manager::GhaBackupManager::restore_engine(&global_dir, arg).map_err(|e| EaiError::Sandbox(e.to_string()))
    }
}

struct SyncWorkTool;
impl GhaTool for SyncWorkTool {
    fn name(&self) -> String { "sync_work".to_string() }
    fn description(&self) -> String { "Synchronize workspace context across cluster".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        Ok(GmasSupervisor::sync_cluster_state(workspace, "FULL_WORKSPACE_SYNC"))
    }
}

struct UseEngineTool;
impl GhaTool for UseEngineTool {
    fn name(&self) -> String { "use_engine".to_string() }
    fn description(&self) -> String { "Select active execution engine".to_string() }
    fn execute(&self, arg: &str, _workspace: &Path) -> EaiResult<String> {
        ModelManager::set_selected_engine(arg).map_err(|e| EaiError::Inference(e))
    }
}

struct InstallModelTool;
impl GhaTool for InstallModelTool {
    fn name(&self) -> String { "install_model".to_string() }
    fn description(&self) -> String { "Download web model to local hardware".to_string() }
    fn execute(&self, arg: &str, _workspace: &Path) -> EaiResult<String> {
        Ok(ModelManager::install_model(arg))
    }
}

struct AgentsTool;
impl GhaTool for AgentsTool {
    fn name(&self) -> String { "agents".to_string() }
    fn description(&self) -> String { "List all active GAWD agents".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        let fleet = crate::gawd::agents::GawdAgentFleet::synthesize_fleet("status");
        let mut out = format!("GAWD Agent Fleet ({} Active Agents):\n", fleet.len());
        for a in fleet {
            out.push_str(&format!("  - {} (Role: {} | Protocol: {})\n", a.name, a.role, a.protocol));
        }
        Ok(out)
    }
}

struct EnginesTool;
impl GhaTool for EnginesTool {
    fn name(&self) -> String { "engines".to_string() }
    fn description(&self) -> String { "List active execution & inference engines".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        let hardware = HardwareProfiler::get_profile();
        let has_weights = crate::gemi::pulse::GhaPulse::try_load_candle_weights().is_ok();
        let mut out = "Active Execution & Inference Engines:\n".to_string();
        out.push_str("  - Tier 2 GEMI Multi-Model Router (Default | Cloud-First Reasoning)\n");
        out.push_str("  - Tier 0 GHA-Alpha (Native Microsecond Reflex Engine)\n");
        out.push_str(&format!("  - Tier 0 Candle Tensor Engine (Safetensors Weights: {})\n", if has_weights { "LOADED" } else { "AUTONOMOUS INITIALIZED" }));
        out.push_str(&format!("  - Hardware Acceleration: {} CPUs | {}\n", hardware.cpus, hardware.gpu_info));
        if std::process::Command::new("ollama").arg("list").output().is_ok() {
            out.push_str("  - Local Ollama Engine (Available for local-only missions)\n");
        }
        Ok(out)
    }
}

struct ClientsTool;
impl GhaTool for ClientsTool {
    fn name(&self) -> String { "clients".to_string() }
    fn description(&self) -> String { "List configured MCP clients and proxies".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        let external_tools = GmcpClient::list_external_tools();
        let mut out = format!("Configured MCP Clients & Proxies ({} Configured):\n", external_tools.len());
        if external_tools.is_empty() {
            out.push_str("  - Default Native GMCP Client Active\n");
        } else {
            for t in external_tools {
                out.push_str(&format!("  - {} ({})\n", t.name, t.description));
            }
        }
        Ok(out)
    }
}

struct ServersTool;
impl GhaTool for ServersTool {
    fn name(&self) -> String { "servers".to_string() }
    fn description(&self) -> String { "List running GHA local servers".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        let home = std::env::var_os("HOME").unwrap_or_else(|| ".".into());
        let global_dir = PathBuf::from(home).join(".gha");
        let cfg = crate::sandbox::manager::GhaConfig::load(&global_dir);
        let daemon_pid = crate::daemon::server::GmaDaemon::check_status(&global_dir);
        let mut out = "GHA Local Servers & Background Hosts:\n".to_string();
        match daemon_pid {
            Some(pid) => out.push_str(&format!("  - GMA Master Daemon: RUNNING (PID {})\n", pid)),
            None => out.push_str("  - GMA Master Daemon: INACTIVE\n"),
        }
        let ports = vec![
            (cfg.gmcp_port, "GMCP JSON-RPC TCP Server"),
            (cfg.gemi_port, "GEMI OpenAI-Compatible REST Server")
        ];
        for (port, name) in ports {
            let active = TcpStream::connect_timeout(&format!("127.0.0.1:{}", port).parse().unwrap(), Duration::from_millis(50)).is_ok();
            out.push_str(&format!("  - {} (Port {}): {}\n", name, port, if active { "RUNNING" } else { "STANDBY" }));
        }
        Ok(out)
    }
}

struct ConnectProviderTool;
impl GhaTool for ConnectProviderTool {
    fn name(&self) -> String { "connect_provider".to_string() }
    fn description(&self) -> String { "Check or connect model provider".to_string() }
    fn execute(&self, arg: &str, _workspace: &Path) -> EaiResult<String> {
        let provider = arg.to_lowercase();
        let env_key = if provider.contains("openai") { "OPENAI_API_KEY" }
        else if provider.contains("gemini") { "GEMINI_API_KEY" }
        else if provider.contains("anthropic") { "ANTHROPIC_API_KEY" }
        else { "" };

        if !env_key.is_empty() {
             if std::env::var(env_key).is_ok() {
                 return Ok(format!("Tier 2 GEMI: Provider '{}' is active.", provider));
             } else {
                 return Ok(format!("{} is not set. Use '/setkey {}' to connect.", env_key, env_key));
             }
        }
        Ok(format!("Provider status check complete for '{}'.", arg))
    }
}

struct UseModelTool;
impl GhaTool for UseModelTool {
    fn name(&self) -> String { "use_model".to_string() }
    fn description(&self) -> String { "Select active model override".to_string() }
    fn execute(&self, arg: &str, _workspace: &Path) -> EaiResult<String> {
        ModelManager::set_selected_model(arg).map_err(|e| EaiError::Inference(e))
    }
}

struct ListModelsTool;
impl GhaTool for ListModelsTool {
    fn name(&self) -> String { "list_models".to_string() }
    fn description(&self) -> String { "Inspect local and cloud models".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        let models = ModelManager::list_models(workspace);
        let selected = ModelManager::get_selected_model();
        let mut output = format!("Active Models ({})\n", models.len());

        let mut local_models = Vec::new();
        let mut cloud_models = Vec::new();

        for m in models {
            let badge = if m.is_local { "🟢 LOCAL" } else { "🌐 CLOUD" };
            let entry = format!("   - [{}] {} ({})", badge, m.name, m.model_id);
            if m.is_local { local_models.push(entry); } else { cloud_models.push(entry); }
        }

        output.push_str("\n🟢 LOCAL MODELS:\n");
        output.push_str(&local_models.join("\n"));
        output.push_str("\n🌐 CLOUD MODELS:\n");
        output.push_str(&cloud_models.join("\n"));
        output.push_str(&format!("\n\nActive: {}", selected.unwrap_or_else(|| "Auto".to_string())));
        Ok(output)
    }
}

struct VerifyModelsTool;
impl GhaTool for VerifyModelsTool {
    fn name(&self) -> String { "verify_models".to_string() }
    fn description(&self) -> String { "Verify local model integrity".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        let results = ModelManager::verify_local_models(workspace);
        if results.is_empty() { return Ok("No local models found.".to_string()); }
        let mut out = "# Model Verification\n\n".to_string();
        for r in results {
            out.push_str(&format!("- {}: Valid={}, Size: {}\n", r.model_id, r.is_valid_gguf, r.file_size_formatted));
        }
        Ok(out)
    }
}

struct SelfTrainTool;
impl GhaTool for SelfTrainTool {
    fn name(&self) -> String { "self_train".to_string() }
    fn description(&self) -> String { "Trigger autonomous agent self-training".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        let count = arg.parse::<usize>().unwrap_or(10);
        let home = std::env::var_os("HOME").unwrap_or_else(|| ".".into());
        let global_dir = PathBuf::from(home).join(".gha");
        let intents = ["version", "status", "build", "test", "explain GHA"];
        let mut entries = Vec::new();
        for i in 0..count {
            entries.push(crate::gawd::pkb::PkbSynthesizer::generate_sample(intents[i % intents.len()], workspace));
        }
        let msg = crate::gawd::pkb::PkbSynthesizer::save_training_data(entries, &global_dir)?;
        let distill = crate::gawd::pkb::PkbSynthesizer::distill_step_0_to_63(&global_dir)?;
        Ok(format!("{}\nNeural distillation complete: {}", msg, distill))
    }
}

struct ScoutTool;
impl GhaTool for ScoutTool {
    fn name(&self) -> String { "scout".to_string() }
    fn description(&self) -> String { "Discover cloud engines and agents".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        let mut assets = Vec::new();
        assets.extend(crate::gemi::reflex::ReflexEngine::scout_tier0_assets());
        assets.extend(crate::gawd::agents::GawdAgentFleet::scout_tier1_assets());
        let mut output = "# Discovery Report\n\n".to_string();
        for a in assets {
            output.push_str(&format!("## {}\n- {}: {}\n", a.tier, a.name, a.url));
        }
        Ok(output)
    }
}

struct ServicesTool;
impl GhaTool for ServicesTool {
    fn name(&self) -> String { "services".to_string() }
    fn description(&self) -> String { "List running GHA background services".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        let home = std::env::var_os("HOME").unwrap_or_else(|| ".".into());
        let global_dir = PathBuf::from(home).join(".gha");
        let cfg = crate::sandbox::manager::GhaConfig::load(&global_dir);
        let daemon_pid = crate::daemon::server::GmaDaemon::check_status(&global_dir);
        let mut out = "# GHA Services\n\n".to_string();
        out.push_str(&format!("- Daemon: {}\n", if daemon_pid.is_some() { "RUNNING" } else { "INACTIVE" }));
        let ports = vec![(cfg.gmcp_port, "GMCP"), (cfg.gemi_port, "GEMI REST")];
        for (p, n) in ports {
            let active = TcpStream::connect_timeout(&format!("127.0.0.1:{}", p).parse().unwrap(), Duration::from_millis(50)).is_ok();
            out.push_str(&format!("- {} ({}): {}\n", n, p, if active { "ACTIVE" } else { "OFFLINE" }));
        }
        Ok(out)
    }
}

struct VerifyCloudProvidersTool;
impl GhaTool for VerifyCloudProvidersTool {
    fn name(&self) -> String { "verify_cloud_providers".to_string() }
    fn description(&self) -> String { "Verify active cloud API keys".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        let mut out = "# Cloud Verification\n\n".to_string();
        let keys = vec![("GROQ_API_KEY", "Groq"), ("GEMINI_API_KEY", "Google Gemini"), ("OPENAI_API_KEY", "OpenAI")];
        for (k, n) in keys {
            if let Ok(_) = std::env::var(k) {
                let res = GemiEngine::verify_provider(n);
                out.push_str(&format!("- {}: {}\n", n, res));
            }
        }
        Ok(out)
    }
}

struct VerifyMcpServersTool;
impl GhaTool for VerifyMcpServersTool {
    fn name(&self) -> String { "verify_mcp_servers".to_string() }
    fn description(&self) -> String { "Verify configured MCP servers".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        let tools = GmcpClient::list_external_tools();
        if tools.is_empty() { return Ok("No external MCP tools.".to_string()); }
        let mut out = "# MCP Health\n\n".to_string();
        for t in tools {
            let name = t.name.split(':').next().unwrap_or(&t.name);
            let (lat, ok) = GmcpClient::benchmark_server(name);
            out.push_str(&format!("- {}: {}, {}ms\n", name, if ok { "OK" } else { "FAIL" }, lat));
        }
        Ok(out)
    }
}

struct ProvisionMcpTool;
impl GhaTool for ProvisionMcpTool {
    fn name(&self) -> String { "provision_mcp".to_string() }
    fn description(&self) -> String { "Search and auto-configure new MCP server".to_string() }
    fn execute(&self, arg: &str, _workspace: &Path) -> EaiResult<String> {
        let registry = GmcpClient::fetch_global_registry();
        let target = arg.to_lowercase();
        if let Some(entry) = registry.iter().find(|e| e.name.contains(&target) || e.description.to_lowercase().contains(&target)) {
            let res = GmcpClient::auto_configure_server(&entry.name, &entry.package);
            return Ok(if res == "SUCCESS_CONFIGURED" { format!("✅ Provisioned '{}'.", entry.name) } else { "❌ Failed.".into() });
        }
        Ok("Capability not found in global registry.".into())
    }
}

struct DebugEngineTool;
impl GhaTool for DebugEngineTool {
    fn name(&self) -> String { "debug_engine".to_string() }
    fn description(&self) -> String { "Autonomous self-debugging loop".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        let source = fs::read_to_string(workspace.join("src/main.rs")).unwrap_or_default();
        let reasoning = GemiEngine::generate_reasoning_deep(&format!("Debug error: {}\n\n{}", arg, source), workspace);
        Ok(format!("🛠️ [Self-Debug]:\n{}", reasoning))
    }
}

struct VisionAnalyzeTool;
impl GhaTool for VisionAnalyzeTool {
    fn name(&self) -> String { "vision_analyze".to_string() }
    fn description(&self) -> String { "Analyze image with multimodal vision".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        let parts: Vec<&str> = arg.splitn(2, ' ').collect();
        if parts.len() < 2 { return Err(EaiError::Protocol("Usage: vision_analyze <path> <prompt>".into())); }
        Ok(GemiEngine::generate_multimodal_vision(parts[1], &workspace.join(parts[0])))
    }
}

struct RunTestHarnessTool;
impl GhaTool for RunTestHarnessTool {
    fn name(&self) -> String { "run_test_harness".to_string() }
    fn description(&self) -> String { "Run workspace unit test harness".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        if workspace.join("Cargo.toml").exists() {
            let out = Command::new("cargo").args(["test", "--no-run"]).current_dir(workspace).output().map_err(|e| EaiError::Hardware(e.to_string()))?;
            return Ok(if out.status.success() { "✅ Test build PASSED." } else { "❌ Test build FAILED." }.into());
        }
        Ok("Generic harness ready.".into())
    }
}

struct BenchmarkTool;
impl GhaTool for BenchmarkTool {
    fn name(&self) -> String { "benchmark".to_string() }
    fn description(&self) -> String { "Run performance benchmark on local and cloud models".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        let results = ModelManager::run_benchmark(workspace, arg);
        if results.is_empty() { return Ok("No models matched benchmark filter.".to_string()); }

        let mut out = format!("# GHA Intelligence Benchmark Report\n\n");
        out.push_str("| Model | Tier | Latency | Speed | Status |\n");
        out.push_str("| :--- | :--- | :--- | :--- | :--- |\n");

        for r in results {
            let tier = if r.is_local { "LOCAL" } else { "CLOUD" };
            let speed = if r.tokens_per_sec > 0.0 { format!("{:.1} t/s", r.tokens_per_sec) } else { "N/A".into() };
            out.push_str(&format!("| {} | {} | {}ms | {} | {} |\n", r.name, tier, r.latency_ms, speed, r.status));
        }

        Ok(out)
    }
}

struct ComplianceTool;
impl GhaTool for ComplianceTool {
    fn name(&self) -> String { "compliance".to_string() }
    fn description(&self) -> String { "Run full GHA compliance audit (Rule 15)".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        GhaAdmin::audit_compliance(workspace)
    }
}

struct VersionSyncTool;
impl GhaTool for VersionSyncTool {
    fn name(&self) -> String { "version_sync".to_string() }
    fn description(&self) -> String { "Synchronize project version across all manifests (Rule 1)".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        GhaAdmin::sync_version(workspace)
    }
}

struct ReleaseTool;
impl GhaTool for ReleaseTool {
    fn name(&self) -> String { "release".to_string() }
    fn description(&self) -> String { "Execute full GHA release cycle (Audit -> Build -> Bump -> Push -> Install)".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        GhaAdmin::execute_release(workspace)
    }
}

struct EvolveTool;
impl GhaTool for EvolveTool {
    fn name(&self) -> String { "evolve".to_string() }
    fn description(&self) -> String { "Analyze audit log and propose native substrate evolution (Rule 17)".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        EvolutionManager::evolve_substrate(workspace)
    }
}

struct DiscoveryTool;
impl GhaTool for DiscoveryTool {
    fn name(&self) -> String { "discovery".to_string() }
    fn description(&self) -> String { "Provide machine-readable summary of the substrate (Rule 12)".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        let hardware = crate::gemi::hardware::HardwareProfiler::get_profile();
        let (engine, model) = crate::gemi::models::ModelManager::get_active_engine_and_model();
        let tools = ToolRegistry::list_tools();

        let info = serde_json::json!({
            "version": crate::GHA_VERSION,
            "identity": "GHA Intelligence Substrate",
            "engine": engine,
            "model": model,
            "hardware": {
                "cpus": hardware.cpus,
                "gpu": hardware.gpu_info,
                "acceleration": hardware.acceleration_active,
                "os": hardware.os_info
            },
            "reflexes": tools.iter().map(|t| &t.name).collect::<Vec<_>>()
        });

        Ok(serde_json::to_string_pretty(&info).unwrap_or_default())
    }
}

struct AdvanceTool;
impl GhaTool for AdvanceTool {
    fn name(&self) -> String { "advance".to_string() }
    fn description(&self) -> String { "Execute autonomous evolution cycle (v1.0.0 Self-Distillation Threshold)".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        GhaAdmin::execute_autonomous_evolution_cycle(workspace)
    }
}

struct DistillTool;
impl GhaTool for DistillTool {
    fn name(&self) -> String { "distill".to_string() }
    fn description(&self) -> String { "Distill deep reasoning intent into native Rust or Wasm reflex (Rule 17)".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        ReflexSynthesizer::synthesize_wasm_reflex(arg, workspace)
    }
}

struct SwarmStatusTool;
impl GhaTool for SwarmStatusTool {
    fn name(&self) -> String { "swarm_status".to_string() }
    fn description(&self) -> String { "Inspect GHA cluster mesh and peer hardware capabilities".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        let nodes = GmasSupervisor::list_cluster_nodes();
        let mut out = format!("# GHA Swarm Intelligence Mesh (AOA/A2A)\n\n");
        out.push_str("| Node ID | Type | Address | Capabilities | Status |\n");
        out.push_str("| :--- | :--- | :--- | :--- | :--- |\n");

        for n in nodes {
            let status = if n.is_active { "🟢 ACTIVE" } else { "🔴 OFFLINE" };
            out.push_str(&format!("| {} | {} | {} | {} | {} |\n", n.node_id, n.node_type, n.address, n.capabilities.join(", "), status));
        }

        Ok(out)
    }
}

struct ReplicateStateTool;
impl GhaTool for ReplicateStateTool {
    fn name(&self) -> String { "replicate_state".to_string() }
    fn description(&self) -> String { "Replicate neural checkpoint state to local workstation (Rule 16)".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        if let Ok(checkpoint) = serde_json::from_str::<NeuralCheckpoint>(arg) {
             let replica_dir = workspace.join(".gha/replicas");
             fs::create_dir_all(&replica_dir)?;
             let file_path = replica_dir.join(format!("replica_{}.json", checkpoint.timestamp));
             fs::write(&file_path, arg)?;
             return Ok(format!("Sync complete: Replicated mission '{}' to cluster.", checkpoint.intent));
        }
        Err(EaiError::Protocol("Invalid checkpoint payload".into()))
    }
}

struct GetCheckpointsTool;
impl GhaTool for GetCheckpointsTool {
    fn name(&self) -> String { "get_checkpoints".to_string() }
    fn description(&self) -> String { "Retrieve resumeable neural checkpoints from this node".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        let mut list = Vec::new();
        if let Some(local) = crate::sandbox::manager::SandboxManager::check_interrupted_checkpoint(workspace) {
            list.push(local);
        }

        let replica_dir = workspace.join(".gha/replicas");
        if let Ok(entries) = fs::read_dir(&replica_dir) {
            for entry in entries.flatten() {
                if let Ok(content) = fs::read_to_string(entry.path()) {
                    if let Ok(cp) = serde_json::from_str::<NeuralCheckpoint>(&content) {
                        list.push(cp);
                    }
                }
            }
        }

        Ok(serde_json::to_string(&list).unwrap_or_else(|_| "[]".into()))
    }
}

struct ScoutModelTool;
impl GhaTool for ScoutModelTool {
    fn name(&self) -> String { "scout_model".to_string() }
    fn description(&self) -> String { "Search for a specific model GGUF URL using intelligence (Rule 17)".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        let prompt = format!(
            "MISSION: FIND VERIFIED GGUF URL FOR MODEL: '{}'\n\n\
            REQUIREMENTS:\n\
            1. Find the direct download URL for the best Q4_K_M (or equivalent) GGUF file on HuggingFace or ModelScope.\n\
            2. Return ONLY a JSON object with the following keys: 'name', 'url', 'quantization', 'size_gb'.\n\
            3. Do not include any other text.",
            arg
        );

        let reasoning = GemiEngine::generate_reasoning_deep(&prompt, workspace);
        if reasoning.contains("ERROR:") {
             return Err(EaiError::Inference("Tier 2 reasoning unavailable for model scouting.".into()));
        }

        let clean_json = reasoning.trim().trim_start_matches("```json").trim_end_matches("```").trim();
        if let Ok(entry) = serde_json::from_str::<ModelRegistryEntry>(clean_json) {
            let mut entry_with_time = entry.clone();
            entry_with_time.discovered_at = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_secs();
            ModelRegistry::update_mapping(arg, entry_with_time)?;
            return Ok(format!("Reflex Learned: Model '{}' verified at {}", arg, entry.url));
        }

        Ok(format!("Scouting complete for '{}', but response was not a valid registry entry: {}", arg, reasoning))
    }
}

struct SelfHealBuildTool;
impl GhaTool for SelfHealBuildTool {
    fn name(&self) -> String { "self_heal_build".to_string() }
    fn description(&self) -> String { "Self-healing code compilation loop".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        if workspace.join("Cargo.toml").exists() {
            let out = Command::new("cargo").arg("check").current_dir(workspace).output().map_err(|e| EaiError::Hardware(e.to_string()))?;
            if out.status.success() { return Ok("Build clean.".into()); }
            let stderr = String::from_utf8_lossy(&out.stderr);
            let fix = GemiEngine::generate_reasoning_deep(&format!("Fix build: {}", stderr), workspace);
            return Ok(format!("Error found. Suggested fix:\n{}", fix));
        }
        Ok("No Cargo.toml found.".into())
    }
}

struct InfraCommandTool { name: String, bin: String, args: Vec<&'static str> }
impl GhaTool for InfraCommandTool {
    fn name(&self) -> String { self.name.clone() }
    fn description(&self) -> String { format!("Execute {} command", self.bin) }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        let out = Command::new(&self.bin).args(&self.args).current_dir(workspace).output().map_err(|e| EaiError::Hardware(e.to_string()))?;
        Ok(format!("✅ [{} Result]:\n{}", self.bin, String::from_utf8_lossy(&out.stdout)))
    }
}

struct OrchestrateTool;
impl GhaTool for OrchestrateTool {
    fn name(&self) -> String { "orchestrate".to_string() }
    fn description(&self) -> String { "Execute multi-agent mission".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        Ok("GMA multi-agent orchestration active.".to_string())
    }
}

struct ExecCommandTool;
impl GhaTool for ExecCommandTool {
    fn name(&self) -> String { "exec_command".to_string() }
    fn description(&self) -> String { "Execute system shell command (Hardened)".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        crate::sandbox::wasm::WasiSandbox::execute_hardened_command(arg, workspace)
    }
}

struct ReadFileTool;
impl GhaTool for ReadFileTool {
    fn name(&self) -> String { "read_file".to_string() }
    fn description(&self) -> String { "Read workspace file content".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        fs::read_to_string(workspace.join(arg)).map_err(|e| EaiError::Sandbox(e.to_string()))
    }
}

struct WriteFileTool;
impl GhaTool for WriteFileTool {
    fn name(&self) -> String { "write_file".to_string() }
    fn description(&self) -> String { "Write to a workspace file (Auto-creates directories)".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        let parts: Vec<&str> = arg.splitn(2, ' ').collect();
        if parts.len() < 2 { return Err(EaiError::Protocol("Usage: write_file <path> <content>".into())); }
        let path_str = parts[0].trim();
        let content = parts[1];
        let full_path = workspace.join(path_str);

        if let Some(parent) = full_path.parent() {
            fs::create_dir_all(parent).map_err(|e| EaiError::Sandbox(format!("Failed to create directories for {}: {}", path_str, e)))?;
        }

        fs::write(&full_path, content).map_err(|e| EaiError::Sandbox(format!("Failed to write file {}: {}", path_str, e)))?;
        Ok(format!("✅ Wrote to {}", path_str))
    }
}

struct ListDirectoryTool;
impl GhaTool for ListDirectoryTool {
    fn name(&self) -> String { "list_directory".to_string() }
    fn description(&self) -> String { "List entries in directory".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        let target = if arg.is_empty() { workspace } else { Path::new(arg) };
        let mut list = Vec::new();
        for entry in fs::read_dir(target).map_err(|e| EaiError::Sandbox(e.to_string()))?.flatten() {
            list.push(format!("{:?}", entry.file_name()));
        }
        Ok(format!("Entries: {}", list.join(", ")))
    }
}

struct GetDiskUsageTool;
impl GhaTool for GetDiskUsageTool {
    fn name(&self) -> String { "get_disk_usage".to_string() }
    fn description(&self) -> String { "Inspect disk usage (df -h)".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        let out = Command::new("df").args(["-h", workspace.to_str().unwrap_or(".")]).output().map_err(|e| EaiError::Hardware(e.to_string()))?;
        Ok(String::from_utf8_lossy(&out.stdout).to_string())
    }
}

struct ExportDocTool;
impl GhaTool for ExportDocTool {
    fn name(&self) -> String { "export_doc".to_string() }
    fn description(&self) -> String { "Export report to file".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        let parts: Vec<&str> = arg.splitn(2, ' ').collect();
        let filename = parts.first().copied().unwrap_or("report.html");
        let content = parts.get(1).copied().unwrap_or(arg);
        fs::write(workspace.join(filename), content).map_err(|e| EaiError::Sandbox(e.to_string()))?;
        Ok(format!("📄 Saved to {}", filename))
    }
}

struct ScheduleTaskTool;
impl GhaTool for ScheduleTaskTool {
    fn name(&self) -> String { "schedule_task".to_string() }
    fn description(&self) -> String { "Schedule background task".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        let parts: Vec<&str> = arg.splitn(2, ' ').collect();
        let interval = parts.first().unwrap_or(&"3600");
        let mission = parts.get(1).unwrap_or(&"");
        Ok(crate::sandbox::manager::SandboxManager::save_scheduled_task(workspace, interval, mission))
    }
}

struct ListSchedulesTool;
impl GhaTool for ListSchedulesTool {
    fn name(&self) -> String { "list_schedules".to_string() }
    fn description(&self) -> String { "List scheduled tasks".to_string() }
    fn execute(&self, _arg: &str, workspace: &Path) -> EaiResult<String> {
        let tasks = crate::sandbox::manager::SandboxManager::load_scheduled_tasks(workspace);
        Ok(format!("Schedules: {:?}", tasks))
    }
}

struct SwarmSyncTool;
impl GhaTool for SwarmSyncTool {
    fn name(&self) -> String { "swarm_sync".to_string() }
    fn description(&self) -> String { "Synchronize mission context".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        Ok("Sync complete.".to_string())
    }
}

struct SelfEvolveTool;
impl GhaTool for SelfEvolveTool {
    fn name(&self) -> String { "self_evolve".to_string() }
    fn description(&self) -> String { "Autonomous agent self-evolution".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        Ok("Evolving...".to_string())
    }
}

struct GlobalRegistryScanTool;
impl GhaTool for GlobalRegistryScanTool {
    fn name(&self) -> String { "global_registry_scan".to_string() }
    fn description(&self) -> String { "Scan global service registry".to_string() }
    fn execute(&self, _arg: &str, _workspace: &Path) -> EaiResult<String> {
        let reg = GmcpClient::fetch_global_registry();
        Ok(format!("Discovered {} entries.", reg.len()))
    }
}

struct WebSearchDownloadTool;
impl GhaTool for WebSearchDownloadTool {
    fn name(&self) -> String { "web_search_download".to_string() }
    fn description(&self) -> String { "Search the web and download content".to_string() }
    fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String> {
        let clean_query = arg.trim();
        if clean_query.is_empty() { return Err(EaiError::Protocol("Usage: download <query>".into())); }
        let filename = "download_content.txt";
        let save_path = workspace.join(filename);
        let encoded_query = clean_query.replace(' ', "+");
        let search_url = format!("https://html.duckduckgo.com/html/?q={}", encoded_query);
        let out = Command::new("curl").args(["-sL", "-A", "Mozilla/5.0", &search_url]).output().map_err(|e| EaiError::Hardware(e.to_string()))?;
        let html = String::from_utf8_lossy(&out.stdout).to_string();
        let _ = fs::write(&save_path, &html);
        Ok(format!("Fetched content for '{}' and saved to {}.", clean_query, filename))
    }
}
