// GHA Wasm Host Substrate
// RULE 11: Native Integration - High-performance reflex execution environment

use std::path::Path;
use wasmer::{Instance, Module, Store};
use wasmer_wasi::WasiState;
use crate::error::{EaiError, EaiResult};

pub struct WasmHost;

#[unsafe(no_mangle)]
pub extern "C" fn __rust_probestack() {}

impl WasmHost {
    /// Executes a distilled reflex from a Wasm file
    pub fn execute_reflex(wasm_path: &Path, arg: &str) -> EaiResult<String> {
        let mut store = Store::default();
        let module = Module::from_file(&store, wasm_path)
            .map_err(|e| EaiError::Hardware(format!("Failed to load Wasm module: {}", e)))?;

        let mut state_builder = WasiState::new("gha-reflex");
        state_builder.arg(arg);

        let wasi_env = state_builder
            .finalize(&mut store)
            .map_err(|e| EaiError::Sandbox(format!("WASI finalize failed: {:?}", e)))?;

        let import_object = wasi_env.import_object(&mut store, &module)
            .map_err(|e| EaiError::Sandbox(format!("WASI import failed: {:?}", e)))?;

        let instance = Instance::new(&mut store, &module, &import_object)
            .map_err(|e| EaiError::Hardware(format!("Wasm instantiation failed: {}", e)))?;

        let start = instance.exports.get_function("_start")
            .map_err(|_| EaiError::Hardware("Wasm missing _start entry point".into()))?;

        // This is a blocking call
        start.call(&mut store, &[])
            .map_err(|e| EaiError::Hardware(format!("Wasm execution failed: {}", e)))?;

        Ok("Wasm execution success (Reflex Distilled)".to_string())
    }
}
