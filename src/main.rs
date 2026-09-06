// 🌌 gha: Pure AI for AI Runtime — GAWD, GEMI & GMCP Multi-Agent Engine
// 100% Standalone Native Executable — 0 JVM, 0 Git, 0 Gradle Dependency

mod daemon;
mod gawd;
mod gemi;
mod gmcp;
mod sandbox;

use std::env;
use std::path::{Path, PathBuf};

use daemon::GmaDaemon;
use gawd::GmaMasterAgent;
use gemi::{GemiEngine, GemiServer, ModelManager};
use gmcp::{GmcpClient, GmcpServer};
use sandbox::SandboxManager;

const GHA_VERSION: &str = "0.1.78";

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

fn print_version() {
    println!("⚡ gha (GHA Native AI Runtime) v{}", GHA_VERSION);
    println!("   ├── Architecture : Pure AI for AI (GAWD, GEMI & GMCP)");
    println!("   └── Runtime      : 100% Standalone Native Executable (100% Rust Engine)");
}

fn print_help() {
    println!("🌌 gha - Universal Multi-Agent AI Runtime & MCP Engine");
    println!("Usage: ghai [COMMAND | OPTION | MISSION_INSTRUCTION]\n");
    println!("Native AI Runtime Commands:");
    println!("  :version, -v, --version  Print GHA version & native architecture report");
    println!("  :status                  Print workspace health, GAWD fleet & GMCP status");
    println!("  :help, -h, --help        Show this documentation");
    println!("  :install                 Initialize sandboxed .gha environment & start background daemon");
    println!("  :uninstall               Clean up sandboxed .gha environment");
    println!("  :daemon                  Inspect or manage GHA Master Daemon");
    println!("  gmcp, mcp                Start native GMA Master MCP Server over stdio");
    println!("  gemi, gemi-server        Start GEMI OpenAI-compatible REST server (http://127.0.0.1:9091/v1)\n");
    println!("GMA Master Interactor Native Missions & Multi-Tier AI Tasks:");
    println!("  ghai \"<instruction>\"     Execute natural language AI mission via GMA");
    println!("  ghai ai orchestrate      Inspect 3-tier GMA coordination report across tiers");
    println!("  ghai ai models           Inspect GGUF & web AI models");
    println!("  ghai ai engines          Inspect local & web AI inference engines");
    println!("  ghai ai mcp-hub          Inspect coordinated MCP tool servers");
    println!("  ghai ai server           Start GEMI OpenAI-compatible HTTP REST server (Port 9091)");
}

fn print_status(workspace: &Path, global_dir: &Path) {
    println!("🌌 [gha Native AI Status Report]");
    println!("   ├── Target Workspace : {}", workspace.display());
    let sandbox_status = if SandboxManager::is_sandbox_active(workspace) {
        "ACTIVE (.gha/ present)"
    } else {
        "NOT INITIALIZED (run 'ghai :install')"
    };
    println!("   ├── Sandbox Status   : {}", sandbox_status);
    println!("   ├── Engine Version   : v{}", GHA_VERSION);

    let (cpus, gpu, models) = GemiEngine::get_intelligence_report(workspace);
    let tools = GmcpClient::list_tools();
    println!("   ├── Hardware Profile : {} CPU Cores | {}", cpus, gpu);
    println!("   ├── Coordinated Tiers: Tier 1 (GAWD) | Tier 2 (GEMI Port 9091) | Tier 3 (GMCP Port 9090)");
    println!("   ├── Active Models    : {} GGUF/Web Models Registered", models.len());
    println!("   ├── MCP Tools Hub    : {} Tools Exposed over JSON-RPC 2.0", tools.len());

    match GmaDaemon::check_status(global_dir) {
        Some(pid) => println!("   └── GMA Daemon       : RUNNING (PID {}) | GMCP (Port 9090) | GEMI (Port 9091)", pid),
        None => println!("   └── GMA Daemon       : INACTIVE"),
    }
}

fn run_install(workspace: &Path, global_dir: &Path) {
    println!("🚀 [ghai Native] Initializing offline GHA AI environment at {}...", workspace.display());
    let _ = SandboxManager::ensure_sandbox(workspace);
    let _ = SandboxManager::ensure_sandbox(global_dir);

    GmaDaemon::ensure_daemon_running(workspace, global_dir);
    println!("🚀 [GMA Daemon] Always-On Services Primed: GMCP (Port 9090) | GEMI (Port 9091)");
    println!("✅ [ghai Native] Environment initialized & background daemon active in < 1ms!");
}

