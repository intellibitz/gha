// gha: Multi-Agent Engine

mod error;
mod daemon;
mod gawd;
mod gemi;
mod gmcp;
mod sandbox;

use std::env;
use std::io::{self, Write, Read, IsTerminal};
use std::path::{Path, PathBuf};

use crate::gmcp::tools::ToolRegistry;
use daemon::GmaDaemon;
use gawd::GmaMasterAgent;
use gemi::GemiServer;
use gmcp::server::GmcpServer;
use sandbox::SandboxManager;

use rustyline::completion::{Completer, Pair};
use rustyline::highlight::Highlighter;
use rustyline::hint::Hinter;
use rustyline::validate::Validator;
use rustyline::{Context, Helper};

pub const GHA_VERSION: &str = "0.1.335";

// ANSI Formatting Codes
const COLOR_CYAN: &str = "\x1b[1;36m";
const COLOR_GREEN: &str = "\x1b[1;32m";
const COLOR_DIM: &str = "\x1b[90m";
const COLOR_BOLD: &str = "\x1b[1m";
const COLOR_RESET: &str = "\x1b[0m";
const CLEAR_SCREEN: &str = "\x1b[2J\x1b[1;1H";

pub struct GhaHelper;

impl Helper for GhaHelper {}

impl Completer for GhaHelper {
    type Candidate = Pair;

    fn complete(&self, line: &str, _pos: usize, _ctx: &Context<'_>) -> rustyline::Result<(usize, Vec<Pair>)> {
        let commands = [
            "/help", "/domain", "/simple", "/backup", "/restore",
            "/audit", "/memory", "/forget", "/setkey", "/renew", "/agents",
            "/engines", "/clients", "/servers", "/debug", "/models", "/services",
            "/status", "/schedule", "/export_doc", "/clear", "/exit",
        ];

        let mut matches = Vec::new();
        if line.starts_with('/') {
            for cmd in commands {
                if cmd.starts_with(line) {
                    matches.push(Pair {
                        display: cmd.to_string(),
                        replacement: cmd.to_string(),
                    });
                }
            }
        }
        Ok((0, matches))
    }
}

impl Hinter for GhaHelper {
    type Hint = String;
}

impl Highlighter for GhaHelper {}

impl Validator for GhaHelper {}

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
    println!("  /domain, :domain         Inspect available domain intelligence substrates");
    println!("  /simple, :simple         Toggle simplified output mode");
    println!("  /backup, :backup         Backup workspace files and state");
    println!("  /restore, :restore       Restore workspace files and state from backup");
    println!("  /audit, :audit           Inspect workspace audit trail");
    println!("  /memory, :memory         Inspect session memory");
    println!("  /forget, :forget         Clear session memory");
    println!("  /setkey <KEY> <VAL>      Save API key to environment");
    println!("  /renew, :renew           Reload session with latest binary");
    println!("  /agents, :agents         List active agents");
    println!("  /engines, :engines       List active engines");
    println!("  /clients, :clients       List MCP clients");
    println!("  /servers, :servers       List running local servers");
    println!("  /debug, :debug           Toggle developer debug mode");
    println!("  /models, :models         List available models");
    println!("  /services, :services     List running services");
    println!("  /status, :status         Inspect health & hardware status");
    println!("  /schedule <sec> <task>   Schedule background task");
    println!("  /export_doc <file> <txt> Export document to HTML or Markdown");
    println!("  /clear, :clear           Clear screen");
    println!("  /exit, :exit, exit       Exit interactive console");
    println!("\nSystem Commands:");
    println!("  install                  Initialize global gha runtime");
    println!("  uninstall                Remove global gha runtime");
    println!("  mcp                      Start native MCP server");
    println!("  gemi                     Start GEMI REST server");
}

