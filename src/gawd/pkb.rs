// 🌌 GHA Protocol Knowledge Base (PKB)
// Tier 0: Reflex Data Synthesis for GHA-Alpha Training

use std::fs;
use std::path::Path;
use serde::{Deserialize, Serialize};
use super::gmas::A2AMessage;

#[derive(Debug, Serialize, Deserialize)]
pub struct PkbTrainingEntry {
    pub instruction: String,
    pub swarm_flux: Vec<A2AMessage>,
    pub tool_calls: Vec<String>,
    pub outcome: String,
}

pub struct PkbSynthesizer;

impl PkbSynthesizer {
    pub fn generate_sample(intent: &str, workspace: &Path) -> PkbTrainingEntry {
        // This is a bootstrap synthesizer. In a full run, it would use GEMI to generate
        // thousands of these variations.
        let mut logs = Vec::new();
        let mut tool_calls = Vec::new();

        logs.push(A2AMessage {
            sender: "GhaSafetyAgent".to_string(),
            recipient: "GMA".to_string(),
            action: "MISSION_FLUX".to_string(),
            payload: "Governance protocols active.".to_string(),
        });

        match intent {
            "version" => {
                logs.push(A2AMessage {
                    sender: "GhaReasoningAgent".to_string(),
                    recipient: "GMA".to_string(),
                    action: "MISSION_FLUX".to_string(),
                    payload: "🧠 [Native Synthesis]: ACTION: version".to_string(),
                });
                tool_calls.push("version".to_string());
            }
            _ => {
                logs.push(A2AMessage {
                    sender: "GhaContextAgent".to_string(),
                    recipient: "GMA".to_string(),
                    action: "MISSION_FLUX".to_string(),
                    payload: format!("Contextualizing mission for '{}' in {}", intent, workspace.display()),
                });
                logs.push(A2AMessage {
                    sender: "GhaReasoningAgent".to_string(),
                    recipient: "GMA".to_string(),
                    action: "MISSION_FLUX".to_string(),
                    payload: format!("🧠 [Native Synthesis]: ACTION: status"),
                });
                tool_calls.push("status".to_string());
            }
        }

        PkbTrainingEntry {
            instruction: intent.to_string(),
            swarm_flux: logs,
            tool_calls,
            outcome: "SUCCESS".to_string(),
        }
    }

    pub fn save_training_data(entries: Vec<PkbTrainingEntry>, global_dir: &Path) -> Result<String, String> {
        let train_dir = global_dir.join("train");
        fs::create_dir_all(&train_dir).map_err(|e| e.to_string())?;

        let timestamp = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .map(|d| d.as_secs())
            .unwrap_or(0);

        let file_path = train_dir.join(format!("pkb_dataset_{}.jsonl", timestamp));
        let mut content = String::new();
        let len = entries.len();
        for entry in entries {
            if let Ok(line) = serde_json::to_string(&entry) {
                content.push_str(&line);
                content.push('\n');
            }
        }

        fs::write(&file_path, content).map_err(|e| e.to_string())?;
        let _ = Self::ensure_default_candle_weights(global_dir);
        Ok(format!("Saved {} entries to {}", len, file_path.display()))
    }

    pub fn ensure_default_candle_weights(global_dir: &Path) -> Result<String, String> {
        let models_dir = global_dir.join("models");
        fs::create_dir_all(&models_dir).map_err(|e| e.to_string())?;
        let weights_file = models_dir.join("gha-alpha.safetensors");

        if !weights_file.exists() {
            use candle_core::{Tensor, Device, DType};
            use std::collections::HashMap;

            let device = Device::Cpu;
            let mut tensors = HashMap::new();
            let weight = Tensor::ones((64, 64), DType::F32, &device).map_err(|e| e.to_string())?;
            let bias = Tensor::zeros(64, DType::F32, &device).map_err(|e| e.to_string())?;

            tensors.insert("reflex.weight".to_string(), weight);
            tensors.insert("reflex.bias".to_string(), bias);

            candle_core::safetensors::save(&tensors, &weights_file).map_err(|e| e.to_string())?;
            return Ok(format!("Initialized native Candle weights at {}", weights_file.display()));
        }
        Ok(format!("Native Candle weights present at {}", weights_file.display()))
    }
}
