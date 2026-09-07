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

const GHA_VERSION: &str = "0.1.133";

fn get_home_dir() -> PathBuf {
    env::var_os("HOME")
        .or_else(|| env::var_os("USERPROFILE"))
        .map(PathBuf::from)
        .unwrap_or_else(|| PathBuf::from("."))
}

fn print_help() {
    println!("gha v{}", GHA_VERSION);
    println!("Usage: gha \"<intent>\"\n");
    println!("Commands:");
    println!("  install                  Initialize global gha runtime");
    println!("  uninstall                Remove global gha runtime");
    println!("  mcp                      Start native MCP server");
    println!("  gemi                     Start GEMI REST server");
    println!("  services                 List running services");
}

fn run_install(global_dir: &Path) {
    println!("Initializing gha runtime...");
    let _ = SandboxManager::ensure_global_sandbox(global_dir);
    GmaDaemon::ensure_daemon_running(global_dir, global_dir);
    println!("gha runtime initialized.");
}

fn run_interactive_shell(cwd: &Path) {
    use std::io::{self, Write};

    println!("gha v{} interactive mode", GHA_VERSION);
    println!("Type intent, or :help, :models, :services, :exit to quit.\n");

    let gma = GmaMasterAgent::new();

    loop {
        print!("gha> ");
        if io::stdout().flush().is_err() {
            break;
        }

        let mut input = String::new();
        match io::stdin().read_line(&mut input) {
            Ok(0) => break,
            Ok(_) => {
                let line = input.trim();
                if line.is_empty() {
                    continue;
                }

                let line_lower = line.to_lowercase();
                match line_lower.as_str() {
                    ":exit" | ":quit" | "exit" | "quit" => break,
                    ":help" | "help" => {
                        print_help();
                    }
                    ":version" | "version" => {
                        println!("gha v{}", GHA_VERSION);
                    }
                    ":models" | "models" => {
                        let report = gma.solve("list_models", cwd, GHA_VERSION);
                        println!("{}", report);
                    }
                    ":services" | "services" => {
                        let res = ToolRegistry::execute_tool("services", "", cwd);
                        println!("{}", res);
                    }
                    _ => {
                        let report = gma.solve(line, cwd, GHA_VERSION);
                        println!("{}", report);
                    }
                }
            }
            Err(_) => break,
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
            let goal = args.join(" ");
            let gma = GmaMasterAgent::new();
            let report = gma.solve(&goal, &cwd, GHA_VERSION);
            println!("{}", report);
        }
    }
}
