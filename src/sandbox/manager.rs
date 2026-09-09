// 🏠 GHA Sandbox Manager
// 100% Rust implementation for Global Engine State Management

use std::fs;
use std::path::{Path, PathBuf};
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GhaConfig {
    pub gmcp_port: u16,
    pub gemi_port: u16,
    pub udp_discovery_port: u16,
    pub default_engine: String,
    pub default_model: String,
    pub auto_download_models: bool,
}

impl Default for GhaConfig {
    fn default() -> Self {
        GhaConfig {
            gmcp_port: 9090,
            gemi_port: 9091,
            udp_discovery_port: 9092,
            default_engine: "gha".to_string(),
            default_model: "gha-alpha".to_string(),
            auto_download_models: true,
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
    pub fn save(&self, global_dir: &Path) -> Result<String, String> {
        let path = Self::get_config_path(global_dir);
        let content = serde_json::to_string_pretty(self).map_err(|e| e.to_string())?;
        fs::write(&path, content).map_err(|e| e.to_string())?;
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
                            std::env::set_var(key, val);
                        }
                    }
                }
            }
        }
    }

    pub fn save_env_key(global_dir: &Path, key: &str, val: &str) -> Result<String, String> {
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
        fs::write(&env_file, lines.join("\n")).map_err(|e| e.to_string())?;
        unsafe {
            std::env::set_var(key, val);
        }
        Ok(format!("Saved {} to {}", key, env_file.display()))
    }

    #[allow(dead_code)]
    pub fn is_global_sandbox_active(global_dir: &Path) -> bool {
        global_dir.join("bin").is_dir()
    }

    pub fn save_mission_checkpoint(workspace: &Path, intent: &str, completed_tools: &[String], status: &str) {
        let gha_dir = workspace.join(".gha");
        let _ = fs::create_dir_all(&gha_dir);
        let checkpoint_file = gha_dir.join("mission_checkpoint.json");
        let timestamp = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).map(|d| d.as_secs()).unwrap_or(0);
        let checkpoint = serde_json::json!({
            "intent": intent,
            "timestamp": timestamp,
            "completed_tools": completed_tools,
            "status": status
        });
        let _ = fs::write(&checkpoint_file, checkpoint.to_string());
    }

    pub fn check_interrupted_checkpoint(workspace: &Path) -> Option<String> {
        let checkpoint_file = workspace.join(".gha/mission_checkpoint.json");
        if checkpoint_file.is_file()
            && let Ok(content) = fs::read_to_string(&checkpoint_file)
            && let Ok(val) = serde_json::from_str::<serde_json::Value>(&content)
            && val.get("status").and_then(|s| s.as_str()) == Some("IN_PROGRESS")
            && let Some(intent) = val.get("intent").and_then(|i| i.as_str())
        {
            return Some(intent.to_string());
        }
        None
    }

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
            "assistant_response": response.chars().take(500).collect::<String>()
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
    pub fn backup_work(workspace: &Path) -> Result<String, String> {
        let backups_dir = workspace.join(".gha/backups");
        fs::create_dir_all(&backups_dir).map_err(|e| e.to_string())?;

        let timestamp = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .map(|d| d.as_secs())
            .unwrap_or(0);

        let backup_file = backups_dir.join(format!("work_backup_{}.tar.gz", timestamp));

        let status = std::process::Command::new("tar")
            .args(["-czf", backup_file.to_str().unwrap_or("backup.tar.gz"), "--exclude=.gha/backups", "."])
            .current_dir(workspace)
            .status();

        match status {
            Ok(s) if s.success() => Ok(format!("Workspace work backed up successfully to {}", backup_file.display())),
            _ => Err("Failed to create workspace backup archive using tar.".to_string()),
        }
    }

    pub fn restore_work(workspace: &Path, backup_path: &str) -> Result<String, String> {
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
                return Err("No backup archive found in .gha/backups/".to_string());
            }
            latest
        } else {
            PathBuf::from(backup_path.trim())
        };

        if !archive.is_file() {
            return Err(format!("Backup archive file not found: {}", archive.display()));
        }

        let status = std::process::Command::new("tar")
            .args(["-xzf", archive.to_str().unwrap_or("")])
            .current_dir(workspace)
            .status();

        match status {
            Ok(s) if s.success() => Ok(format!("Workspace work restored successfully from {}", archive.display())),
            _ => Err(format!("Failed to restore workspace work from {}", archive.display())),
        }
    }

    pub fn backup_engine(global_dir: &Path) -> Result<String, String> {
        let backups_dir = global_dir.join("backups");
        fs::create_dir_all(&backups_dir).map_err(|e| e.to_string())?;

        let timestamp = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .map(|d| d.as_secs())
            .unwrap_or(0);

        let backup_file = backups_dir.join(format!("gha_engine_backup_{}.tar.gz", timestamp));

        let status = std::process::Command::new("tar")
            .args(["-czf", backup_file.to_str().unwrap_or("engine_backup.tar.gz"), "--exclude=backups", "--exclude=models", "."])
            .current_dir(global_dir)
            .status();

        match status {
            Ok(s) if s.success() => Ok(format!("GHA engine backed up successfully to {}", backup_file.display())),
            _ => Err("Failed to create engine backup archive.".to_string()),
        }
    }

    pub fn restore_engine(global_dir: &Path, backup_path: &str) -> Result<String, String> {
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
                return Err("No engine backup archive found in ~/.gha/backups/".to_string());
            }
            latest
        } else {
            PathBuf::from(backup_path.trim())
        };

        if !archive.is_file() {
            return Err(format!("Engine backup archive file not found: {}", archive.display()));
        }

        let status = std::process::Command::new("tar")
            .args(["-xzf", archive.to_str().unwrap_or("")])
            .current_dir(global_dir)
            .status();

        match status {
            Ok(s) if s.success() => Ok(format!("GHA engine restored successfully from {}", archive.display())),
            _ => Err(format!("Failed to restore engine from {}", archive.display())),
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

        SandboxManager::save_mission_checkpoint(&temp_dir, "test mission", &["tool1".to_string()], "IN_PROGRESS");
        let interrupted = SandboxManager::check_interrupted_checkpoint(&temp_dir);
        assert_eq!(interrupted, Some("test mission".to_string()));

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
