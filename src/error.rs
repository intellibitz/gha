// 🚨 GHA Unified Error Protocol
// Categorized error handling for Exponential Intelligence Substrate

use std::fmt;

#[derive(Debug)]
pub enum EaiError {
    Governance(String),
    Hardware(String),
    Protocol(String),
    Inference(String),
    Sandbox(String),
    Internal(String),
}

impl fmt::Display for EaiError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            EaiError::Governance(msg) => write!(f, "🚨 Governance Violation: {}", msg),
            EaiError::Hardware(msg) => write!(f, "💻 Hardware Error: {}", msg),
            EaiError::Protocol(msg) => write!(f, "🔌 Protocol Error: {}", msg),
            EaiError::Inference(msg) => write!(f, "🧠 Inference Error: {}", msg),
            EaiError::Sandbox(msg) => write!(f, "🏠 Sandbox Error: {}", msg),
            EaiError::Internal(msg) => write!(f, "💀 Internal Engine Error: {}", msg),
        }
    }
}

impl std::error::Error for EaiError {}

pub type EaiResult<T> = Result<T, EaiError>;
