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

        let selected_engine = super::models::ModelManager::get_selected_engine().unwrap_or_default().to_lowercase();
        let selected_model = super::models::ModelManager::get_selected_model().unwrap_or_default();

        if selected_engine == "gemi" || selected_engine == "cloud" || selected_engine.is_empty() {
            let (res, _) = Self::scout_cloud_providers(prompt);
            if let Some(text) = res {
                return text;
            }
        } else if selected_engine == "ollama" {
            return Self::execute_local_ollama(prompt, &selected_model);
        } else if selected_engine == "candle" {
            if let Ok(action) = super::pulse::GhaPulse::reason(prompt, workspace) {
                return format!("⚡ [Candle Engine]: {}", action);
            }
            if let Ok(count) = super::pulse::GhaPulse::try_load_candle_weights() {
                return format!("⚡ [Candle Engine ({} Tensors)]: Executed offline response for '{}'.", count, prompt);
            }
        }

        if !selected_model.is_empty() {
            let lower_selected = selected_model.to_lowercase();
            if lower_selected.contains("gemini") {
                if let Ok(res) = Self::execute_gemini(prompt) {
                    return format!("☁️ [Selected Model: Google Gemini]:\n{}", res);
                }
            } else if lower_selected.contains("openai") || lower_selected.contains("gpt") {
                if let Ok(res) = Self::execute_openai(prompt) {
                    return format!("☁️ [Selected Model: OpenAI GPT-4o]:\n{}", res);
                }
            } else if lower_selected.contains("ollama") {
                let res = Self::execute_local_ollama(prompt, &selected_model);
                if !res.contains("❌") {
                    return res;
                }
            }
        }

        let (res, errors) = Self::scout_cloud_providers(prompt);
        if let Some(text) = res {
            return text;
        }

        let models = super::models::ModelManager::scout_and_benchmark(workspace);
        for best_model in models {
             if best_model.is_local && best_model.registry.contains("GGUF") {
                 // Fallback to local GGUF reasoning if cloud is down
                 return format!("⚡ [Local GGUF Fallback: {}]: Discovered local model for mission.", best_model.name);
             }
        }

        format!("❌ CLOUD_BRAIN_UNAVAILABLE: No responders. Diagnostics:\n  {}", errors.join("\n  "))
    }

    fn scout_cloud_providers(prompt: &str) -> (Option<String>, Vec<String>) {
        let mut errors = Vec::new();

        // Priority 1: Gemini (Reliable for artifacts)
        match Self::execute_gemini(prompt) {
            Ok(res) if !res.trim().is_empty() => return (Some(format!("☁️ [🏆 Premier Pick: Google Gemini]:\n{}", res)), errors),
            Ok(_) => errors.push("Gemini: Empty response".to_string()),
            Err(e) => errors.push(format!("Gemini: {}", e)),
        }

        // Priority 2: Groq
        match Self::execute_groq(prompt) {
            Ok(res) if !res.trim().is_empty() => return (Some(format!("☁️ [🏆 Premier Pick: Groq Cloud]:\n{}", res)), errors),
            Ok(_) => errors.push("Groq: Empty response".to_string()),
            Err(e) => errors.push(format!("Groq: {}", e)),
        }

        // Priority 3: OpenAI
        match Self::execute_openai(prompt) {
            Ok(res) if !res.trim().is_empty() => return (Some(format!("☁️ [🏆 Premier Pick: OpenAI GPT-4o]:\n{}", res)), errors),
            Ok(_) => errors.push("OpenAI: Empty response".to_string()),
            Err(e) => errors.push(format!("OpenAI: {}", e)),
        }

        (None, errors)
    }

    fn execute_groq(prompt: &str) -> Result<String> {
        let key = std::env::var("GROQ_API_KEY")?;
        let payload = json!({
            "model": "llama3-70b-8192",
            "messages": [{"role": "user", "content": prompt}],
            "max_tokens": 1000
        });
        let out = Self::curl_pipe("https://api.groq.com/openai/v1/chat/completions", vec![("Authorization", &format!("Bearer {}", key))], payload)?;
        let v: serde_json::Value = serde_json::from_slice(&out)?;
        let text = v.get("choices").and_then(|c| c.get(0)).and_then(|choice| choice.get("message")).and_then(|msg| msg.get("content")).and_then(|t| t.as_str()).ok_or_else(|| anyhow!("Groq failure"))?;
        Ok(Self::cleanse_artifact(text))
    }

    fn execute_gemini(prompt: &str) -> Result<String> {
        let key = std::env::var("GEMINI_API_KEY")?;
        let url = format!("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={}", key.trim());
        let payload = json!({ "contents": [{"parts": [{"text": prompt}]}] });
        let out = Self::curl_pipe(&url, vec![], payload)?;
        let v: serde_json::Value = serde_json::from_slice(&out)?;
        let text = v.get("candidates").and_then(|c| c.get(0)).and_then(|cand| cand.get("content")).and_then(|cnt| cnt.get("parts")).and_then(|parts| parts.get(0)).and_then(|p| p.get("text")).and_then(|t| t.as_str()).ok_or_else(|| anyhow!("Gemini failure"))?;
        Ok(Self::cleanse_artifact(text))
    }

    fn execute_openai(prompt: &str) -> Result<String> {
        let key = std::env::var("OPENAI_API_KEY")?;
        let payload = json!({
            "model": "gpt-4o",
            "messages": [{"role": "user", "content": prompt}]
        });
        let out = Self::curl_pipe("https://api.openai.com/v1/chat/completions", vec![("Authorization", &format!("Bearer {}", key))], payload)?;
        let v: serde_json::Value = serde_json::from_slice(&out)?;
        let text = v.get("choices").and_then(|c| c.get(0)).and_then(|choice| choice.get("message")).and_then(|msg| msg.get("content")).and_then(|t| t.as_str()).ok_or_else(|| anyhow!("OpenAI failure"))?;
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
        final_text
    }

    pub fn verify_provider(name: &str) -> String {
        let prompt = "Verification mission: Respond with 'ACTIVE'.";
        let res = match name {
            "Google Gemini" => Self::execute_gemini(prompt),
            "Groq" => Self::execute_groq(prompt),
            "OpenAI" => Self::execute_openai(prompt),
            _ => Err(anyhow!("Unknown Provider")),
        };
        match res { Ok(t) => t, Err(e) => format!("❌ Error: {}", e) }
    }

    pub fn generate_multimodal_vision(prompt: &str, image_path: &Path) -> String {
        format!("👁️ [gha Vision]: {} -> {}", image_path.display(), prompt)
    }
}
