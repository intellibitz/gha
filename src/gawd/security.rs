// 🔒 GHA Security & Violation Detector
// 100% Rust implementation for detecting credential leaks and exfiltration

pub struct SecurityDetector;

impl SecurityDetector {
    pub fn audit_action(_tool_name: &str, arg: &str) -> Result<(), String> {
        let secret_patterns = vec![
            "sk-", // OpenAI
            "ghp_", // GitHub
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
