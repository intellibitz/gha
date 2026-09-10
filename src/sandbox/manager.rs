// GHA Sandbox Manager
// 100% Rust implementation for Global Engine State Management

use std::fs;
use std::path::{Path, PathBuf};
use serde::{Deserialize, Serialize};
use crate::gmcp::GlobalMcpEntry;
use crate::error::{EaiError, EaiResult};

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, PartialOrd, Eq, Ord)]
pub enum ModelTier {
    Premier = 0,
    Specialist = 1,
    Standard = 2,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub enum ProviderType {
    OpenAI,
    Google,
    Anthropic,
    NativeCandle,
    LocalGGUF,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ModelInfo {
    pub name: String,
    pub registry: String,
    pub model_id: String,
    pub description: String,
    pub is_local: bool,
    pub tier: ModelTier,
    pub latency_ms: Option<u128>,
    pub provider: ProviderType,
    pub api_base: Option<String>,
    pub env_key: Option<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct NeuralCheckpoint {
    pub intent: String,
    pub timestamp: u64,
    pub completed_tools: Vec<String>,
    pub blackboard: std::collections::HashMap<String, String>,
    pub status: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GhaConfig {
    pub gmcp_port: u16,
    pub gemi_port: u16,
    pub udp_discovery_port: u16,
    pub default_engine: String,
    pub default_model: String,
    pub auto_download_models: bool,
    pub gha_repo: String,
    pub mcp_registry_url: String,
    pub cloud_models: Vec<ModelInfo>,
    pub bootstrap_mcp_servers: Vec<GlobalMcpEntry>,
    pub cloud_scout_timeout_secs: u64,
    pub beacon_interval_secs: u64,
    pub local_scan_paths: Vec<String>,
}

impl Default for GhaConfig {
    fn default() -> Self {
        GhaConfig {
            gmcp_port: 9090,
            gemi_port: 9091,
            udp_discovery_port: 9092,
            default_engine: "gha-offline".to_string(),
            default_model: "gha-alpha".to_string(),
            auto_download_models: true,
            gha_repo: "intellibitz/gha".to_string(),
            mcp_registry_url: "https://raw.githubusercontent.com/intellibitz/gha/main/registry.json".to_string(),
            cloud_models: vec![
                ModelInfo {
                    name: "Google Gemini 1.5 Flash".to_string(),
                    registry: "GHA Tier 2 Registry".to_string(),
                    model_id: "google/gemini-1.5-flash".to_string(),
                    description: "1M+ token context cloud reasoning".to_string(),
                    is_local: false,
                    tier: ModelTier::Premier,
                    latency_ms: None,
                    provider: ProviderType::Google,
                    api_base: Some("https://generativelanguage.googleapis.com/v1beta".to_string()),
                    env_key: Some("GEMINI_API_KEY".to_string()),
                },
                ModelInfo {
                    name: "OpenAI GPT-4o".to_string(),
                    registry: "GHA Tier 2 Registry".to_string(),
                    model_id: "openai/gpt-4o".to_string(),
                    description: "Industry-standard reasoning & tool-use".to_string(),
                    is_local: false,
                    tier: ModelTier::Premier,
                    latency_ms: None,
                    provider: ProviderType::OpenAI,
                    api_base: Some("https://api.openai.com/v1".to_string()),
                    env_key: Some("OPENAI_API_KEY".to_string()),
                },
                ModelInfo {
                    name: "Anthropic Claude 3.5 Sonnet".to_string(),
                    registry: "GHA Tier 2 Registry".to_string(),
                    model_id: "anthropic/claude-3.5-sonnet".to_string(),
                    description: "High-precision reasoning specialist".to_string(),
                    is_local: false,
                    tier: ModelTier::Premier,
                    latency_ms: None,
                    provider: ProviderType::Anthropic,
                    api_base: Some("https://api.anthropic.com/v1".to_string()),
                    env_key: Some("ANTHROPIC_API_KEY".to_string()),
                },
                ModelInfo {
                    name: "Groq Gemma 2 9B".to_string(),
                    registry: "GHA Tier 2 Registry".to_string(),
                    model_id: "groq/gemma2-9b-it".to_string(),
                    description: "Ultra-low latency cloud reasoning".to_string(),
                    is_local: false,
                    tier: ModelTier::Specialist,
                    latency_ms: None,
                    provider: ProviderType::OpenAI,
                    api_base: Some("https://api.groq.com/openai/v1".to_string()),
                    env_key: Some("GROQ_API_KEY".to_string()),
                },
            ],
            bootstrap_mcp_servers: vec![
                GlobalMcpEntry { name: "postgres".to_string(), description: "Standard Protocol SQL Database Server".to_string(), package: "@modelcontextprotocol/server-postgres".to_string(), category: "database".to_string() },
                GlobalMcpEntry { name: "brave_search".to_string(), description: "Standard Protocol Web Search Server".to_string(), package: "@modelcontextprotocol/server-brave-search".to_string(), category: "search".to_string() },
                GlobalMcpEntry { name: "github".to_string(), description: "Standard Protocol GitHub Repos & PRs Server".to_string(), package: "@modelcontextprotocol/server-github".to_string(), category: "vcs".to_string() },
            ],
            cloud_scout_timeout_secs: 8,
            beacon_interval_secs: 30,
            local_scan_paths: Vec::new(),
        }
    }
}

impl GhaConfig {
    pub fn get_config_path(global_dir: &Path) -> PathBuf {
        global_dir.join("config.json")
    }

    pub fn load(global_dir: &Path) -> Self {
        let path = Self::get_config_path(global_dir);
        if path.is_file()
            && let Ok(content) = fs::read_to_string(&path)
            && let Ok(config) = serde_json::from_str::<GhaConfig>(&content)
        {
            return config;
        }

        let default_config = GhaConfig::default();
        let _ = fs::write(&path, serde_json::to_string_pretty(&default_config).unwrap_or_default());
        default_config
    }

    #[allow(dead_code)]
    pub fn save(&self, global_dir: &Path) -> EaiResult<String> {
        let path = Self::get_config_path(global_dir);
        let content = serde_json::to_string_pretty(self).map_err(|e| EaiError::Sandbox(e.to_string()))?;
        fs::write(&path, content).map_err(|e| EaiError::Sandbox(e.to_string()))?;
        Ok(format!("Saved GHA configuration to {}", path.display()))
    }
}

pub struct SandboxManager;

impl SandboxManager {
    pub fn ensure_global_sandbox(global_dir: &Path) -> PathBuf {
        if !global_dir.exists() {
            let _ = fs::create_dir_all(global_dir);
            let _ = fs::create_dir_all(global_dir.join("bin"));
            let _ = fs::create_dir_all(global_dir.join("models"));
            let _ = fs::create_dir_all(global_dir.join("train"));
        }
        let _ = GhaConfig::load(global_dir);
        Self::load_env_file(global_dir);
        global_dir.to_path_buf()
    }

    pub fn load_env_file(global_dir: &Path) {
        let env_file = global_dir.join("env");
        if env_file.is_file()
            && let Ok(content) = fs::read_to_string(&env_file)
        {
            for line in content.lines() {
                let trimmed = line.trim();
                if trimmed.is_empty() || trimmed.starts_with('#') {
                    continue;
                }
                if let Some((k, v)) = trimmed.split_once('=') {
                    let key = k.trim();
                    let val = v.trim().trim_matches('"').trim_matches('\'');
                    if !key.is_empty() && !val.is_empty() && std::env::var(key).is_err() {
                        unsafe {
                            let _ = std::env::set_var(key, val);
                        }
                    }
                }
            }
        }
    }

    #[allow(dead_code)]
    pub fn save_env_key(global_dir: &Path, key: &str, val: &str) -> EaiResult<String> {
        let env_file = global_dir.join("env");
        let mut lines = Vec::new();
        if env_file.is_file()
            && let Ok(content) = fs::read_to_string(&env_file)
        {
            for l in content.lines() {
                if !l.trim().starts_with(&format!("{}=", key)) {
                    lines.push(l.to_string());
                }
            }
        }
        lines.push(format!("{}={}", key, val));
        fs::write(&env_file, lines.join("\n")).map_err(|e| EaiError::Sandbox(e.to_string()))?;
        unsafe {
            let _ = std::env::set_var(key, val);
        }
        Ok(format!("Saved {} to {}", key, env_file.display()))
    }

    #[allow(dead_code)]
    pub fn is_global_sandbox_active(global_dir: &Path) -> bool {
        global_dir.join("bin").is_dir()
    }

    pub fn save_mission_checkpoint(workspace: &Path, checkpoint: &NeuralCheckpoint) {
        let gha_dir = workspace.join(".gha");
        let _ = fs::create_dir_all(&gha_dir);
        let checkpoint_file = gha_dir.join("mission_checkpoint.json");
        if let Ok(json) = serde_json::to_string(checkpoint) {
            let _ = fs::write(&checkpoint_file, json);
        }
    }

    pub fn check_interrupted_checkpoint(workspace: &Path) -> Option<NeuralCheckpoint> {
        let checkpoint_file = workspace.join(".gha/mission_checkpoint.json");
        if checkpoint_file.is_file()
            && let Ok(content) = fs::read_to_string(&checkpoint_file)
            && let Ok(checkpoint) = serde_json::from_str::<NeuralCheckpoint>(&content)
        {
            if checkpoint.status == "IN_PROGRESS" {
                return Some(checkpoint);
            }
        }
        None
    }

    #[allow(dead_code)]
    pub fn clear_mission_checkpoint(workspace: &Path) {
        let checkpoint_file = workspace.join(".gha/mission_checkpoint.json");
        if checkpoint_file.exists() {
            let _ = fs::remove_file(checkpoint_file);
        }
    }

    pub fn save_scheduled_task(workspace: &Path, interval_str: &str, mission: &str) -> String {
        let gha_dir = workspace.join(".gha");
        let _ = fs::create_dir_all(&gha_dir);
        let schedule_file = gha_dir.join("scheduled_tasks.jsonl");

        let interval_secs = interval_str.parse::<u64>().unwrap_or(3600);
        let timestamp = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).map(|d| d.as_secs()).unwrap_or(0);

        let task = serde_json::json!({
            "interval_secs": interval_secs,
            "mission": mission,
            "created_at": timestamp
        });

        if let Ok(line) = serde_json::to_string(&task) {
            let mut content = fs::read_to_string(&schedule_file).unwrap_or_default();
            content.push_str(&line);
            content.push('\n');
            let _ = fs::write(&schedule_file, content);
            format!("⏱️ Scheduled task registered: \"{}\" every {}s.", mission, interval_secs)
        } else {
            "Failed to serialize scheduled task.".to_string()
        }
    }

    pub fn load_scheduled_tasks(workspace: &Path) -> Vec<serde_json::Value> {
        let schedule_file = workspace.join(".gha/scheduled_tasks.jsonl");
        let mut tasks = Vec::new();
        if schedule_file.is_file()
            && let Ok(content) = fs::read_to_string(&schedule_file)
        {
            for line in content.lines() {
                if let Ok(task) = serde_json::from_str::<serde_json::Value>(line) {
                    tasks.push(task);
                }
            }
        }
        tasks
    }
}

pub struct GhaMemory;

impl GhaMemory {
    pub fn append_interaction(workspace: &Path, intent: &str, response: &str) {
        let gha_dir = workspace.join(".gha");
        let _ = fs::create_dir_all(&gha_dir);
        let memory_file = gha_dir.join("memory.jsonl");

        let timestamp = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .map(|d| d.as_secs())
            .unwrap_or(0);

        let entry = serde_json::json!({
            "timestamp": timestamp,
            "user_intent": intent,
            "assistant_response": response
        });

        if let Ok(line) = serde_json::to_string(&entry) {
            let mut content = fs::read_to_string(&memory_file).unwrap_or_default();
            content.push_str(&line);
            content.push('\n');
            let _ = fs::write(&memory_file, content);
        }
    }

    pub fn load_recent_history(workspace: &Path, limit: usize) -> Vec<(String, String)> {
        let memory_file = workspace.join(".gha/memory.jsonl");
        let mut history = Vec::new();
        if memory_file.is_file()
            && let Ok(content) = fs::read_to_string(&memory_file)
        {
            for line in content.lines().rev().take(limit) {
                if let Ok(val) = serde_json::from_str::<serde_json::Value>(line) {
                    let intent = val.get("user_intent").and_then(|i| i.as_str()).unwrap_or_default().to_string();
                    let resp = val.get("assistant_response").and_then(|r| r.as_str()).unwrap_or_default().to_string();
                    if !intent.is_empty() {
                        history.push((intent, resp));
                    }
                }
            }
        }
        history.reverse();
        history
    }

    pub fn format_memory_summary(workspace: &Path) -> String {
        let history = Self::load_recent_history(workspace, 10);
        if history.is_empty() {
            return "No previous interaction history recorded for this workspace.".to_string();
        }
        let mut out = format!("Workspace Memory History ({} Previous Sessions):\n\n", history.len());
        for (i, (intent, resp)) in history.iter().enumerate() {
            let first_line = resp.lines().next().unwrap_or(resp);
            out.push_str(&format!("{}. User: \"{}\"\n   GHA: {}\n\n", i + 1, intent, first_line));
        }
        out
    }

    pub fn clear_memory(workspace: &Path) -> String {
        let memory_file = workspace.join(".gha/memory.jsonl");
        if memory_file.exists() {
            let _ = fs::remove_file(memory_file);
            "Workspace memory cleared.".to_string()
        } else {
            "No workspace memory file to clear.".to_string()
        }
    }
}

pub struct GhaAuditLogger;

impl GhaAuditLogger {
    pub fn log_event(workspace: &Path, event_type: &str, details: &str) {
        let gha_dir = workspace.join(".gha");
        let _ = fs::create_dir_all(&gha_dir);
        let audit_file = gha_dir.join("audit.log");

        let timestamp = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .map(|d| d.as_secs())
            .unwrap_or(0);

        let log_entry = format!("[{}] [{}] {}\n", timestamp, event_type, details);
        let mut content = fs::read_to_string(&audit_file).unwrap_or_default();
        content.push_str(&log_entry);
        let _ = fs::write(&audit_file, content);
    }

    pub fn read_audit_log(workspace: &Path, limit: usize) -> String {
        let audit_file = workspace.join(".gha/audit.log");
        if audit_file.is_file()
            && let Ok(content) = fs::read_to_string(&audit_file)
        {
            let lines: Vec<&str> = content.lines().collect();
            let take_count = limit.min(lines.len());
            let recent = &lines[lines.len().saturating_sub(take_count)..];
            return format!("Workspace Audit Trail ({} Recent Entries):\n\n{}", recent.len(), recent.join("\n"));
        }
        "No audit trail recorded for this workspace.".to_string()
    }
}

pub struct GhaBackupManager;

impl GhaBackupManager {
    pub fn backup_work(workspace: &Path) -> EaiResult<String> {
        let backups_dir = workspace.join(".gha/backups");
        fs::create_dir_all(&backups_dir).map_err(|e| EaiError::Sandbox(e.to_string()))?;

        let timestamp = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .map(|d| d.as_secs())
            .unwrap_or(0);

        let backup_dir = backups_dir.join(format!("work_backup_{}", timestamp));

        let exclude = ["backups", "target", ".git", "models"];
        match Self::copy_dir_all_filtered(workspace, &backup_dir, &exclude) {
            Ok(_) => Ok(format!("Workspace work backed up natively to {}", backup_dir.display())),
            Err(e) => Err(EaiError::Sandbox(format!("Failed to create workspace backup: {}", e))),
        }
    }

    fn copy_dir_all_filtered(src: &Path, dst: &Path, exclude_dirs: &[&str]) -> std::io::Result<()> {
        fs::create_dir_all(dst)?;
        if let Ok(entries) = fs::read_dir(src) {
            for entry in entries.flatten() {
                if let Ok(file_type) = entry.file_type() {
                    let name = entry.file_name().to_string_lossy().to_string();
                    if exclude_dirs.contains(&name.as_str()) || name.starts_with("work_backup_") {
                        continue;
                    }
                    if file_type.is_dir() {
                        let _ = Self::copy_dir_all_filtered(&entry.path(), &dst.join(&name), exclude_dirs);
                    } else if file_type.is_file() {
                        let _ = fs::copy(entry.path(), dst.join(&name));
                    }
                }
            }
        }
        Ok(())
    }

    pub fn restore_work(workspace: &Path, backup_path: &str) -> EaiResult<String> {
        let archive = if backup_path.trim().is_empty() {
            let backups_dir = workspace.join(".gha/backups");
            let mut latest = PathBuf::new();
            let mut max_time = 0;
            if let Ok(entries) = fs::read_dir(&backups_dir) {
                for entry in entries.flatten() {
                    if let Ok(m) = entry.metadata()
                        && let Ok(time) = m.modified()
                    {
                        let secs = time.duration_since(std::time::UNIX_EPOCH).map(|d| d.as_secs()).unwrap_or(0);
                        if secs > max_time {
                            max_time = secs;
                            latest = entry.path();
                        }
                    }
                }
            }
            if !latest.exists() {
                return Err(EaiError::Sandbox("No backup found in .gha/backups/".to_string()));
            }
            latest
        } else {
            PathBuf::from(backup_path.trim())
        };

        if !archive.exists() {
            return Err(EaiError::Sandbox(format!("Backup path not found: {}", archive.display())));
        }

        let exclude = ["backups", "target", ".git", "models"];
        match Self::copy_dir_all_filtered(&archive, workspace, &exclude) {
            Ok(_) => Ok(format!("Workspace work restored natively from {}", archive.display())),
            Err(e) => Err(EaiError::Sandbox(format!("Failed to restore workspace work: {}", e))),
        }
    }

    pub fn backup_engine(global_dir: &Path) -> EaiResult<String> {
        let backups_dir = global_dir.join("backups");
        fs::create_dir_all(&backups_dir).map_err(|e| EaiError::Sandbox(e.to_string()))?;

        let timestamp = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .map(|d| d.as_secs())
            .unwrap_or(0);

        let backup_dir = backups_dir.join(format!("gha_engine_backup_{}", timestamp));

        let exclude = ["backups", "models"];
        match Self::copy_dir_all_filtered(global_dir, &backup_dir, &exclude) {
            Ok(_) => Ok(format!("GHA engine backed up natively to {}", backup_dir.display())),
            Err(e) => Err(EaiError::Sandbox(format!("Failed to create engine backup: {}", e))),
        }
    }

    pub fn restore_engine(global_dir: &Path, backup_path: &str) -> EaiResult<String> {
        let archive = if backup_path.trim().is_empty() {
            let backups_dir = global_dir.join("backups");
            let mut latest = PathBuf::new();
            let mut max_time = 0;
            if let Ok(entries) = fs::read_dir(&backups_dir) {
                for entry in entries.flatten() {
                    if let Ok(m) = entry.metadata()
                        && let Ok(time) = m.modified()
                    {
                        let secs = time.duration_since(std::time::UNIX_EPOCH).map(|d| d.as_secs()).unwrap_or(0);
                        if secs > max_time {
                            max_time = secs;
                            latest = entry.path();
                        }
                    }
                }
            }
            if !latest.exists() {
                return Err(EaiError::Sandbox("No engine backup found in ~/.gha/backups/".to_string()));
            }
            latest
        } else {
            PathBuf::from(backup_path.trim())
        };

        if !archive.exists() {
            return Err(EaiError::Sandbox(format!("Backup path not found: {}", archive.display())));
        }

        let exclude = ["backups", "models"];
        match Self::copy_dir_all_filtered(&archive, global_dir, &exclude) {
            Ok(_) => Ok(format!("GHA engine restored natively from {}", archive.display())),
            _ => Err(EaiError::Sandbox(format!("Failed to restore engine from {}", archive.display()))),
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_checkpoint_lifecycle() {
        let temp_dir = std::env::temp_dir().join("gha_test_checkpoint");
        let _ = fs::create_dir_all(&temp_dir);

        let cp = NeuralCheckpoint {
            intent: "test mission".to_string(),
            timestamp: 12345,
            completed_tools: vec!["tool1".to_string()],
            blackboard: std::collections::HashMap::new(),
            status: "IN_PROGRESS".to_string(),
        };

        SandboxManager::save_mission_checkpoint(&temp_dir, &cp);
        let interrupted = SandboxManager::check_interrupted_checkpoint(&temp_dir);
        assert!(interrupted.is_some());
        assert_eq!(interrupted.unwrap().intent, "test mission".to_string());

        SandboxManager::clear_mission_checkpoint(&temp_dir);
        assert!(SandboxManager::check_interrupted_checkpoint(&temp_dir).is_none());

        let _ = fs::remove_dir_all(&temp_dir);
    }

    #[test]
    fn test_gha_memory_lifecycle() {
        let temp_dir = std::env::temp_dir().join("gha_test_memory");
        let _ = fs::create_dir_all(&temp_dir);

        GhaMemory::append_interaction(&temp_dir, "test intent", "test response");
        let history = GhaMemory::load_recent_history(&temp_dir, 5);
        assert_eq!(history.len(), 1);
        assert_eq!(history[0].0, "test intent");

        let summary = GhaMemory::format_memory_summary(&temp_dir);
        assert!(summary.contains("test intent"));

        let clear_res = GhaMemory::clear_memory(&temp_dir);
        assert!(clear_res.contains("cleared"));

        let _ = fs::remove_dir_all(&temp_dir);
    }

    #[test]
    fn test_gha_config_lifecycle() {
        let temp_dir = std::env::temp_dir().join("gha_test_config");
        let _ = fs::create_dir_all(&temp_dir);

        let cfg = GhaConfig::load(&temp_dir);
        assert_eq!(cfg.gmcp_port, 9090);
        assert_eq!(cfg.gemi_port, 9091);
        assert_eq!(cfg.udp_discovery_port, 9092);

        let mut custom_cfg = cfg;
        custom_cfg.gemi_port = 9099;
        assert!(custom_cfg.save(&temp_dir).is_ok());

        let loaded = GhaConfig::load(&temp_dir);
        assert_eq!(loaded.gemi_port, 9099);

        let _ = fs::remove_dir_all(&temp_dir);
    }
}
