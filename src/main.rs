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

const GHA_VERSION: &str = "0.1.193";

// ANSI Formatting Codes
const COLOR_CYAN: &str = "\x1b[1;36m";
const COLOR_GREEN: &str = "\x1b[1;32m";
const COLOR_DIM: &str = "\x1b[90m";
const COLOR_BOLD: &str = "\x1b[1m";
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
    println!("Usage: Type any prompt or natural language instruction.\n");
    println!("Slash Commands:");
    println!("  /help, :help             Display this help menu");
    println!("  /backup, :backup         Backup workspace files and state to archive");
    println!("  /restore, :restore       Restore workspace files and state from backup archive");
    println!("  /audit, :audit           Inspect workspace audit trail and self-audit records");
    println!("  /memory, :memory         Inspect workspace session memory and history");
    println!("  /forget, :forget         Clear workspace session memory");
    println!("  /setkey <KEY> <VAL>      Save API key to ~/.gha/env (e.g. /setkey OPENAI_API_KEY sk-...)");
    println!("  /renew, :renew           Reload session with latest installed gha binary");
    println!("  /agents, :agents         List active agents in GAWD fleet");
    println!("  /engines, :engines       List active execution & inference engines");
    println!("  /clients, :clients       List configured MCP clients & proxies");
    println!("  /servers, :servers       List running MCP servers & background hosts");
    println!("  /debug, :debug           Toggle developer debug mode (execution trace)");
    println!("  /models, :models         List available cloud and local models");
    println!("  /services, :services     List running background services");
    println!("  /status, :status         Inspect workspace health & hardware status");
    println!("  /clear, :clear           Clear display screen");
    println!("  /exit, :exit, exit       Exit interactive console");
    println!("\nSystem Commands:");
    println!("  install                  Initialize global gha runtime");
    println!("  uninstall                Remove global gha runtime");
    println!("  mcp                      Start native MCP server");
    println!("  gemi                     Start GEMI REST server");
}

fn print_header(cwd: &Path, debug_mode: bool) {
    let mode_label = if debug_mode { "DEBUG TRACE" } else { "CONVERSATIONAL" };
    let (engine, model) = crate::gemi::models::ModelManager::get_active_engine_and_model();
    println!("{}─────────────────────────────────────────────────────────────{}", COLOR_DIM, COLOR_RESET);
    println!("{}Ask GHA (v{}){} | Engine: {}{}{} | Model: {}{}{} | Mode: {}{}{}", COLOR_BOLD, GHA_VERSION, COLOR_RESET, COLOR_CYAN, engine, COLOR_RESET, COLOR_CYAN, model, COLOR_RESET, COLOR_GREEN, mode_label, COLOR_RESET);
    println!("{}Workspace: {}{}", COLOR_DIM, cwd.display(), COLOR_RESET);

    let proactive_prompts = crate::gawd::agents::GhaUserAgent::generate_proactive_prompts(cwd);
    if !proactive_prompts.is_empty() {
        println!("\n{}💡 Proactive Suggestions for this Workspace:{}", COLOR_GREEN, COLOR_RESET);
        for (num, prompt) in &proactive_prompts {
            println!("  [{}] {}", num, prompt);
        }
    }
    println!("{}─────────────────────────────────────────────────────────────{}\n", COLOR_DIM, COLOR_RESET);
}

fn run_install(global_dir: &Path) {
    println!("Initializing gha runtime...");
    let _ = SandboxManager::ensure_global_sandbox(global_dir);
    GmaDaemon::ensure_daemon_running(global_dir, global_dir);
    println!("gha runtime initialized.");
}