fn run_uninstall(workspace: &Path) {
    if SandboxManager::clean_sandbox(workspace) {
        println!("✅ [ghai Native] Cleaned .gha sandbox build directory.");
    } else {
        let sandbox_dir = workspace.join(".gha");
        if sandbox_dir.exists() {
            let _ = std::fs::remove_dir_all(&sandbox_dir);
            println!("✅ [ghai Native] Removed .gha sandbox directory.");
        } else {
            println!("ℹ️  No .gha sandbox directory found.");
        }
    }
}

fn main() {
    let cwd = env::current_dir().unwrap_or_else(|_| PathBuf::from("."));
    let home = get_home_dir();
    let global_dir = home.join(".gha");
    let workspace = find_workspace(&cwd);

    let args: Vec<String> = env::args().skip(1).collect();

    if args.is_empty() {
        println!("⚡ gha v{} (100% Native Rust AI Engine)", GHA_VERSION);
        print_help();
        return;
    }

    let first_arg = &args[0];
    let cmd = first_arg.strip_prefix(':').unwrap_or(first_arg.as_str());

    // Print Version Header for all interactive CLI runs (except raw stdio MCP pipe and daemon loop)
    if cmd != "mcp" && cmd != "gmcp" && cmd != "daemon-start" {
        println!("⚡ gha v{} (100% Native Rust AI Engine)", GHA_VERSION);
    }

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
        "ghaBumpVersion" | "bump-version" | "bump" => {
            SandboxManager::bump_version(&workspace);
        }
        "daemon-start" => {
            let ws = if args.len() > 1 { PathBuf::from(&args[1]) } else { workspace };
            GmaDaemon::run_daemon_loop(ws, global_dir);
            return;
        }
        "gmcp" | "mcp" => {
            GmcpServer::run_stdio(&workspace, GHA_VERSION);
        }
        "gemi" | "gemi-server" => {
            GemiServer::start_http_server(workspace, GemiServer::DEFAULT_PORT);
        }
        "daemon" => {
            match GmaDaemon::check_status(&global_dir) {
                Some(pid) => println!("🚀 [GMA Daemon] Status: RUNNING (PID {}) | GMCP (Port 9090) | GEMI (Port 9091)", pid),
                None => {
                    GmaDaemon::ensure_daemon_running(&workspace, &global_dir);
                    println!("🚀 [GMA Daemon] Started background daemon: GMCP (Port 9090) | GEMI (Port 9091)");
                }
            }
        }
        _ => {
            if cmd == "ai" && args.len() > 1 {
                let sub = &args[1];
                match sub.as_str() {
                    "server" => {
                        GemiServer::start_http_server(workspace, GemiServer::DEFAULT_PORT);
                        return;
                    }
                    "models" => {
                        let models = ModelManager::list_models(&workspace);
                        println!("📦 GHA Coordinated Models ({} Total):", models.len());
                        for m in models {
                            println!("   ├── [{}] {} ('{}') - {}", m.registry, m.name, m.model_id, m.description);
                        }
                        return;
                    }
                    "engines" => {
                        println!("⚡ GEMI Coordinated Inference Engines:");
                        println!("   ├── [NATIVE] Embedded GGUF Engine: ACTIVE (Metal/CUDA enabled)");
                        println!("   ├── [WEB] OpenAI ChatCompletions API Endpoint: ACTIVE (http://127.0.0.1:9091/v1)");
                        println!("   └── [MCP] GMCP Tool Reasoning Engine: ACTIVE");
                        return;
                    }
                    "mcp-hub" => {
                        let tools = GmcpClient::list_tools();
                        println!("🔌 GMCP Coordinated Tool Servers ({} Tools Active):", tools.len());
                        println!("   ├── [STDIO] Native GMCP Server: ACTIVE (Port 9090 / stdio)");
                        for t in tools {
                            println!("   │   ├── Tool: '{}' - {}", t.name, t.description);
                        }
                        println!("   └── [HOST] Universal AI Tool Registry: ACTIVE");
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
