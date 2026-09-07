// gha: Multi-Agent Engine

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
use gmcp::server::GmcpServer;
use sandbox::SandboxManager;

const GHA_VERSION: &str = "0.1.141";

// ANSI Formatting Codes
const COLOR_CYAN: &str = "\x1b[1;36m";
const COLOR_GREEN: &str = "\x1b[1;32m";
const COLOR_DIM: &str = "\x1b[90m";
const COLOR_RESET: &str = "\x1b[0m";
const CLEAR_SCREEN: &str = "\x1b[2J\x1b[1;1H";

fn get_home_dir() -> PathBuf {
    env::var_os("HOME")
        .or_else(|| env::var_os("USERPROFILE"))
        .map(PathBuf::from)
        .unwrap_or_else(|| PathBuf::from("."))
}

fn print_help() {
    println!("gha v{}", GHA_VERSION);
    println!("Usage: gha \"<intent>\"\n");
    println!("Commands / Slash Commands:");
    println!("  /help, :help             Display this help menu");
    println!("  /models, :models         List available cloud and local models");
    println!("  /services, :services     List running background services");
    println!("  /status, :status         Inspect workspace health & hardware status");
    println!("  /clear, :clear           Clear terminal screen");
    println!("  /exit, :exit, exit       Exit interactive console");
    println!("\nSystem Commands:");
    println!("  install                  Initialize global gha runtime");
    println!("  uninstall                Remove global gha runtime");
    println!("  mcp                      Start native MCP server");
    println!("  gemi                     Start GEMI REST server");
}

fn print_tui_dashboard() {
    let (cpus, gpu) = crate::gemi::hardware::HardwareProfiler::profile();
    let gpu_label = if gpu.contains("CUDA") { "GPU Acceleration Active" } else { "CPU Native" };

    println!("{}┌─────────────────────────────────────────────────────────────┐{}", COLOR_CYAN, COLOR_RESET);
    println!("{}│ gha v{} — Guided Multi-Agent Console                    │{}", COLOR_CYAN, GHA_VERSION, COLOR_RESET);
    println!("{}│ Status: ACTIVE | Hardware: {} CPUs | {} │{}", COLOR_CYAN, cpus, gpu_label, COLOR_RESET);
    println!("{}└─────────────────────────────────────────────────────────────┘{}", COLOR_CYAN, COLOR_RESET);
    println!("{}Quick Actions:{}", COLOR_GREEN, COLOR_RESET);
    println!("  [1] Natural Language Mission (Type intent)");
    println!("  [2] List Available Models");
    println!("  [3] System Status & Hardware");
    println!("  [4] Swarm Services");
    println!("  [5] Help Menu");
    println!("  [0] Exit Console");
    println!("\n{}Type a option [0-5] or enter any natural language intent directly:{}\n", COLOR_DIM, COLOR_RESET);
}

fn run_install(global_dir: &Path) {
    println!("Initializing gha runtime...");
    let _ = SandboxManager::ensure_global_sandbox(global_dir);
    GmaDaemon::ensure_daemon_running(global_dir, global_dir);
    println!("gha runtime initialized.");
}

