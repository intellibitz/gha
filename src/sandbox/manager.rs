// 🏠 GHA Sandbox Manager
// 100% Rust implementation for Global Engine State Management

use std::fs;
use std::path::{Path, PathBuf};

pub struct SandboxManager;

impl SandboxManager {
    pub fn ensure_global_sandbox(global_dir: &Path) -> PathBuf {
        if !global_dir.exists() {
            let _ = fs::create_dir_all(global_dir);
            let _ = fs::create_dir_all(global_dir.join("bin"));
            let _ = fs::create_dir_all(global_dir.join("models"));
        }
        global_dir.to_path_buf()
    }

    pub fn is_global_sandbox_active(global_dir: &Path) -> bool {
        global_dir.join("bin").is_dir()
    }
}
