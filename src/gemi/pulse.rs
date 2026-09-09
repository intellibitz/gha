// GHA-Pulse: Tier 0 Native Bootstrap Brain
// Powered by Candle — EAI: Exponential Intelligence for Any AI.

use anyhow::{Result, anyhow};
use candle_core::Device;
use std::path::{Path, PathBuf};
use std::collections::HashMap;
use super::alpha::GhaAlphaModel;

pub struct GhaPulse;

impl GhaPulse {
    #[allow(dead_code)]
    pub fn try_load_candle_weights() -> Result<usize> {
        let home = std::env::var_os("HOME").or_else(|| std::env::var_os("USERPROFILE")).map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let weights_path = home.join(".gha/models/gha-alpha.safetensors");
        if weights_path.is_file() {
            let device = Device::Cpu;
            let tensors = candle_core::safetensors::load(&weights_path, &device)?;
            Ok(tensors.len())
        } else {
            Err(anyhow!("No native safetensors model weights found at ~/.gha/models/gha-alpha.safetensors"))
        }
    }

    pub fn reason(prompt: &str, workspace: &Path) -> Result<String> {
        let prompt_str = prompt.to_string();
        let clean_prompt = if let Some(pos) = prompt_str.find("INTENT: ") {
            prompt_str[pos + 8..].trim().to_string()
        } else {
            prompt_str.trim().to_string()
        };

        let lower = clean_prompt.to_lowercase();
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
        mappings.insert("who", "ACTION: identity");
        mappings.insert("identity", "ACTION: identity");
        mappings.insert("lowercase", "ACTION: exec_command tr '[:upper:]' '[:lower:]'");
        mappings.insert("uppercase", "ACTION: exec_command tr '[:lower:]' '[:upper:]'");

        for word in &words {
            if let Some(action) = mappings.get(word) {
                if *word == "ls" || *word == "dir" {
                     return Ok(format!("ACTION: list_directory {}", workspace.display()));
                }

                // 🚀 If we have input data from a pipe, use it with the command
                if (word == &"lowercase" || word == &"uppercase") && clean_prompt.contains("[INPUT DATA]:") {
                     if let Some(data) = clean_prompt.split("[INPUT DATA]:\n").nth(1) {
                         let cmd = if word == &"lowercase" { "tr '[:upper:]' '[:lower:]'" } else { "tr '[:lower:]' '[:upper:]'" };
                         return Ok(format!("ACTION: exec_command echo \"{}\" | {}", data.replace("\"", "\\\""), cmd));
                     }
                }

                return Ok(action.to_string());
            }
        }

        // 2. High-Fidelity Assistant Intent Parsers
        if lower.contains("directory") || lower.contains("folder") {
             if let Some(pos) = lower.find("named ") {
                 let after_named = &clean_prompt[pos + 6..].trim();
                 let path = after_named.split_whitespace().next().unwrap_or("").trim_end_matches('.');
                 if !path.is_empty() {
                     return Ok(format!("ACTION: exec_command mkdir -p {}", path));
                 }
             }
        }

        if (lower.contains("file") || lower.contains("save") || lower.contains("write")) && (lower.contains("containing") || lower.contains("with content")) {
             let sep = if lower.contains("containing") { "containing" } else { "with content" };
             if let Some(sep_pos) = lower.find(sep) {
                 let content = clean_prompt[sep_pos + sep.len()..].trim();
                 let before_sep = &lower[..sep_pos];
                 if let Some(named_pos) = before_sep.find("named ") {
                     let path = clean_prompt[named_pos + 6..sep_pos].trim().trim_end_matches('.');
                     if !path.is_empty() {
                         return Ok(format!("ACTION: write_file {} {}", path, content));
                     }
                 }
             }
        }

        // 3. Neural Reflex (GHA-Alpha Inference) - Fallback
        let home = std::env::var_os("HOME").or_else(|| std::env::var_os("USERPROFILE")).map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let global_dir = home.join(".gha");
        if let Ok(model) = GhaAlphaModel::load(&global_dir) {
             if let Ok(neural_action) = model.predict_intent(&clean_prompt) {
                 return Ok(neural_action);
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
            return Ok(format!("ACTION: list_directory {}", workspace.display()));
        }

        if lower.contains("translate") {
             return Ok(format!("ACTION: reason {}", clean_prompt));
        }

        if lower.contains("reason") || lower.contains("explain") || lower.contains("summarize") || lower.contains("orchestrate") {
             return Ok(format!("ACTION: reason {}", clean_prompt));
        }

        Err(anyhow!("Pulse Brain: Transitioning to Tier 2 Deep Reasoning..."))
    }
}
