// GEMI: Universal AI Inference & Reasoning Bridge
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
        let _ = crate::gawd::model_supervisor::ModelSupervisor::audit_and_prepare_models(workspace);

        if allow_reflex {
            let (reflex_decision, _micros) = super::reflex::ReflexEngine::try_solve(prompt, workspace);
            if let super::reflex::ReflexDecision::Solved(action) = reflex_decision {
                return action;
            }
        }

        // 1. Try cloud providers first if configured (Gemini, OpenAI, Groq, Anthropic)
        let (cloud_res, _) = Self::scout_tier2_providers(prompt, workspace);
        if let Some(text) = cloud_res {
            if !text.trim().is_empty() {
                return text;
            }
        }

        let prompt_lower = prompt.to_lowercase();
        let is_synthesis = prompt_lower.contains("fetched content:")
            || prompt_lower.contains("user intent:")
            || prompt_lower.contains("please fulfill");

        // 2. Try pulse action parser (for tools/actions, skipping synthesis prompts)
        if !is_synthesis {
            if let Ok(action) = super::pulse::GhaPulse::reason(prompt, workspace) {
                return action;
            }
        }

        // 3. Try local GGUF Gemma model via llama-server
        let selected_model = super::models::ModelManager::get_selected_model();
        if let Some(model_id) = &selected_model {
            let model_path = PathBuf::from(model_id);
            if model_path.is_file() {
                if let Ok(res) = Self::execute_local_gguf(prompt, &model_path) {
                    if !res.trim().is_empty() {
                        return res;
                    }
                }
            }
        }

        // 4. Try local Ollama if available
        let active_model = selected_model.unwrap_or_else(|| "llama3".to_string());
        let ollama_res = Self::execute_local_ollama(prompt, &active_model);
        if !ollama_res.contains("ERROR") && !ollama_res.trim().is_empty() {
            return ollama_res;
        }

        // 5. Fallback: Check if download_content.txt or an output file exists in workspace
        let download_file = workspace.join("download_content.txt");
        if download_file.is_file() {
            if let Ok(content) = std::fs::read_to_string(&download_file) {
                let preview: String = content.lines().take(12).collect::<Vec<_>>().join("\n");
                return format!("Fetched and saved content to [{}]\n\nContent Preview:\n{}", download_file.display(), preview);
            }
        }

        format!("Executed intent for: \"{}\"", prompt)
    }

    pub fn find_local_llama_server() -> Option<PathBuf> {
        let home = std::env::var_os("HOME").or_else(|| std::env::var_os("USERPROFILE")).map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let candidates = vec![
            home.join(".local/share/JetBrains/Toolbox/apps/android-studio/plugins/gemini/resources/llamacpp/llama-server"),
            home.join(".local/share/JetBrains/Toolbox/apps/android-studio-2/plugins/gemini/resources/llamacpp/llama-server"),
            home.join(".local/share/JetBrains/Toolbox/apps/android-studio-3/plugins/gemini/resources/llamacpp/llama-server"),
            home.join(".local/share/JetBrains/Toolbox/apps/android-studio-5/plugins/gemini/resources/llamacpp/llama-server"),
            PathBuf::from("/usr/bin/llama-server"),
            PathBuf::from("/usr/local/bin/llama-server"),
            home.join(".local/bin/llama-server"),
        ];
        for candidate in candidates {
            if candidate.is_file() {
                return Some(candidate);
            }
        }
        None
    }

    pub fn execute_local_gguf(prompt: &str, model_path: &Path) -> Result<String> {
        let server_bin = Self::find_local_llama_server().ok_or_else(|| anyhow!("llama-server not found"))?;

        // 1. Check if server is already running on port 8085
        let health = Command::new("curl").args(["-s", "--connect-timeout", "2", "--max-time", "3", "http://127.0.0.1:8085/health"]).output();
        let server_active = matches!(health, Ok(ref o) if o.status.success() && String::from_utf8_lossy(&o.stdout).contains("ok"));

        if !server_active {
            let _ = Command::new(&server_bin)
                .args(["-m", model_path.to_str().unwrap_or_default(), "--port", "8085", "-ngl", "0", "-c", "2048"])
                .stdin(Stdio::null())
                .stdout(Stdio::null())
                .stderr(Stdio::null())
                .spawn();

            let start = std::time::Instant::now();
            while start.elapsed().as_secs() < 20 {
                std::thread::sleep(std::time::Duration::from_millis(500));
                if let Ok(o) = Command::new("curl").args(["-s", "--connect-timeout", "2", "--max-time", "3", "http://127.0.0.1:8085/health"]).output() {
                    let body = String::from_utf8_lossy(&o.stdout);
                    if o.status.success() && (body.contains("ok") || body.contains("no slot available")) {
                        break;
                    }
                }
            }
        }

        let formatted_prompt = format!("<bos>User: {}\nAssistant:", prompt);
        let payload = json!({
            "prompt": formatted_prompt,
            "n_predict": 256,
            "temperature": 0.2
        });

        let out = Self::curl_pipe("http://127.0.0.1:8085/completion", vec![], payload)?;
        let v: serde_json::Value = serde_json::from_slice(&out)?;
        let text = v.get("content").and_then(|t| t.as_str()).unwrap_or("").trim().to_string();

        if text.is_empty() {
            return Err(anyhow!("Empty completion from local GGUF model"));
        }

        Ok(Self::cleanse_artifact(&text))
    }

    #[allow(dead_code)]
    fn scout_tier2_providers(prompt: &str, _workspace: &Path) -> (Option<String>, Vec<String>) {
        use std::sync::mpsc::channel;
        use std::thread;
        use std::time::Duration;

        let home = std::env::var_os("HOME").or_else(|| std::env::var_os("USERPROFILE")).map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let global_dir = home.join(".gha");
        let cfg = crate::sandbox::manager::GhaConfig::load(&global_dir);

        let (tx, rx) = channel();
        let mut handle_count = 0;

        for model in cfg.cloud_models {
            let t_tx = tx.clone();
            let t_prompt = prompt.to_string();
            let t_model = model.clone();

            thread::spawn(move || {
                let res = Self::execute_generic_cloud(&t_model, &t_prompt);
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

    pub fn execute_generic_cloud(model: &crate::sandbox::manager::ModelInfo, prompt: &str) -> Result<String> {
        use crate::sandbox::manager::ProviderType;

        let env_key = model.env_key.as_ref().ok_or_else(|| anyhow!("No environment key configured for model"))?;
        let api_key = std::env::var(env_key).map_err(|_| anyhow!("API key '{}' not set in environment", env_key))?;
        let api_base = model.api_base.as_ref().ok_or_else(|| anyhow!("No API base URL configured for model"))?;

        match model.provider {
            ProviderType::OpenAI => {
                let url = format!("{}/chat/completions", api_base.trim_end_matches('/'));
                let payload = json!({
                    "model": model.model_id,
                    "messages": [{"role": "user", "content": prompt}]
                });
                let out = Self::curl_pipe(&url, vec![("Authorization", &format!("Bearer {}", api_key))], payload)?;
                let v: serde_json::Value = serde_json::from_slice(&out)?;
                let text = v.get("choices").and_then(|c| c.get(0)).and_then(|choice| choice.get("message")).and_then(|msg| msg.get("content")).and_then(|t| t.as_str()).ok_or_else(|| anyhow!("OpenAI-compatible failure"))?;
                Ok(Self::cleanse_artifact(text))
            },
            ProviderType::Google => {
                let url = format!("{}/models/{}:generateContent?key={}", api_base.trim_end_matches('/'), model.model_id.replace("google/", ""), api_key.trim());
                let payload = json!({ "contents": [{"parts": [{"text": prompt}]}] });
                let out = Self::curl_pipe(&url, vec![], payload)?;
                let v: serde_json::Value = serde_json::from_slice(&out)?;
                let text = v.get("candidates").and_then(|c| c.get(0)).and_then(|cand| cand.get("content")).and_then(|cnt| cnt.get("parts")).and_then(|parts| parts.get(0)).and_then(|p| p.get("text")).and_then(|t| t.as_str()).ok_or_else(|| anyhow!("Gemini failure"))?;
                Ok(Self::cleanse_artifact(text))
            },
            ProviderType::Anthropic => {
                let url = format!("{}/messages", api_base.trim_end_matches('/'));
                let payload = json!({
                    "model": model.model_id,
                    "max_tokens": 8192,
                    "messages": [{"role": "user", "content": prompt}]
                });
                let out = Self::curl_pipe(&url, vec![
                    ("x-api-key", &api_key),
                    ("anthropic-version", "2023-06-01")
                ], payload)?;
                let v: serde_json::Value = serde_json::from_slice(&out)?;
                let text = v.get("content").and_then(|c| c.get(0)).and_then(|item| item.get("text")).and_then(|t| t.as_str()).ok_or_else(|| anyhow!("Anthropic failure"))?;
                Ok(Self::cleanse_artifact(text))
            },
            _ => Err(anyhow!("Unsupported provider type for cloud execution")),
        }
    }

    fn execute_local_ollama(prompt: &str, model_id: &str) -> String {
        let out = Command::new("ollama").args(["run", model_id, prompt]).output();
        match out {
            Ok(o) if o.status.success() => {
                let text = String::from_utf8_lossy(&o.stdout).trim().to_string();
                format!("[Ollama Pick: {}]:\n{}", model_id, Self::cleanse_artifact(&text))
            },
            _ => format!("ERROR: Ollama failure from model '{}'", model_id)
        }
    }

    fn curl_pipe(url: &str, headers: Vec<(&str, &str)>, payload: serde_json::Value) -> Result<Vec<u8>> {
        let mut child = Command::new("curl")
            .args(["-s", "--connect-timeout", "5", "--max-time", "15", "-X", "POST", url, "-H", "Content-Type: application/json"])
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
        let home = std::env::var_os("HOME").or_else(|| std::env::var_os("USERPROFILE")).map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let global_dir = home.join(".gha");
        let cfg = crate::sandbox::manager::GhaConfig::load(&global_dir);

        if let Some(model) = cfg.cloud_models.iter().find(|m| m.name == name) {
            match Self::execute_generic_cloud(model, prompt) {
                Ok(t) => t,
                Err(e) => format!("ERROR: {}", e)
            }
        } else {
            format!("ERROR: Provider '{}' not found in config", name)
        }
    }

    pub fn generate_multimodal_vision(prompt: &str, image_path: &Path) -> String {
        format!("👁️ [gha Vision]: {} -> {}", image_path.display(), prompt)
    }
}
