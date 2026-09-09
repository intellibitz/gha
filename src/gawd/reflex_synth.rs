// GHA Reflex Synthesizer
// RULE 17: Native Evolutionary Assistant Protocol - Deep Reasoning to Native Reflex Transformation

use std::fs;
use std::path::{Path, PathBuf};
use std::process::Command;
use crate::error::{EaiError, EaiResult};
use crate::gemi::engine::GemiEngine;

pub struct ReflexSynthesizer;

impl ReflexSynthesizer {
    /// Distills a mission into a native Rust tool definition and integrates it into the core
    pub fn distill_native_reflex(intent: &str, workspace: &Path) -> EaiResult<String> {
        let clean_intent = intent.replace(|c: char| !c.is_alphanumeric(), "");
        let struct_name = format!("{}Reflex", clean_intent);

        let prompt = format!(
            "MISSION: SYNTHESIZE NATIVE RUST TOOL FOR INTENT: '{}'\n\n\
            REQUIREMENTS:\n\
            1. Create a Rust struct named `{}` implementing the `GhaTool` trait.\n\
            2. The tool must be high-performance, deterministic, and use standard libraries only.\n\
            3. Provide ONLY the code block for the struct and its implementation.\n\n\
            TRAIT DEFINITION:\n\
            pub trait GhaTool: Send + Sync {{\n\
                fn name(&self) -> String;\n\
                fn description(&self) -> String;\n\
                fn execute(&self, arg: &str, workspace: &Path) -> EaiResult<String>;\n\
            }}",
            intent, struct_name
        );

        let code = GemiEngine::generate_reasoning_deep(&prompt, workspace);

        if code.contains("ERROR:") {
            return Err(EaiError::Inference("Tier 2 reasoning unavailable for distillation.".into()));
        }

        let clean_code = code.trim().trim_start_matches("```rust").trim_start_matches("```").trim_end_matches("```").trim().to_string();

        // 1. Save backup to ~/.gha/reflexes/
        let home = std::env::var_os("HOME").map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let reflex_dir = home.join(".gha/reflexes");
        fs::create_dir_all(&reflex_dir)?;

        let timestamp = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_secs();
        let backup_path = reflex_dir.join(format!("reflex_{}.rs", timestamp));
        fs::write(&backup_path, &clean_code)?;

        // 2. 🌀 Autonomous Source Integration (Rule 11 & 17)
        let reflex_rs_path = workspace.join("src/gmcp/reflexes.rs");
        if reflex_rs_path.exists() {
            let mut content = fs::read_to_string(&reflex_rs_path)?;

            // Inject Struct/Impl
            if let Some(pos) = content.find("// [AUTONOMOUS TOOLS END]") {
                 content.insert_str(pos, &format!("{}\n\n", clean_code));
            }

            // Inject Registration
            let registration_line = format!("    tools.insert(\"{}\".to_string(), Arc::new({}));\n", clean_intent.to_lowercase(), struct_name);
            if let Some(reg_pos) = content.find("// [AUTONOMOUS REGISTRATION END]") {
                 content.insert_str(reg_pos, &registration_line);
            }

            fs::write(&reflex_rs_path, content)?;
        }

        Ok(format!("Distilled intelligence for '{}' into native reflex '{}' and integrated into substrate.", intent, struct_name))
    }

    /// (Alpha) Synthesizes a Wasm reflex by compiling generated Rust code
    pub fn synthesize_wasm_reflex(intent: &str, workspace: &Path) -> EaiResult<String> {
        let reflex_code_path = Self::distill_native_reflex(intent, workspace)?;

        // Extract the path from the result message
        if let Some(path_str) = reflex_code_path.split("at ").last() {
            let src_path = PathBuf::from(path_str);
            let wasm_path = src_path.with_extension("wasm");

            // Autonomous Compilation (Rule 11/17)
            let out = Command::new("rustc")
                .args(["--target", "wasm32-wasi", "-O", "-o"])
                .arg(&wasm_path)
                .arg(&src_path)
                .output();

            match out {
                Ok(o) if o.status.success() => {
                    Ok(format!("Distilled Wasm reflex compiled and ready at {}", wasm_path.display()))
                }
                Ok(o) => {
                    Err(EaiError::Hardware(format!("Wasm Compilation Failed: {}", String::from_utf8_lossy(&o.stderr))))
                }
                Err(e) => {
                    Err(EaiError::Hardware(format!("rustc not found: {}", e)))
                }
            }
        } else {
            Err(EaiError::Internal("Reflex synthesis failed to return path.".into()))
        }
    }
}
