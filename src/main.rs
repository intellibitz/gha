// gha: Multi-Agent Engine

mod error;
mod daemon;
mod gawd;
mod gemi;
mod gmcp;
mod native;
mod sandbox;

use std::env;
use std::io::{self, Read, IsTerminal};
use std::path::{Path, PathBuf};

use crate::gmcp::tools::ToolRegistry;
use daemon::GmaDaemon;
use gawd::GmaMasterAgent;
use gemi::GemiServer;
use gmcp::server::GmcpServer;
use sandbox::SandboxManager;

pub const GHA_VERSION: &str = "0.1.2022627";

fn get_home_dir() -> PathBuf {
    env::var_os("HOME")
        .or_else(|| env::var_os("USERPROFILE"))
        .map(PathBuf::from)
        .unwrap_or_else(|| PathBuf::from("."))
}

fn print_help() {
    println!("gha v{}", GHA_VERSION);
    println!("Usage: gha [COMMAND | INTENT]\n");
    println!("Commands & Intents:");
    println!("  version, -v, --version   Print version");
    println!("  help, -h, --help         Show help");
    println!("  install                  Initialize sandboxed .gha environment");
    println!("  uninstall                Clean up sandboxed .gha environment");
    println!("  mcp                      Start native MCP server");
    println!("  gemi                     Start GEMI REST server");
    println!("  status                   Inspect workspace health report");
    println!("  models                   List available models");
    println!("  agents                   List active agents");
    println!("  engines                  List active engines");
    println!("  benchmark                Run performance benchmark");
    println!("  build                    Build validation & autonomous healing");
    println!("  test                     Run test harness");
    println!("  clean                    Clean workspace build artifacts");
    println!("\nPowered by GAWD Agent Fleet & ToolRegistry for any natural language intent.");
    println!("Examples:");
    println!("  gha \"analyze current git status\"");
    println!("  cat error.log | gha \"debug this error\"");
    println!("  gha scout_model llama3 > model.json");
}

fn run_install(global_dir: &Path) {
    println!("Initializing gha runtime...");
    let _ = SandboxManager::ensure_global_sandbox(global_dir);
    GmaDaemon::ensure_daemon_running(global_dir, global_dir);
    println!("gha runtime initialized.");
}

fn main() {
    let cwd = env::current_dir().unwrap_or_else(|_| PathBuf::from("."));
    let home = get_home_dir();
    let global_dir = home.join(".gha");

    if env::args().nth(1).as_deref() != Some("daemon-start") {
        GmaDaemon::ensure_daemon_running(&cwd, &global_dir);
    }

    let args: Vec<String> = env::args().skip(1).collect();

    if args.is_empty() {
        if !io::stdin().is_terminal() {
            let mut buffer = String::new();
            if io::stdin().read_to_string(&mut buffer).is_ok() {
                let trimmed = buffer.trim();
                if !trimmed.is_empty() {
                    let gma = GmaMasterAgent::new();
                    let answer = gma.solve_clean(trimmed, &cwd, GHA_VERSION);
                    print!("{}", answer);
                    return;
                }
            }
        }
        return;
    }

    let first_arg = args[0].to_lowercase();
    let clean_first_arg = first_arg.trim_start_matches(':').trim_start_matches('/');

    match clean_first_arg {
        "help" | "-h" | "--help" => {
            print_help();
        }
        "version" | "-v" | "--version" => {
            println!("gha v{}", GHA_VERSION);
        }
        "install" => {
            run_install(&global_dir);
        }
        "uninstall" => {
            let _ = std::fs::remove_dir_all(&global_dir);
            println!("gha runtime removed.");
        }
        "daemon-start" => {
            GmaDaemon::run_daemon_loop(global_dir.clone(), global_dir);
        }
        "gmcp-server" | "mcp-server" | "mcp" => {
            GmcpServer::run_stdio(&cwd, GHA_VERSION);
        }
        "gemi-server" | "gemi" => {
            let cfg = crate::sandbox::manager::GhaConfig::load(&global_dir);
            GemiServer::start_http_server(cwd.clone(), cfg.gemi_port);
        }
        "clean" => {
            let _ = std::fs::remove_dir_all(cwd.join("target"));
            println!("Workspace build artifacts cleaned.");
        }
        _ => {
            let cmd_name = clean_first_arg;
            let cmd_arg = args.get(1..).map(|s| s.join(" ")).unwrap_or_default();
            let mut goal = args.join(" ");

            if !io::stdin().is_terminal() {
                let mut buffer = String::new();
                if io::stdin().read_to_string(&mut buffer).is_ok() {
                    let trimmed = buffer.trim();
                    if !trimmed.is_empty() {
                         goal = format!("{}\n\n[INPUT DATA]:\n{}", goal, trimmed);
                    }
                }
            }

            let gma = GmaMasterAgent::new();

            // Unified GAWD & ToolRegistry Dispatch for all commands and intents
            if ToolRegistry::exists(cmd_name) {
                let res = ToolRegistry::execute_tool(cmd_name, &cmd_arg, &cwd);
                if !io::stdout().is_terminal() {
                    print!("{}", res);
                } else {
                    println!("{}", res);
                }
                return;
            }

            if !io::stdout().is_terminal() {
                let answer = gma.solve_clean(&goal, &cwd, GHA_VERSION);
                print!("{}", answer);
            } else {
                let report = gma.solve(&goal, &cwd, GHA_VERSION);
                println!("{}", report);
            }
        }
    }
}
