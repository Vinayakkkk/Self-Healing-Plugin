# 🚀 Vinayak Selenium Self-Healing Plugin

**Enterprise-grade Selenium WebDriver self-healing for Java automation frameworks.**

Vinayak Selenium Self-Healing Plugin automatically detects broken Selenium locators at runtime, understands the intended element, discovers replacement candidates from the live DOM, validates those candidates through multiple safety gates, selects a mathematically supported recovery, executes the original Selenium action, and records the healing result for future runs.

> **Current Version: v2.1.0**

---

# 📌 What Is This?

Modern web applications frequently change:

- `id`
- `name`
- `class`
- `placeholder`
- XPath structure
- DOM hierarchy
- labels
- element attributes
- component structure

A Selenium test can therefore fail even though the application functionality is still correct.

For example:

```java
private final By username = By.name("old_username");
```

The application changes the element to:

```html
<input name="username" placeholder="Username" type="text">
```

Traditional Selenium automation fails because:

```java
driver.findElement(By.name("old_username"));
```

can no longer locate the element.

The Self-Healing Plugin intercepts the failure and attempts to recover the intended element automatically.

---

# 🎯 Core Goal

The framework is designed around one principle:

> **A locator failure should be treated as a framework capability problem, not immediately as a test-maintenance problem.**

The plugin attempts to determine:

```text
What element was the test trying to interact with?
```

rather than simply asking:

```text
Which element can I find?
```

This distinction is important because a page can contain multiple elements that technically satisfy the same action.

The framework therefore combines:

- source-code context
- Page Object variable names
- Selenium action
- expected intent
- failed locator identity
- live DOM structure
- element attributes
- labels
- element type
- physical uniqueness
- candidate scoring
- semantic validation
- safety boundaries
- optional AI assistance
- cache
- learning history
- analytics

---

# ✨ Key Features

## 🔹 Automatic Runtime Healing

The plugin operates through the Java Agent and intercepts Selenium execution without requiring test code to use a custom WebDriver.

Your application continues using:

```java
WebDriver driver = new ChromeDriver();
```

and:

```java
driver.findElement(locator).click();
```

The healing framework operates underneath the normal Selenium API.

---

## 🔹 Normal Selenium API

No special driver is required in your test project.

Use:

```java
WebDriver driver = new ChromeDriver();

driver.findElement(By.name("username"))
      .sendKeys("Admin");
```

The standard v2.1.0 workflow does not require:

```java
new HealingWebDriver(...)
```

---

## 🔹 Action-Aware Healing

The framework understands the Selenium action involved in the failure.

Examples:

```text
CLICK
SEND_KEYS
CLEAR
SELECT
GET_TEXT
```

The healing context carries the action throughout the pipeline.

Example:

```text
Variable      = username
Action        = SEND_KEYS
Intent        = INPUT
```

---

## 🔹 Semantic Identity Validation

The framework does not simply select the first element that matches the expected HTML tag.

If the failed element represents:

```text
username
```

the framework should not accidentally recover:

```text
password
```

even if both are:

```html
<input>
```

Example:

```text
Expected identity:
username

Candidate:
name=password

Result:
REJECT
```

---

## 🔹 Physical Uniqueness Validation

A candidate must also be physically valid in the current DOM.

The framework evaluates:

- occurrence count
- uniqueness
- visibility
- enabled state
- element type
- structural validity

A locator matching multiple unrelated elements is not automatically considered safe.

---

## 🔹 Deterministic Candidate Generation

The framework scans the live DOM and generates possible replacement locators.

Examples include:

```text
id
name
class
placeholder
text
label-based XPath
structural XPath
attribute-based locators
```

The generated candidates are then ranked.

---

## 🔹 Mathematical Candidate Ranking

Candidates are not selected randomly.

The ranking system considers multiple evidence categories:

```text
Intent
Identity
Stability
Locator quality
Uniqueness
DOM structure
Semantic evidence
```

The actual score is calculated by the framework's ranking pipeline.

---

# 🤖 AI-Assisted Healing

AI is an optional fallback capability.

The deterministic healing pipeline remains the primary safety boundary.

AI does not directly receive permission to execute arbitrary locators.

Instead, AI can help identify a candidate or candidate index.

The resulting candidate must still pass the framework's normal validation and safety boundaries.

Conceptually:

