// 🚀 Always-On GMA Master Daemon Process Manager
// 100% Rust implementation for daemon background execution & lock handling

use std::fs;
use std::path::{Path, PathBuf};

pub struct GmaDaemon;

impl GmaDaemon {
    pub fn get_lock_file(global_dir: &Path) -> PathBuf {
        global_dir.join("gma.lock")
    }

    pub fn check_status(global_dir: &Path) -> Option<u32> {
        let lock_file = Self::get_lock_file(global_dir);
        if let Ok(content) = fs::read_to_string(&lock_file) {
            if let Ok(pid) = content.trim().parse::<u32>() {
                let proc_path = PathBuf::from(format!("/proc/{}", pid));
                if proc_path.exists() {
                    return Some(pid);
                }
            }
        }
        None
    }

    pub fn start_daemon(global_dir: &Path) -> Result<u32, String> {
        let pid = std::process::id();
        let lock_file = Self::get_lock_file(global_dir);
        if let Err(e) = fs::write(&lock_file, pid.to_string()) {
            return Err(format!("Failed to write lock file: {}", e));
        }
        Ok(pid)
    }

    #[allow(dead_code)]
    pub fn stop_daemon(global_dir: &Path) -> bool {
        let lock_file = Self::get_lock_file(global_dir);
        if lock_file.exists() {
            let _ = fs::remove_file(lock_file);
            true
        } else {
            false
        }
    }
}
