// GHA Alpha-Self: Compiled Immutable Axiomatic Core
// Baked directly into the binary via include_str! for zero-latency self-awareness across all GHA components.

pub struct AlphaSelf;

impl AlphaSelf {
    pub const AGENTS_AXIOM: &str = include_str!("../../.agents/AGENTS.md");
    pub const PROJECTS_AXIOM: &str = include_str!("../../.agents/PROJECTS.md");

    pub fn get_agents_axiom() -> &'static str {
        Self::AGENTS_AXIOM
    }

    pub fn get_projects_axiom() -> &'static str {
        Self::PROJECTS_AXIOM
    }

    pub fn inspect_self() -> String {
        format!(
            "GHA Alpha-Self (Compiled Immutable Core):\n- AGENTS.md Size: {} bytes\n- PROJECTS.md Size: {} bytes\n- Status: Universally Shared Across All GHA Components",
            Self::AGENTS_AXIOM.len(),
            Self::PROJECTS_AXIOM.len()
        )
    }
}