```text
AI
 │
 ▼
Candidate suggestion
 │
 ▼
Deterministic validation
 │
 ├── REJECT
 │
 └── ACCEPT
        │
        ▼
   Selenium action
```

AI therefore does not bypass the safety system.

---

# 🧠 Persistent Learning

Successful healing decisions can be stored locally.

The framework maintains information such as:

```text
Page Object
Variable
Action
Expected Intent
Failed Locator
Recovered Locator
```

Example:

```text
LoginPage
username
SEND_KEYS
INPUT
By.name: old_username
        ↓
By.name: username
```

Future executions can use the stored healing decision instead of repeating the complete discovery process.

---

# ⚡ Locator Cache

Successful healing decisions are persisted to:

```text
cache/healing-cache.json
```

The cache key includes important execution identity such as:

```text
Page Object class
Variable name
Expected intent
Failed locator
```

This prevents unrelated Page Objects or variables from accidentally sharing a healing decision.

---

# 📊 Healing Reports

The framework generates healing information including:

- failed locator
- healed locator
- Page Object
- variable
- action
- intent
- healing source
- confidence
- candidate score
- validation result

Reports can be used to understand what the framework healed during test execution.

---

# 🏗 Architecture

The framework is organized into eight major layers.

```text
┌─────────────────────────────────────────────────────────────┐
│                 1. UNIVERSAL ENTRY POINTS                  │
│                                                             │
│ Selenium WebDriver / WebElement operations                  │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    2. ACTION INTERCEPTOR                    │
│                                                             │
│ Captures failed Selenium operations                         │
│ Preserves action + locator + execution context               │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    3. CAPABILITY ENGINE                     │
│                                                             │
│ Determines action capability and expected element intent    │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    4. HEALING DECISION                      │
│                                                             │
│ Determines whether healing is safe and permitted             │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    5. HEALING PIPELINE                      │
│                                                             │
│ Variable Analyzer                                            │
│ Locator Analyzer                                             │
│ Execution Analyzer                                           │
│ DOM Candidate Finder                                         │
│ Candidate Ranker                                              │
│ Candidate Filter                                              │
│ Candidate Validator                                           │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                       6. EXECUTION                          │
│                                                             │
│ Executes the original Selenium action using the validated   │
│ replacement element                                         │
└──────────────────────────────┬──────────────────────────────┘
                               │
                    ┌──────────┴──────────┐
                    ▼                     ▼
┌────────────────────────────┐ ┌──────────────────────────────┐
│       7. LEARNING          │ │         8. ANALYTICS         │
│                            │ │                              │
│ Cache                      │ │ Healing reports              │
│ Learning history           │ │ Execution information        │
│ Successful decisions       │ │ Healing statistics            │
└────────────────────────────┘ └──────────────────────────────┘
```

---

# 🔍 Detailed Healing Flow

Suppose the Page Object contains:

```java
private final By username = By.name("old_username");
```

and the test executes:

```java
driver.findElement(username).sendKeys("Admin");
```

The browser application has changed the locator.

The framework follows this process.

---

## Step 1 — Selenium Operation

The application executes normal Selenium code:

```java
driver.findElement(username).sendKeys("Admin");
```

No special API is required.

## Step 2 — Failure Detection

Selenium reports that the original locator cannot resolve the intended element.

The framework captures the failure.

Example:

```text
Failed Locator:
By.name: old_username
```

## Step 3 — Action Context

The framework determines the operation:

```text
Action:
SEND_KEYS
```

## Step 4 — Intent Resolution

The framework determines the expected element purpose:

```text
Variable:
username

Intent:
INPUT
```

---

## Step 5 — Source Context Analysis

The framework can use Page Object information to understand the failed element.

Example:

```java
private final By username = ...
```

The variable name `username` provides semantic information.

---

## Step 6 — Live DOM Scan

The framework obtains the current page DOM and generates candidates.

For example:

```text
name=username
placeholder=Username
label-based XPath
class=oxd-input
name=password
placeholder=Password
```

At this stage, these are only candidates.

They are not automatically trusted.

---

## Step 7 — Candidate Ranking

Each candidate is evaluated using multiple evidence sources.

Example:

```text
Candidate: name=username

Intent Match        ✓
Variable Match      ✓
Locator Match       ✓
Correct Tag         ✓
Unique              ✓
Stable Attribute    ✓
```

Another candidate:

```text
Candidate: name=password

Intent Match        ✓
Variable Match      ✗
Identity Match      ✗
```

