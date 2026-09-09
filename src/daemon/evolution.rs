// GHA Substrate Evolution Manager
// RULE 17: Native Evolutionary Assistant Protocol - Analysis & Synthesis

use std::path::Path;
use crate::error::EaiResult;
use crate::sandbox::manager::GhaAuditLogger;
use crate::gawd::reflex_synth::ReflexSynthesizer;
use std::collections::HashMap;

pub struct EvolutionManager;

impl EvolutionManager {
    pub fn evolve_substrate(workspace: &Path) -> EaiResult<String> {
        let intent_to_harden = Self::detect_high_frequency_gap(workspace);

        // 4. Propose and Synthesize
        let proposal = ReflexSynthesizer::distill_native_reflex(&intent_to_harden, workspace)?;

        Ok(format!("# GHA Substrate Evolution Active\n\n\
           The substrate has identified a neural pathway suitable for distillation based on audit log pathology.\n\n\
           - **Intent Target**: '{}'\n\
           - **Action**: {}\n\n\
           The new native reflex has been staged for the next release cycle.",
           intent_to_harden, proposal))
    }

    pub fn detect_high_frequency_gap(workspace: &Path) -> String {
        // 1. Read audit log (last 100 entries)
        let log_content = GhaAuditLogger::read_audit_log(workspace, 100);

        // 2. Pathological Frequency Analysis
        let mut intent_freq = HashMap::new();
        for line in log_content.lines() {
            if line.contains("[MISSION_START]") {
                if let Some(intent) = line.split("[MISSION_START]").nth(1) {
                    let trimmed = intent.trim();
                    if trimmed.len() > 3 {
                        *intent_freq.entry(trimmed.to_string()).or_insert(0) += 1;
                    }
                }
            }
        }

        // 3. Return the most problematic (high-latency) gap
        intent_freq.into_iter()
            .max_by_key(|&(_, count)| count)
            .map(|(intent, _)| intent)
            .unwrap_or_else(|| "calculate square root".to_string())
    }
}
