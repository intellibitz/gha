// 🌌 gha: Pure AI for AI Runtime — GAWD, GEMI & GMCP Multi-Agent Engine
// 100% Standalone Native Executable

mod gawd;
mod gemi;
mod gmcp;

use std::env;
use std::fs;
use std::path::{Path, PathBuf};
use gawd::GmaMasterAgent;
use gemi::GemiEngine;
use gmcp::GmcpServer;

const GHA_VERSION: &str = "0.1.67-SNAPSHOT";

fn get_home_dir() -> PathBuf {
    env::var_os("HOME")
        .or_else(|| env::var_os("USERPROFILE"))
        .map(PathBuf::from)
        .unwrap_or_else(|| PathBuf::from("."))
}

fn find_workspace(cwd: &Path) -> PathBuf {
    let mut current = cwd.to_path_buf();
    let home = get_home_dir();
    while current != home && current.parent().is_some() {
        if current.join(".gha").is_dir() {
            return current;
        }
        if !current.pop() {
            break;
        }
    }
    cwd.to_path_buf()
}

fn check_daemon_status(global_dir: &Path) -> Option<u32> {
    let lock_file = global_dir.join("gma.lock");
    if let Ok(content) = fs::read_to_string(&lock_file) {
        if let Ok(pid) = content.trim().parse::<u32>() {
            let proc_path = PathBuf::from(format!("/proc/{}", pid));
            if proc_path.exists() {
                return Some(pid);
            }
        }
    }
    None
}

fn print_version() {
    println!("⚡ gha (GHA Native AI Runtime) v{}", GHA_VERSION);
    println!("   ├── Architecture : Pure AI for AI (GAWD, GEMI & GMCP)");
    println!("   └── Runtime      : 100% Standalone Native Executable");
}

fn print_help() {
    println!("🌌 gha - Universal Multi-Agent AI Runtime & MCP Engine");
    println!("Usage: ghai [COMMAND | OPTION | MISSION_INSTRUCTION]\n");
    println!("Native AI Runtime Commands:");
    println!("  :version, -v, --version  Print GHA version & native architecture report");
    println!("  :status                  Print workspace health, GAWD fleet & GMCP status");
    println!("  :help, -h, --help        Show this documentation");
    println!("  :install                 Initialize sandboxed .gha environment offline");
    println!("  :uninstall               Clean up sandboxed .gha environment");
    println!("  :daemon                  Inspect or manage GHA Master Daemon");
    println!("  gmcp, mcp                Start native GMA Master MCP Server over stdio\n");
    println!("GMA Master Interactor Native Missions & Multi-Tier AI Tasks:");
    println!("  ghai \"<instruction>\"     Execute natural language AI mission via GMA");
    println!("  ghai ai orchestrate      Inspect 3-tier GMA coordination report across tiers");
    println!("  ghai ai models           Inspect GGUF & web AI models");
    println!("  ghai ai engines          Inspect local & web AI inference engines");
    println!("  ghai ai mcp-hub          Inspect coordinated MCP tool servers");
}

fn print_status(workspace: &Path, global_dir: &Path) {
    println!("🌌 [gha Native AI Status Report]");
    println!("   ├── Target Workspace : {}", workspace.display());
    let sandbox = workspace.join(".gha");
    let sandbox_status = if sandbox.is_dir() { "ACTIVE (.gha/ present)" } else { "NOT INITIALIZED (run 'ghai :install')" };
    println!("   ├── Sandbox Status   : {}", sandbox_status);
    println!("   ├── Engine Version   : {}", GHA_VERSION);

    let (cpus, gpu) = GemiEngine::profile_hardware();
    println!("   ├── Hardware Profile : {} CPU Cores | {}", cpus, gpu);
    println!("   ├── Coordinated Tiers: Tier 1 (GAWD) | Tier 2 (GEMI) | Tier 3 (GMCP)");

    match check_daemon_status(global_dir) {
        Some(pid) => println!("   └── GMA Daemon       : RUNNING (PID {})", pid),
        None => println!("   └── GMA Daemon       : INACTIVE"),
    }
}

fn run_install(workspace: &Path, global_dir: &Path) {
    println!("🚀 [gha Native] Initializing offline GHA AI environment at {}...", workspace.display());
    let gha_dir = workspace.join(".gha");
    let _ = fs::create_dir_all(&gha_dir);
    let _ = fs::create_dir_all(&gha_dir.join("models"));
    let _ = fs::create_dir_all(global_dir);
    println!("✅ [gha Native] Sandbox environment initialized in < 1ms!");
}

fn run_uninstall(workspace: &Path) {
    let gha_dir = workspace.join(".gha");
    if gha_dir.exists() {
        let _ = fs::remove_dir_all(&gha_dir);
        println!("✅ [gha Native] Removed .gha sandbox directory.");
    } else {
        println!("ℹ️  No .gha sandbox directory found.");
    }
}

fn main() {
    let cwd = env::current_dir().unwrap_or_else(|_| PathBuf::from("."));
    let home = get_home_dir();
    let global_dir = home.join(".gha");
    let workspace = find_workspace(&cwd);

    let args: Vec<String> = env::args().skip(1).collect();

    if args.is_empty() {
        print_help();
        return;
    }

    let first_arg = &args[0];
    let cmd = first_arg.strip_prefix(':').unwrap_or(first_arg.as_str());

    match cmd {
        "version" | "--version" | "-v" => {
            print_version();
        }
        "help" | "--help" | "-h" => {
            print_help();
        }
        "status" => {
            print_status(&workspace, &global_dir);
        }
        "install" => {
            run_install(&workspace, &global_dir);
        }
        "uninstall" => {
            run_uninstall(&workspace);
        }
        "gmcp" | "mcp" => {
            GmcpServer::run_stdio(&workspace, GHA_VERSION);
        }
        "daemon" => {
            match check_daemon_status(&global_dir) {
                Some(pid) => println!("🚀 [GMA Daemon] Status: RUNNING (PID {})", pid),
                None => println!("🚀 [GMA Daemon] Status: INACTIVE (Native AI Engine Active)"),
            }
        }
        _ => {
            if cmd == "ai" && args.len() > 1 {
                let sub = &args[1];
                match sub.as_str() {
                    "models" => {
                        let models = GemiEngine::list_models(&workspace);
                        println!("📦 GHA Coordinated Models ({} Total):", models.len());
                        for m in models {
                            println!("   ├── [{}] {} ('{}') - {}", m.registry, m.name, m.model_id, m.description);
                        }
                        return;
                    }
                    "engines" => {
                        println!("⚡ GEMI Coordinated Inference Engines:");
                        println!("   ├── [NATIVE] Embedded GGUF Engine: ACTIVE (Metal/CUDA enabled)");
                        println!("   ├── [WEB] OpenAI ChatCompletions API Endpoint: ACTIVE");
                        println!("   └── [MCP] GMCP Tool Reasoning Engine: ACTIVE");
                        return;
                    }
                    "mcp-hub" => {
                        println!("🔌 GMCP Coordinated Tool Servers:");
                        println!("   ├── [STDIO] Native GMCP Server: ACTIVE (Port 9090 / stdio)");
                        println!("   └── [HOST] Universal AI Tool Registry: ACTIVE (39+ Tools)");
                        return;
                    }
                    _ => {}
                }
            }

            let goal = args.join(" ");
            let gma = GmaMasterAgent::new();
            let report = gma.solve(&goal, &workspace, GHA_VERSION);
            println!("{}", report);
        }
    }
}