The second candidate can therefore be rejected by the semantic safety boundary.

---

## Step 8 — Safety Validation

Before a candidate is accepted, the framework validates:

### Identity
Does the candidate represent the same intended element?

### Intent
Does the candidate support the requested Selenium operation?

### Physical uniqueness
Does the locator identify the expected number of elements?

### Visibility
Is the element usable?

### Enabled state
Can the requested operation actually be performed?

### Semantic firewall
Does the candidate share meaningful identity with the failed element?

### Hard identity gate
Does the candidate provide sufficient identity evidence?

---

## Step 9 — Candidate Acceptance

After all safety checks pass:

```text
Failed:
By.name: old_username

Recovered:
By.name: username
```

---

## Step 10 — Runtime Execution

The framework maps the physical Selenium element and executes the original action.

Example:

```text
SEND_KEYS
```

continues against the validated replacement element.

---

## Step 11 — Cache

The validated decision can be stored:

```text
healing-cache.json
```

Example:

```text
LoginPage
username
INPUT
By.name: old_username
        ↓
By.name: username
```

---

## Step 12 — Learning

The successful decision is recorded in learning history.

Future executions can benefit from previous validated decisions.

---

## Step 13 — Reporting

The healing event is written to the report system.

The report can contain:

```text
Page Object
Variable
Action
Intent
Failed locator
Recovered locator
Score
Confidence
Healing source
```

---

# 🔐 Safety Philosophy

Self-healing is powerful, but blindly changing a locator can be dangerous.

A test that passes against the wrong element can be worse than a failed test.

Therefore:

> **A candidate must be semantically and physically validated before it can be executed.**

The framework should prefer:

```text
SAFE FAILURE
```

over:

```text
WRONG ELEMENT SUCCESS
```

---

# ⚙️ Installation

## Prerequisites

The validated v2.1.0 distribution uses:

- Java 21
- Maven
- Selenium WebDriver
- TestNG or another compatible test framework
- Chrome/Chromedriver or another supported Selenium browser
- Optional: Ollama for AI-assisted healing

---

## Step 1 — Download the Release

Download:

```text
vinayak-healing-plugin-2.1.0.jar
```

from:

```text
releases/v2.1.0/
```

The release also contains:

```text
vinayak-healing-plugin-2.1.0.jar.sha256
```

---

## Step 2 — Verify the JAR

SHA256 for the validated v2.1.0 artifact:

```text
db75596a11f9bfe9f99aa358bc2f3e10436dbd37c65be2670b8947f2951f8748
```

On macOS/Linux:

```bash
shasum -a 256 vinayak-healing-plugin-2.1.0.jar
```

The generated checksum should match the published checksum.

---

## Step 3 — Install the Plugin JAR

Install it into your local Maven repository:

```bash
mvn install:install-file \
  -Dfile=/path/to/vinayak-healing-plugin-2.1.0.jar \
  -DgroupId=com.vinayak \
  -DartifactId=vinayak-healing-plugin \
  -Dversion=2.1.0 \
  -Dpackaging=maven-plugin
```

---

## Step 4 — Configure Maven

Add the plugin to your project's `pom.xml`:

```xml
<build>
    <plugins>

        <plugin>
            <groupId>com.vinayak</groupId>
            <artifactId>vinayak-healing-plugin</artifactId>
            <version>2.1.0</version>

            <executions>
                <execution>
                    <goals>
                        <goal>setup</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

    </plugins>
</build>
```

The `setup` goal prepares the runtime environment and configures the Java Agent.

---

## Step 5 — Add `healing.properties`

Create:

```text
src/test/resources/healing.properties
```

Use:

```properties
# ==========================================
# Vinayak AI Self-Healing Configuration
# ==========================================

healing.enabled=true
healing.ai.enabled=true
healing.cache.enabled=true
healing.report.enabled=true

# AI Provider Settings
healing.ai.host=http://localhost:11434
healing.ai.model=qwen2.5-coder:1.5b
healing.ai.confidence.threshold=95.0

# WebDriver Settings
healing.wait.timeout.seconds=5

# CRITICAL SAFETY CONTROL
# Source code repair is OFF by default.
healing.source.repair.enabled=false
```

`healing.properties` is required by the framework configuration.

---

## Step 6 — Optional AI Configuration

If AI-assisted healing is enabled:

