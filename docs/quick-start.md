# Quick Start

## Vinayak Healing Plugin v2.1.0

This is the fastest way to run the plugin with a normal Selenium project.

---

## 1. Project Structure

A minimal project can look like this:

```text
my-selenium-project/
├── pom.xml
└── src/
    ├── main/java/
    └── test/
        ├── java/
        └── resources/
            └── healing.properties
```

---

## 2. Install the Plugin

Install the downloaded release JAR into your local Maven repository:

```bash
mvn install:install-file   -Dfile=/path/to/vinayak-healing-plugin-2.1.0.jar   -DgroupId=com.vinayak   -DartifactId=vinayak-healing-plugin   -Dversion=2.1.0   -Dpackaging=maven-plugin
```

---

## 3. Configure Maven

Add the plugin to your `pom.xml`:

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

The complete example also needs Selenium and TestNG.

Example dependencies:

```xml
<dependencies>

    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-java</artifactId>
        <version>4.22.0</version>
    </dependency>

    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <version>7.10.2</version>
        <scope>test</scope>
    </dependency>

</dependencies>
```

Use the Selenium/TestNG versions appropriate for your project if they differ from the example.

---

## 4. Add Configuration

Create:

```text
src/test/resources/healing.properties
```

Use:

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

## 5. Write Normal Selenium Code

Example page object:

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

There is no custom WebDriver in the test.

---

## 6. Create the Test

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

## 7. Run

From the project root:

```bash
mvn clean test
```

The Maven `healing:setup` goal prepares the Java Agent automatically.

---

## 8. What Happens During Healing?

When Selenium encounters a failed locator, the plugin can process the failure through the healing pipeline.

The high-level flow is:

```text
Selenium findElement()
        |
        v
Action Interception
        |
        v
Capability / Intent Detection
        |
        v
Healing Decision
        |
        v
DOM Candidate Discovery
        |
        v
Candidate Ranking
        |
        v
Semantic Validation
        |
        v
Physical Uniqueness
        |
        v
AI assistance when enabled
        |
        v
Safe Candidate
        |
        v
Element Execution
        |
        v
Cache / Learning / Report
```

The important point is that AI selection does not replace the safety validation pipeline.

---

## 9. Clean Runtime Data

If you want to run from a clean state:

```bash
rm -rf target cache reports
mvn clean test
```

---

## 10. Verify the Example

The repository contains a complete OrangeHRM example under:

```text
examples/orangehrm/
```

Run it with:

```bash
cd examples/orangehrm
mvn clean test
```

The example is intended to demonstrate the packaged plugin and Java Agent workflow using normal Selenium code.

---

## Next

For configuration details, see [Configuration](configuration.md).

For installation details, see [Installation](installation.md).
