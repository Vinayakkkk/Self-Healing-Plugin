package com.vinayak.healing.core;

import com.vinayak.healing.analytics.HealingAnalytics;
import com.vinayak.healing.builder.FailureContextFactory;
import com.vinayak.healing.cache.LocatorCache;
import com.vinayak.healing.config.HealingConfig;
import com.vinayak.healing.engine.SelfHealingEngine;
import com.vinayak.healing.execution.ExecutionTracker;
import com.vinayak.healing.logging.HealingLogger;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.validator.SuccessfulLocatorValidator;
import java.util.List;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HealingWebDriver implements WebDriver {


private final WebDriver driver;

private final HealingConfig config;

private final FailureContextFactory failureContextFactory =
        new FailureContextFactory();

private final SelfHealingEngine healingEngine =
        new SelfHealingEngine();

        private final SuccessfulLocatorValidator
        successfulLocatorValidator =
        new SuccessfulLocatorValidator();


public HealingWebDriver(
        WebDriver driver,
        HealingConfig config) {

    this.driver = driver;
    this.config = config;
}

@Override
public WebElement findElement(By locator) {

    try {

        HealingLogger.debug(
                "Finding locator : "
                        + locator);

        WebElement element =
                driver.findElement(locator);

        /*
         * The locator exists, but it may point to
         * the wrong semantic element.
         */

FailureContext context =
        failureContextFactory.build(
                driver,
                locator);

boolean suspicious =
        successfulLocatorValidator
                .isSuspicious(
                        context,
                        element);
                        System.out.println("Suspicious = " + suspicious);
        if (suspicious) {

            HealingLogger.debug(
                    "LOCATOR EXISTS BUT APPEARS "
                            + "SEMANTICALLY INCORRECT");

            HealingLogger.debug(
                    "Attempting corrective healing : "
                            + locator);

            WebElement healedElement =
                    healingEngine.heal(
                            driver,
                            locator);

            if (healedElement != null) {

                HealingLogger.debug(
        "HEALED RETURN | requested="
                + locator
                + " | actualTag="
                + healedElement.getTagName()
                + " | data-test="
                + healedElement.getAttribute("data-test")
                + " | id="
                + healedElement.getAttribute("id"));

                return new HealingWebElement(
                        healedElement,
                        locator,
                        this);
            }

            /*
             * If corrective healing cannot safely
             * find another element, do not silently
             * return the suspicious original element.
             */
            throw new NoSuchElementException(
                    "Existing locator resolved to "
                            + "a suspicious element : "
                            + locator);
        }
HealingLogger.debug(
        "NORMAL RETURN | requested="
                + locator
                + " | actualTag="
                + element.getTagName()
                + " | data-test="
                + element.getAttribute("data-test")
                + " | id="
                + element.getAttribute("id"));
        return new HealingWebElement(
                element,
                locator,
                this);

    } catch (
        NoSuchElementException
                | StaleElementReferenceException originalException) {

    HealingLogger.debug(
            "Locator failed : "
                    + locator);
                    if (isNegativeWaitLookup()) {

        HealingLogger.debug(
                "NEGATIVE WAIT DETECTED | "
                        + locator);

        HealingLogger.debug(
                "HEALING SKIPPED");

        throw originalException;
    }


    HealingLogger.debug(
            "Attempting self healing...");

    try {

        WebElement healedElement =
                healingEngine.heal(
                        driver,
                        locator);

        if (healedElement != null) {

            HealingLogger.debug(
        "HEALED RETURN | requested="
                + locator
                + " | actualTag="
                + healedElement.getTagName()
                + " | data-test="
                + healedElement.getAttribute("data-test")
                + " | id="
                + healedElement.getAttribute("id"));

            return new HealingWebElement(
                    healedElement,
                    locator,
                    this);
        }

        /*
         * Healing may intentionally return null.
         *
         * Example:
         * - negative verification
         * - optional element
         * - element expected to appear later
         *
         * Preserve normal Selenium behaviour so that
         * WebDriverWait can continue polling.
         */
        throw originalException;

    } catch (
            NoSuchElementException
                    | StaleElementReferenceException seleniumException) {

        throw seleniumException;

    } catch (Exception healingException) {

        throw new RuntimeException(
                "Healing failed for locator : "
                        + locator,
                healingException);
    }
}catch (Exception exception) {

        throw new RuntimeException(
                "Element lookup failed for locator : "
                        + locator,
                exception);
    }
}
@Override
public List<WebElement> findElements(By locator) {

   List<WebElement> elements =
        driver.findElements(locator);

// Negative conditions must preserve empty-list behavior.
if (isNegativeWaitLookup()) {

    HealingLogger.debug(
            "NEGATIVE WAIT COLLECTION LOOKUP | "
                    + locator);

    HealingLogger.debug(
            "COLLECTION HEALING SKIPPED");

    return elements;
}

/*
 * Always attempt collection healing.
 *
 * This allows the framework to recover
 * partially broken collections where
 * Selenium still finds some elements.
 */
try {

    List<WebElement> healedElements =
            healingEngine.healCollection(
                    driver,
                    locator);

    /*
     * Accept healed collection only if it
     * safely improves the original result.
     */
    if (healedElements != null
            && healedElements.size() > elements.size()) {

        HealingLogger.debug(
                "COLLECTION IMPROVED | "
                        + elements.size()
                        + " -> "
                        + healedElements.size());

        return healedElements;
    }

} catch (Exception exception) {

    HealingLogger.debug(
            "COLLECTION HEALING FAILED | "
                    + exception.getMessage());
}

return elements;


}

/*
 * Framework action methods.
 *
 * They record the operation BEFORE locator lookup.
 * This gives CandidateValidator the correct context
 * when the locator is broken.
 */

public void click(By locator) {

    ExecutionTracker.recordAction("CLICK");

    try {
        findElement(locator).click();
    } finally {
        ExecutionTracker.clearAction();
    }
}

public void type(
        By locator,
        CharSequence... value) {

    ExecutionTracker.recordAction("SEND_KEYS");

    try {

        WebElement element =
                findElement(locator);

        element.clear();
        element.sendKeys(value);

    } finally {
        ExecutionTracker.clearAction();
    }
}

public void clear(By locator) {

    ExecutionTracker.recordAction("CLEAR");

    try {
        findElement(locator).clear();
    } finally {
        ExecutionTracker.clearAction();
    }
}

@Override
public void get(String url) {
    driver.get(url);
}

@Override
public String getCurrentUrl() {
    return driver.getCurrentUrl();
}

@Override
public String getTitle() {
    return driver.getTitle();
}

@Override
public String getPageSource() {
    return driver.getPageSource();
}

@Override
public void close() {
    driver.close();
}

@Override
public void quit() {

    LocatorCache.flush();

    HealingAnalytics.printSummary();

    driver.quit();
}

@Override
public Set<String> getWindowHandles() {
    return driver.getWindowHandles();
}

@Override
public String getWindowHandle() {
    return driver.getWindowHandle();
}

@Override
public TargetLocator switchTo() {
    return driver.switchTo();
}

@Override
public Navigation navigate() {
    return driver.navigate();
}

@Override
public Options manage() {
    return driver.manage();
}

public WebDriver getWrappedDriver() {
    return driver;
}

public HealingConfig getConfig() {
    return config;
}
public List<WebElement> findElementsWithHealing(
        By locator) {

    

    HealingLogger.debug(
            "Collection locator failed : "
                    + locator);

    HealingLogger.debug(
            "Attempting explicit collection healing...");

    try {

        List<WebElement> healedElements =
                healingEngine.healCollection(
                        driver,
                        locator);

        if (healedElements != null
                && !healedElements.isEmpty()) {

            return healedElements;
        }

    } catch (Exception exception) {

        HealingLogger.debug(
                "Collection healing failed : "
                        + exception.getMessage());
    }

    return List.of();
}
private boolean isNegativeWaitLookup() {

    for (StackTraceElement element :
            Thread.currentThread().getStackTrace()) {

        String className =
                element.getClassName();

        /*
         * ExpectedConditions.invisibilityOfElementLocated()
         * executes through an anonymous ExpectedConditions
         * implementation.
         *
         * We should not heal a missing element during
         * negative verification.
         */
        if (className.startsWith(
                "org.openqa.selenium.support.ui.ExpectedConditions$20")) {

            return true;
        }
    }

    return false;
}
}