```properties
healing.ai.enabled=true
```

the configured AI provider must be available.

The default example configuration uses:

```text
Host:
http://localhost:11434

Model:
qwen2.5-coder:1.5b
```

This configuration is intended for a local Ollama installation.

AI suggestions remain subject to the framework's safety and validation boundaries.

---

## Step 7 — Use Normal Selenium

You do **not** need to replace your Selenium WebDriver.

Use:

```java
WebDriver driver = new ChromeDriver();

driver.get("https://example.com");
```

and normal Selenium operations:

```java
driver.findElement(By.name("username"))
      .sendKeys("Admin");
```

The Java Agent handles the healing infrastructure.

---

# 🏗 Page Object Example

```java
package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage {

    private final WebDriver driver;

    private final By username =
            By.name("username");

    private final By password =
            By.name("password");

    private final By loginButton =
            By.xpath("//button[normalize-space()='Login']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public void login(
            String usernameValue,
            String passwordValue) {

        driver.findElement(username)
                .sendKeys(usernameValue);

        driver.findElement(password)
                .sendKeys(passwordValue);

        driver.findElement(loginButton)
                .click();
    }
}
```

---

# 🧪 Test Example

```java
package tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pages.LoginPage;

public class LoginTest {

    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.get(
            "https://opensource-demo.orangehrmlive.com/"
        );
    }

    @Test
    public void loginTest() {
        LoginPage loginPage =
                new LoginPage(driver);

        loginPage.login(
                "Admin",
                "admin123"
        );
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
```

---

# ▶️ Run the Test

From the project root:

```bash
mvn clean test
```

The Maven plugin executes the setup phase and attaches the Java Agent.

The Selenium test then runs normally.

---

# 🧪 Verified Example

The repository contains an OrangeHRM example:

```text
examples/orangehrm/
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── pages/
    │           └── LoginPage.java
    │
    └── test/
        ├── java/
        │   └── tests/
        │       └── LoginTest.java
        │
        └── resources/
            └── healing.properties
```

The distribution JAR has been validated using this example.

Successful validation:

```text
Tests run: 1
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

---

# 📁 Runtime Output

During execution the framework can create:

```text
cache/
├── healing-cache.json
└── learning-history.json
```

and:

```text
reports/
├── healing-report-*.json
└── healing-dashboard.html
```

These are runtime-generated files and should generally not be committed to source control.

---

# 🔄 Complete Runtime Pipeline

```text
                 Selenium Test
                       │
                       ▼
             Normal WebDriver API
                       │
                       ▼
              Java Agent Runtime
                       │
                       ▼
              Action Interceptor
                       │
                       ▼
             Failure Detection
                       │
                       ▼
              Action Context
                       │
                       ▼
              Capability Engine
                       │
                       ▼
              Healing Decision
                       │
                       ▼
             ┌─────────────────┐
             │ Healing Pipeline│
             └────────┬────────┘
                      │
       ┌──────────────┼──────────────┐
       ▼              ▼              ▼
 Variable Analyzer  Locator      Execution
                    Analyzer     Analyzer
       │              │              │
       └──────────────┼──────────────┘
                      ▼
             DOM Candidate Finder
                      │
                      ▼
              Candidate Ranking
                      │
                      ▼
               Candidate Filter
                      │
                      ▼
             Candidate Validator
                      │
             ┌────────┴────────┐
             │                 │
           REJECT            ACCEPT
             │                 │
             ▼                 ▼
        Safe Failure      Runtime Execution
                               │
                    ┌──────────┴──────────┐
                    ▼                     ▼
                Learning               Analytics
                    │                     │
                    ▼                     ▼
                  Cache                Reports
