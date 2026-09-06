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

        // 2. Scan Local GGUF Vault
        let model_paths = vec![workspace.join(".gha/models"), PathBuf::from(std::env::var("HOME").unwrap_or_default()).join(".gha/models")];
        for dir in model_paths {
            if let Ok(entries) = fs::read_dir(dir) {
                for entry in entries.flatten() {
                    let p = entry.path();
                    if p.extension().map_or(false, |ext| ext == "gguf") {
                        if let Ok(name) = entry.file_name().into_string() {
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
        }

        // 3. 🚀 Autonomous Scouting
        if let Ok(o) = Command::new("ollama").arg("list").output() {
            if o.status.success() {
                let stdout = String::from_utf8_lossy(&o.stdout);
                for line in stdout.lines().skip(1) {
                    let parts: Vec<&str> = line.split_whitespace().collect();
                    if let Some(m) = parts.get(0) {
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
                url: "https://github.com/vllm-project/vllm".to_string(),
            },
        ]
    }
}
