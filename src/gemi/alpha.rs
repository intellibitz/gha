// 🌌 GHA-Alpha: Native Neural Intelligence Substrate
// 100% Rust implementation using Candle for Tier 0 Reflex Distillation

use anyhow::{Result, anyhow};
use candle_core::{Device, Tensor, DType};
use candle_nn::{Linear, Module, VarBuilder};
use std::path::Path;

/// GHA-Alpha Intent Classifier (Neural Reflex)
pub struct GhaAlphaModel {
    fc1: Linear,
    fc2: Linear,
}

impl GhaAlphaModel {
    pub const DIM: usize = 128;

    pub fn load(global_dir: &Path) -> Result<Self> {
        let weights_path = global_dir.join("models/gha-alpha.safetensors");
        if !weights_path.exists() {
            return Err(anyhow!("GHA-Alpha weights not found at {}", weights_path.display()));
        }

        let device = Device::Cpu;
        let vb = unsafe { VarBuilder::from_mmaped_safetensors(&[weights_path], DType::F32, &device)? };

        let fc1 = candle_nn::linear(Self::DIM, Self::DIM, vb.pp("reflex"))?;
        let fc2 = candle_nn::linear(Self::DIM, Self::DIM, vb.pp("reflex_out"))
            .unwrap_or(candle_nn::linear(Self::DIM, Self::DIM, vb.pp("reflex"))?);

        Ok(Self { fc1, fc2 })
    }

    pub fn predict_intent(&self, prompt: &str) -> Result<String> {
        let device = Device::Cpu;
        let input_vec = self.vectorize(prompt)?;
        let input_tensor = Tensor::from_vec(input_vec, (1, Self::DIM), &device)?;

        let output = self.fc1.forward(&input_tensor)?;
        let output = output.relu()?;
        let output = self.fc2.forward(&output)?;

        let probs = candle_nn::ops::softmax(&output, 1)?;
        let results = probs.to_vec2::<f32>()?[0].clone();

        let mut max_idx = 0;
        let mut max_val = 0.0;
        for (i, &val) in results.iter().enumerate() {
            if val > max_val {
                max_val = val;
                max_idx = i;
            }
        }

        // Mapping index back to tool names
        if max_val > 0.5 {
             let intents = ["status", "version", "self_heal_build", "run_test_harness", "write_file", "read_file", "list_directory", "scout", "reason"];
             if let Some(&intent) = intents.get(max_idx) {
                 return Ok(format!("ACTION: {}", intent));
             }
        }

        Err(anyhow!("Low confidence in neural reflex."))
    }

    fn vectorize(&self, prompt: &str) -> Result<Vec<f32>> {
        let mut vec = vec![0.0f32; Self::DIM];
        let prompt_lower = prompt.to_lowercase();
        let words: Vec<&str> = prompt_lower.split_whitespace().collect();

        for (i, word) in words.iter().enumerate().take(Self::DIM) {
            // Simple hash-based embedding for bootstrap phase
            let mut sum = 0u32;
            for b in word.as_bytes() {
                sum = sum.wrapping_add(*b as u32);
            }
            vec[i] = (sum % 100) as f32 / 100.0;
        }
        Ok(vec)
    }
}
