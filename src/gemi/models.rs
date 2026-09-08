// 📦 Model Manager: GGUF, Cloud & Autonomous Model Discovery
// 100% Rust implementation for world-scale model orchestration

use std::fs;
use std::path::{Path, PathBuf};
use std::process::Command;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, PartialOrd, Eq, Ord)]
pub enum ModelTier {
    Premier = 0,
    Specialist = 1,
    Standard = 2,
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
}

pub struct ModelManager;

impl ModelManager {
    pub fn list_models(workspace: &Path) -> Vec<ModelInfo> {
        let mut list = Vec::new();

        // 1. Scan authenticated Cloud Providers
        if std::env::var("GEMINI_API_KEY").is_ok() {
            list.push(ModelInfo {
                name: "Google Gemini 1.5 Flash".to_string(),
                registry: "Google AI Studio".to_string(),
                model_id: "google/gemini-1.5-flash".to_string(),
                description: "1M+ token context cloud reasoning".to_string(),
                is_local: false,
                tier: ModelTier::Premier,
                latency_ms: None,
            });
        }
        if std::env::var("OPENAI_API_KEY").is_ok() {
            list.push(ModelInfo {
                name: "OpenAI GPT-4o".to_string(),
                registry: "OpenAI Cloud".to_string(),
                model_id: "openai/gpt-4o".to_string(),
                description: "Industry-standard reasoning & tool-use".to_string(),
                is_local: false,
                tier: ModelTier::Premier,
                latency_ms: None,
            });
        }
        if std::env::var("ANTHROPIC_API_KEY").is_ok() {
            list.push(ModelInfo {
                name: "Anthropic Claude 3.5 Sonnet".to_string(),
                registry: "Anthropic Cloud".to_string(),
                model_id: "anthropic/claude-3.5-sonnet".to_string(),
                description: "High-precision reasoning specialist".to_string(),
                is_local: false,
                tier: ModelTier::Premier,
                latency_ms: None,
            });
        }
        if std::env::var("DEEPSEEK_API_KEY").is_ok() {
            list.push(ModelInfo {
                name: "DeepSeek Chat".to_string(),
                registry: "DeepSeek Cloud".to_string(),
                model_id: "deepseek/deepseek-chat".to_string(),
                description: "High-throughput code & logic reasoning".to_string(),
                is_local: false,
                tier: ModelTier::Specialist,
                latency_ms: None,
            });
        }
        if std::env::var("MISTRAL_API_KEY").is_ok() {
            list.push(ModelInfo {
                name: "Mistral Small".to_string(),
                registry: "Mistral AI".to_string(),
                model_id: "mistral/mistral-small-latest".to_string(),
                description: "Efficient European reasoning specialist".to_string(),
                is_local: false,
                tier: ModelTier::Premier,
                latency_ms: None,
            });
        }
        if std::env::var("GROQ_API_KEY").is_ok() {
            list.push(ModelInfo {
                name: "Groq Llama 3.3 70B".to_string(),
                registry: "Groq Cloud".to_string(),
                model_id: "groq/llama-3.3-70b-versatile".to_string(),
                description: "Ultra-low latency inference".to_string(),
                is_local: false,
                tier: ModelTier::Premier,
                latency_ms: None,
            });
        }

        // 2. Scan Local GGUF Vault
        let model_paths = vec![workspace.join(".gha/models"), PathBuf::from(std::env::var("HOME").unwrap_or_default()).join(".gha/models")];
        for dir in model_paths {
            if let Ok(entries) = fs::read_dir(dir) {
                for entry in entries.flatten() {
                    let p = entry.path();
                    if p.extension().is_some_and(|ext| ext == "gguf")
                        && let Ok(name) = entry.file_name().into_string()
                    {
                        list.push(ModelInfo {
                            name: name.clone(),
                            registry: "Local GGUF Vault".to_string(),
                            model_id: name,
                            description: "Native hardware-accelerated model".to_string(),
                            is_local: true,
                            tier: ModelTier::Standard,
                            latency_ms: None,
                        });
                    }
                }
            }
        }

        // 3. 🚀 Autonomous Scouting
        if let Ok(o) = Command::new("ollama").arg("list").output()
            && o.status.success()
        {
            let stdout = String::from_utf8_lossy(&o.stdout);
            for line in stdout.lines().skip(1) {
                let parts: Vec<&str> = line.split_whitespace().collect();
                if let Some(m) = parts.first() {
                    list.push(ModelInfo {
                        name: format!("Ollama: {}", m),
                        registry: "Local Ollama Engine".to_string(),
                        model_id: m.to_string(),
                        description: "High-throughput local inference".to_string(),
                        is_local: true,
                        tier: ModelTier::Specialist,
                        latency_ms: None,
                    });
                }
            }
        }

        if list.is_empty() {
             list.push(ModelInfo {
                name: "Native Rust Logic".to_string(),
                registry: "gha Native".to_string(),
                model_id: "gha-native-synthesis".to_string(),
                description: "Deterministic protocol-level reasoning".to_string(),
                is_local: true,
                tier: ModelTier::Standard,
                latency_ms: Some(0),
            });
        }

        list
    }

    pub fn set_selected_model(model_name: &str) -> Result<String, String> {
        let home = std::env::var_os("HOME").map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let gha_dir = home.join(".gha");
        let _ = fs::create_dir_all(&gha_dir);
        let model_file = gha_dir.join("selected_model.txt");
        fs::write(&model_file, model_name.trim()).map_err(|e| e.to_string())?;
        Ok(format!("Selected active model set to: '{}'", model_name.trim()))
    }

