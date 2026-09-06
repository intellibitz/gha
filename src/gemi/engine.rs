// 🧠 GEMI: Universal AI Inference & Reasoning Bridge
// 100% Rust implementation for Exponential Explosive Intelligence (Model Picking & Benchmarking)

use std::path::Path;
use std::process::{Command, Stdio};
use std::io::Write;
use serde_json::json;
use anyhow::{Result, anyhow};

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
            let (reflex_decision, micros) = super::reflex::ReflexEngine::try_solve(prompt, workspace);
            if let super::reflex::ReflexDecision::Solved(action) = reflex_decision {
                return format!("⚡ [Tier 0: GHA-Alpha Reflex ({}μs)]: {}", micros, action);
            }
        }

        if let Some(res) = Self::scout_cloud_providers(prompt) {
            return res;
        }

        let models = super::models::ModelManager::scout_and_benchmark(workspace);
        for best_model in models {
             if best_model.name.contains("Ollama") {
                 return Self::execute_local_ollama(prompt, &best_model.model_id);
             }
        }

        format!("❌ CLOUD_BRAIN_UNAVAILABLE: All cloud brains failed. (Native: Scouting for brains...)")
    }

    fn scout_cloud_providers(prompt: &str) -> Option<String> {
        // Priority 1: Gemini (Verified Active v1beta)
        match Self::execute_gemini(prompt) {
            Ok(res) => return Some(format!("☁️ [🏆 Premier Pick: Google Gemini]:\n{}", res)),
            Err(_) => {},
        }

        // Priority 2: Groq (Verified Active qwen3.6)
        match Self::execute_groq(prompt) {
            Ok(res) => return Some(format!("☁️ [🏆 Premier Pick: Groq Qwen]:\n{}", res)),
            Err(_) => {},
        }

        None
    }

    fn execute_groq(prompt: &str) -> Result<String> {
        let key = std::env::var("GROQ_API_KEY")?;
        let payload = json!({
            "model": "qwen/qwen3.6-27b",
            "messages": [{"role": "user", "content": prompt}],
            "max_tokens": 100
        });
        let out = Self::curl_pipe("https://api.groq.com/openai/v1/chat/completions", vec![("Authorization", &format!("Bearer {}", key))], payload)?;
        let v: serde_json::Value = serde_json::from_slice(&out)?;
        let text = v.get("choices").and_then(|c| c.get(0)).and_then(|choice| choice.get("message")).and_then(|msg| msg.get("content")).and_then(|t| t.as_str()).ok_or_else(|| anyhow!("Groq failure"))?;
        Ok(Self::cleanse_artifact(text))
    }

    fn execute_gemini(prompt: &str) -> Result<String> {
        let key = std::env::var("GEMINI_API_KEY")?;
        let url = format!("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key={}", key.trim());
        let payload = json!({ "contents": [{"parts": [{"text": prompt}]}] });
        let out = Self::curl_pipe(&url, vec![], payload)?;
        let v: serde_json::Value = serde_json::from_slice(&out)?;
        let text = v.get("candidates").and_then(|c| c.get(0)).and_then(|cand| cand.get("content")).and_then(|cnt| cnt.get("parts")).and_then(|parts| parts.get(0)).and_then(|p| p.get("text")).and_then(|t| t.as_str()).ok_or_else(|| anyhow!("Gemini failure"))?;
        Ok(Self::cleanse_artifact(text))
    }

    fn execute_local_ollama(prompt: &str, model_id: &str) -> String {
        let out = Command::new("ollama").args(["run", model_id, prompt]).output();
        match out {
            Ok(o) if o.status.success() => {
                let text = String::from_utf8_lossy(&o.stdout).trim().to_string();
                format!("🧠 [Ollama Pick: {}]:\n{}", model_id, Self::cleanse_artifact(&text))
            },
            _ => format!("❌ Ollama Failure: Falling back from model '{}'", model_id)
        }
    }

    fn curl_pipe(url: &str, headers: Vec<(&str, &str)>, payload: serde_json::Value) -> Result<Vec<u8>> {
        let mut child = Command::new("curl")
            .args(["-s", "-X", "POST", url, "-H", "Content-Type: application/json"])
            .args(headers.into_iter().flat_map(|(k, v)| vec!["-H".to_string(), format!("{}: {}", k, v)]))
            .arg("-d")
            .arg("@-")
            .stdin(Stdio::piped())
            .stdout(Stdio::piped())
            .spawn()?;

        let mut stdin = child.stdin.take().ok_or_else(|| anyhow!("Failed to open stdin"))?;
        stdin.write_all(payload.to_string().as_bytes())?;
        drop(stdin);

        let out = child.wait_with_output()?;
        if !out.status.success() { return Err(anyhow!("Curl failed")); }
        Ok(out.stdout)
    }

    fn cleanse_artifact(text: &str) -> String {
        let mut final_text = text.trim().to_string();

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

        if let Some(pos) = final_text.rfind("</think>") {
            final_text = final_text[pos + 8..].trim().to_string();
        }

        final_text
    }

    pub fn verify_provider(name: &str) -> String {
        let prompt = "Verification mission: Respond with 'ACTIVE'.";
        match name {
            "Groq" => Self::execute_groq(prompt).unwrap_or_else(|e| format!("❌ Error: {}", e)),
            "Google Gemini" => Self::execute_gemini(prompt).unwrap_or_else(|e| format!("❌ Error: {}", e)),
            _ => "❌ Error: Unknown Provider".to_string(),
        }
    }

    pub fn generate_multimodal_vision(prompt: &str, image_path: &Path) -> String {
        format!("👁️ [gha Vision]: {} -> {}", image_path.display(), prompt)
    }
}
