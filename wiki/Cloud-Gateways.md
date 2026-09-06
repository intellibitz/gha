# ☁️ Zero-Download Cloud Provider Gateways

`gha` features a built-in gateway bridge that allows you to unleash trillion-parameter AI intelligence with **0 local model downloads** and **0 local disk footprint**.

---

## 🌍 Supported Cloud Providers

`gha` automatically discovers and routes reasoning/completions to cloud providers when their API keys are detected in your environment.

| Provider / Model | Environment Key | Capability |
| :--- | :--- | :--- |
| **Google Gemini AI Studio** | `GEMINI_API_KEY` | **1M–2M token context** for massive codebases. |
| **Mistral AI Cloud** | `MISTRAL_API_KEY` | Elite reasoning & code architecture. |
| **Hugging Face Hub** | `HF_TOKEN` | Access to 100,000+ open-weights models. |
| **DeepSeek Cloud** | `DEEPSEEK_API_KEY` | SOTA math, logic, and deep code reasoning. |
| **Groq Cloud** | `GROQ_API_KEY` | Blazing fast **500+ tokens/second** streaming. |
| **OpenAI Cloud** | `OPENAI_API_KEY` | Industry-standard multimodal REST completions. |

---

## ⚙️ Custom Dynamic Cloud Config (`~/.gha/cloud_providers.json`)

You can also define custom cloud providers dynamically. GMA will auto-discover and list these in `ghai ai models`.

```json
{
  "providers": [
    {
      "name": "Custom Enterprise Cloud",
      "api_url": "https://api.internal.corp/v1/chat/completions",
      "api_key": "sk-your-key",
      "model_id": "llama-3-internal"
    }
  ]
}
```

---

## 🚀 How to use in CLI
```bash
# Verify discovered cloud models:
ghai ai models

# Execute reasoning via cloud gateway:
ghai "Explain the universe using the most capable cloud model"
```
