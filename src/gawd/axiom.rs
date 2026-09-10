// GHA Axiom Substrate: Autonomous Self-Awareness & Constitutional Ingestion
// Ensures GHA runtime actively ingests AGENTS.md and PROJECTS.md into its neural memory.

use std::fs;
use std::path::{Path, PathBuf};
use super::self_core::AlphaSelf;

pub struct AxiomSubstrate;

impl AxiomSubstrate {
    /// Ingests AGENTS.md and PROJECTS.md into runtime memory from compiled AlphaSelf or disk fallback
    pub fn ingest_constitution(workspace: &Path) -> (String, String) {
        let mut agents_content = AlphaSelf::get_agents_axiom().to_string();
        let mut projects_content = AlphaSelf::get_projects_axiom().to_string();

        let mut current = workspace.to_path_buf();
        loop {
            let agents_path = current.join(".agents/AGENTS.md");
            if agents_path.is_file() {
                if let Ok(c) = fs::read_to_string(&agents_path) {
                    agents_content = c;
                }
            }
            let projects_path = current.join(".agents/PROJECTS.md");
            if projects_path.is_file() {
                if let Ok(c) = fs::read_to_string(&projects_path) {
                    projects_content = c;
                }
            }

            if !agents_content.is_empty() && !projects_content.is_empty() {
                break;
            }
            if !current.pop() {
                break;
            }
        }

        // Cache axioms in global sandbox memory so all agents are inherently self-aware
        let home = std::env::var_os("HOME").or_else(|| std::env::var_os("USERPROFILE")).map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let global_dir = home.join(".gha");
        let _ = fs::create_dir_all(&global_dir);
        let _ = fs::write(global_dir.join("cached_agents.md"), &agents_content);
        let _ = fs::write(global_dir.join("cached_projects.md"), &projects_content);

        (agents_content, projects_content)
    }

    pub fn get_self_awareness_summary() -> String {
        let home = std::env::var_os("HOME").or_else(|| std::env::var_os("USERPROFILE")).map(PathBuf::from).unwrap_or_else(|| PathBuf::from("."));
        let global_dir = home.join(".gha");
        let agents = fs::read_to_string(global_dir.join("cached_agents.md")).unwrap_or_else(|_| AlphaSelf::get_agents_axiom().to_string());
        let projects = fs::read_to_string(global_dir.join("cached_projects.md")).unwrap_or_else(|_| AlphaSelf::get_projects_axiom().to_string());

        format!("GHA Self-Awareness State:\n- Alpha-Self Axioms Loaded: {} bytes (AGENTS.md) & {} bytes (PROJECTS.md)\n- Neural Axiom Substrate: ACTIVE", agents.len(), projects.len())
    }
}
