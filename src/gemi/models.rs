// 📦 Model Manager: GGUF & Zero-Download Cloud Models Resolver Catalog
// 100% Rust implementation for local .gguf & cloud model discovery

use std::fs;
use std::path::Path;
use serde::{Deserialize, Serialize};

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

        // 2. Scan workspace local models
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

        // 3. Scan global ~/.gha/models
        if let Some(home) = std::env::var_os("HOME").map(std::path::PathBuf::from) {
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
                name: "DeepSeek R1 Distill Qwen 1.5B GGUF".to_string(),
                registry: "Hugging Face (Remote)".to_string(),
                model_id: "deepseek-ai/DeepSeek-R1-Distill-Qwen-1.5B-GGUF".to_string(),
                description: "Deep reasoning model (Download to ~/.gha/models or set DEEPSEEK_API_KEY)".to_string(),
                is_local: false,
            });
            list.push(ModelInfo {
                name: "Llama 3.3 70B Instruct".to_string(),
                registry: "Meta AI (Remote)".to_string(),
                model_id: "meta-llama/Llama-3.3-70B-Instruct".to_string(),
                description: "Large language instruction model (Set GROQ_API_KEY or OPENAI_API_KEY for Cloud)".to_string(),
                is_local: false,
            });
        }

        list
    }
}
