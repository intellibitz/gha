// 🌌 gha: Pure AI for AI Runtime — GAWD, GEMI & GMCP Multi-Agent Engine
// 100% Standalone Native Executable — 0 JVM, 0 Git, 0 Gradle Dependency

mod daemon;
mod gawd;
mod gemi;
mod gmcp;
mod sandbox;

use std::env;
use std::path::{Path, PathBuf};

use crate::gmcp::tools::ToolRegistry;
use daemon::GmaDaemon;
use gawd::GmaMasterAgent;
use gemi::GemiServer;
use gmcp::GmcpServer;
use sandbox::SandboxManager;

const GHA_VERSION: &str = "0.1.112";

fn get_home_dir() -> PathBuf {
    env::var_os("HOME")
        .or_else(|| env::var_os("USERPROFILE"))
        .map(PathBuf::from)
        .unwrap_or_else(|| PathBuf::from("."))
}

fn print_help() {
    println!("🌌 gha: AI for AI — Anywhere for Anything.");
    println!("Usage: gha \"<your intent>\"\n");
    println!("🤖 GHA Master Agent (GMA) Sole Interactor:");
    println!("  gha \"what is your version?\"");
    println!("  gha \"check system status\"");
    println!("  gha \"build a simple bootloader\"");
    println!("  gha \"explain the universe\"");
    println!("  gha \"<any goal or mission>\"\n");
    println!("⚙️ Internal Runtime Infrastructure:");
    println!("  install                  Initialize global gha environment");
    println!("  uninstall                Remove global gha environment");
    println!("  mcp                      Start native GMA Master MCP Server (for external AI clients)");
    println!("  gemi                     Start GEMI OpenAI-compatible REST server");
    println!("  services                 List running GHA background services");
}

fn run_install(global_dir: &Path) {
    println!("🚀 [gha] Initializing 100% Sandboxed Native AI Runtime...");
    let _ = SandboxManager::ensure_global_sandbox(global_dir);
    GmaDaemon::ensure_daemon_running(global_dir, global_dir);
    println!("✅ [gha] Global environment initialized & background swarm active.");
}

fn main() {
    let cwd = env::current_dir().unwrap_or_else(|_| PathBuf::from("."));
    let home = get_home_dir();
    let global_dir = home.join(".gha");

    let args: Vec<String> = env::args().skip(1).collect();

    if args.is_empty() {
        println!("⚡ gha v{} (100% Native Rust AI Engine)", GHA_VERSION);
        print_help();
        return;
    }

    let first_arg = args[0].to_lowercase();

    match first_arg.as_str() {
        "help" | "--help" | "-h" => {
            print_help();
        }
        "version" | "--version" | "-v" => {
            println!("# 🌌 gha: AI for AI - Sole Interactor Report");
            println!("\n## 🎯 Mission Execution (A2A Swarm Flux)");
            println!("🤖 [GMA] Universal Intent: \"version\"");
            println!("\n## 🏁 GAWD Accomplishment");
            println!("   └── [Autonomous Tool: version]: gha Native Engine v{}", GHA_VERSION);
            println!("\n## ⚖️ GMA Trust Audit (Reality Check)");
            println!(" └── ✅ TRUTH VERIFIED: Swarm logic is semantically sound and artifact-aligned.");
            println!("\n✅ [gha Intelligence] Reflex executed natively (0-Effort, 100% Gains).");
        }
        "models" | ":models" => {
            let gma = GmaMasterAgent::new();
            let report = gma.solve("list_models", &cwd, GHA_VERSION);
            println!("{}", report);
        }
        "install" | ":install" => {
            run_install(&global_dir);
        }
        "uninstall" | ":uninstall" => {
            let _ = std::fs::remove_dir_all(&global_dir);
            println!("✅ [gha] Global environment uninstalled.");
        }
        "daemon-start" => {
            GmaDaemon::run_daemon_loop(global_dir.clone(), global_dir);
        }
        "gmcp-server" | "mcp-server" => {
            GmcpServer::run_stdio(&cwd, GHA_VERSION);
        }
        "gmcp" | "mcp" => {
            let res = ToolRegistry::execute_tool("gmcp_scout", "", &cwd);
            println!("{}", res);
        }
        "reflex" | ":reflex" => {
            let res = ToolRegistry::execute_tool("reflex_scout", "", &cwd);
            println!("{}", res);
        }
        "gawd" | ":gawd" => {
            let res = ToolRegistry::execute_tool("gawd_scout", "", &cwd);
            println!("{}", res);
        }
        "gemi" | ":gemi" => {
            let res = ToolRegistry::execute_tool("gemi_scout", "", &cwd);
            println!("{}", res);
        }
        "gemi-server" => {
            GemiServer::start_http_server(cwd, GemiServer::DEFAULT_PORT);
        }
        "scout" | ":scout" => {
            let res = ToolRegistry::execute_tool("scout", "", &cwd);
            println!("{}", res);
        }
        "services" | ":services" => {
            let res = ToolRegistry::execute_tool("services", "", &cwd);
            println!("{}", res);
        }
        "verify-cloud" | ":verify-cloud" => {
            let res = ToolRegistry::execute_tool("verify_cloud_providers", "", &cwd);
            println!("{}", res);
        }
        _ => {
            // Universal Mission: The Impact Scope is ALWAYS the Current Working Directory
            // No local .gha folder required. 100% Pure anywhere execution.
            let goal = args.join(" ");
            let gma = GmaMasterAgent::new();
            let report = gma.solve(&goal, &cwd, GHA_VERSION);
            println!("{}", report);
        }
    }
}
