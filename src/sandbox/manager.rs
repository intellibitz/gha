// 🏠 GHA Sandbox Manager
// 100% Rust implementation for Global Engine State Management

use std::fs;
use std::path::{Path, PathBuf};

pub struct SandboxManager;

impl SandboxManager {
    pub fn ensure_global_sandbox(global_dir: &Path) -> PathBuf {
        if !global_dir.exists() {
            let _ = fs::create_dir_all(global_dir);
            let _ = fs::create_dir_all(global_dir.join("bin"));
            let _ = fs::create_dir_all(global_dir.join("models"));
            let _ = fs::create_dir_all(global_dir.join("train"));
        }
        Self::load_env_file(global_dir);
        global_dir.to_path_buf()
    }

    pub fn load_env_file(global_dir: &Path) {
        let env_file = global_dir.join("env");
        if env_file.is_file() {
            if let Ok(content) = fs::read_to_string(&env_file) {
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
    }

    pub fn save_env_key(global_dir: &Path, key: &str, val: &str) -> Result<String, String> {
        let env_file = global_dir.join("env");
        let mut lines = Vec::new();
        if env_file.is_file() {
            if let Ok(content) = fs::read_to_string(&env_file) {
                for l in content.lines() {
                    if !l.trim().starts_with(&format!("{}=", key)) {
                        lines.push(l.to_string());
                    }
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
        if checkpoint_file.is_file() {
            if let Ok(content) = fs::read_to_string(&checkpoint_file) {
                if let Ok(val) = serde_json::from_str::<serde_json::Value>(&content) {
                    if val.get("status").and_then(|s| s.as_str()) == Some("IN_PROGRESS") {
                        if let Some(intent) = val.get("intent").and_then(|i| i.as_str()) {
                            return Some(intent.to_string());
                        }
                    }
                }
            }
        }
        None
    }

    pub fn clear_mission_checkpoint(workspace: &Path) {
        let checkpoint_file = workspace.join(".gha/mission_checkpoint.json");
        if checkpoint_file.exists() {
            let _ = fs::remove_file(checkpoint_file);
        }
    }
}
