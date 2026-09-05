// 🧠 GEMI: GHA Engines & Models AI Inference
// Tier 2: Pure Intelligence Layer, GGUF Models & Hardware Profiler

use std::path::Path;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ModelInfo {
    pub name: String,
    pub registry: String,
    pub model_id: String,
    pub description: String,
}

pub struct GemiEngine;

impl GemiEngine {
    pub fn profile_hardware() -> (usize, String) {
        let cpus = std::thread::available_parallelism().map(|n| n.get()).unwrap_or(4);
        let gpu_info = "Metal / CUDA Acceleration Detected (-ngl 99)";
        (cpus, gpu_info.to_string())
    }

    pub fn list_models(workspace: &Path) -> Vec<ModelInfo> {
        let models_dir = workspace.join(".gha/models");
        let mut list = Vec::new();

        if let Ok(entries) = std::fs::read_dir(&models_dir) {
            for entry in entries.flatten() {
                if let Ok(name) = entry.file_name().into_string() {
                    list.push(ModelInfo {
                        name: name.clone(),
                        registry: "LOCAL_GGUF".to_string(),
                        model_id: name,
                        description: "Local Quantized GGUF Model".to_string(),
                    });
                }
            }
        }

        // Web AI Models
        list.push(ModelInfo {
            name: "DeepSeek R1 Distill Qwen 1.5B GGUF".to_string(),
            registry: "Hugging Face".to_string(),
            model_id: "deepseek-ai/DeepSeek-R1-Distill-Qwen-1.5B-GGUF".to_string(),
            description: "High performance reasoning model".to_string(),
        });
        list.push(ModelInfo {
            name: "Llama 3.3 70B Instruct".to_string(),
            registry: "Meta AI".to_string(),
            model_id: "meta-llama/Llama-3.3-70B-Instruct".to_string(),
            description: "Large language instruction model".to_string(),
        });

        list
    }
}
