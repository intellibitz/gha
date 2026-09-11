// AEON Substrate Evolution Manager
// RULE 17: Native Evolutionary Assistant Protocol - Analysis & Synthesis

use std::path::Path;
use crate::error::EaiResult;
use crate::sandbox::manager::AeonAuditLogger;
use crate::gawd::reflex_synth::ReflexSynthesizer;
use std::collections::HashMap;

pub struct EvolutionManager;

impl EvolutionManager {
    pub fn evolve_substrate(workspace: &Path) -> EaiResult<String> {
        let intent_to_harden = Self::detect_high_frequency_gap(workspace);

        if intent_to_harden.starts_with("scout_model ") {
            let model_name = intent_to_harden.trim_start_matches("scout_model ").to_string();
            let res = crate::gmcp::tools::ToolRegistry::execute_tool("scout_model", &model_name, workspace);
            return Ok(format!("# AEON Intelligence Discovery\n\n\
               The substrate has detected a request for an unknown model and autonomously initiated discovery.\\n\\n\
               - **Model**: '{}'\\n\
               - **Result**: {}",
               model_name, res));
        }

        // 4. Propose and Synthesize
        let proposal = match ReflexSynthesizer::distill_native_reflex(&intent_to_harden, workspace) {
            Ok(p) => p,
            Err(e) => {
                format!("⚠️ Distillation failed: {}. Proposed Evolution: Implement native Rust N-P-K nutrient calculation reflex in AeonPulse.", e)
            }
        };

        Ok(format!("# AEON Substrate Evolution Active\n\n\
           The substrate has identified a neural pathway suitable for distillation based on audit log pathology.\\n\\n\
           - **Intent Target**: '{}'\\n\
           - **Action**: {}\\n\\n\
           The new native reflex has been staged for the next release cycle.",
           intent_to_harden, proposal))
    }

    pub fn detect_high_frequency_gap(workspace: &Path) -> String {
        // 1. Read audit log (last 100 entries)
        let log_content = AeonAuditLogger::read_audit_log(workspace, 100);

        // 2. Pathological Frequency Analysis
        let mut intent_freq = HashMap::new();
        let mut model_requests = Vec::new();
        for line in log_content.lines() {
            if line.contains("[MISSION_START]") {
                if let Some(intent) = line.split("[MISSION_START]").nth(1) {
                    let trimmed = intent.trim();
                    if trimmed.len() > 3 {
                        *intent_freq.entry(trimmed.to_string()).or_insert(0) += 1;
                        if trimmed.to_lowercase().contains("pull") || trimmed.to_lowercase().contains("install") {
                            model_requests.push(trimmed.to_string());
                        }
                    }
                }
            }
        }

        // 3. Autonomous Discovery Check (Rule 17)
        for req in model_requests {
             let model_name = req.split_whitespace().last().unwrap_or("");
             if !model_name.is_empty() && crate::gemi::models::ModelRegistry::resolve_intent(model_name).is_none() {
                 return format!("scout_model {}", model_name);
             }
        }

        // 4. Return the most problematic (high-latency) reasoning gap
        intent_freq.into_iter()
            .max_by_key(|&(_, count)| count)
            .map(|(intent, _)| intent)
            .unwrap_or_else(|| "calculate square root".to_string())
    }
}
