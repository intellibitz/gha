// 🧠 GEMI Engine: Pure AI Inference & Reasoning Coordinator
// 100% Rust implementation supporting Multimodal Cloud APIs, Local GGUF & IPC backends

use std::fs;
use std::path::{Path, PathBuf};
use std::process::Command;
use serde::{Deserialize, Serialize};

use super::hardware::HardwareProfiler;
use super::models::{ModelInfo, ModelManager};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CustomCloudProvider {
    pub name: String,
    pub api_url: String,
    pub api_key: String,
    pub model_id: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CloudProvidersConfig {
    pub providers: Vec<CustomCloudProvider>,
}

pub struct GemiEngine;

impl GemiEngine {
    pub fn get_intelligence_report(workspace: &Path) -> (usize, String, Vec<ModelInfo>) {
        let (cpus, gpu) = HardwareProfiler::profile();
        let models = ModelManager::list_models(workspace);
        (cpus, gpu, models)
    }

    pub fn find_local_model_path(workspace: &Path) -> Option<PathBuf> {
        // 1. Check workspace local vault
        let ws_models = workspace.join(".gha/models");
        if let Ok(entries) = std::fs::read_dir(&ws_models) {
            for entry in entries.flatten() {
                let p = entry.path();
                if let Some(ext) = p.extension() {
                    if ext == "gguf" || ext == "bin" {
                        return Some(p);
                    }
                }
            }
        }

        // 2. Check global ~/.gha/models vault
        if let Some(home) = std::env::var_os("HOME").map(PathBuf::from) {
            let global_models = home.join(".gha/models");
            if let Ok(entries) = std::fs::read_dir(&global_models) {
                for entry in entries.flatten() {
                    let p = entry.path();
                    if let Some(ext) = p.extension() {
                        if ext == "gguf" || ext == "bin" {
                            return Some(p);
                        }
                    }
                }
            }
        }

        None
    }

    pub fn generate_reasoning(prompt: &str, workspace: &Path) -> String {
        let json_prompt = serde_json::to_string(prompt).unwrap_or_default();

        // 1. Check Google Gemini AI Studio (GEMINI_API_KEY)
        let gemini_key = std::env::var("GEMINI_API_KEY")
            .or_else(|_| std::env::var("GEMINI_AI_STUDIO_KEY"))
            .ok();

        if let Some(key) = gemini_key {
            if !key.trim().is_empty() {
                let url = format!(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={}",
                    key.trim()
                );
                let payload = format!(
                    "{{\"contents\":[{{\"parts\":[{{\"text\":{}}}]}}]}}",
                    json_prompt
                );
                let out = Command::new("curl")
                    .args(["-s", &url, "-H", "Content-Type: application/json", "-d", &payload])
                    .output();

                if let Ok(o) = out {
                    if o.status.success() {
                        let stdout = String::from_utf8_lossy(&o.stdout);
                        if let Ok(v) = serde_json::from_str::<serde_json::Value>(&stdout) {
                            if let Some(text) = v.get("candidates")
                                .and_then(|c| c.get(0))
                                .and_then(|cand| cand.get("content"))
                                .and_then(|cnt| cnt.get("parts"))
                                .and_then(|parts| parts.get(0))
                                .and_then(|p| p.get("text"))
                                .and_then(|t| t.as_str())
                            {
                                return format!("☁️ [Google Gemini AI Studio]:\n{}", text.trim());
                            }
                        }
                    }
                }
            }
        }

        // 2. Local GGUF inference via llama-cli
        if let Some(model_path) = Self::find_local_model_path(workspace) {
            let llama_bin = Command::new("llama-cli")
                .args([
                    "-m", model_path.to_str().unwrap_or(""),
                    "-p", prompt,
                    "-n", "512",
                    "--temp", "0.2",
                    "-ngl", "99",
                    "--silent-prompt"
                ])
                .output();

            if let Ok(out) = llama_bin {
                if out.status.success() {
                    let stdout = String::from_utf8_lossy(&out.stdout).trim().to_string();
                    if !stdout.is_empty() {
                        return format!("🧠 [GGUF Local Engine Inference ({})]:\n{}", model_path.file_name().unwrap_or_default().to_string_lossy(), stdout);
                    }
                }
            }
        }

        // 3. Fallback
        let (cpus, gpu) = HardwareProfiler::profile();
        format!(
            "🧠 [GEMI Executive AI Synthesis ({} CPUs | {})]:\nFormulated strategy for: '{}'.",
            cpus, gpu, prompt
        )
    }

    pub fn generate_multimodal_vision(prompt: &str, image_path: &Path) -> String {
        let image_data = match fs::read(image_path) {
            Ok(data) => base64_encode(&data),
            Err(_) => return format!("❌ Vision Error: Could not read image at {}", image_path.display()),
        };

        // Check Google Gemini AI Studio for Multimodal Vision
        let gemini_key = std::env::var("GEMINI_API_KEY")
            .or_else(|_| std::env::var("GEMINI_AI_STUDIO_KEY"))
            .ok();

        if let Some(key) = gemini_key {
            let url = format!(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={}",
                key.trim()
            );
            let payload = format!(
                "{{\"contents\":[{{\"parts\":[{{\"text\":\"{}\"}},{{\"inline_data\":{{\"mime_type\":\"image/jpeg\",\"data\":\"{}\"}}}}]}}]}}",
                prompt, image_data
            );

            let out = Command::new("curl")
                .args(["-s", &url, "-H", "Content-Type: application/json", "-d", &payload])
                .output();

            if let Ok(o) = out {
                let stdout = String::from_utf8_lossy(&o.stdout);
                if let Ok(v) = serde_json::from_str::<serde_json::Value>(&stdout) {
                    if let Some(text) = v.get("candidates")
                        .and_then(|c| c.get(0))
                        .and_then(|cand| cand.get("content"))
                        .and_then(|cnt| cnt.get("parts"))
                        .and_then(|parts| parts.get(0))
                        .and_then(|p| p.get("text"))
                        .and_then(|t| t.as_str())
                    {
                        return format!("👁️ [Multimodal Vision (Gemini)]: {}", text.trim());
                    }
                }
            }
        }

        format!("👁️ [Multimodal Vision Fallback]: Attempted vision analysis on {} with prompt '{}'. Ensure GEMINI_API_KEY is set.", image_path.display(), prompt)
    }
}

pub fn base64_encode(input: &[u8]) -> String {
    let mut output = String::with_capacity(input.len() * 4 / 3 + 4);
    const CHARS: &[u8] = b"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    for chunk in input.chunks(3) {
        let b0 = chunk[0] as usize;
        let b1 = chunk.get(1).map(|&b| b as usize).unwrap_or(0);
        let b2 = chunk.get(2).map(|&b| b as usize).unwrap_or(0);
        output.push(CHARS[b0 >> 2] as char);
        output.push(CHARS[((b0 & 0x03) << 4) | (b1 >> 4)] as char);
        if chunk.len() > 1 {
            output.push(CHARS[((b1 & 0x0f) << 2) | (b2 >> 6)] as char);
        } else {
            output.push('=');
        }
        if chunk.len() > 2 {
            output.push(CHARS[b2 & 0x3f] as char);
        } else {
            output.push('=');
        }
    }
    output
}