fn run_interactive_shell(cwd: &Path) {
    use std::io::{self, Write};

    print_tui_dashboard();

    let gma = GmaMasterAgent::new();

    loop {
        print!("{}{}gha{}{}>{} ", COLOR_CYAN, "\x1b[1m", COLOR_RESET, COLOR_GREEN, COLOR_RESET);
        if io::stdout().flush().is_err() {
            break;
        }

        let mut input_buffer = String::new();

        loop {
            let mut line = String::new();
            match io::stdin().read_line(&mut line) {
                Ok(0) => return,
                Ok(_) => {
                    let trimmed = line.trim_end();
                    if trimmed.ends_with('\\') {
                        input_buffer.push_str(&trimmed[..trimmed.len() - 1]);
                        input_buffer.push('\n');
                        print!("{}...{} ", COLOR_DIM, COLOR_RESET);
                        let _ = io::stdout().flush();
                        continue;
                    } else {
                        input_buffer.push_str(trimmed);
                        break;
                    }
                }
                Err(_) => return,
            }
        }

        let command = input_buffer.trim();
        if command.is_empty() {
            continue;
        }

        let command_lower = command.to_lowercase();
        match command_lower.as_str() {
            "0" | "/exit" | ":exit" | "/quit" | ":quit" | "exit" | "quit" => {
                println!("{}Exiting console.{}", COLOR_DIM, COLOR_RESET);
                break;
            }
            "1" => {
                print!("{}Enter your natural language intent:{} ", COLOR_GREEN, COLOR_RESET);
                let _ = io::stdout().flush();
                let mut intent = String::new();
                if io::stdin().read_line(&mut intent).is_ok() {
                    let trimmed = intent.trim();
                    if !trimmed.is_empty() {
                        let report = gma.solve(trimmed, cwd, GHA_VERSION);
                        println!("{}", report);
                    }
                }
            }
            "2" | "/models" | ":models" | "models" => {
                let report = gma.solve("list_models", cwd, GHA_VERSION);
                println!("{}", report);
            }
            "3" | "/status" | ":status" | "status" => {
                let res = ToolRegistry::execute_tool("status", "", cwd);
                println!("{}", res);
            }
            "4" | "/services" | ":services" | "services" => {
                let res = ToolRegistry::execute_tool("services", "", cwd);
                println!("{}", res);
            }
            "5" | "/help" | ":help" | "help" => {
                print_help();
            }
            "/clear" | ":clear" | "clear" => {
                print!("{}", CLEAR_SCREEN);
                let _ = io::stdout().flush();
                print_tui_dashboard();
                continue;
            }
            "/version" | ":version" | "version" => {
                println!("gha v{}", GHA_VERSION);
            }
            _ => {
                let report = gma.solve(command, cwd, GHA_VERSION);
                println!("{}", report);
            }
        }
        println!();
    }
}

fn main() {
    let cwd = env::current_dir().unwrap_or_else(|_| PathBuf::from("."));
    let home = get_home_dir();
    let global_dir = home.join(".gha");

    let args: Vec<String> = env::args().skip(1).collect();

    if args.is_empty() {
        run_interactive_shell(&cwd);
        return;
    }

    let first_arg = args[0].to_lowercase();

    match first_arg.as_str() {
        "help" | "--help" | "-h" => {
            print_help();
        }
        "version" | "--version" | "-v" => {
            println!("gha v{}", GHA_VERSION);
        }
        "models" | ":models" | "/models" => {
            let gma = GmaMasterAgent::new();
            let report = gma.solve("list_models", &cwd, GHA_VERSION);
            println!("{}", report);
        }
        "install" | ":install" | "/install" => {
            run_install(&global_dir);
        }
        "uninstall" | ":uninstall" | "/uninstall" => {
            let _ = std::fs::remove_dir_all(&global_dir);
            println!("gha runtime removed.");
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
        "scout" | ":scout" | "/scout" => {
            let res = ToolRegistry::execute_tool("scout", "", &cwd);
            println!("{}", res);
        }
        "services" | ":services" | "/services" => {
            let res = ToolRegistry::execute_tool("services", "", &cwd);
            println!("{}", res);
        }
        "status" | ":status" | "/status" => {
            let res = ToolRegistry::execute_tool("status", "", &cwd);
            println!("{}", res);
        }
        "verify-cloud" | ":verify-cloud" | "/verify-cloud" => {
            let res = ToolRegistry::execute_tool("verify_cloud_providers", "", &cwd);
            println!("{}", res);
        }
        _ => {
            let goal = args.join(" ");
            let gma = GmaMasterAgent::new();
            let report = gma.solve(&goal, &cwd, GHA_VERSION);
            println!("{}", report);
        }
    }
}
