# Configuration

## `healing.properties`

Vinayak Healing Plugin reads its runtime configuration from `healing.properties`.

For the distribution example, the file is located at:

```text
examples/orangehrm/src/test/resources/healing.properties
```

### Recommended v2.1.0 configuration

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
# Source code repair is OFF by default to prevent parallel execution race conditions.
healing.source.repair.enabled=false
```

---

## Core Settings

### `healing.enabled`

Controls the main self-healing functionality.

```properties
healing.enabled=true
```

Set to `false` to disable the healing feature.

---

### `healing.ai.enabled`

Controls AI-assisted healing.

```properties
healing.ai.enabled=true
```

When enabled, the configured AI provider can assist the healing pipeline.

AI assistance does not replace the normal candidate safety and validation pipeline.

---

### `healing.cache.enabled`

Controls healing cache usage.

```properties
healing.cache.enabled=true
```

The cache allows previously learned successful healing information to be reused.

---

### `healing.report.enabled`

Controls healing report generation.

```properties
healing.report.enabled=true
```

Reports provide information about healing activity and execution.

---

## AI Provider Settings

### `healing.ai.host`

Specifies the AI service endpoint.

Default example:

```properties
healing.ai.host=http://localhost:11434
```

The example configuration uses a local Ollama service.

---

### `healing.ai.model`

Specifies the AI model used by the healing system.

Example:

```properties
healing.ai.model=qwen2.5-coder:1.5b
```

The configured model must be available from the configured AI service.

For Ollama:

```bash
ollama list
```

---

### `healing.ai.confidence.threshold`

Specifies the configured AI confidence threshold.

Example:

```properties
healing.ai.confidence.threshold=95.0
```

The value is expressed as a percentage-style numeric threshold.

---

## WebDriver Settings

### `healing.wait.timeout.seconds`

Controls the healing-related wait timeout.

Example:

```properties
healing.wait.timeout.seconds=5
```

---

## Source Repair

### `healing.source.repair.enabled`

Controls automatic source-code repair.

Recommended/default setting:

```properties
healing.source.repair.enabled=false
```

Source repair is intentionally disabled by default.

Automatic source modification can create race conditions or unexpected source changes during parallel test execution.

Enable source repair only when you explicitly require it and understand the implications for your test environment.

---

## Recommended Production Configuration

For a normal production test run:

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

---

## Running Without AI

If AI-assisted healing is not required:

```properties
healing.ai.enabled=false
```

The remaining healing features can stay enabled.

---

## Changing the AI Model

Change the model name:

```properties
healing.ai.model=your-model-name
```

Then make sure that model exists in the configured AI service.

For Ollama:

```bash
ollama list
```

---

## Cleaning Cache and Reports

When debugging healing behavior, stale cache data can affect results.

From the project root:

```bash
rm -rf cache reports target
```

Then run:

```bash
mvn clean test
```

This starts the test from a clean runtime state.

---

## Configuration File Location

For Maven test execution, place the configuration file under:

```text
src/test/resources/healing.properties
```

For the repository's OrangeHRM example:

```text
examples/orangehrm/src/test/resources/healing.properties
```

---

## Configuration and Safety

Configuration settings do not bypass the safety checks implemented by the healing pipeline.

Candidate processing can include:

- semantic identity validation
- action/capability intent
- physical uniqueness
- candidate validation
- duplicate-element protection
- locator safety checks
- AI-assisted selection when enabled

AI assists the healing process, while the safety pipeline remains responsible for accepting or rejecting candidates.
