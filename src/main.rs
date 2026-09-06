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
use gemi::{GemiServer, ModelManager};
use gmcp::tools::ToolRegistry;
use gmcp::{GmcpClient, GmcpServer};
use sandbox::SandboxManager;

const GHA_VERSION: &str = "0.1.102";

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
    println!("🌌 gha - Universal Multi-Agent AI Runtime & MCP Engine");
    println!("Usage: ghai [NATURAL_LANGUAGE_GOAL | RUNTIME_COMMAND]\n");
    println!("🤖 GHA Master Agent (GMA) Sole Interactor:");
    println!("  ghai \"check system status\"         Mission: Full executive health & hardware report");
    println!("  ghai \"what is your version?\"       Mission: Architecture & engine version report");
    println!("  ghai \"list models\"                 Mission: Discover cloud & local AI models");
    println!("  ghai \"analyze this image...\"       Mission: Multimodal vision analysis");
    println!("  ghai \"provision infrastructure\"    Mission: Terraform / Docker / K8s orchestration");
    println!("  ghai \"<any user intent>\"           Mission: Parallel multi-agent execution\n");
    println!("⚙️ Native Runtime Management:");
    println!("  :install                 Initialize sandboxed .gha environment & start background daemon");
    println!("  :uninstall               Clean up sandboxed .gha environment");
    println!("  :daemon                  Inspect or manage GHA Master Daemon");
    println!("  mcp, gmcp                Start native GMA Master MCP Server over stdio");
    println!("  gemi, gemi-server        Start GEMI OpenAI-compatible REST server (Port 9091)");
}

fn run_install(workspace: &Path, global_dir: &Path) {
    println!("🚀 [ghai Native] Initializing offline GHA AI environment at {}...", workspace.display());
    let _ = SandboxManager::ensure_sandbox(workspace);
    let _ = SandboxManager::ensure_sandbox(global_dir);

    GmaDaemon::ensure_daemon_running(workspace, global_dir);
    println!("🚀 [GMA Daemon] Always-On Services Primed: GMCP (Port 9090) | GEMI (Port 9091) | World-Scale UDP (Port 9092)");
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
        "help" | "--help" | "-h" => {
            print_help();
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
                Some(pid) => println!("🚀 [GMA Daemon] Status: RUNNING (PID {}) | GMCP (Port 9090) | GEMI (Port 9091) | World-Scale UDP (Port 9092)", pid),
                None => {
                    GmaDaemon::ensure_daemon_running(&workspace, &global_dir);
                    println!("🚀 [GMA Daemon] Started background daemon: GMCP (Port 9090) | GEMI (Port 9091) | World-Scale UDP (Port 9092)");
                }
            }
        }
        _ => {
            // Specialized AI subcommands for developer-centric fast-access (still routed through ToolRegistry)
            if cmd == "ai" && args.len() > 1 {
                let sub = &args[1];
                match sub.as_str() {
                    "swarm" => {
                        println!("{}", ToolRegistry::execute_tool("swarm_sync", "", &workspace));
                        return;
                    }
                    "evolve" => {
                        println!("{}", ToolRegistry::execute_tool("self_evolve", "", &workspace));
                        return;
                    }
                    "scan-global" => {
                        println!("{}", ToolRegistry::execute_tool("global_registry_scan", "", &workspace));
                        return;
                    }
                    "provision" => {
                        println!("{}", ToolRegistry::execute_tool("cloud_provision", "", &workspace));
                        return;
                    }
                    "docker-ps" => {
                        println!("{}", ToolRegistry::execute_tool("docker_ps", "", &workspace));
                        return;
                    }
                    "kube-pods" => {
                        println!("{}", ToolRegistry::execute_tool("kube_pods", "", &workspace));
                        return;
                    }
                    "vision" => {
                        let path = args.get(2).map(|s| s.as_str()).unwrap_or("");
                        let prompt = args.get(3..).map(|s| s.join(" ")).unwrap_or_else(|| "Describe this image.".to_string());
                        println!("{}", ToolRegistry::execute_tool("vision_analyze", &format!("{} {}", path, prompt), &workspace));
                        return;
                    }
                    "ocr" => {
                        let path = args.get(2).map(|s| s.as_str()).unwrap_or("");
                        println!("{}", ToolRegistry::execute_tool("ocr_read", path, &workspace));
                        return;
                    }
                    "cluster" => {
                        println!("{}", ToolRegistry::execute_tool("cluster_status", "", &workspace));
                        return;
                    }
                    "self-heal" => {
                        println!("{}", ToolRegistry::self_heal_build(&workspace));
                        return;
                    }
                    "auto-branch" => {
                        println!("{}", ToolRegistry::git_auto_branch(&workspace));
                        return;
                    }
                    "run-tests" => {
                        println!("{}", ToolRegistry::run_test_harness(&workspace));
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
                    "mcp-hub" => {
                        let tools = GmcpClient::list_tools();
                        println!("🔌 GMCP Coordinated Tool Servers ({} Tools Active):", tools.len());
                        for t in tools {
                            println!("   │   ├── Tool: '{}' - {}", t.name, t.description);
                        }
                        return;
                    }
                    _ => {}
                }
            }

            // Universal GMA Natural Language Interactor
            let goal = args.join(" ");
            let gma = GmaMasterAgent::new();
            let report = gma.solve(&goal, &workspace, GHA_VERSION);
            println!("{}", report);
        }
    }
}
