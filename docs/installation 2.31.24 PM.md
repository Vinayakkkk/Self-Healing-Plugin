# Installation

## Vinayak Healing Plugin v2.1.0

This guide explains how to install and run Vinayak Healing Plugin with a normal Selenium WebDriver project.

> **Important:** Your test code does not need `HealingWebDriver`. Use the normal Selenium `WebDriver`. The plugin is attached automatically through the Java Agent configured by the Maven `healing:setup` goal.

---

## 1. Prerequisites

Install the following:

- Java JDK 21
- Apache Maven 3.9+
- Selenium WebDriver
- TestNG or another supported test framework
- Chrome/Chromium and a compatible driver setup
- Ollama if AI-assisted healing is enabled

Verify Java and Maven:

```bash
java -version
mvn -version
```

The v2.1.0 distribution example uses Java 21.

---

## 2. Get the Plugin JAR

Download:

```text
releases/v2.1.0/vinayak-healing-plugin-2.1.0.jar
```

The SHA-256 checksum for the v2.1.0 release is:

```text
db75596a11f9bfe9f99aa358bc2f3e10436dbd37c65be2670b8947f2951f8748
```

Verify it after downloading:

```bash
shasum -a 256 vinayak-healing-plugin-2.1.0.jar
```

The generated checksum must match the value above.

---

## 3. Install the JAR into Your Local Maven Repository

The v2.1.0 public distribution is currently provided as a release JAR. It is not being presented as a Maven Central artifact.

Install the JAR locally:

```bash
mvn install:install-file   -Dfile=/path/to/vinayak-healing-plugin-2.1.0.jar   -DgroupId=com.vinayak   -DartifactId=vinayak-healing-plugin   -Dversion=2.1.0   -Dpackaging=maven-plugin
```

After this command Maven can resolve the plugin from your local repository.

---

## 4. Add the Plugin to `pom.xml`

Add the plugin under `<build><plugins>`:

```xml
<plugin>
    <groupId>com.vinayak</groupId>
    <artifactId>vinayak-healing-plugin</artifactId>
    <version>2.1.0</version>
    <extensions>true</extensions>
    <executions>
        <execution>
            <goals>
                <goal>setup</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

The `setup` goal prepares the runtime directories and configures the Java Agent.

---

## 5. Add `healing.properties`

Create:

```text
src/test/resources/healing.properties
```

Example:

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

See [Configuration](configuration.md) for the full reference.

---

## 6. Use Normal Selenium

Your application/test code remains standard Selenium:

```java
WebDriver driver = new ChromeDriver();

driver.get("https://example.com");

driver.findElement(By.id("username")).sendKeys("Admin");
driver.findElement(By.id("password")).sendKeys("admin123");
driver.findElement(By.id("login")).click();
```

You do **not** need to replace `WebDriver` with a custom driver.

---

## 7. Run the Test

From the project root:

```bash
mvn clean test
```

The Maven setup goal configures the Java Agent before the tests execute.

Runtime artifacts are generated in the project according to the active configuration, including cache and report data when those features are enabled.

---

## 8. AI-Assisted Healing

If AI-assisted healing is enabled, start Ollama and make sure the configured model is available.

Example:

```bash
ollama serve
```

Then verify the model:

```bash
ollama list
```

For the default example configuration:

```text
qwen2.5-coder:1.5b
```

If your model name is different, update:

```properties
healing.ai.model=your-model-name
```

The AI component assists with candidate selection. Candidate safety and validation remain part of the healing pipeline.

---

## 9. Source Repair

Source-code repair is disabled by default:

```properties
healing.source.repair.enabled=false
```

This is intentional. Keeping source repair disabled avoids modifying source files during normal or parallel test execution.

Only enable source repair when you explicitly want that behavior and understand the implications for your test environment.

---

## 10. Troubleshooting

### Agent is not starting

Run:

```bash
mvn clean test
```

and inspect the Maven output for the healing setup step.

Confirm the plugin version is:

```text
2.1.0
```

### AI healing is unavailable

Check:

```bash
ollama list
```

Then verify:

```properties
healing.ai.host=http://localhost:11434
healing.ai.model=qwen2.5-coder:1.5b
```

### Old cache is affecting a test

Remove generated runtime data:

```bash
rm -rf cache reports target
```

Then run:

```bash
mvn clean test
```

### Selenium/CDP warning

A Chrome DevTools Protocol version warning can occur when the Selenium version and Chrome version are not an exact match. It is separate from the locator-healing logic unless the test itself fails because of the browser/driver mismatch.

---

## 11. Installation Verification

A successful installation should allow a normal Selenium test to execute with:

```bash
mvn clean test
```

The v2.1.0 distribution OrangeHRM example was validated using this workflow.

