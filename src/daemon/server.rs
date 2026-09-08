// 🚀 Always-On GMA Master Daemon Process Manager
// 100% Rust implementation managing GMCP (Port 9090), GEMI (Port 9091) & A2A Cluster UDP (Port 9092)

use std::fs;
use std::io::{BufRead, BufReader, Write};
use std::net::{TcpListener, TcpStream, UdpSocket};
use std::path::{Path, PathBuf};
use std::process::{Command, Stdio};
use std::thread;
use std::time::Duration;

use crate::gemi::GemiServer;
use crate::gmcp::server::{extract_json_id, extract_tool_arg, extract_tool_name};
use crate::gmcp::tools::ToolRegistry;

pub struct GmaDaemon;

impl GmaDaemon {
    pub const GMCP_PORT: u16 = 9090;
    #[allow(dead_code)]
    pub const GEMI_PORT: u16 = 9091;
    #[allow(dead_code)]
    pub const UDP_DISCOVERY_PORT: u16 = 9092;

    pub fn get_lock_file(global_dir: &Path) -> PathBuf {
        global_dir.join("gma.lock")
    }

    pub fn check_status(global_dir: &Path) -> Option<u32> {
        let lock_file = Self::get_lock_file(global_dir);
        if let Ok(content) = fs::read_to_string(&lock_file)
            && let Ok(pid) = content.trim().parse::<u32>()
        {
            if cfg!(target_os = "linux") {
                let proc_path = PathBuf::from(format!("/proc/{}", pid));
                if proc_path.exists() {
                    return Some(pid);
                }
            } else {
                // Cross-platform fallback for Windows & macOS: TCP ping on GMCP server port 9090
                let addr = format!("127.0.0.1:{}", Self::GMCP_PORT);
                if let Ok(addr_parsed) = addr.parse()
                    && TcpStream::connect_timeout(&addr_parsed, Duration::from_millis(100)).is_ok()
                {
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
        let bin_name = if cfg!(target_os = "windows") { "bin/gha-engine.exe" } else { "bin/gha-engine" };
        let global_bin = global_dir.join(bin_name);

        let bin_to_run = if let Some(ref exe) = current_exe {
            exe.clone()
        } else if global_bin.exists() {
            global_bin
        } else {
            PathBuf::from(if cfg!(target_os = "windows") { "gha.exe" } else { "gha" })
        };

        if cfg!(target_os = "windows") {
            let _ = Command::new(&bin_to_run)
                .arg("daemon-start")
                .arg(workspace.to_str().unwrap_or("."))
                .stdin(Stdio::null())
                .stdout(Stdio::null())
                .stderr(Stdio::null())
                .spawn();
        } else {
            let _ = Command::new("nohup")
                .arg(bin_to_run)
                .arg("daemon-start")
                .arg(workspace.to_str().unwrap_or("."))
                .stdin(Stdio::null())
                .stdout(Stdio::null())
                .stderr(Stdio::null())
                .spawn();
        }
    }

    pub fn run_daemon_loop(workspace: PathBuf, global_dir: PathBuf) {
        let pid = std::process::id();
        let lock_file = Self::get_lock_file(&global_dir);
        let _ = fs::write(&lock_file, pid.to_string());

        let cfg = crate::sandbox::manager::GhaConfig::load(&global_dir);

        // 🚀 High-Priority Hardware-Bounded Model Auto-Provisioning (Background Thread)
        crate::gemi::models::ModelManager::spawn_background_hardware_model_provisioner(&workspace);

        let workspace_gemi = workspace.clone();
        let gemi_port = cfg.gemi_port;
        // 1. Spawn GEMI HTTP REST Server Thread (Port 9091 / Dynamic)
        thread::spawn(move || {
            GemiServer::start_http_server(workspace_gemi, gemi_port);
        });

        let workspace_gmcp = workspace.clone();
        let gmcp_port = cfg.gmcp_port;
        // 2. Spawn GMCP TCP Server Thread (Port 9090 / Dynamic)
        thread::spawn(move || {
            Self::start_gmcp_tcp_server(workspace_gmcp, gmcp_port);
        });

        let udp_port = cfg.udp_discovery_port;
        // 3. Spawn A2A Cluster UDP Discovery Listener Thread (Port 9092 / Dynamic)
        thread::spawn(move || {
            Self::start_udp_discovery_server(udp_port);
        });

        // 4. Keep main daemon thread alive
        loop {
            thread::sleep(Duration::from_secs(3600));
        }
    }

    fn start_udp_discovery_server(port: u16) {
        let addr = format!("0.0.0.0:{}", port);
        if let Ok(socket) = UdpSocket::bind(&addr) {
            eprintln!("🌐 [A2A Cluster UDP] Discovery listener active on {}", addr);
            let mut buf = [0u8; 512];
            while let Ok((amt, src)) = socket.recv_from(&mut buf) {
                let msg = String::from_utf8_lossy(&buf[..amt]);
                if msg.contains("GHA_LAN_PING") {
                    let pong = format!("GHA_LAN_PONG:gha-daemon-node:{}", Self::GMCP_PORT);
                    let _ = socket.send_to(pong.as_bytes(), src);
                }
            }
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
                        let id = extract_json_id(trimmed).unwrap_or(1);
                        let resp = format!(
                            "{{\"jsonrpc\":\"2.0\",\"id\":{},\"result\":{{\"protocolVersion\":\"2024-11-05\",\"capabilities\":{{\"tools\":{{}}}},\"serverInfo\":{{\"name\":\"gmcp-native-server\",\"version\":\"0.1.87\"}}}}}}\n",
                            id
                        );
                        let _ = writer.write_all(resp.as_bytes());
                        let _ = writer.flush();
                    } else if trimmed.contains("\"method\":\"tools/list\"") {
                        let id = extract_json_id(trimmed).unwrap_or(2);
                        let tools = ToolRegistry::list_tools();
                        let tools_json: Vec<String> = tools
                            .iter()
                            .map(|t| format!("{{\"name\":\"{}\",\"description\":\"{}\"}}", t.name, t.description))
                            .collect();
                        let resp = format!(
                            "{{\"jsonrpc\":\"2.0\",\"id\":{},\"result\":{{\"tools\":[{}]}}}}\n",
                            id, tools_json.join(",")
                        );
                        let _ = writer.write_all(resp.as_bytes());
                        let _ = writer.flush();
                    } else if trimmed.contains("\"method\":\"tools/call\"") {
                        let id = extract_json_id(trimmed).unwrap_or(3);
                        let tool_name = extract_tool_name(trimmed).unwrap_or_else(|| "status".to_string());
                        let tool_arg = extract_tool_arg(trimmed).unwrap_or_default();

                        let result_text = ToolRegistry::execute_tool(&tool_name, &tool_arg, &workspace);
                        let escaped_text = serde_json::to_string(&result_text).unwrap_or_default();

                        let resp = format!(
                            "{{\"jsonrpc\":\"2.0\",\"id\":{},\"result\":{{\"content\":[{{\"type\":\"text\",\"text\":{}}}]}}}}\n",
                            id, escaped_text
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
