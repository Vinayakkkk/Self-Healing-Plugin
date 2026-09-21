# 🚀 Self-Healing Plugin

An intelligent, enterprise-grade Selenium WebDriver self-healing framework that automatically detects broken locators, understands the intended element, discovers replacement candidates, validates them, ranks them using deterministic and semantic evidence, repairs Page Object source code, and records the healing outcome for future learning.

**Current Version: v2.1.0 — The Unified Collection & Source Repair Release**

# 📌 Overview

Modern web applications frequently change DOM structures, element attributes, IDs, names, classes, and other locator characteristics. These changes can cause otherwise valid Selenium automation tests to fail because their locators no longer identify the intended element.

**Self-Healing Plugin** automatically intercepts these failures and attempts to recover the intended element without requiring immediate manual test maintenance.

The framework combines:

- Failure interception



- Unified healing context



- Dynamic Variable Tokenization



- Plural Collection / List Healing



- Deterministic Candidate Ranking (Math-based)



- Runtime healing



- Locator caching



- Shadow DOM & iFrame support



- **AST-based Page Object source code repair**



- JSON reporting




The v2.1.0 architecture introduces a **decoupled, dual-engine healing strategy pipeline** where deterministic DOM strategies and AI-assisted semantic strategies independently evaluate failures, routing to the fastest and most accurate solution without hardcoded application logic.

# ✨ Enterprise Features

### 🔹 Deterministic Plural Collection Healing (Ranking 2.0)

The framework natively supports healing lists and collections of elements. It cures standard Selenium "DOM blindness" by dynamically scanning structural attributes, applying mathematical sub-string heuristics to rank valid plural structures, and strictly validating element counts before passing them back to your test.

### 🔹 Dynamic Variable Tokenization

No hardcoded dictionaries required. If your legacy locator completely changes, the engine dynamically tokenizes your Page Object variable names (e.g., parsing `navigationItems` into `"navigation"` and `"items"`). It uses this semantic intent to mathematically rank newly generated DOM candidates, ensuring perfect accuracy.

### 🔹 Automatic Page Object Repair (AST Engine)

After a successful healing decision, the framework reaches into your actual physical Java files and permanently fixes the broken code.

Modifications are performed safely through Abstract Syntax Tree (AST) analysis rather than unsafe string replacement, ensuring your formatting and logic remain intact.

### 🔹 Dual-Engine Strategy Routing (DOM + AI)

The framework routes failures mathematically to the most efficient engine:

- **Clean Code (Deterministic DOM Engine):** If a variable is named clearly, the DOM Engine heals it in milliseconds at zero cost using mathematical token overlap and structural scoring.



- **Garbage Code (AI Semantic Engine):** If a variable is poorly named or the DOM structure was entirely rewritten, the DOM Engine gracefully fails without blindly guessing. The Orchestrator automatically routes the failure to the AI Engine, which uses LLM common sense to deduce the typo and heal the element.




### 🔹 Persistent Learning & Caching

Successful healing results are retained in a local `healing-cache.json`. This avoids repeatedly performing expensive DOM analysis when the framework has already learned a reliable replacement locator.

# ⚡ Installation

Add the framework to your target Selenium project via Maven.

XML

```
<dependency>
    <groupId>com.vinayak</groupId>
    <artifactId>vinayak-healing-plugin</artifactId>
    <version>2.1.0</version>
</dependency>

```

# 💻 How to Use the Plugin

The plugin is designed to wrap your existing Selenium setup with zero disruption to your current Page Object Model (POM) architecture.

### Step 1: Initialize the HealingWebDriver

Wrap your standard Selenium `WebDriver` with the `HealingWebDriver`. This activates the interception engine.

Java

```
import org.openqa.selenium.chrome.ChromeDriver;
import com.vinayak.healing.core.HealingWebDriver;

public class BaseTest {
    protected HealingWebDriver driver;

    public void setUp() {
        ChromeDriver baseDriver = new ChromeDriver();
        // Wrap the driver to enable self-healing
        driver = new HealingWebDriver(baseDriver); 
    }
}

```

### Step 2: Define Page Objects Normally

Declare your locators as standard `By` fields at the top of your class. **The AST Repair Engine relies on these class-level declarations to physically fix your code.**

Java

```
import org.openqa.selenium.By;

public class UserDashboardPage {
    // Standard declarations - the framework will repair these if they break!
    private final By profileButton = By.id("user-profile");
    private final By navigationLinks = By.className("nav-item");
}

```

### Step 3: Use the `HealingWait` Utility

For interactions and collections, use the `HealingWait` class. Pass the locator, expected conditions, and the **variable name as a string** (so the engine understands your semantic intent).

Java

