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
        format!("❌ CLOUD_BRAIN_UNAVAILABLE: No providers responded to mission. (Native: Scouting for brains...)")
    }

    fn scout_cloud_providers(prompt: &str) -> Option<String> {
        // Priority 1: Gemini (Reliable for artifacts)
        if let Ok(res) = Self::execute_gemini(prompt) { return Some(res); }

        // Priority 2: Groq
        if let Ok(res) = Self::execute_groq(prompt) { return Some(res); }

        // Priority 3: OpenAI
        if let Ok(res) = Self::execute_openai(prompt) { return Some(res); }

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
        let out = Self::curl_json("https://api.anthropic.com/v1/messages", vec![("x-api-key", &key), ("anthropic-version", "2023-06-01")], payload)?;
        let text = out.get("content").and_then(|c| c.get(0)).and_then(|item| item.get("text")).and_then(|t| t.as_str()).ok_or_else(|| anyhow!("Anthropic failure"))?;
        Ok(Self::cleanse_artifact(text))
    }

    fn execute_groq(prompt: &str) -> Result<String> {
        let key = std::env::var("GROQ_API_KEY")?;
        let payload = json!({
            "model": "qwen/qwen3.6-27b",
            "messages": [{"role": "user", "content": prompt}],
            "max_tokens": 500
        });
        let out = Self::curl_json("https://api.groq.com/openai/v1/chat/completions", vec![("Authorization", &format!("Bearer {}", key))], payload)?;
        let text = out.get("choices").and_then(|c| c.get(0)).and_then(|choice| choice.get("message")).and_then(|msg| msg.get("content")).and_then(|t| t.as_str()).ok_or_else(|| anyhow!("Groq failure"))?;
        Ok(Self::cleanse_artifact(text))
    }

    fn execute_gemini(prompt: &str) -> Result<String> {
        let key = std::env::var("GEMINI_API_KEY")?;
        let url = format!("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={}", key.trim());
        let payload = json!({ "contents": [{"parts": [{"text": prompt}]}] });
        let out = Self::curl_json(&url, vec![], payload)?;
        let text = out.get("candidates").and_then(|c| c.get(0)).and_then(|cand| cand.get("content")).and_then(|cnt| cnt.get("parts")).and_then(|parts| parts.get(0)).and_then(|p| p.get("text")).and_then(|t| t.as_str()).ok_or_else(|| anyhow!("Gemini failure"))?;
        Ok(Self::cleanse_artifact(text))
    }

    fn execute_openai(prompt: &str) -> Result<String> {
        let key = std::env::var("OPENAI_API_KEY")?;
        let payload = json!({
            "model": "gpt-4o",
            "messages": [{"role": "user", "content": prompt}]
        });
        let out = Self::curl_json("https://api.openai.com/v1/chat/completions", vec![("Authorization", &format!("Bearer {}", key))], payload)?;
        let text = out.get("choices").and_then(|c| c.get(0)).and_then(|choice| choice.get("message")).and_then(|msg| msg.get("content")).and_then(|t| t.as_str()).ok_or_else(|| anyhow!("OpenAI failure"))?;
        Ok(Self::cleanse_artifact(text))
    }

    fn curl_json(url: &str, headers: Vec<(&str, &str)>, payload: serde_json::Value) -> Result<serde_json::Value> {
        let payload_file = std::env::temp_dir().join(format!("gha_payload_{}.json", std::process::id()));
        std::fs::write(&payload_file, payload.to_string())?;

        let mut cmd = Command::new("curl");
        cmd.args(["-s", url, "-H", "Content-Type: application/json"]);
        for (k, v) in headers {
            cmd.args(["-H", &format!("{}: {}", k, v)]);
        }
        cmd.args(["-d", &format!("@{}", payload_file.display())]);

        let out = cmd.output()?;
        let _ = std::fs::remove_file(payload_file);

        if !out.status.success() {
             return Err(anyhow!("Curl failed"));
        }

        let v: serde_json::Value = serde_json::from_slice(&out.stdout)?;
        Ok(v)
    }

    fn cleanse_artifact(text: &str) -> String {
        let mut final_text = text.trim().to_string();

        if let Some(start) = final_text.find("<GHA_ARTIFACT>") {
            if let Some(end) = final_text.find("</GHA_ARTIFACT>") {
                return final_text[start + 14..end].trim().to_string();
            }
        }

        while let Some(start) = final_text.find("<think>") {
            if let Some(end) = final_text.find("</think>") {
                let mut new_text = final_text[..start].to_string();
                new_text.push_str(&final_text[end + 8..]);
                final_text = new_text.trim().to_string();
            } else {
                final_text = final_text[..start].trim().to_string();
                break;
            }
        }

        final_text
    }

    pub fn generate_multimodal_vision(prompt: &str, image_path: &Path) -> String {
        format!("👁️ [gha Vision]: {} -> {}", image_path.display(), prompt)
    }
}
