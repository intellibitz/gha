# 👁️ Multimodal Vision & Audio Processing

`gha` supports high-performance native tool plugins for processing non-textual data, bridging the gap between files and multi-sensory intelligence.

---

## 🖼️ Vision Analysis (`ghai ai vision`)

Analyze images using multimodal cloud models or local GGUF vision models (LLaVA/Moondream).
*   **Image Understanding**: Describe scenes, identify UI components, or explain diagrams.
*   **Format Support**: JPG, PNG, WEBP, GIF.

## 📄 OCR Text Extraction (`ghai ai ocr`)

GMA uses vision-reasoning pipelines to extract high-fidelity text from documents, screenshots, and handwritten notes. Unlike traditional OCR, GMA understands context and layout.

## 🎙️ Audio Processing (`ghai ai transcribe`)

*   **Transcription**: Dispatches audio files to local high-performance Whisper-cli binaries or Cloud STT providers.
*   **Synthesis**: Convert text to natural-sounding speech audio files, saved directly into the sandbox vault.

---

## 🧪 CLI Examples

```bash
# Analyze a screenshot of a bug:
ghai ai vision "screenshots/bug.png" "Explain why this UI component is misaligned."

# Extract text from a scanned PDF image:
ghai ai ocr "docs/invoice.jpg"

# Transcribe a voice note:
ghai ai transcribe "recordings/mission_plan.m4a"
```
