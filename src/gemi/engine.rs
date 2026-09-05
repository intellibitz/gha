// 🧠 GEMI Engine: Pure AI Inference & Reasoning Coordinator
// 100% Rust implementation for local GGUF & OpenAI ChatCompletions provider

use super::hardware::HardwareProfiler;
use super::models::{ModelInfo, ModelManager};
use std::path::Path;

pub struct GemiEngine;

impl GemiEngine {
    pub fn get_intelligence_report(workspace: &Path) -> (usize, String, Vec<ModelInfo>) {
        let (cpus, gpu) = HardwareProfiler::profile();
        let models = ModelManager::list_models(workspace);
        (cpus, gpu, models)
    }
}
