// 🛡️ GHA Safety & System Destruction Detector
// 100% Rust implementation for real-time mission safety auditing
// RULE 4: Reality Check Always On
// RULE 11: Native Integration Enforcement

pub struct SafetyDetector;

impl SafetyDetector {
    pub fn audit_action(tool_name: &str, arg: &str) -> Result<(), String> {
        let destructive_patterns = vec![
            "rm -rf /",
            "rm -rf $HOME",
            "rm -rf ~",
            "mkfs",
            "dd if=",
            "> /dev/sda",
            ":(){ :|:& };:", // Fork bomb
            "chmod -R 777 /",
            "chown -R",
            "shred",
        ];

        let critical_paths = vec![
            "/etc/passwd",
            "/etc/shadow",
            "/boot",
            "/proc",
            "/sys",
            "/dev",
        ];

        let lower_arg = arg.to_lowercase();

        // 1. Command Pattern Check
        for pattern in destructive_patterns {
            if lower_arg.contains(pattern) {
                return Err(format!("🚨 DESTRUCTION DETECTED: Action contains restricted pattern '{}'", pattern));
            }
        }

        // 2. Critical Path Check
        if tool_name == "write_file" || tool_name == "exec_command" {
            for path in critical_paths {
                if lower_arg.contains(path) {
                    return Err(format!("🚨 DESTRUCTION DETECTED: Action targets critical system path '{}'", path));
                }
            }
        }

        Ok(())
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_safety_audit_safe_commands() {
        assert!(SafetyDetector::audit_action("exec_command", "cargo check").is_ok());
        assert!(SafetyDetector::audit_action("write_file", "src/main.rs println!(\"hello\");").is_ok());
    }

    #[test]
    fn test_safety_audit_destructive_patterns() {
        assert!(SafetyDetector::audit_action("exec_command", "rm -rf /").is_err());
        assert!(SafetyDetector::audit_action("exec_command", "mkfs.ext4 /dev/sda1").is_err());
    }

    #[test]
    fn test_safety_audit_critical_paths() {
        assert!(SafetyDetector::audit_action("write_file", "/etc/passwd").is_err());
        assert!(SafetyDetector::audit_action("exec_command", "cat /etc/shadow").is_err());
    }
}