```
import com.vinayak.healing.util.HealingWait;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.WebElement;

public class UserDashboardPage {
    private HealingWebDriver driver;
    private HealingWait wait;

    private final By profileButton = By.id("user-profile");
    private final By navigationLinks = By.className("nav-item");

    public UserDashboardPage(HealingWebDriver driver) {
        this.driver = driver;
        this.wait = new HealingWait(driver, Duration.ofSeconds(10));
    }

    public void clickProfile() {
        // Automatically heals single elements
        driver.findElement(profileButton).click(); 
    }

    public int getNavigationMenuCount() {
        // Automatically heals plural collections!
        // Parameters: Locator, Expected Count, Variable Name
        List<WebElement> navItems = wait.waitForElements(navigationLinks, 5, "navigationLinks");
        return navItems.size();
    }
}

```

### What happens when a test breaks?

If the developers change `className("nav-item")` to `className("main-menu-link")` in the next release:

1. `HealingWait` intercepts the `TimeoutException`.



2. The `CollectionHealingEngine` scans the live DOM and generates candidates.



3. The Ranker tokenizes your variable `"navigationLinks"` and finds the mathematical winner.



4. The test **passes successfully**.



5. The `SourceCodeRepairEngine` overwrites your physical Java file so the locator is permanently updated for tomorrow's run.




# ⚙ Configuration

The framework uses a decoupled configuration file (`healing.properties`) placed in your `src/main/resources` folder.

Properties

```
# Enable/Disable Healing entirely
healing.enabled=true

# Enable AI Fallback Strategy
ai.enabled=true

# AI Engine Routing (Supports "ollama" or "openrouter")
ai.provider=openrouter

# OpenRouter Configuration (Cloud LLM)
ai.openrouter.model=openrouter/free
ai.openrouter.endpoint=https://openrouter.ai/api/v1/chat/completions
ai.openrouter.key=YOUR_API_KEY_HERE

# Ollama Configuration (Local LLM)
ai.ollama.model=qwen3:8b
ai.ollama.endpoint=http://localhost:11434/api/generate

```

# 🏗 Architecture Pipeline

Plaintext

```
                         Target Selenium Test Project
                                      │
                                      ▼
                             HealingWebDriver
                                      │
                                      ▼
                           Failure Interception
                                      │
                                      ▼
                             Unified HealingContext
                                      │
             ┌────────────────────────┼────────────────────────┐
             │                        │                        │
             ▼                        ▼                        ▼
      Variable Name           Expected Intent            Source Identity
             │                        │                        │
             └────────────────────────┼────────────────────────┘
                                      ▼
                         SelfHealingOrchestrator
                                      │
                 ┌────────────────────┼────────────────────┐
                 ▼                    ▼                    ▼
          Cache Strategy        DOM Strategy        AI Strategy
                 │                    │                    │
                 └────────────────────┼────────────────────┘
                                      ▼
                      Dynamic Candidate Generation
                                      │
                                      ▼
                     Mathematical Candidate Ranking 
                                      │
                                      ▼
                            Validation Engine
                          (Counts & Visibility)
                                      │
                          ┌───────────┴───────────┐
                          ▼                       ▼
                       REJECT                   ACCEPT
                                                  │
                                                  ▼
                                         Runtime Interaction
                                                  │
                                                  ▼
                                       AST Source Code Repair
                                                  │
                                                  ▼
                                        JSON Cache & Analytics

```

# 📊 Enterprise Analytics Dashboard

The framework generates a structured HTML dashboard containing healing and execution analytics after every run.

Metrics include:

- Healing success rate



- Healing source distribution (DOM vs. AI vs. Cache)



- Failed locator vs. Healed locator tracking



- Candidate confidence scores



- Source-code repair confirmations




# 🛣 Roadmap

**Completed — v2.1.0**

- Plural Collection Healing (God Mode 2.0 bypass)



- Dynamic Variable Tokenization



- Mathematical Candidate Ranking 2.0



- AST Source Code Repair



- Runtime Locator Cache




**Future Enhancements — v3.0.0+**

- Distributed cross-project healing intelligence



- Jira / Slack integrations for automated bug reporting



- Playwright & Appium support



- Computer-vision-assisted healing fallbacks




# 🤝 Contributing

Contributions are welcome. If you find a bug or identify a healing edge case:

- Open an issue



- Submit a pull request



- Provide a reproducible healing scenario




The primary design principle is:

> **Every new locator failure should be treated as a missing framework capability rather than an application-specific workaround.**
>
>
>

# 📄 License

This project is licensed under the MIT License.

# 👨‍💻 Author

**Vinayak Hanagi**

Automation Test Engineer

GitHub: `[https://github.com/Vinayakkkk](https://github.com/Vinayakkkk)`

# ⭐ Support

If you find the project useful, consider giving it a ⭐ on GitHub. It helps others discover the framework and supports continued development.