fn print_header(cwd: &Path, debug_mode: bool, simple_mode: bool) {
    let home = get_home_dir();
    let global_dir = home.join(".gha");
    let cfg = crate::sandbox::manager::GhaConfig::load(&global_dir);
    let repo_home = format!("https://github.com/{}", cfg.gha_repo);

    if simple_mode {
        println!("{}─────────────────────────────────────────────────────────────{}", COLOR_DIM, COLOR_RESET);
        println!("{}GHA Simplified Output Mode (v{}){}", COLOR_BOLD, GHA_VERSION, COLOR_RESET);
        println!("{}Workspace: {}{}", COLOR_DIM, cwd.display(), COLOR_RESET);
        println!("{}Home: {}{}", COLOR_DIM, repo_home, COLOR_RESET);

        let proactive_prompts = crate::gawd::agents::GhaUserAgent::generate_proactive_prompts(cwd);
        if !proactive_prompts.is_empty() {
            println!("\n{}Proactive Suggestions:{}", COLOR_GREEN, COLOR_RESET);
            for (num, prompt) in &proactive_prompts {
                println!("  [{}] {}", num, prompt);
            }
        }
        println!("{}─────────────────────────────────────────────────────────────{}\n", COLOR_DIM, COLOR_RESET);
        return;
    }

    let mode_label = if debug_mode { "DEBUG" } else { "CONVERSATIONAL" };
    let (engine, model) = crate::gemi::models::ModelManager::get_active_engine_and_model();
    println!("{}─────────────────────────────────────────────────────────────{}", COLOR_DIM, COLOR_RESET);
    println!("{}GHA (v{}){} | Substrate: {}Universal Intelligence Layer{}", COLOR_BOLD, GHA_VERSION, COLOR_RESET, COLOR_GREEN, COLOR_RESET);
    println!("{}Engine: {}{}{} | Model: {}{}{} | Mode: {}{}{}", COLOR_DIM, COLOR_CYAN, engine, COLOR_RESET, COLOR_CYAN, model, COLOR_RESET, COLOR_GREEN, mode_label, COLOR_RESET);
    println!("{}Workspace: {}{}", COLOR_DIM, cwd.display(), COLOR_RESET);
    println!("{}Home: {}{}", COLOR_DIM, repo_home, COLOR_RESET);

    let proactive_prompts = crate::gawd::agents::GhaUserAgent::generate_proactive_prompts(cwd);
    if !proactive_prompts.is_empty() {
        println!("\n{}Proactive Missions:{}", COLOR_GREEN, COLOR_RESET);
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
    let bin_filename = if cfg!(target_os = "windows") { ".gha/bin/gha.exe" } else { ".gha/bin/gha" };
    let fallback_name = if cfg!(target_os = "windows") { "gha.exe" } else { "gha" };
    let bin_path = get_home_dir().join(bin_filename);
    let target_bin = if bin_path.exists() { bin_path } else { env::current_exe().unwrap_or_else(|_| PathBuf::from(fallback_name)) };
    let initial_mtime = std::fs::metadata(&target_bin).and_then(|m| m.modified()).ok();

    let mut debug_mode = false;
    let mut simple_mode = false;
    print!("{}", CLEAR_SCREEN);
    print_header(cwd, debug_mode, simple_mode);

    let gma = GmaMasterAgent::new();

    if let Some(interrupted_intent) = SandboxManager::check_interrupted_checkpoint(cwd) {
        println!("{}> Interrupted mission detected: \"{}\". Resuming execution...{}\n", COLOR_GREEN, interrupted_intent, COLOR_RESET);
        let clean_answer = gma.solve_clean(&interrupted_intent, cwd, GHA_VERSION);
        println!("{}\n", clean_answer);
    }

    let history_file = get_home_dir().join(".gha/history.txt");
    let mut rl = match rustyline::Editor::new() {
        Ok(mut editor) => {
            editor.set_helper(Some(GhaHelper));
            let _ = editor.load_history(&history_file);
            Some(editor)
        }
        Err(_) => None,
    };

    loop {
        if let Some(initial_time) = initial_mtime
            && let Ok(m) = std::fs::metadata(&target_bin)
            && let Ok(current_mtime) = m.modified()
            && current_mtime > initial_time
        {
            println!("Runtime binary update detected on disk. Auto-renewing session...");
            if let Some(ref mut editor) = rl {
                let _ = editor.save_history(&history_file);
            }
            let _ = std::process::Command::new(&target_bin).status();
            break;
        }

        let prompt = if simple_mode {
            format!("{}GHA>{} ", COLOR_GREEN, COLOR_RESET)
        } else {
            let (engine, model) = crate::gemi::models::ModelManager::get_active_engine_and_model();
            format!("{}{}GHA (v{}){} {}{}[{}]{} {}{}[{}]{}{}>{} ", COLOR_CYAN, COLOR_BOLD, GHA_VERSION, COLOR_RESET, COLOR_DIM, COLOR_CYAN, engine, COLOR_RESET, COLOR_DIM, COLOR_CYAN, model, COLOR_RESET, COLOR_GREEN, COLOR_RESET)
        };

        let line_res = if let Some(ref mut editor) = rl {
            editor.readline(&prompt)
        } else {
            print!("{}", prompt);
            let _ = io::stdout().flush();
            let mut l = String::new();
            match io::stdin().read_line(&mut l) {
                Ok(0) => Err(rustyline::error::ReadlineError::Eof),
                Ok(_) => Ok(l),
                Err(e) => Err(rustyline::error::ReadlineError::Io(e)),
            }
        };

        let line = match line_res {
            Ok(l) => l,
            Err(rustyline::error::ReadlineError::Interrupted) | Err(rustyline::error::ReadlineError::Eof) => {
                println!("{}Exiting session.{}", COLOR_DIM, COLOR_RESET);
                break;
            }
            Err(e) => {
                println!("Error reading input: {}", e);
                break;
            }
        };

        let command = line.trim();
        if command.is_empty() {
            continue;
        }

        if let Some(ref mut editor) = rl {
            let _ = editor.add_history_entry(command);
            let _ = editor.save_history(&history_file);
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

        if command_lower.starts_with("/schedule") || command_lower.starts_with(":schedule") {
            let arg = command.trim_start_matches("/schedule").trim_start_matches(":schedule").trim();
            let res = ToolRegistry::execute_tool("schedule_task", arg, cwd);
            println!("\n{}", res);
            println!();
            continue;
        }

        if command_lower.starts_with("/attach") || command_lower.starts_with(":attach") {
            let arg = command.trim_start_matches("/attach").trim_start_matches(":attach").trim();
            let parts: Vec<&str> = arg.splitn(2, ' ').collect();
            let file_path = parts.first().copied().unwrap_or("").trim();
            let instruction = parts.get(1).copied().unwrap_or("analyze attached file").trim();

            if file_path.is_empty() {
                println!("Usage: /attach <file_path> [instruction]");
                println!("Example: /attach ~/Documents/budget.csv summarize expenses");
            } else {
                let full_path = if PathBuf::from(file_path).is_absolute() {
                    PathBuf::from(file_path)
                } else {
                    cwd.join(file_path)
                };

                if full_path.is_file() {
                    if let Ok(content) = std::fs::read_to_string(&full_path) {
                        let attached_prompt = format!("{}\n\n[ATTACHED FILE CONTENT: {}]\n{}", instruction, file_path, content);
                        println!("📎 Attached file: '{}' ({} bytes)", file_path, content.len());
                        if debug_mode {
                            let report = gma.solve(&attached_prompt, cwd, GHA_VERSION);
                            println!("{}", report);
                        } else {
                            let clean_answer = gma.solve_clean(&attached_prompt, cwd, GHA_VERSION);
                            println!("{}", clean_answer);
                        }
                    } else {
                        println!("❌ Error: Could not read file content at '{}'", full_path.display());
                    }
                } else {
                    println!("❌ Error: File not found at '{}'", full_path.display());
                }
            }
            println!();
            continue;
        }

        if command_lower.starts_with("/export_doc") || command_lower.starts_with(":export_doc") {
            let arg = command.trim_start_matches("/export_doc").trim_start_matches(":export_doc").trim();
            let res = ToolRegistry::execute_tool("export_doc", arg, cwd);
            println!("\n{}", res);
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
                println!("Usage: /setkey <KEY_NAME> <KEY_VALUE> (e.g. /setkey OPENAI_API_KEY [KEY_VALUE])");
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
            "/friendly" | ":friendly" | "friendly" | "/simple" | ":simple" | "simple" => {
                simple_mode = !simple_mode;
                println!("Simplified Output Mode set to: {}", if simple_mode { "ON" } else { "OFF" });
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
            "/verify_models" | ":verify_models" | "verify_models" => {
                let res = ToolRegistry::execute_tool("verify_models", "", cwd);
                println!("\n{}", res);
            }
            "/domain" | ":domain" | "domain" | "/domains" | ":domains" => {
                println!("\nIntelligence Substrates for World Missions:");
                println!("  Agronomy & Crop Intelligence     (e.g. soil pH, N-P-K ratios, crop yield)");
                println!("  Clinical & Health Diagnostics    (e.g. medical guidance, patient health)");
                println!("  Legal & Contract Analysis       (e.g. contract review, clause risk)");
                println!("  Pedagogical & Science Learning  (e.g. STEM synthesis, interactive tutoring)");
                println!("  Renewable Energy & Climate      (e.g. solar potential, grid optimization)");
                println!("  Software & Kernel Engineering   (e.g. Rust/C architecture, debugging)");
                println!("  Dynamic GHA Home Support        (Use /setkey GHA_REPO <owner/repo>)");
                println!("  Truly Unbiased Architecture     (100% Config-Driven models & tools)");
                println!("  Universal Substrate              (e.g. general multi-agent execution)");
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
                print_header(cwd, debug_mode, simple_mode);
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

                let mut expanded_prompt = target_command.to_string();
                for word in target_command.split_whitespace() {
                    if let Some(file_ref) = word.strip_prefix('@') {
                        let full_p = if PathBuf::from(file_ref).is_absolute() { PathBuf::from(file_ref) } else { cwd.join(file_ref) };
                        if full_p.is_file()
                            && let Ok(content) = std::fs::read_to_string(&full_p)
                        {
                            println!("📎 Auto-attached file: '{}' ({} bytes)", file_ref, content.len());
                            expanded_prompt.push_str(&format!("\n\n[ATTACHED FILE: {}]\n{}", file_ref, content));
                        }
                    }
                }

                let (badge, badge_desc) = crate::gawd::agents::GhaUserAgent::detect_domain_badge(&expanded_prompt);
                println!("{}Substrate Mode: {} ({}){}", COLOR_CYAN, badge, badge_desc, COLOR_RESET);

                if debug_mode {
                    let report = gma.solve(&expanded_prompt, cwd, GHA_VERSION);
                    println!("{}", report);
                } else {
                    let clean_answer = gma.solve_clean(&expanded_prompt, cwd, GHA_VERSION);
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

    // Liveness Verification & Instant Background Recovery (Compliance Rule)
    // Ensures GMCP/GEMI servers are always available for external systems/IDEs.
    if env::args().nth(1).as_deref() != Some("daemon-start") {
        GmaDaemon::ensure_daemon_running(&cwd, &global_dir);
    }

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
        "verify_models" | "verify-models" | ":verify_models" | "/verify_models" => {
            let res = ToolRegistry::execute_tool("verify_models", "", &cwd);
            println!("{}", res);
        }
        "run_100_tests" | "run-100-tests" | ":run_100_tests" | "/run_100_tests" => {
            let res = ToolRegistry::execute_tool("run_100_tests", "", &cwd);
            println!("{}", res);
        }
        "run_1000_tests" | "run-1000-tests" | ":run_1000_tests" | "/run_1000_tests" => {
            let res = ToolRegistry::execute_tool("run_1000_tests", "", &cwd);
            println!("{}", res);
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
            let cfg = crate::sandbox::manager::GhaConfig::load(&global_dir);
            GemiServer::start_http_server(cwd, cfg.gemi_port);
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
        "domain" | "domains" | ":domain" | "/domain" => {
            println!("\nIntelligence Substrates for World Missions:");
            println!("  Agronomy & Crop Intelligence     (e.g. soil pH, N-P-K ratios, crop yield)");
            println!("  Clinical & Health Diagnostics    (e.g. medical guidance, patient health)");
            println!("  Legal & Contract Analysis       (e.g. contract review, clause risk)");
            println!("  Pedagogical & Science Learning  (e.g. STEM synthesis, interactive tutoring)");
            println!("  Renewable Energy & Climate      (e.g. solar potential, grid optimization)");
            println!("  Software & Kernel Engineering   (e.g. Rust/C architecture, debugging)");
            println!("  Dynamic GHA Home Support        (Use /setkey GHA_REPO <owner/repo>)");
            println!("  Truly Unbiased Architecture     (100% Config-Driven models & tools)");
            println!("  Universal Substrate              (e.g. general multi-agent execution)\n");
        }
        "verify-cloud" | ":verify-cloud" | "/verify-cloud" => {
            let res = ToolRegistry::execute_tool("verify_cloud_providers", "", &cwd);
            println!("{}", res);
        }
        _ => {
            let mut goal = args.join(" ");

            // 🚀 Support for Pipes and Redirection (Rule 7 & 15 Compliance)
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
            if !io::stdout().is_terminal() {
                // 🚀 Filter Mode: Output strictly the clean answer for piping
                let answer = gma.solve_clean(&goal, &cwd, GHA_VERSION);
                print!("{}", answer);
            } else {
                let report = gma.solve(&goal, &cwd, GHA_VERSION);
                println!("{}", report);
            }
        }
    }
}
