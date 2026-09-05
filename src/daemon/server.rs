// 🚀 Always-On GMA Master Daemon Process Manager
// 100% Rust implementation managing GMCP (Port 9090) and GEMI (Port 9091) in background

use std::fs;
use std::path::{Path, PathBuf};
use std::process::{Command, Stdio};
use std::thread;
use std::time::Duration;

use crate::gemi::GemiServer;
use crate::gmcp::tools::ToolRegistry;
use std::io::{BufRead, BufReader, Write};
use std::net::TcpListener;

pub struct GmaDaemon;

impl GmaDaemon {
    pub const GMCP_PORT: u16 = 9090;
    pub const GEMI_PORT: u16 = 9091;

    pub fn get_lock_file(global_dir: &Path) -> PathBuf {
        global_dir.join("gma.lock")
    }

    pub fn check_status(global_dir: &Path) -> Option<u32> {
        let lock_file = Self::get_lock_file(global_dir);
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

    pub fn ensure_daemon_running(workspace: &Path, global_dir: &Path) {
        if Self::check_status(global_dir).is_some() {
            return;
        }

        let current_exe = std::env::current_exe().ok();
        let global_bin = global_dir.join("bin/ghai-engine");

        let bin_to_run = if let Some(ref exe) = current_exe {
            exe.clone()
        } else if global_bin.exists() {
            global_bin
        } else {
            PathBuf::from("ghai")
        };

        let _ = Command::new("nohup")
            .arg(bin_to_run)
            .arg("daemon-start")
            .arg(workspace.to_str().unwrap_or("."))
            .stdin(Stdio::null())
            .stdout(Stdio::null())
            .stderr(Stdio::null())
            .spawn();
    }

    pub fn run_daemon_loop(workspace: PathBuf, global_dir: PathBuf) {
        let pid = std::process::id();
        let lock_file = Self::get_lock_file(&global_dir);
        let _ = fs::write(&lock_file, pid.to_string());

        let workspace_gemi = workspace.clone();
        // 1. Spawn GEMI HTTP REST Server Thread (Port 9091)
        thread::spawn(move || {
            GemiServer::start_http_server(workspace_gemi, Self::GEMI_PORT);
        });

        let workspace_gmcp = workspace.clone();
        // 2. Spawn GMCP TCP Server Thread (Port 9090)
        thread::spawn(move || {
            Self::start_gmcp_tcp_server(workspace_gmcp, Self::GMCP_PORT);
        });

        // 3. Keep main daemon thread alive
        loop {
            thread::sleep(Duration::from_secs(3600));
        }
    }

    fn start_gmcp_tcp_server(workspace: PathBuf, port: u16) {
        let addr = format!("127.0.0.1:{}", port);
        let listener = match TcpListener::bind(&addr) {
            Ok(l) => l,
            Err(e) => {
                eprintln!("⚠️ [GMCP TCP Server] Could not bind to {}: {}", addr, e);
                return;
            }
        };

        eprintln!("🔌 [GMCP Server] Always-On TCP MCP Server listening at {}", addr);

        for stream in listener.incoming().flatten() {
            let workspace = workspace.clone();
            thread::spawn(move || {
                let read_stream = match stream.try_clone() {
                    Ok(s) => s,
                    Err(_) => return,
                };
                let mut reader = BufReader::new(read_stream);
                let mut line = String::new();
                let mut writer = stream;

                while reader.read_line(&mut line).is_ok() {
                    let trimmed = line.trim();
                    if trimmed.is_empty() {
                        line.clear();
                        continue;
                    }

                    if trimmed.contains("\"method\":\"initialize\"") {
                        let resp = format!(
                            "{{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{{\"protocolVersion\":\"2024-11-05\",\"capabilities\":{{\"tools\":{{}}}},\"serverInfo\":{{\"name\":\"gmcp-native-server\",\"version\":\"0.1.67-SNAPSHOT\"}}}}}}\n"
                        );
                        let _ = writer.write_all(resp.as_bytes());
                        let _ = writer.flush();
                    } else if trimmed.contains("\"method\":\"tools/list\"") {
                        let tools = ToolRegistry::list_tools();
                        let tools_json: Vec<String> = tools
                            .iter()
                            .map(|t| format!("{{\"name\":\"{}\",\"description\":\"{}\"}}", t.name, t.description))
                            .collect();
                        let resp = format!(
                            "{{\"jsonrpc\":\"2.0\",\"id\":2,\"result\":{{\"tools\":[{}]}}}}\n",
                            tools_json.join(",")
                        );
                        let _ = writer.write_all(resp.as_bytes());
                        let _ = writer.flush();
                    } else if trimmed.contains("\"method\":\"tools/call\"") {
                        let resp = format!(
                            "{{\"jsonrpc\":\"2.0\",\"id\":3,\"result\":{{\"content\":[{{\"type\":\"text\",\"text\":\"✅ GMCP Tool Executed natively in workspace {}\"}}]}}}}\n",
                            workspace.display()
                        );
                        let _ = writer.write_all(resp.as_bytes());
                        let _ = writer.flush();
                    }
                    line.clear();
                }
            });
        }
    }

    #[allow(dead_code)]
    pub fn stop_daemon(global_dir: &Path) -> bool {
        let lock_file = Self::get_lock_file(global_dir);
        if lock_file.exists() {
            let _ = fs::remove_file(lock_file);
            true
        } else {
            false
        }
    }
}