fn run_interactive_shell(cwd: &Path) {
    use std::io::{self, Write};

    let bin_path = get_home_dir().join(".gha/bin/gha");
    let target_bin = if bin_path.exists() { bin_path } else { env::current_exe().unwrap_or_else(|_| PathBuf::from("gha")) };
    let initial_mtime = std::fs::metadata(&target_bin).and_then(|m| m.modified()).ok();

    let mut debug_mode = false;
    print!("{}", CLEAR_SCREEN);
    print_header(cwd, debug_mode);

    let gma = GmaMasterAgent::new();

    if let Some(interrupted_intent) = SandboxManager::check_interrupted_checkpoint(cwd) {
        println!("{}> Interrupted mission detected: \"{}\". Resuming execution...{}\n", COLOR_GREEN, interrupted_intent, COLOR_RESET);
        let clean_answer = gma.solve_clean(&interrupted_intent, cwd, GHA_VERSION);
        println!("{}\n", clean_answer);
    }

    loop {
        if let Some(initial_time) = initial_mtime {
            if let Ok(m) = std::fs::metadata(&target_bin) {
                if let Ok(current_mtime) = m.modified() {
                    if current_mtime > initial_time {
                        println!("{}⚡ Runtime binary update detected on disk. Auto-renewing session...{}", COLOR_CYAN, COLOR_RESET);
                        let _ = std::process::Command::new(&target_bin).status();
                        break;
                    }
                }
            }
        }

        let (engine, model) = crate::gemi::models::ModelManager::get_active_engine_and_model();
        println!("{}─────────────────────────────────────────────────────────────{}", COLOR_DIM, COLOR_RESET);
        print!("{}{}Ask GHA (v{}){} {}{}[{}]{} {}{}[{}]{}{}>{} ", COLOR_CYAN, COLOR_BOLD, GHA_VERSION, COLOR_RESET, COLOR_DIM, COLOR_CYAN, engine, COLOR_RESET, COLOR_DIM, COLOR_CYAN, model, COLOR_RESET, COLOR_GREEN, COLOR_RESET);
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

        if command_lower.starts_with("/use_engine") || command_lower.starts_with(":use_engine") || command_lower.starts_with("use_engine") || command_lower.starts_with("use engine") || command_lower.starts_with("select engine") || command_lower.starts_with("set_engine") || command_lower.starts_with("set engine") {
            let engine_arg = command
                .trim_start_matches("/use_engine")
                .trim_start_matches(":use_engine")
                .trim_start_matches("use_engine")
                .trim_start_matches("use engine")
                .trim_start_matches("select engine")
                .trim_start_matches("set_engine")
                .trim_start_matches("set engine")
                .trim();
            let target_engine = if engine_arg.is_empty() { "Auto" } else { engine_arg };
            match crate::gemi::models::ModelManager::set_selected_engine(target_engine) {
                Ok(msg) => println!("{}{}{}", COLOR_GREEN, msg, COLOR_RESET),
                Err(e) => println!("Error setting engine: {}", e),
            }
            println!();
            continue;
        }

        if command_lower.starts_with("/use_model") || command_lower.starts_with(":use_model") || command_lower.starts_with("use_model") || command_lower.starts_with("use model") || command_lower.starts_with("select model") || command_lower.starts_with("set_model") || command_lower.starts_with("set model") {
            let model_arg = command
                .trim_start_matches("/use_model")
                .trim_start_matches(":use_model")
                .trim_start_matches("use_model")
                .trim_start_matches("use model")
                .trim_start_matches("select model")
                .trim_start_matches("set_model")
                .trim_start_matches("set model")
                .trim();
            let target_model = if model_arg.is_empty() { "Auto-Scout" } else { model_arg };
            match crate::gemi::models::ModelManager::set_selected_model(target_model) {
                Ok(msg) => println!("{}{}{}", COLOR_GREEN, msg, COLOR_RESET),
                Err(e) => println!("Error setting model: {}", e),
            }
            println!();
            continue;
        }

        if command_lower == "auto_scout" || command_lower == "/auto_scout" || command_lower == ":auto_scout" || command_lower == "auto scout" {
            match crate::gemi::models::ModelManager::set_selected_model("Auto-Scout") {
                Ok(msg) => println!("{}{}{}", COLOR_GREEN, msg, COLOR_RESET),
                Err(e) => println!("Error setting model: {}", e),
            }
            println!();
            continue;
        }

        if command_lower.starts_with("/setkey") || command_lower.starts_with(":setkey") {
            let parts: Vec<&str> = command.split_whitespace().collect();
            if parts.len() >= 3 {
                let key = parts[1];
                let val = parts[2];
                let home = get_home_dir();
                let global_dir = home.join(".gha");
                match SandboxManager::save_env_key(&global_dir, key, val) {
                    Ok(msg) => println!("{}{}{}", COLOR_GREEN, msg, COLOR_RESET),
                    Err(e) => println!("Error saving key: {}", e),
                }
            } else {
                println!("Usage: /setkey <KEY_NAME> <KEY_VALUE> (e.g. /setkey OPENAI_API_KEY sk-...)");
            }
            println!();
            continue;
        }

        match command_lower.as_str() {
            "0" | "/exit" | ":exit" | "/quit" | ":quit" | "exit" | "quit" => {
                println!("{}Exiting session.{}", COLOR_DIM, COLOR_RESET);
                break;
            }
            "/renew" | ":renew" | "/reload" | ":reload" | "/update" | ":update" => {
                println!("{}Renewing session (loading latest installed gha binary)...{}", COLOR_DIM, COLOR_RESET);
                let bin_path = get_home_dir().join(".gha/bin/gha");
                let target_bin = if bin_path.exists() { bin_path } else { env::current_exe().unwrap_or_else(|_| PathBuf::from("gha")) };
                let _ = std::process::Command::new(target_bin).status();
                break;
            }
            "/debug" | ":debug" | "debug" => {
                debug_mode = !debug_mode;
                println!("{}Developer Debug Mode set to: {}{}", COLOR_DIM, if debug_mode { "ON (Full Execution Trace)" } else { "OFF (Clean Conversational Answer)" }, COLOR_RESET);
            }
            "/backup" | ":backup" | "backup" => {
                let res = ToolRegistry::execute_tool("backup_work", "", cwd);
                println!("\n{}", res);
            }
            "/restore" | ":restore" | "restore" => {
                let res = ToolRegistry::execute_tool("restore_work", "", cwd);
                println!("\n{}", res);
            }
            "/audit" | ":audit" | "audit" | "audit_log" => {
                let res = ToolRegistry::execute_tool("audit", "", cwd);
                println!("\n{}", res);
            }
            "/memory" | ":memory" | "/history" | ":history" | "memory" | "history" => {
                let res = ToolRegistry::execute_tool("memory", "", cwd);
                println!("\n{}", res);
            }
            "/forget" | ":forget" | "forget" | "clear_memory" => {
                let res = ToolRegistry::execute_tool("clear_memory", "", cwd);
                println!("\n{}", res);
            }
            "/agents" | ":agents" | "agents" => {
                let res = ToolRegistry::execute_tool("agents", "", cwd);
                println!("\n{}", res);
            }
            "/engines" | ":engines" | "engines" => {
                let res = ToolRegistry::execute_tool("engines", "", cwd);
                println!("\n{}", res);
            }
            "/clients" | ":clients" | "clients" => {
                let res = ToolRegistry::execute_tool("clients", "", cwd);
                println!("\n{}", res);
            }
            "/servers" | ":servers" | "servers" => {
                let res = ToolRegistry::execute_tool("servers", "", cwd);
                println!("\n{}", res);
            }
            "/models" | ":models" | "models" => {
                let report = gma.solve("list_models", cwd, GHA_VERSION);
                println!("\n{}", report);
            }
            "/status" | ":status" | "status" => {
                let res = ToolRegistry::execute_tool("status", "", cwd);
                println!("\n{}", res);
            }
            "/services" | ":services" | "services" => {
                let res = ToolRegistry::execute_tool("services", "", cwd);
                println!("\n{}", res);
            }
            "/help" | ":help" | "help" => {
                println!();
                print_help();
            }
            "/clear" | ":clear" | "clear" => {
                print!("{}", CLEAR_SCREEN);
                let _ = io::stdout().flush();
                print_header(cwd, debug_mode);
                continue;
            }
            "/version" | ":version" | "version" => {
                println!("\ngha v{}", GHA_VERSION);
            }
            _ => {
                println!();
                let proactive_prompts = crate::gawd::agents::GhaUserAgent::generate_proactive_prompts(cwd);
                let target_command = if let Some((_, prompt_text)) = proactive_prompts.iter().find(|(n, _)| n == command) {
                    println!("{}Selected Proactive Mission: {}{}\n", COLOR_GREEN, prompt_text, COLOR_RESET);
                    prompt_text.as_str()
                } else {
                    command
                };

                if debug_mode {
                    let report = gma.solve(target_command, cwd, GHA_VERSION);
                    println!("{}", report);
                } else {
                    let clean_answer = gma.solve_clean(target_command, cwd, GHA_VERSION);
                    println!("{}", clean_answer);
                }
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
        "use_engine" | "use-engine" => {
            let engine_arg = args.get(1).map(|s| s.as_str()).unwrap_or("auto");
            match crate::gemi::models::ModelManager::set_selected_engine(engine_arg) {
                Ok(msg) => println!("{}", msg),
                Err(e) => println!("Error setting engine: {}", e),
            }
        }
        "use_model" | "use-model" => {
            let model_arg = args.get(1).map(|s| s.as_str()).unwrap_or("Auto-Scout");
            match crate::gemi::models::ModelManager::set_selected_model(model_arg) {
                Ok(msg) => println!("{}", msg),
                Err(e) => println!("Error setting model: {}", e),
            }
        }
        "agents" | ":agents" | "/agents" => {
            let res = ToolRegistry::execute_tool("agents", "", &cwd);
            println!("{}", res);
        }
        "engines" | ":engines" | "/engines" => {
            let res = ToolRegistry::execute_tool("engines", "", &cwd);
            println!("{}", res);
        }
        "clients" | ":clients" | "/clients" => {
            let res = ToolRegistry::execute_tool("clients", "", &cwd);
            println!("{}", res);
        }
        "servers" | ":servers" | "/servers" => {
            let res = ToolRegistry::execute_tool("servers", "", &cwd);
            println!("{}", res);
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
