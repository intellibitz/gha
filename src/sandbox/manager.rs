// 🏠 GHA Sandbox Manager & Security Engine
// 100% Rust implementation of workspace sandboxing, versioning & health verification

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

    pub fn bump_version(workspace: &Path) -> String {
        let version_file = workspace.join("version.txt");
        let current = if version_file.is_file() {
            fs::read_to_string(&version_file).unwrap_or_else(|_| "0.1.68".to_string()).trim().to_string()
        } else {
            "0.1.68".to_string()
        };

        let current_base = current.split('-').next().unwrap_or("0.1.68");
        let parts: Vec<&str> = current_base.split('.').collect();
        let new_version = if parts.len() == 3 {
            let patch: u32 = parts[2].parse().unwrap_or(0) + 1;
            format!("{}.{}.{}", parts[0], parts[1], patch)
        } else {
            "0.1.69".to_string()
        };

        // Write version.txt
        let _ = fs::write(&version_file, format!("{}\n", new_version));

        // Update Cargo.toml
        let cargo_toml = workspace.join("Cargo.toml");
        if cargo_toml.is_file() {
            if let Ok(content) = fs::read_to_string(&cargo_toml) {
                let updated = content.replace(&format!("version = \"{}\"", current), &format!("version = \"{}\"", new_version));
                let _ = fs::write(&cargo_toml, updated);
            }
        }

        // Update src/main.rs
        let main_rs = workspace.join("src/main.rs");
        if main_rs.is_file() {
            if let Ok(content) = fs::read_to_string(&main_rs) {
                let updated = content.replace(&format!("const GHA_VERSION: &str = \"{}\";", current), &format!("const GHA_VERSION: &str = \"{}\";", new_version));
                let _ = fs::write(&main_rs, updated);
            }
        }

        println!("⚡ [GHA Version Bumper] Version bumped: v{} ➔ v{}", current, new_version);
        new_version
    }
}
