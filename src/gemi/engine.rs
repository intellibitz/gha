// 🧠 GEMI: Universal AI Inference & Reasoning Bridge
// 100% Rust implementation for Exponential Explosive Intelligence (Model Picking & Benchmarking)

use std::path::Path;
use std::process::Command;
use serde_json::json;
use anyhow::{Result, anyhow};

use super::hardware::HardwareProfiler;
use super::models::ModelManager;

pub struct GemiEngine;

impl GemiEngine {
    pub fn generate_reasoning(prompt: &str, workspace: &Path) -> String {
        Self::reason_internal(prompt, workspace, true)
    }

    pub fn generate_reasoning_deep(prompt: &str, workspace: &Path) -> String {
        Self::reason_internal(prompt, workspace, false)
    }

    fn reason_internal(prompt: &str, workspace: &Path, allow_reflex: bool) -> String {
        if allow_reflex {
            // 🚀 Tier 0: GHA-Alpha Reflex Reasoning (<10ms)
            let (reflex_decision, micros) = super::reflex::ReflexEngine::try_solve(prompt, workspace);
            if let super::reflex::ReflexDecision::Solved(action) = reflex_decision {
                return format!("⚡ [Tier 0: GHA-Alpha Reflex ({}μs)]: {}", micros, action);
            }
        }

        // 1. 🚀 Exponential Discovery: Scout and Benchmark all available brains
        let models = ModelManager::scout_and_benchmark(workspace);

        // 2. 🎯 Picking the Winner: Mission-based model selection
        if let Some(best_model) = models.get(0) {
             if prompt.contains("local") && best_model.name.contains("Ollama") {
                 return Self::execute_local_ollama(prompt, &best_model.model_id);
             }
        }

        // 3. Fallback to Cloud (Gemini, OpenAI, Anthropic, DeepSeek)
        if let Some(res) = Self::scout_cloud_providers(prompt) {
            return res;
        }

        // 4. Native GHA Synthesis (Protocol-Level Fallback)
        format!("❌ CLOUD_BRAIN_UNAVAILABLE: No providers responded to mission. Suggest smaller context or check API health. (Native: {})", Self::native_synthesis(prompt))
    }

    fn scout_cloud_providers(prompt: &str) -> Option<String> {
        let mut errors = Vec::new();

        // Priority 1: Gemini (Reliable for artifacts)
        match Self::execute_gemini(prompt) {
            Ok(res) => return Some(format!("☁️ [🏆 Premier Pick: Google Gemini]:\n{}", res)),
            Err(e) => errors.push(format!("Gemini: {}", e)),
        }

        // Priority 2: Groq (Fast)
        match Self::execute_groq(prompt) {
            Ok(res) => return Some(format!("☁️ [🏆 Premier Pick: Groq Qwen]:\n{}", res)),
            Err(e) => errors.push(format!("Groq: {}", e)),
        }

        // Priority 3: OpenAI
        match Self::execute_openai(prompt) {
            Ok(res) => return Some(format!("☁️ [🏆 Premier Pick: OpenAI GPT-4o]:\n{}", res)),
            Err(e) => errors.push(format!("OpenAI: {}", e)),
        }

        if !errors.is_empty() {
            eprintln!("⚠️ Cloud Intelligence Scouting Failures:\n  {}", errors.join("\n  "));
        }

        None
    }

    fn execute_local_ollama(prompt: &str, model_id: &str) -> String {
        let out = Command::new("ollama").args(["run", model_id, prompt]).output();
        match out {
            Ok(o) if o.status.success() => format!("🧠 [Ollama Pick: {}]:\n{}", model_id, String::from_utf8_lossy(&o.stdout).trim()),
            _ => format!("❌ Ollama Failure: Falling back from model '{}'", model_id)
        }
    }

    fn native_synthesis(prompt: &str) -> String {
        let _ = HardwareProfiler::profile();
        let lower = prompt.to_lowercase();

        if lower == "version" || lower == "what is your version?" {
            return "ACTION: version".to_string();
        }

        format!("Scouting for brains to fulfill: \"{}\"", prompt)
    }

    pub fn verify_provider(name: &str) -> String {
        let prompt = "Verification mission: Respond with 'ACTIVE'.";
        let res = match name {
            "Groq" => Self::execute_groq(prompt),
            "Google Gemini" => Self::execute_gemini(prompt),
            "OpenAI" => Self::execute_openai(prompt),
            _ => Err(anyhow!("Unknown Provider")),
        };
        match res {
            Ok(text) => text,
            Err(e) => format!("❌ Error: {}", e),
        }
    }

