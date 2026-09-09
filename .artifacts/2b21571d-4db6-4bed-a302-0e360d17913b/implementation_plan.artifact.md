# Implementation Plan: Native Model Benchmark Tool

This plan introduces a native `benchmark` tool to the `gha` engine. This tool will measure the actual inference latency of local models (GGUF via Ollama or native Candle) and report performance metrics, specifically targeting the Gemma 4 series as requested.

## User Review Required

> [!IMPORTANT]
> The benchmarking process for local models requires a running inference engine. This implementation will primarily target **Ollama** for GGUF models and the **Native Candle** engine. If these runtimes are not available, the tool will report fallback diagnostics.

## Proposed Changes

### [Engine Core]

#### [MODIFY] [models.rs](file:///home/ramadoss/Projects/AI/gha/src/gemi/models.rs)
- Enhance `ModelManager` with a `run_benchmark` method that performs a multi-sample inference test.
- Implement specialized logic for Gemma 4 series detection and performance characterization.

#### [MODIFY] [tools.rs](file:///home/ramadoss/Projects/AI/gha/src/gmcp/tools.rs)
- Add `BenchmarkTool` struct and implement `GhaTool` trait.
- Register the `benchmark` tool in the `ToolRegistry` bootstrap.
- Add `/benchmark` command support if not already implicitly handled by the tool execution logic in `main.rs`.

### [Interface]

#### [MODIFY] [main.rs](file:///home/ramadoss/Projects/AI/gha/src/main.rs)
- Ensure the `/benchmark` command is correctly routed to the `ToolRegistry`.

## Verification Plan

### Automated Tests
- Run `cd test/world && ../../target/debug/gha /benchmark gemma` to verify the tool filters and benchmarks the Gemma models.
- Verify output formatting in the GHA Execution Report.

### Manual Verification
- Inspect the generated report to ensure latency (ms) and tokens-per-second (estimate) are reported accurately.