```

---

# 🛡️ Safety Boundaries

The framework intentionally contains multiple safety layers.

## 1. Semantic Identity

The replacement should represent the intended element.

## 2. Action Compatibility

The replacement must support the requested operation.

## 3. Physical Uniqueness

The locator should resolve to the expected number of elements.

## 4. Visibility and Availability

The candidate must be usable for the requested interaction.

## 5. Candidate Validation

A high score alone does not make a candidate safe.

## 6. AI Safety Boundary

AI suggestions are still subject to deterministic validation.

## 7. Source Repair Safety

Source-code repair is disabled by default:

```properties
healing.source.repair.enabled=false
```

---

# 📝 Source Code Repair

v2.1.0 contains source-repair capabilities, but they are controlled through configuration.

Default:

```properties
healing.source.repair.enabled=false
```

When enabled intentionally, the framework can use Page Object source information to determine where a locator is declared and perform controlled source updates.

Source repair should be enabled only after evaluating the execution environment and concurrency model.

---

# 📦 Project Structure

```text
selenium-self-healing/
│
├── README.md
├── LICENSE
├── .gitignore
│
├── docs/
│   ├── installation.md
│   ├── quick-start.md
│   └── configuration.md
│
├── releases/
│   └── v2.1.0/
│       ├── vinayak-healing-plugin-2.1.0.jar
│       └── vinayak-healing-plugin-2.1.0.jar.sha256
│
└── examples/
    └── orangehrm/
        ├── pom.xml
        └── src/
            ├── main/
            │   └── java/
            │       └── pages/
            │           └── LoginPage.java
            │
            └── test/
                ├── java/
                │   └── tests/
                │       └── LoginTest.java
                │
                └── resources/
                    └── healing.properties
```

---

# 🔧 Configuration Reference

| Property | Purpose |
|---|---|
| `healing.enabled` | Enables or disables the healing framework |
| `healing.ai.enabled` | Enables AI-assisted healing |
| `healing.cache.enabled` | Enables locator cache |
| `healing.report.enabled` | Enables healing reports |
| `healing.ai.host` | AI service host |
| `healing.ai.model` | AI model used for fallback |
| `healing.ai.confidence.threshold` | AI confidence threshold |
| `healing.wait.timeout.seconds` | Healing wait timeout |
| `healing.source.repair.enabled` | Enables controlled source repair |

---

# 🚦 Recommended Safety Configuration

```properties
healing.enabled=true
healing.ai.enabled=true
healing.cache.enabled=true
healing.report.enabled=true

healing.ai.host=http://localhost:11434
healing.ai.model=qwen2.5-coder:1.5b
healing.ai.confidence.threshold=95.0

healing.wait.timeout.seconds=5

healing.source.repair.enabled=false
```

Start with source repair disabled.

Enable it only when you intentionally want the framework to modify Page Object source files.

---

# 🧪 Testing Strategy

The framework is designed to distinguish between:

```text
Correct healing
```

and:

```text
Wrong-element success
```

Examples of safety scenarios include:

```text
WRONG_LOGIN
WRONG_ADMIN
WRONG_RANDOM_ELEMENT
WRONG_DELETE_INPUT
WRONG_SAVE_BUTTON
WRONG_LOGIN_SUBMIT
WRONG_PASSWORD_FIELD
```

The important requirement is not merely:

```text
Did the test pass?
```

but:

```text
Did the test interact with the correct intended element?
```

---

# ⚠️ Important Notes

### The plugin is not a guarantee that every broken locator can be healed.

A safe system must sometimes reject a candidate.

If the framework cannot establish sufficient identity or safety evidence, the original failure should remain visible rather than silently interacting with an unrelated element.

### AI does not replace deterministic validation.

AI is a candidate-generation/semantic capability, not an unrestricted execution mechanism.

### Cache should be treated as learned runtime state.

If the application's DOM or Page Object semantics change significantly, clearing the cache may be appropriate.

---

# 🧹 Clear Runtime Cache

From the OrangeHRM example:

```bash
rm -rf examples/orangehrm/cache
rm -rf examples/orangehrm/reports
```

---

# 🛣 Roadmap

## v2.1.0

Current release focuses on:

- Universal Selenium interception
- Action-aware healing
- Unified healing context
- Deterministic DOM candidate generation
- Candidate ranking
- Candidate filtering
- Candidate validation
- Semantic identity protection
- Physical uniqueness validation
- AI-assisted candidate selection
- Persistent locator cache
- Learning history
- Healing analytics
- Optional source repair
- Collection/list healing capabilities

## Future

Potential future capabilities include:

- distributed healing intelligence
- cross-project learning
- advanced analytics integrations
- broader browser automation framework support
- visual/AI-assisted recovery
- additional enterprise integrations

---

# 🤝 Contributing

Contributions are welcome.

If you discover a healing problem, please provide:

1. The failed locator
2. The Selenium action
3. The expected element
4. Relevant DOM structure
5. Page Object context
6. Healing logs
7. A reproducible test case

Please avoid submitting credentials, API keys, or private application data.

---

# 📄 License

This project is licensed under the MIT License.

See:

```text
LICENSE
```

for details.

---

# 👨‍💻 Author

**Vinayak Hanagi**

Automation Test Engineer

GitHub:

https://github.com/Vinayakkkk

---

# ⭐ Support

If this project is useful, consider giving the repository a ⭐ on GitHub.

Bug reports, reproducible healing scenarios, and technical contributions are welcome.

---

# 🔥 Final Concept

The framework is not designed simply to find *an* element.

It is designed to determine:

```text
What did the test intend to interact with?
        ↓
