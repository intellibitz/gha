// 🏠 GHA Sandbox Manager & Security Engine
// 100% Rust implementation of workspace sandboxing & health verification

use std::fs;
use std::path::{Path, PathBuf};

pub struct SandboxManager;

impl SandboxManager {
    pub fn ensure_sandbox(workspace: &Path) -> PathBuf {
        let sandbox_dir = workspace.join(".gha");
        if !sandbox_dir.exists() {
            let _ = fs::create_dir_all(&sandbox_dir);
            let _ = fs::create_dir_all(sandbox_dir.join("models"));
            let _ = fs::create_dir_all(sandbox_dir.join("build"));
        }
        sandbox_dir
    }

    pub fn is_sandbox_active(workspace: &Path) -> bool {
        workspace.join(".gha").is_dir()
    }

    pub fn clean_sandbox(workspace: &Path) -> bool {
        let sandbox_build = workspace.join(".gha/build");
        if sandbox_build.exists() {
            let _ = fs::remove_dir_all(&sandbox_build);
            true
        } else {
            false
        }
    }
}
