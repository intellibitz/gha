// Hardware Profiler: Dynamic CPU Cores & Metal / CUDA Acceleration Detector
// 100% Rust implementation for autonomous hardware profiling

use std::process::Command;
use serde::{Deserialize, Serialize};
use candle_core::Device;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct HardwareProfile {
    pub cpus: usize,
    pub gpu_info: String,
    pub ram_gb: usize,
    pub acceleration_active: bool,
    pub native_acceleration: String,
    pub os_info: String,
    pub disk_gb: usize,
}

pub struct HardwareProfiler;

impl HardwareProfiler {
    pub fn get_profile() -> HardwareProfile {
        let (cpus, _) = Self::profile();
        let ram_gb = Self::determine_total_ram_gb();

        // 1. Direct Interrogation via Candle Substrate
        let (native_accel, gpu_name) = Self::interrogate_native_acceleration();

        let acceleration_active = !native_accel.contains("None") && !native_accel.contains("Cpu");
        let gpu_display = if acceleration_active {
            format!("{} ({})", native_accel, gpu_name)
        } else {
            // 2. Fallback to Shell-Parsing for diagnostics if native probe is inactive
            let (_, shell_gpu) = Self::profile();
            shell_gpu
        };

        HardwareProfile {
            cpus,
            gpu_info: gpu_display,
            ram_gb,
            acceleration_active,
            native_acceleration: native_accel,
            os_info: Self::get_os_info(),
            disk_gb: Self::determine_disk_gb(),
        }
    }

    fn get_os_info() -> String {
        if cfg!(target_os = "linux") {
            if let Ok(out) = Command::new("uname").arg("-sr").output() {
                return String::from_utf8_lossy(&out.stdout).trim().to_string();
            }
        } else if cfg!(target_os = "macos") {
            if let Ok(out) = Command::new("sw_vers").arg("-productVersion").output() {
                return format!("macOS {}", String::from_utf8_lossy(&out.stdout).trim());
            }
        } else if cfg!(target_os = "windows") {
             return "Windows".to_string();
        }
        "Unknown OS".to_string()
    }

    fn determine_disk_gb() -> usize {
        if cfg!(target_os = "linux") || cfg!(target_os = "macos") {
            if let Ok(out) = Command::new("df").arg("-k").arg("/").output() {
                let s = String::from_utf8_lossy(&out.stdout);
                if let Some(line) = s.lines().nth(1) {
                    let parts: Vec<&str> = line.split_whitespace().collect();
                    if let Some(kb_str) = parts.get(1) {
                        if let Ok(kb) = kb_str.parse::<usize>() {
                            return kb / (1024 * 1024);
                        }
                    }
                }
            }
        }
        256 // Fallback
    }

    fn interrogate_native_acceleration() -> (String, String) {
        if let Ok(dev) = Device::new_cuda(0) {
            return ("CUDA".to_string(), format!("{:?}", dev));
        }

        #[cfg(feature = "metal")]
        if let Ok(dev) = Device::new_metal(0) {
            return ("Metal".to_string(), format!("{:?}", dev));
        }

        if candle_core::utils::cuda_is_available() {
             return ("CUDA (Detected)".to_string(), "NVIDIA Driver found".to_string());
        }

        if candle_core::utils::metal_is_available() {
             return ("Metal (Detected)".to_string(), "Apple Silicon / macOS".to_string());
        }

        ("None".to_string(), "Cpu".to_string())
    }

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
        } else if cfg!(target_os = "windows") {
            let out = Command::new("wmic").args(["ComputerSystem", "get", "TotalPhysicalMemory"]).output();
            if let Ok(o) = out {
                let s = String::from_utf8_lossy(&o.stdout);
                for line in s.lines() {
                    let trimmed = line.trim();
                    if !trimmed.is_empty() && trimmed.chars().all(|c| c.is_ascii_digit()) {
                        if let Ok(bytes) = trimmed.parse::<u64>() {
                            return (bytes / (1024 * 1024 * 1024)) as usize;
                        }
                    }
                }
            }
        }
        16 // Conservative fallback
    }

    pub fn get_progressive_model_ladder() -> Vec<ModelLadderStep> {
        let ram_gb = Self::determine_total_ram_gb();
        let mut ladder = vec![
            ModelLadderStep {
                step: 1,
                label: "1.5B Parameters (Fast Local Edge)",
                hf_repo: "gha-alpha/gha-alpha-1.5b-instruct-v0.1-GGUF",
                hf_file: "gha-alpha-1.5b-instruct-q4_k_m.gguf",
            },
        ];

        if ram_gb >= 8 {
            ladder.push(ModelLadderStep {
                step: 2,
                label: "7B Parameters (Mid-Range Desktop)",
                hf_repo: "gha-alpha/gha-alpha-7b-instruct-v0.1-GGUF",
                hf_file: "gha-alpha-7b-instruct-q4_k_m.gguf",
            });
        }
        if ram_gb >= 16 {
            ladder.push(ModelLadderStep {
                step: 3,
                label: "14B Parameters (High-Accuracy Workstation)",
                hf_repo: "gha-alpha/gha-alpha-14b-instruct-v0.1-GGUF",
                hf_file: "gha-alpha-14b-instruct-q4_k_m.gguf",
            });
        }
        if ram_gb >= 32 {
            ladder.push(ModelLadderStep {
                step: 4,
                label: "32B Parameters (High-End Workstation)",
                hf_repo: "gha-alpha/gha-alpha-32b-instruct-v0.1-GGUF",
                hf_file: "gha-alpha-32b-instruct-q4_k_m.gguf",
            });
        }
        if ram_gb >= 64 {
            ladder.push(ModelLadderStep {
                step: 5,
                label: "72B Parameters (Ultra-Capacity Workstation)",
                hf_repo: "gha-alpha/gha-alpha-72b-instruct-v0.1-GGUF",
                hf_file: "gha-alpha-72b-instruct-q4_k_m.gguf",
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
                recommended_hf_repo: "gha-alpha/gha-alpha-72b-instruct-v0.1-GGUF",
                recommended_file: "gha-alpha-72b-instruct-q4_k_m.gguf",
                model_size_label: "72B Parameters (Ultra-Workstation Capacity)",
            }
        } else if ram_gb >= 32 {
            HardwareCapacity {
                ram_gb,
                recommended_hf_repo: "gha-alpha/gha-alpha-32b-instruct-v0.1-GGUF",
                recommended_file: "gha-alpha-32b-instruct-q4_k_m.gguf",
                model_size_label: "32B Parameters (High-End Workstation Capacity)",
            }
        } else if ram_gb >= 16 {
            HardwareCapacity {
                ram_gb,
                recommended_hf_repo: "gha-alpha/gha-alpha-14b-instruct-v0.1-GGUF",
                recommended_file: "gha-alpha-14b-instruct-q4_k_m.gguf",
                model_size_label: "14B Parameters (Desktop/Laptop Capacity)",
            }
        } else if ram_gb >= 8 {
            HardwareCapacity {
                ram_gb,
                recommended_hf_repo: "gha-alpha/gha-alpha-7b-instruct-v0.1-GGUF",
                recommended_file: "gha-alpha-7b-instruct-q4_k_m.gguf",
                model_size_label: "7B Parameters (Mid-Range Hardware Capacity)",
            }
        } else {
            HardwareCapacity {
                ram_gb,
                recommended_hf_repo: "gha-alpha/gha-alpha-1.5b-instruct-v0.1-GGUF",
                recommended_file: "gha-alpha-1.5b-instruct-q4_k_m.gguf",
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
    pub hf_file: &'static str,
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
