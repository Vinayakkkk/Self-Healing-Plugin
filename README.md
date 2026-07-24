# 🚀 Self-Healing Plugin

An intelligent Selenium WebDriver plugin that automatically heals broken locators at runtime using DOM analysis, candidate ranking, validation, caching, and AI-assisted decision making.

---

## 📌 Overview

Modern web applications frequently change their UI, causing Selenium tests to fail because locators become outdated.

**Self-Healing Plugin** minimizes test maintenance by automatically identifying alternative locators and recovering from broken elements without requiring manual updates.

The framework combines deterministic algorithms with AI-assisted healing to improve reliability and reduce flaky test failures.

---

# ✨ Features

- ✅ Runtime Self-Healing
- ✅ HealingWebDriver Wrapper
- ✅ DOM Candidate Discovery
- ✅ Failure Context Generation
- ✅ Candidate Ranking
- ✅ Candidate Filtering
- ✅ Candidate Validation
- ✅ Healing Decision Engine
- ✅ AI Assisted Locator Selection
- ✅ Runtime Locator Cache
- ✅ Persistent Cache
- ✅ XPath Fallback Generation
- ✅ Collection Healing
- ✅ Dynamic Locator Handling
- ✅ Healing Analytics
- ✅ JSON Healing Reports
- ✅ Source Code Analysis
- ✅ Variable Analysis
- ✅ Locator Analysis
- ✅ Execution Context Tracking
- ✅ Logging Framework

---

# 🏗 Architecture

```
                    Selenium Test
                          │
                          ▼
                 HealingWebDriver
                          │
                          ▼
                 SelfHealingEngine
                          │
          ┌───────────────┴────────────────┐
          ▼                                ▼
  FailureContext                    Locator Cache
          │                                │
          ▼                                ▼
    Healing Pipeline               Cached Locator
          │
          ▼
   DOM Candidate Finder
          │
          ▼
   Candidate Ranking
          │
          ▼
   Candidate Filtering
          │
          ▼
   Candidate Validation
          │
          ▼
  Healing Decision Engine
          │
    ┌─────┴──────┐
    ▼            ▼
Deterministic   AI Healing
 Healing
          │
          ▼
 Return Healed WebElement
```

---

# 🔄 Healing Workflow

1. Selenium fails to locate an element.
2. FailureContext is generated.
3. Runtime cache is checked.
4. DOM is analyzed.
5. Candidate elements are collected.
6. Candidates are ranked.
7. Invalid candidates are filtered.
8. Best candidate is validated.
9. HealingDecisionEngine determines whether healing is safe.
10. If deterministic healing fails, AI suggests the best locator.
11. Validated locator is cached.
12. Test execution continues.

---

# 📂 Project Structure

```
src
 ├── main
 │    ├── ai
 │    ├── analytics
 │    ├── analyzer
 │    ├── builder
 │    ├── cache
 │    ├── config
 │    ├── context
 │    ├── core
 │    ├── decision
 │    ├── dom
 │    ├── engine
 │    ├── execution
 │    ├── extractor
 │    ├── factory
 │    ├── filter
 │    ├── intent
 │    ├── logging
 │    ├── metrics
 │    ├── model
 │    ├── pipeline
 │    ├── policy
 │    ├── ranking
 │    ├── report
 │    ├── source
 │    ├── util
 │    └── validator
```

---

# ⚙ Core Components

| Component | Description |
|------------|-------------|
| HealingWebDriver | Wrapper around Selenium WebDriver |
| SelfHealingEngine | Main healing engine |
| FailureContextBuilder | Creates healing context |
| DomCandidateFinder | Finds candidate elements |
| CandidateRanker | Scores candidate locators |
| CandidateFilter | Removes weak candidates |
| CandidateValidator | Validates candidate elements |
| HealingDecisionEngine | Determines whether healing is safe |
| LocatorCache | Runtime & persistent cache |
| HealingPipeline | Executes deterministic healing flow |
| AiModelClient | Communicates with AI model |
| HealingAnalytics | Collects healing statistics |
| HealingReportManager | Generates JSON reports |

---

# ⚡ Installation

Clone the repository

```bash
git clone https://github.com/Vinayakkkk/Self-Healing-Plugin.git
```

Move into the project

```bash
cd Self-Healing-Plugin
```

Build the project

```bash
mvn clean install
```

---

# 📦 Requirements

- Java 21+
- Maven 3.9+
- Selenium 4+
- ChromeDriver
- Ollama (for AI healing)

---

# ▶ Quick Start

```java
WebDriver driver = new ChromeDriver();

HealingConfig config = new HealingConfig();

WebDriver healingDriver =
        new HealingWebDriver(driver, config);

healingDriver.get("https://example.com");
```

---

# 📊 Reports

The framework generates healing reports including:

- Failed locator
- Healed locator
- Confidence score
- Healing type
- Timestamp
- Healing statistics

Example:

```json
{
  "failedLocator":"By.id: username",
  "healedLocator":"By.name: username",
  "confidence":98.7,
  "timestamp":"2026-07-08T10:30:15"
}
```

---

# 📈 Current Capabilities

| Feature | Status |
|---------|:------:|
| Runtime Healing | ✅ |
| AI Healing | ✅ |
| DOM Analysis | ✅ |
| Candidate Ranking | ✅ |
| Candidate Validation | ✅ |
| Runtime Cache | ✅ |
| Persistent Cache | ✅ |
| Collection Healing | ✅ |
| XPath Fallback | ✅ |
| Healing Analytics | ✅ |
| JSON Reports | ✅ |
| Logging | ✅ |

---

# 🛣 Roadmap

Planned improvements include:

- DOM Fingerprinting
- Visual Element Recognition
- Dashboard for Healing Analytics
- Browser Extension Support
- Cloud Execution Support
- Multi-AI Model Integration
- Performance Optimization
- Enhanced Reporting

---

# 🤝 Contributing

Contributions are welcome.

If you find a bug or have ideas for improvement, feel free to:

- Open an issue
- Submit a pull request
- Suggest new features

---

# 📄 License

This project is licensed under the MIT License.

---

# 👨‍💻 Author

**Vinayak Hanagi**

Automation Test Engineer

GitHub: https://github.com/Vinayakkkk

---

# ⭐ Support

If you find this project useful, consider giving it a ⭐ on GitHub.

It helps others discover the project and motivates further development.