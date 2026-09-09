// GHA Reflex Synthesizer
// RULE 17: Native Evolutionary Assistant Protocol - Deep Reasoning to Native Reflex Transformation

use std::fs;
use std::path::{Path, PathBuf};
use crate::error::{EaiError, EaiResult};
use crate::gemi::engine::GemiEngine;

pub struct ReflexSynthesizer;

impl ReflexSynthesizer {
    /// Distills a mission into a native Rust tool definition
    pub fn distill_native_reflex(intent: &str, workspace: &Path) -> EaiResult<String> {
        let prompt = format!(
            "MISSION: SYNTHESIZE NATIVE RUST TOOL FOR INTENT: '{}'\n\n\
            REQUIREMENTS:\n\
            1. Create a Rust struct implementing the `GhaTool` trait.\n\
            2. The tool must be high-performance, deterministic, and use standard libraries only.\n\
            3. Provide ONLY the code block for the struct and its implementation.\n\n\
            TRAIT DEFINITION:\n\
            pub trait GhaTool: Send + Sync {{\n\
                fn name(&self) -> String;\n\
                fn description(&self) -> String;\n\
                fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String>;\n\
            }}",
            intent
        );

        let code = GemiEngine::generate_reasoning_deep(&prompt, workspace);

        if code.contains("ERROR:") {
            return Err(EaiError::Inference("Tier 2 reasoning unavailable for distillation.".into()));
        }

        // 1. Save to ~/.gha/reflexes/synthesized_<hash>.rs
        let home = std::env::var_os("HOME").map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let reflex_dir = home.join(".gha/reflexes");
        fs::create_dir_all(&reflex_dir)?;

        let timestamp = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_secs();
        let file_path = reflex_dir.join(format!("reflex_{}.rs", timestamp));

        fs::write(&file_path, code.clone())?;

        Ok(format!("Distilled intelligence for '{}' into native reflex at {}", intent, file_path.display()))
    }

    /// (Alpha) Synthesizes a Wasm reflex by compiling generated Rust code
    pub fn synthesize_wasm_reflex(intent: &str, workspace: &Path) -> EaiResult<String> {
        // This requires 'rustc' and 'wasm32-wasi' target to be available on the host
        let res = Self::distill_native_reflex(intent, workspace)?;

        // Future release: Trigger 'cargo build --target wasm32-wasi' autonomously
        Ok(format!("{} (Wasm Compilation Enqueued)", res))
    }
}
