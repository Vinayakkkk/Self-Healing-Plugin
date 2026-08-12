# 🚀 Self-Healing Plugin

An intelligent Selenium WebDriver plugin that automatically heals broken locators at runtime using DOM analysis, semantic context, candidate ranking, validation, capability analysis, caching, recovery, and AI-assisted decision making.

---

## 📌 Overview

Modern web applications frequently change their UI, causing Selenium tests to fail because locators become outdated.

**Self-Healing Plugin** minimizes test maintenance by automatically identifying alternative locators and recovering from broken elements without requiring manual updates.

The framework combines deterministic algorithms with execution context, semantic analysis, DOM intelligence, candidate ranking, validation, capability analysis, locator stability, recovery mechanisms, and optional AI-assisted healing.

The long-term goal is to evolve the framework into an **Automation Intelligence Engine** that understands not only which locator failed, but also what the automation was trying to accomplish.

---

# ✨ Features

- ✅ Runtime Self-Healing
- ✅ Single Element Healing
- ✅ Collection Healing
- ✅ Duplicate Element Resolution
- ✅ AI Assisted Locator Selection
- ✅ Runtime Locator Cache
- ✅ Persistent Locator Cache
- ✅ Failure Context Generation
- ✅ Execution Context Tracking
- ✅ Execution Recording
- ✅ DOM Candidate Discovery
- ✅ Candidate Ranking
- ✅ Candidate Filtering
- ✅ Candidate Validation
- ✅ Expected Context Resolution
- ✅ Semantic Evidence Analysis
- ✅ Expected Element Verification
- ✅ Expected Outcome Verification
- ✅ Element Capability Analysis
- ✅ Healing Decision Engine
- ✅ XPath Fallback Generation
- ✅ Unique Locator Generation
- ✅ Source Code Repair
- ✅ Source Code Analysis
- ✅ Variable Analysis
- ✅ Locator Analysis
- ✅ Dynamic Attribute Detection
- ✅ Locator Stability Analysis
- ✅ Shadow DOM Healing
- ✅ iFrame Healing
- ✅ Action Recovery
- ✅ DOM Recovery
- ✅ Healing Analytics
- ✅ JSON Healing Reports
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
                    HealingWebElement
                             │
                             ▼
                    SelfHealingEngine
                             │
                             ▼
                 FailureContextBuilder
                             │
                             ▼
                     FailureContext
                             │
                 ┌───────────┴───────────┐
                 ▼                       ▼
          Execution Context       Expected Context
                 │                       │
                 └───────────┬───────────┘
                             ▼
                    Healing Decision Engine
                             │
                 ┌───────────┴───────────┐
                 ▼                       ▼
          Capability Analysis      Healing Pipeline
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
                              Expected Verification
                                         │
                          ┌──────────────┴──────────────┐
                          ▼                             ▼
                Deterministic Healing              AI Healing
                          │                             │
                          └──────────────┬──────────────┘
                                         ▼
                              Duplicate Resolution
                                         │
                                         ▼
                              Unique Locator Generation
                                         │
                                         ▼
                              Browser Context Recovery
                            (Shadow DOM / iFrame)
                                         │
                                         ▼
                                  Locator Cache
                                         │
                                         ▼
                               Source Code Repair
                                         │
                                         ▼
                              Expected Outcome Check
                                         │
                                         ▼
                              Healing Analytics
                                         │
                                         ▼
                              Return Healed Element