    pub fn get_selected_model() -> Option<String> {
        let home = std::env::var_os("HOME").map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let model_file = home.join(".gha/selected_model.txt");
        if model_file.is_file()
            && let Ok(content) = fs::read_to_string(&model_file)
        {
            let trimmed = content.trim();
            if !trimmed.is_empty() {
                return Some(trimmed.to_string());
            }
        }
        None
    }

    pub fn set_selected_engine(engine_name: &str) -> Result<String, String> {
        let home = std::env::var_os("HOME").map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let gha_dir = home.join(".gha");
        let _ = fs::create_dir_all(&gha_dir);
        let engine_file = gha_dir.join("selected_engine.txt");
        fs::write(&engine_file, engine_name.trim()).map_err(|e| e.to_string())?;
        Ok(format!("Active execution engine set to: '{}'", engine_name.trim()))
    }

    pub fn get_selected_engine() -> Option<String> {
        let home = std::env::var_os("HOME").map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let engine_file = home.join(".gha/selected_engine.txt");
        if engine_file.is_file()
            && let Ok(content) = fs::read_to_string(&engine_file)
        {
            let trimmed = content.trim();
            if !trimmed.is_empty() {
                return Some(trimmed.to_string());
            }
        }
        None
    }

    pub fn get_active_engine_and_model() -> (String, String) {
        let model = Self::get_selected_model().unwrap_or_else(|| "Auto-Scout".to_string());
        let engine_override = Self::get_selected_engine();

        let engine = if let Some(e) = engine_override {
            e
        } else {
            let lower = model.to_lowercase();
            if lower.contains("ollama") {
                "Ollama".to_string()
            } else if lower.contains("candle") || lower.contains("safetensors") || lower.contains("gha-alpha") {
                "Candle".to_string()
            } else if lower.contains("gemini") || lower.contains("google") {
                "Google AI".to_string()
            } else if lower.contains("openai") || lower.contains("gpt") {
                "OpenAI".to_string()
            } else if lower.contains("groq") {
                "Groq".to_string()
            } else if lower.contains("anthropic") || lower.contains("claude") {
                "Anthropic".to_string()
            } else if lower.contains("deepseek") {
                "DeepSeek".to_string()
            } else {
                "GEMI".to_string()
            }
        };

        (engine, model)
    }

    pub fn scout_and_benchmark(workspace: &Path) -> Vec<ModelInfo> {
        let mut models = Self::list_models(workspace);
        for m in &mut models {
            if m.is_local && m.registry.contains("Ollama") {
                let start = std::time::Instant::now();
                let _ = Command::new("ollama").args(["run", &m.model_id, "hi"]).output();
                m.latency_ms = Some(start.elapsed().as_millis());
            }
        }
        models.sort_by_key(|m| m.latency_ms.unwrap_or(9999));
        models
    }

    pub fn install_model(query_or_url: &str) -> String {
        let home = std::env::var_os("HOME").map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let models_dir = home.join(".gha/models");
        let _ = fs::create_dir_all(&models_dir);

        let target = query_or_url.trim();

        if target.starts_with("http://") || target.starts_with("https://") {
            let file_name = target.split('/').next_back().unwrap_or("model.gguf");
            let dest_path = models_dir.join(file_name);
            let status = Command::new("curl")
                .args(["-L", "-C", "-", "--retry", "3", "--retry-connrefused", "-o", dest_path.to_str().unwrap_or("model.gguf"), target])
                .status();

            match status {
                Ok(s) if s.success() => format!("Resumed/Downloaded native model weight to {}", dest_path.display()),
                _ => format!("Failed to download model from {}", target),
            }
        } else if Command::new("ollama").arg("pull").arg(target).status().is_ok_and(|s| s.success()) {
            format!("Pulled model '{}' into local Ollama engine.", target)
        } else {
            let hf_url = if target.contains('/') {
                format!("https://huggingface.co/{}/resolve/main/model.gguf", target)
            } else {
                format!("https://huggingface.co/TheBloke/{}-GGUF/resolve/main/{}.Q4_K_M.gguf", target, target)
            };

            let file_name = format!("{}.gguf", target.replace('/', "_"));
            let dest_path = models_dir.join(&file_name);

            let status = Command::new("curl")
                .args(["-L", "-C", "-", "--retry", "3", "--retry-connrefused", "-o", dest_path.to_str().unwrap_or("model.gguf"), &hf_url])
                .status();

            match status {
                Ok(s) if s.success() => format!("Resumed/Downloaded GGUF weights for '{}' to {}", target, dest_path.display()),
                _ => "Model download failed. Usage: 'gha install_model <model_name_or_url>'".to_string(),
            }
        }
    }

    pub fn scout_tier2_assets() -> Vec<crate::gawd::agents::DiscoverableAsset> {
        vec![
            crate::gawd::agents::DiscoverableAsset {
                tier: "Tier 2: GEMI (Intelligence)".to_string(),
                name: "Llama-3.1-8B-GGUF".to_string(),
                provider: "HuggingFace".to_string(),
                url: "https://huggingface.co/meta-llama/Llama-3.1-8B-GGUF".to_string(),
            },
            crate::gawd::agents::DiscoverableAsset {
                tier: "Tier 2: GEMI (Intelligence)".to_string(),
                name: "vLLM-Server-Binary".to_string(),
                provider: "vLLM Project".to_string(),
                url: "https://vllm.ai".to_string(),
            },
        ]
    }
}
