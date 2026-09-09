// ⚡ GHA-Pulse: Tier 0 Native Bootstrap Brain
// Powered by Candle — EAI: Exponential Intelligence for Any AI.

use anyhow::{Result, anyhow};
use candle_core::Device;
use std::path::{Path, PathBuf};
use std::collections::HashMap;

pub struct GhaPulse;

impl GhaPulse {
    #[allow(dead_code)]
    pub fn try_load_candle_weights() -> Result<usize> {
        let home = std::env::var_os("HOME").map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let weights_path = home.join(".gha/models/gha-alpha.safetensors");
        if weights_path.is_file() {
            let device = Device::Cpu;
            let tensors = candle_core::safetensors::load(&weights_path, &device)?;
            Ok(tensors.len())
        } else {
            Err(anyhow!("No native safetensors model weights found at ~/.gha/models/gha-alpha.safetensors"))
        }
    }

    pub fn reason(prompt: &str, _workspace: &Path) -> Result<String> {
        let lower = prompt.to_lowercase();
        let words: Vec<&str> = lower.split_whitespace().collect();

        // 1. Precise Keyword Mapping (High-Speed Reflex)
        let mut mappings = HashMap::new();
        mappings.insert("status", "ACTION: status");
        mappings.insert("version", "ACTION: version");
        mappings.insert("memory", "ACTION: memory");
        mappings.insert("history", "ACTION: memory");
        mappings.insert("forget", "ACTION: clear_memory");
        mappings.insert("scout", "ACTION: scout");
        mappings.insert("build", "ACTION: self_heal_build");
        mappings.insert("test", "ACTION: run_test_harness");
        mappings.insert("models", "ACTION: list_models");
        mappings.insert("ls", "ACTION: list_directory");
        mappings.insert("dir", "ACTION: list_directory");

        for word in &words {
            if let Some(action) = mappings.get(word) {
                return Ok(action.to_string());
            }
        }

        // 2. Pattern Matchers (Structured Reflex)
        if lower.contains("create") || lower.contains("write") {
             let parts: Vec<&str> = lower.split("containing").collect();
             if parts.len() >= 2 {
                 let file_name = parts[0]
                    .replace("create", "")
                    .replace("write", "")
                    .replace("file", "")
                    .replace("named", "")
                    .replace(" a ", " ")
                    .trim()
                    .to_string();
                 let content = parts[1].trim();
                 return Ok(format!("ACTION: write_file {} {}", file_name, content));
             }
        }

        if lower.contains("chat gpt") || lower.contains("openai") {
            return Ok("ACTION: connect_provider openai".to_string());
        }

        if lower.contains("gemini") && (lower.contains("connect") || lower.contains("use")) {
            return Ok("ACTION: connect_provider gemini".to_string());
        }

        if lower.contains("claude") || lower.contains("anthropic") {
            return Ok("ACTION: connect_provider anthropic".to_string());
        }

        if lower.contains("backup") || lower.contains("restore") {
            if lower.contains("engine") || lower.contains("gha") {
                return Ok(if lower.contains("restore") { "ACTION: restore_engine" } else { "ACTION: backup_engine" }.to_string());
            }
            return Ok(if lower.contains("restore") { "ACTION: restore_work" } else { "ACTION: backup_work" }.to_string());
        }

        if lower.contains("install mcp") || lower.contains("need capability") {
             let name = words.last().unwrap_or(&"search");
             return Ok(format!("ACTION: provision_mcp {}", name));
        }

        if lower.contains("list files") || lower.contains("show files") || lower.contains("workspace") {
            return Ok("ACTION: list_directory".to_string());
        }

        if lower.contains("translate") {
             return Ok(format!("ACTION: reason {}", prompt));
        }

        if lower.contains("reason") || lower.contains("explain") || lower.contains("summarize") || lower.contains("orchestrate") {
             return Ok(format!("ACTION: reason {}", prompt));
        }

        // 3. Fallback to Deep Reasoning
        Err(anyhow!("Pulse Brain: Transitioning to Tier 2 Deep Reasoning..."))
    }
}
