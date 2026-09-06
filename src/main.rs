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

fn print_help() {
    println!("🌌 gha: AI for AI — Anywhere for Anything.");
    println!("Usage: ghai \"<your intent>\"\n");
    println!("🤖 GHA Master Agent (GMA) Sole Interactor:");
    println!("  ghai \"what is your version?\"");
    println!("  ghai \"check system status\"");
    println!("  ghai \"build a simple bootloader\"");
    println!("  ghai \"explain the universe\"");
    println!("  ghai \"<any goal or mission>\"\n");
    println!("⚙️ Internal Runtime Infrastructure:");
    println!("  install                  Initialize gha environment");
    println!("  uninstall                Remove gha environment");
    println!("  mcp                      Start native GMA Master MCP Server");
    println!("  gemi                     Start GEMI OpenAI-compatible REST server");
}

fn run_install(workspace: &Path, global_dir: &Path) {
    println!("🚀 [gha] Initializing 100% Sandboxed Native AI Runtime...");
    let _ = SandboxManager::ensure_sandbox(workspace);
    let _ = SandboxManager::ensure_sandbox(global_dir);
    GmaDaemon::ensure_daemon_running(workspace, global_dir);
    println!("✅ [gha] Environment initialized & background swarm active.");
}

fn main() {
    let cwd = env::current_dir().unwrap_or_else(|_| PathBuf::from("."));
    let home = get_home_dir();
    let global_dir = home.join(".gha");

    // Internal workspace for context/config, but mission scope is always CWD
    let workspace = find_workspace(&cwd);

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
        "install" | ":install" => {
            run_install(&cwd, &global_dir);
        }
        "uninstall" | ":uninstall" => {
            let _ = std::fs::remove_dir_all(cwd.join(".gha"));
            println!("✅ [gha] Environment uninstalled.");
        }
        "daemon-start" => {
            GmaDaemon::run_daemon_loop(workspace, global_dir);
        }
        "gmcp" | "mcp" => {
            GmcpServer::run_stdio(&workspace, GHA_VERSION);
        }
        "gemi" | "gemi-server" => {
            GemiServer::start_http_server(workspace, GemiServer::DEFAULT_PORT);
        }
        _ => {
            // Universal Mission: The Impact Scope is ALWAYS the Current Working Directory
            let goal = args.join(" ");
            let gma = GmaMasterAgent::new();
            let report = gma.solve(&goal, &cwd, GHA_VERSION);
            println!("{}", report);
        }
    }
}
