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
        let mut clean_prompt = if let Some(pos) = prompt_str.find("INTENT: ") {
            prompt_str[pos + 8..].trim().to_string()
        } else {
            prompt_str.trim().to_string()
        };

        if clean_prompt.starts_with(':') {
             clean_prompt = clean_prompt[1..].to_string();
        }

        let lower = clean_prompt.to_lowercase();
        let words: Vec<&str> = lower.split_whitespace().collect();

        // 🚀 Strip Colons for Keyword Mapping Consistency
        let words_clean: Vec<String> = words.iter().map(|w| w.trim_start_matches(':').to_string()).collect();

        // 1. High-Fidelity Assistant Intent Parsers (Priority)
        // Note: Generic Domain Reflexes removed in v0.1.2022590.
        // Evolution engine will now distill specific tools for these intents.

        // Model Downloading: "pull model [NAME]" or "install model [NAME]"
        if lower.contains("model") && (lower.contains("pull") || lower.contains("install")) {
            let model_name = if let Some(pos) = lower.find("model ") {
                clean_prompt[pos + 6..].trim().trim_end_matches('.').to_string()
            } else {
                words.last().unwrap_or(&"").to_string()
            };
            if !model_name.is_empty() && !model_name.contains("list") {
                return Ok(format!("ACTION: pull_model {}", model_name));
            }
        }

        // Fetch / Download / Web Search / Get / Find / URL Retrieval
        if lower.contains("get ") || lower.contains("fetch") || lower.contains("download") || lower.contains("search") || lower.contains("find ") || lower.contains("http://") || lower.contains("https://") {
            let query = if lower.contains("http://") || lower.contains("https://") {
                words.iter().find(|w| w.starts_with("http")).map(|s| s.to_string()).unwrap_or_default()
            } else if let Some(pos) = lower.find("download ") {
                clean_prompt[pos + 9..].trim().to_string()
            } else if let Some(pos) = lower.find("fetch ") {
                clean_prompt[pos + 6..].trim().to_string()
            } else if let Some(pos) = lower.find("get ") {
                clean_prompt[pos + 4..].trim().to_string()
            } else if let Some(pos) = lower.find("search ") {
                clean_prompt[pos + 7..].trim().to_string()
            } else if let Some(pos) = lower.find("find ") {
                clean_prompt[pos + 5..].trim().to_string()
            } else {
                clean_prompt.clone()
            };
            if !query.is_empty() && !query.contains("list") {
                return Ok(format!("ACTION: web_search_download {}", query));
            }
        }

        // 2. Precise Keyword Mapping (High-Speed Reflex)
        let mut mappings = HashMap::new();
        mappings.insert("status", "ACTION: status");
        mappings.insert("hardware", "ACTION: status");
        mappings.insert("capabilities", "ACTION: status");
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
        mappings.insert("identity", "ACTION: identity");
        mappings.insert("tools", "ACTION: tool_inventory");
        mappings.insert("inventory", "ACTION: tool_inventory");
        mappings.insert("tool_inventory", "ACTION: tool_inventory");
        mappings.insert("lowercase", "ACTION: exec_command tr '[:upper:]' '[:lower:]'");
        mappings.insert("uppercase", "ACTION: exec_command tr '[:lower:]' '[:upper:]'");

        for word in &words_clean {
            if let Some(action) = mappings.get(word.as_str()) {
                if word == "ls" || word == "dir" {
                     return Ok(format!("ACTION: list_directory {}", workspace.display()));
                }

                // If we have input data from a pipe, use it with the command
                if (word == &"lowercase" || word == &"uppercase") && clean_prompt.contains("[INPUT DATA]:") {
                     if let Some(data) = clean_prompt.split("[INPUT DATA]:\n").nth(1) {
                         let cmd = if word == &"lowercase" { "tr '[:upper:]' '[:lower:]'" } else { "tr '[:lower:]' '[:upper:]'" };
                         return Ok(format!("ACTION: exec_command echo \"{}\" | {}", data.replace("\"", "\\\""), cmd));
                     }
                }

                return Ok(action.to_string());
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

        if lower.contains("connect_provider") || lower.contains("connect provider") {
            let provider = lower.split_whitespace().last().unwrap_or("cloud");
            return Ok(format!("ACTION: connect_provider {}", provider));
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

        Err(anyhow!("Pulse Brain: Transitioning to Tier 2 Deep Reasoning..."))
    }
}
