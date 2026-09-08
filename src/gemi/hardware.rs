// 💻 Hardware Profiler: Dynamic CPU Cores & Metal / CUDA Acceleration Detector
// 100% Rust implementation for autonomous hardware profiling

use std::process::Command;

pub struct HardwareProfiler;

impl HardwareProfiler {
    pub fn profile() -> (usize, String) {
        let cpus = std::thread::available_parallelism()
            .map(|n| n.get())
            .unwrap_or(4);

        let gpu_info = if cfg!(target_os = "macos") {
            let is_arm64 = Command::new("uname")
                .arg("-m")
                .output()
                .ok()
                .map(|o| String::from_utf8_lossy(&o.stdout).contains("arm64"))
                .unwrap_or(false);

            if is_arm64 {
                "Apple Silicon Metal Unified Memory Acceleration Active (-ngl 99)".to_string()
            } else {
                "macOS Metal GPU Acceleration Active".to_string()
            }
        } else if Command::new("nvidia-smi").output().is_ok() {
            "NVIDIA CUDA GPU Offload Active (-ngl 99)".to_string()
        } else if Command::new("rocm-smi").output().is_ok() {
            "AMD ROCm GPU Offload Active (-ngl 99)".to_string()
        } else {
            format!("High-Throughput SIMD CPU Parallel Execution ({} Threads)", cpus)
        };

        (cpus, gpu_info)
    }

    pub fn determine_total_ram_gb() -> usize {
        if cfg!(target_os = "linux") {
            if let Ok(content) = std::fs::read_to_string("/proc/meminfo") {
                for line in content.lines() {
                    if line.starts_with("MemTotal:") {
                        let parts: Vec<&str> = line.split_whitespace().collect();
                        if let Some(kb_str) = parts.get(1)
                            && let Ok(kb) = kb_str.parse::<usize>()
                        {
                            return kb / (1024 * 1024);
                        }
                    }
                }
            }
        } else if cfg!(target_os = "macos")
            && let Ok(out) = Command::new("sysctl").arg("-n").arg("hw.memsize").output()
            && let Ok(bytes_str) = String::from_utf8(out.stdout)
            && let Ok(bytes) = bytes_str.trim().parse::<usize>()
        {
            return bytes / (1024 * 1024 * 1024);
        }
        16 // Conservative fallback
    }

    pub fn get_progressive_model_ladder() -> Vec<ModelLadderStep> {
        let ram_gb = Self::determine_total_ram_gb();
        let mut ladder = vec![
            ModelLadderStep {
                step: 1,
                label: "1.5B Parameters (Fast Local Edge)",
                hf_repo: "Qwen/Qwen2.5-1.5B-Instruct-GGUF",
            },
        ];

        if ram_gb >= 8 {
            ladder.push(ModelLadderStep {
                step: 2,
                label: "7B Parameters (Mid-Range Desktop)",
                hf_repo: "Qwen/Qwen2.5-7B-Instruct-GGUF",
            });
        }
        if ram_gb >= 16 {
            ladder.push(ModelLadderStep {
                step: 3,
                label: "14B Parameters (High-Accuracy Workstation)",
                hf_repo: "Qwen/Qwen2.5-14B-Instruct-GGUF",
            });
        }
        if ram_gb >= 32 {
            ladder.push(ModelLadderStep {
                step: 4,
                label: "32B Parameters (High-End Workstation)",
                hf_repo: "Qwen/Qwen2.5-32B-Instruct-GGUF",
            });
        }
        if ram_gb >= 64 {
            ladder.push(ModelLadderStep {
                step: 5,
                label: "72B Parameters (Ultra-Capacity Workstation)",
                hf_repo: "Qwen/Qwen2.5-72B-Instruct-GGUF",
            });
        }

        ladder
    }

    #[allow(dead_code)]
    pub fn determine_max_model_capacity() -> HardwareCapacity {
        let ram_gb = Self::determine_total_ram_gb();

        if ram_gb >= 64 {
            HardwareCapacity {
                ram_gb,
                recommended_hf_repo: "Qwen/Qwen2.5-72B-Instruct-GGUF",
                recommended_file: "qwen2.5-72b-instruct-q4_k_m.gguf",
                model_size_label: "72B Parameters (Ultra-Workstation Capacity)",
            }
        } else if ram_gb >= 32 {
            HardwareCapacity {
                ram_gb,
                recommended_hf_repo: "Qwen/Qwen2.5-32B-Instruct-GGUF",
                recommended_file: "qwen2.5-32b-instruct-q4_k_m.gguf",
                model_size_label: "32B Parameters (High-End Workstation Capacity)",
            }
        } else if ram_gb >= 16 {
            HardwareCapacity {
                ram_gb,
                recommended_hf_repo: "Qwen/Qwen2.5-14B-Instruct-GGUF",
                recommended_file: "qwen2.5-14b-instruct-q4_k_m.gguf",
                model_size_label: "14B Parameters (Desktop/Laptop Capacity)",
            }
        } else if ram_gb >= 8 {
            HardwareCapacity {
                ram_gb,
                recommended_hf_repo: "Qwen/Qwen2.5-7B-Instruct-GGUF",
                recommended_file: "qwen2.5-7b-instruct-q4_k_m.gguf",
                model_size_label: "7B Parameters (Mid-Range Hardware Capacity)",
            }
        } else {
            HardwareCapacity {
                ram_gb,
                recommended_hf_repo: "Qwen/Qwen2.5-1.5B-Instruct-GGUF",
                recommended_file: "qwen2.5-1.5b-instruct-q4_k_m.gguf",
                model_size_label: "1.5B Parameters (Embedded/Edge Hardware Capacity)",
            }
        }
    }
}

#[allow(dead_code)]
#[derive(Debug, Clone)]
pub struct ModelLadderStep {
    pub step: usize,
    pub label: &'static str,
    pub hf_repo: &'static str,
}

#[allow(dead_code)]
#[derive(Debug, Clone)]
pub struct HardwareCapacity {
    pub ram_gb: usize,
    pub recommended_hf_repo: &'static str,
    pub recommended_file: &'static str,
    pub model_size_label: &'static str,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_hardware_profiler() {
        let (cpus, gpu_info) = HardwareProfiler::profile();
        assert!(cpus > 0);
        assert!(!gpu_info.is_empty());
    }
}
