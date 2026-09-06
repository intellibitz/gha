// 📦 Model Manager: GGUF & Dynamic Zero-Download Cloud Models Catalog
// 100% Rust implementation for local .gguf & cloud model discovery

use std::fs;
use std::path::{Path, PathBuf};
use serde::{Deserialize, Serialize};

use super::engine::CloudProvidersConfig;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ModelInfo {
    pub name: String,
    pub registry: String,
    pub model_id: String,
    pub description: String,
    pub is_local: bool,
}

pub struct ModelManager;

impl ModelManager {
    pub fn list_models(workspace: &Path) -> Vec<ModelInfo> {
        let mut list = Vec::new();

        // 1. Scan Zero-Download Cloud API Keys
        if std::env::var("GEMINI_API_KEY").is_ok() || std::env::var("GEMINI_AI_STUDIO_KEY").is_ok() {
            list.push(ModelInfo {
                name: "Google Gemini 1.5 Flash (1M Context)".to_string(),
                registry: "Google Gemini AI Studio (Zero Download)".to_string(),
                model_id: "google/gemini-1.5-flash".to_string(),
                description: "1M–2M token context window cloud model".to_string(),
                is_local: false,
            });
        }

        if std::env::var("MISTRAL_API_KEY").is_ok() {
            list.push(ModelInfo {
                name: "Mistral Large / Pixtral Cloud".to_string(),
                registry: "Mistral AI Cloud (Zero Download)".to_string(),
                model_id: "mistralai/mistral-large-latest".to_string(),
                description: "Mistral AI flagship cloud reasoning & coding model".to_string(),
                is_local: false,
            });
        }

        if std::env::var("HF_TOKEN").is_ok() || std::env::var("HUGGINGFACE_TOKEN").is_ok() {
            list.push(ModelInfo {
                name: "Hugging Face Serverless Hub (100,000+ Models)".to_string(),
                registry: "Hugging Face Hub (Zero Download)".to_string(),
                model_id: "huggingface/serverless-hub".to_string(),
                description: "Serverless inference across 100,000+ open-weights models".to_string(),
                is_local: false,
            });
        }

        if std::env::var("DEEPSEEK_API_KEY").is_ok() {
            list.push(ModelInfo {
                name: "DeepSeek R1 / V3 Cloud API".to_string(),
                registry: "DeepSeek Cloud (Zero Download)".to_string(),
                model_id: "deepseek-ai/DeepSeek-R1-Cloud".to_string(),
                description: "Deep reasoning & coding model over Cloud REST API".to_string(),
                is_local: false,
            });
        }

        if std::env::var("GROQ_API_KEY").is_ok() {
            list.push(ModelInfo {
                name: "Groq Llama 3.3 70B (500 tok/s)".to_string(),
                registry: "Groq Cloud (Zero Download)".to_string(),
                model_id: "groq/llama-3.3-70b-versatile".to_string(),
                description: "Blazing fast 500+ tokens/sec cloud inference engine".to_string(),
                is_local: false,
            });
        }

        if std::env::var("OPENAI_API_KEY").is_ok() {
            list.push(ModelInfo {
                name: "OpenAI GPT-4o-mini Cloud".to_string(),
                registry: "OpenAI Cloud (Zero Download)".to_string(),
                model_id: "openai/gpt-4o-mini".to_string(),
                description: "Universal OpenAI multimodal cloud API".to_string(),
                is_local: false,
            });
        }

        // 2. Scan Custom Dynamic Cloud Providers Config (~/.gha/cloud_providers.json)
        if let Some(home) = std::env::var_os("HOME").map(PathBuf::from) {
            let cfg_file = home.join(".gha/cloud_providers.json");
            if cfg_file.is_file() {
                if let Ok(content) = fs::read_to_string(&cfg_file) {
                    if let Ok(cfg) = serde_json::from_str::<CloudProvidersConfig>(&content) {
                        for p in cfg.providers {
                            list.push(ModelInfo {
                                name: p.name.clone(),
                                registry: "Custom Dynamic Cloud".to_string(),
                                model_id: p.model_id,
                                description: format!("Dynamic endpoint: {}", p.api_url),
                                is_local: false,
                            });
                        }
                    }
                }
            }
        }

        // 3. Scan workspace local models
        let workspace_models_dir = workspace.join(".gha/models");
        if let Ok(entries) = fs::read_dir(&workspace_models_dir) {
            for entry in entries.flatten() {
                if let Ok(name) = entry.file_name().into_string() {
                    if name.ends_with(".gguf") || name.ends_with(".bin") {
                        list.push(ModelInfo {
                            name: name.clone(),
                            registry: "Local GGUF Vault".to_string(),
                            model_id: name,
                            description: "Local Quantized GGUF Model".to_string(),
                            is_local: true,
                        });
                    }
                }
            }
        }

        // 4. Scan global ~/.gha/models
        if let Some(home) = std::env::var_os("HOME").map(PathBuf::from) {
            let global_models_dir = home.join(".gha/models");
            if let Ok(entries) = fs::read_dir(&global_models_dir) {
                for entry in entries.flatten() {
                    if let Ok(name) = entry.file_name().into_string() {
                        if (name.ends_with(".gguf") || name.ends_with(".bin")) && !list.iter().any(|m| m.model_id == name) {
                            list.push(ModelInfo {
                                name: name.clone(),
                                registry: "Global GGUF Vault".to_string(),
                                model_id: name,
                                description: "Global Quantized GGUF Model".to_string(),
                                is_local: true,
                            });
                        }
                    }
                }
            }
        }

        // If no local or key-authenticated models found, list public remote specifications
        if list.is_empty() {
            list.push(ModelInfo {
                name: "Google Gemini 1.5 Flash".to_string(),
                registry: "Google AI Studio (Remote)".to_string(),
                model_id: "google/gemini-1.5-flash".to_string(),
                description: "Set GEMINI_API_KEY to enable 1M context Zero-Download Cloud inference".to_string(),
                is_local: false,
            });
            list.push(ModelInfo {
                name: "Mistral Large Latest".to_string(),
                registry: "Mistral AI (Remote)".to_string(),
                model_id: "mistralai/mistral-large-latest".to_string(),
                description: "Set MISTRAL_API_KEY to enable Mistral AI Zero-Download Cloud inference".to_string(),
                is_local: false,
            });
            list.push(ModelInfo {
                name: "DeepSeek R1 Distill Qwen 1.5B GGUF".to_string(),
                registry: "Hugging Face (Remote)".to_string(),
                model_id: "deepseek-ai/DeepSeek-R1-Distill-Qwen-1.5B-GGUF".to_string(),
                description: "Deep reasoning model (Set DEEPSEEK_API_KEY or download to ~/.gha/models)".to_string(),
                is_local: false,
            });
            list.push(ModelInfo {
                name: "Llama 3.3 70B Instruct".to_string(),
                registry: "Meta AI (Remote)".to_string(),
                model_id: "meta-llama/Llama-3.3-70B-Instruct".to_string(),
                description: "Large language instruction model (Set GROQ_API_KEY or OPENAI_API_KEY)".to_string(),
                is_local: false,
            });
        }

        list
    }
}
