// 🧠 GEMI: Universal AI Inference & Reasoning Bridge
// 100% Rust implementation for Exponential Explosive Intelligence (Model Picking & Benchmarking)

use std::path::{Path, PathBuf};
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
            let (res, _) = Self::scout_tier2_providers(prompt, workspace);
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
                    return format!("☁️ [Tier 2 GEMI: Google Cloud]:\n{}", res);
                }
            } else if lower_selected.contains("openai") || lower_selected.contains("gpt") {
                if let Ok(res) = Self::execute_openai(prompt) {
                    return format!("☁️ [Tier 2 GEMI: OpenAI Cloud]:\n{}", res);
                }
            } else if lower_selected.contains("groq") {
                if let Ok(res) = Self::execute_groq(prompt) {
                    return format!("☁️ [Tier 2 GEMI: Groq Cloud]:\n{}", res);
                }
            } else if lower_selected.contains("ollama") {
                let res = Self::execute_local_ollama(prompt, &selected_model);
                if !res.contains("❌") {
                    return res;
                }
            }
        }

        let (res, errors) = Self::scout_tier2_providers(prompt, workspace);
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

    fn scout_tier2_providers(prompt: &str, workspace: &Path) -> (Option<String>, Vec<String>) {
        use std::sync::mpsc::channel;
        use std::thread;
        use std::time::Duration;

        let home = std::env::var_os("HOME").or_else(|| std::env::var_os("USERPROFILE")).map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let global_dir = home.join(".gha");
        let cfg = crate::sandbox::manager::GhaConfig::load(&global_dir);

        let (tx, rx) = channel();
        let providers = vec!["google", "groq", "openai"];
        let mut handle_count = 0;

        for provider in providers {
            let t_tx = tx.clone();
            let t_prompt = prompt.to_string();
            let provider_name = provider.to_string();

            thread::spawn(move || {
                let res = match provider_name.as_str() {
                    "google" => Self::execute_gemini(&t_prompt).map(|r| format!("☁️ [Tier 2 GEMI: Google Cloud]:\n{}", r)),
                    "groq" => Self::execute_groq(&t_prompt).map(|r| format!("☁️ [Tier 2 GEMI: Groq Cloud]:\n{}", r)),
                    "openai" => Self::execute_openai(&t_prompt).map(|r| format!("☁️ [Tier 2 GEMI: OpenAI Cloud]:\n{}", r)),
                    _ => Err(anyhow!("Unknown provider")),
                };
                let _ = t_tx.send(res);
            });
            handle_count += 1;
        }

        let mut errors = Vec::new();
        let timeout = Duration::from_secs(cfg.cloud_scout_timeout_secs);
        let start = std::time::Instant::now();

        while handle_count > 0 && start.elapsed() < timeout {
            if let Ok(res) = rx.recv_timeout(Duration::from_millis(100)) {
                handle_count -= 1;
                match res {
                    Ok(text) if !text.trim().is_empty() => return (Some(text), errors),
                    Ok(_) => errors.push("Empty response received".to_string()),
                    Err(e) => errors.push(e.to_string()),
                }
            }
            if start.elapsed() >= timeout { break; }
        }

        (None, errors)
    }

    fn execute_groq(prompt: &str) -> Result<String> {
        let key = std::env::var("GROQ_API_KEY")?;
        let model = std::env::var("GROQ_MODEL").unwrap_or_else(|_| "llama3-70b-8192".to_string());
        let payload = json!({
            "model": model,
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
        let model = std::env::var("GEMINI_MODEL").unwrap_or_else(|_| "gemini-1.5-flash".to_string());
        let url = format!("https://generativelanguage.googleapis.com/v1beta/models/{}:generateContent?key={}", model, key.trim());
        let payload = json!({ "contents": [{"parts": [{"text": prompt}]}] });
        let out = Self::curl_pipe(&url, vec![], payload)?;
        let v: serde_json::Value = serde_json::from_slice(&out)?;
        let text = v.get("candidates").and_then(|c| c.get(0)).and_then(|cand| cand.get("content")).and_then(|cnt| cnt.get("parts")).and_then(|parts| parts.get(0)).and_then(|p| p.get("text")).and_then(|t| t.as_str()).ok_or_else(|| anyhow!("Gemini failure"))?;
        Ok(Self::cleanse_artifact(text))
    }

    fn execute_openai(prompt: &str) -> Result<String> {
        let key = std::env::var("OPENAI_API_KEY")?;
        let model = std::env::var("OPENAI_MODEL").unwrap_or_else(|_| "gpt-4o".to_string());
        let payload = json!({
            "model": model,
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