    fn execute_anthropic(prompt: &str) -> Result<String> {
        let key = std::env::var("ANTHROPIC_API_KEY")?;
        let payload = json!({
            "model": "claude-3-5-sonnet-20240620",
            "max_tokens": 4096,
            "messages": [{"role": "user", "content": prompt}]
        });

        let payload_file = std::env::temp_dir().join("gha_anthropic_payload.json");
        std::fs::write(&payload_file, payload.to_string())?;

        let out = Command::new("curl").args(["-s", "https://api.anthropic.com/v1/messages", "-H", &format!("x-api-key: {}", key.trim()), "-H", "anthropic-version: 2023-06-01", "-H", "Content-Type: application/json", "-d", &format!("@{}", payload_file.display())]).output()?;
        let _ = std::fs::remove_file(payload_file);

        let v: serde_json::Value = serde_json::from_slice(&out.stdout)?;
        v.get("content").and_then(|c| c.get(0)).and_then(|item| item.get("text")).and_then(|t| t.as_str()).map(|s| s.to_string()).ok_or_else(|| anyhow!("Anthropic failure: {}", v))
    }

    fn execute_groq(prompt: &str) -> Result<String> {
        let key = std::env::var("GROQ_API_KEY")?;
        let payload = json!({
            "model": "qwen/qwen3.6-27b",
            "messages": [{"role": "user", "content": prompt}],
            "max_tokens": 500
        });

        let payload_file = std::env::temp_dir().join("gha_groq_payload.json");
        std::fs::write(&payload_file, payload.to_string())?;

        let out = Command::new("curl").args(["-s", "https://api.groq.com/openai/v1/chat/completions", "-H", &format!("Authorization: Bearer {}", key.trim()), "-H", "Content-Type: application/json", "-d", &format!("@{}", payload_file.display())]).output()?;
        let _ = std::fs::remove_file(payload_file);

        let v: serde_json::Value = serde_json::from_slice(&out.stdout)?;
        v.get("choices").and_then(|c| c.get(0)).and_then(|choice| choice.get("message")).and_then(|msg| msg.get("content")).and_then(|t| t.as_str()).map(|s| s.to_string()).ok_or_else(|| anyhow!("Groq failure: {}", v))
    }

    fn execute_gemini(prompt: &str) -> Result<String> {
        let key = std::env::var("GEMINI_API_KEY")?;
        let url = format!("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={}", key.trim());
        let payload = json!({ "contents": [{"parts": [{"text": prompt}]}] });

        let payload_file = std::env::temp_dir().join("gha_gemini_payload.json");
        std::fs::write(&payload_file, payload.to_string())?;

        let out = Command::new("curl").args(["-s", &url, "-H", "Content-Type: application/json", "-d", &format!("@{}", payload_file.display())]).output()?;
        let _ = std::fs::remove_file(payload_file);

        let v: serde_json::Value = serde_json::from_slice(&out.stdout)?;
        v.get("candidates").and_then(|c| c.get(0)).and_then(|cand| cand.get("content")).and_then(|cnt| cnt.get("parts")).and_then(|parts| parts.get(0)).and_then(|p| p.get("text")).and_then(|t| t.as_str()).map(|s| s.to_string()).ok_or_else(|| anyhow!("Gemini failure: {}", v))
    }

    fn execute_openai(prompt: &str) -> Result<String> {
        let key = std::env::var("OPENAI_API_KEY")?;
        let payload = json!({
            "model": "gpt-4o",
            "messages": [{"role": "user", "content": prompt}]
        });
        let out = Command::new("curl").args(["-s", "https://api.openai.com/v1/chat/completions", "-H", &format!("Authorization: Bearer {}", key.trim()), "-H", "Content-Type: application/json", "-d", &payload.to_string()]).output()?;
        let v: serde_json::Value = serde_json::from_slice(&out.stdout)?;
        v.get("choices").and_then(|c| c.get(0)).and_then(|choice| choice.get("message")).and_then(|msg| msg.get("content")).and_then(|t| t.as_str()).map(|s| s.to_string()).ok_or_else(|| anyhow!("OpenAI failure: {}", v))
    }

    pub fn generate_multimodal_vision(prompt: &str, image_path: &Path) -> String {
        format!("👁️ [gha Vision]: {} -> {}", image_path.display(), prompt)
    }
}
