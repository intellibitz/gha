// 🛡️ GHA Safety & System Destruction Detector
// 100% Rust implementation for real-time mission safety auditing

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
