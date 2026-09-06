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
}