What changed in the application?
        ↓
Which current DOM element represents that intent?
        ↓
Can that candidate be proven safe?
        ↓
Can the original Selenium action continue?
        ↓
Can the validated decision be learned?
```

The ultimate objective is:

> **Recover automation from locator changes while preserving semantic correctness and preventing wrong-element execution.**


---

# 🖥️ Windows, macOS, and Linux Setup

This repository is the **release/distribution repository**. It contains the packaged plugin JAR, documentation, and an OrangeHRM consumer example. It is not the plugin source-code project, so the repository root does not contain a `pom.xml`.

## Choose how you want to use the project

- **Test or use the released plugin:** download the repository ZIP or clone this repository, then follow the steps below.
- **Contribute to plugin implementation:** use the separate plugin source repository. The distribution repository is intended for releases, documentation, and consumer examples.

## Option A — Clone with Git (recommended)

Cloning is recommended if you want to pull updates, create branches, commit changes, or contribute to the repository.

### Windows (PowerShell)

```powershell
cd C:\Users\Dell\Documents
git clone https://github.com/Vinayakkkk/selenium-self-healing.git
cd selenium-self-healing
```

### macOS / Linux (Terminal)

```bash
cd ~/Documents
git clone https://github.com/Vinayakkkk/selenium-self-healing.git
cd selenium-self-healing
```

You can open the folder in VS Code with:

```bash
code .
```

The default Git branch is named `main`. That is normal; it is the branch name, not a separate version of the plugin.

## Option B — Download ZIP

If you choose **Code → Download ZIP** on GitHub, extract the ZIP and open the extracted folder in VS Code.

GitHub may name the extracted directory with a `-main` suffix, such as `selenium-self-healing-main`. This is just the extracted folder name. It does not mean Maven should be run from that folder, and a ZIP download does not provide Git history or remote tracking.

## Install the released JAR

Run the following from the distribution repository root, after confirming that the release JAR exists at the shown relative path.

### Windows (PowerShell)

```powershell
mvn install:install-file "-Dfile=releases/v2.1.0/vinayak-healing-plugin-2.1.0.jar" "-DgroupId=com.vinayak" "-DartifactId=vinayak-healing-plugin" "-Dversion=2.1.0" "-Dpackaging=maven-plugin"
```

PowerShell does not use the Unix `\` line-continuation character. Keep this command on one line, or use PowerShell's backtick for line continuation.

### macOS / Linux (Terminal)

```bash
mvn install:install-file \
  -Dfile=releases/v2.1.0/vinayak-healing-plugin-2.1.0.jar \
  -DgroupId=com.vinayak \
  -DartifactId=vinayak-healing-plugin \
  -Dversion=2.1.0 \
  -Dpackaging=maven-plugin
```

If you saved the JAR elsewhere, replace the `-Dfile` value with its actual full path. Do not use the placeholder `/path/to/...` literally.

## Run the OrangeHRM consumer example

The distribution repository root does not contain a Maven project file. The example's `pom.xml` is inside `examples/orangehrm`.

### Windows (PowerShell)

```powershell
cd examples\orangehrm
mvn clean test
```

### macOS / Linux (Terminal)

```bash
cd examples/orangehrm
mvn clean test
```

Before running, ensure Java 21 and Maven are installed. If AI is enabled in `healing.properties`, the configured Ollama service and model must also be available.

## Check your current directory if Maven says no POM was found

Maven commands such as `mvn clean test` must run in a directory containing a `pom.xml`.

From the distribution repository root, list the example directory:

```powershell
Get-ChildItem examples
```

Or find the POM on Windows:

```powershell
Get-ChildItem -Path . -Filter pom.xml -Recurse | Select-Object -ExpandProperty FullName
```

On macOS / Linux:

```bash
find . -name pom.xml
```

Then change into the directory containing the intended `pom.xml` before running Maven.