```

---

# 🔄 Healing Workflow

1. Selenium operation is executed.
2. Element lookup or action fails.
3. FailureContext is generated.
4. Execution context is analyzed.
5. Expected context is resolved.
6. Runtime cache is checked.
7. DOM is analyzed.
8. Candidate elements are collected.
9. Candidate attributes and stability are analyzed.
10. Candidates are ranked.
11. Invalid candidates are filtered.
12. Element capability is evaluated.
13. Candidate is validated against the browser.
14. HealingDecisionEngine determines whether healing is safe.
15. Deterministic healing is attempted.
16. If deterministic healing fails, AI suggests a locator.
17. AI suggestions are validated before use.
18. Duplicate candidates are resolved.
19. A unique locator can be generated when required.
20. Successful locators are cached.
21. Source code can optionally be repaired.
22. Expected outcome is verified.
23. Healing analytics are recorded.
24. Test execution continues.
---

# 📂 Project Structure

```
src
├── main
│ ├── ai
│ ├── analysis
│ ├── analyzer
│ ├── builder
│ ├── cache
│ ├── capability
│ ├── config
│ ├── context
│ ├── core
│ ├── decision
│ ├── dom
│ ├── dynamic
│ ├── engine
│ ├── execution
│ ├── extractor
│ ├── expected
│ │ ├── provider
│ │ └── verifier
│ ├── filter
│ ├── generator
│ ├── intent
│ ├── logging
│ ├── metrics
│ ├── model
│ ├── outcome
│ │ ├── decision
│ │ ├── engine
│ │ ├── model
│ │ ├── selector
│ │ └── verifier
│ ├── pipeline
│ ├── policy
│ ├── ranking
│ ├── recovery
│ ├── repair
│ ├── report
│ ├── resolver
│ ├── shadow
│ ├── util
│ ├── validator
│ └── verification
```

---

# ⚙ Core Components
| Component | Description |
| ----------------------- | -------------------------------------------- |
| HealingWebDriver | Main Selenium WebDriver integration |
| HealingWebElement | Healing-aware WebElement wrapper |
| SelfHealingEngine | Main healing orchestration engine |
| FailureContextBuilder | Builds complete failure context |
| ExecutionAnalyzer | Analyzes previous execution flow |
| ExecutionRecorder | Records execution steps |
| ExpectedContextManager | Resolves expected element context |
| DomCandidateFinder | Discovers DOM candidates |
| CandidateRanker | Ranks possible replacement elements |
| CandidateFilter | Removes unsuitable candidates |
| CandidateValidator | Validates candidates in the browser |
| ActionCapabilityResolver | Determines element/action capability |
| CapabilityValidator | Validates action capability |
| DynamicAttributeDetector | Detects dynamic locator patterns |
| ContextAwareLocatorGenerator | Generates context-aware locators |
| UniqueLocatorGenerator | Generates unique healing locators |
| DuplicateResolver | Resolves duplicate candidate elements |
| HealingDecisionEngine | Determines whether healing is safe |
| ActionRecoveryEngine | Performs action recovery |
| ShadowDomHealingEngine | Handles Shadow DOM healing |
| ExpectedOutcomeEngine | Determines expected post-action outcome |
| ExpectedElementVerifier | Verifies expected element behavior |
| SourceCodeRepairEngine | Performs source code repair |
| HealingReportManager | Generates healing reports |


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

-Failed locator
-Healed locator
-Expected intent
-Confidence score
-Candidate score
-Healing type
-Healing source
-Timestamp
-Healing statistics

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
| ------------------------------- | :----: |
| Runtime Self-Healing | ✅ |
| Single Element Healing | ✅ |
| Collection Healing | ✅ |
| Deterministic Healing | ✅ |
| AI-Assisted Healing | ✅ |
| Runtime Locator Cache | ✅ |
| Persistent Locator Cache | ✅ |
| Failure Context Generation | ✅ |
| Execution Context Tracking | ✅ |
| Execution Recording | ✅ |
| Variable Analysis | ✅ |
| Locator Analysis | ✅ |
| DOM Candidate Discovery | ✅ |
| Candidate Filtering | ✅ |
| Candidate Ranking | ✅ |
| Candidate Validation | ✅ |
| Semantic Evidence Analysis | ✅ |
| Expected Context Resolution | ✅ |
| Expected Element Verification | ✅ |
| Expected Outcome Verification | ✅ |
| Element Capability Analysis | ✅ |
| Dynamic Attribute Detection | ✅ |
| Locator Stability Analysis | ✅ |
| Duplicate Element Resolution | ✅ |
| Unique Locator Generation | ✅ |
| XPath Fallback Generation | ✅ |
| Action Recovery | ✅ |
| DOM Recovery | ✅ |
| Shadow DOM Healing | ✅ |
| iFrame Healing | ✅ |
| Healing Decision Engine | ✅ |
| Source Code Analysis | ✅ |
| Source Code Repair | ✅ |
| Healing Analytics | ✅ |
| JSON Healing Reports | ✅ |
| Healing Logging | ✅ |


---

# 🛣 Roadmap


### ✅ Phase 1 (Completed)
- Locator Healing Engine
- Collection Healing
- AI-Assisted Healing
- Runtime & Persistent Cache
- Source Code Repair
- Shadow DOM Healing
- iFrame Healing
- Analytics & Reporting

### 🚧 Phase 2 (Completed)
- Runtime Self-Healing
- Execution Context
- Expected Context
- Semantic Evidence
- Capability Engine
- DOM Intelligence
- Candidate Generation
- Candidate Filtering
- Candidate Ranking Foundation
- Candidate Validation
- Dynamic Attribute Detection
- Locator Stability Analysis
- Duplicate Resolution
- Unique Locator Generation
- Action Recovery
- Shadow DOM Healing
- iFrame Healing
- Expected Outcome Verification
- Source Code Repair
- Runtime & Persistent Cache
- Healing Analytics & Reporting

### 🔮 Future Enhancements
- Smart Synchronization Engine
- Advanced AI Root Cause Analysis
- Adaptive Candidate Ranking
- Self-Learning Locator Selection
- Enterprise Healing Dashboard
- Advanced Healing Analytics
- Performance Optimization
- Multi-Browser Optimization

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

## Current Version

**v1.1.0 — Automation Intelligence Foundation**