package com.vinayak.healing.core;

import com.vinayak.healing.analytics.HealingAnalytics;
import com.vinayak.healing.cache.LocatorCache;
import com.vinayak.healing.config.HealingConfig;
import com.vinayak.healing.engine.SelfHealingEngine;
import com.vinayak.healing.execution.ExecutionAction;
import com.vinayak.healing.execution.ExecutionTracker;
import com.vinayak.healing.logging.HealingLogger;

import java.util.List;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;



import org.openqa.selenium.TimeoutException;


public class HealingWebDriver
        implements WebDriver,
                   JavascriptExecutor {


private final WebDriver driver;

private final HealingConfig config;



private final SelfHealingEngine healingEngine =
        new SelfHealingEngine();




public HealingWebDriver(
        WebDriver driver,
        HealingConfig config) {

    this.driver = driver;
    this.config = config;
}

@Override
public WebElement findElement(By locator) {

        By runtimeHealedLocator =
        SelfHealingEngine.getRuntimeHealedLocator(locator);

if (runtimeHealedLocator != null) {

    HealingLogger.debug(
            "RUNTIME HEALED LOCATOR FOUND | old="
                    + locator
                    + " | new="
                    + runtimeHealedLocator);

    try {

        WebElement runtimeElement =
                driver.findElement(
                        runtimeHealedLocator);

        HealingLogger.debug(
                "RUNTIME HEAL SUCCESS | locator="
                        + runtimeHealedLocator
                        + " | actualTag="
                        + runtimeElement.getTagName());

        return new HealingWebElement(
                runtimeElement,
                runtimeHealedLocator,
                this);

    } catch (Exception runtimeException) {

        HealingLogger.debug(
                "RUNTIME HEALED LOCATOR FAILED | "
                        + runtimeHealedLocator);

        // Continue with normal healing.
    }
}

  try {

    HealingLogger.debug(
            "Finding locator : "
                    + locator);

    WebElement element;

    try {

        /*
         * First try immediately.
         */
        element =
                driver.findElement(locator);

    } catch (NoSuchElementException
             | StaleElementReferenceException firstFailure) {

        /*
         * IMPORTANT:
         *
         * The application may still be rendering.
         *
         * Give the original locator a chance to
         * become available before starting healing.
         */
        HealingLogger.debug(
                "LOCATOR NOT AVAILABLE IMMEDIATELY | "
                        + locator);

        try {

            WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(
                                    config.getWaitTimeoutSeconds()));

            element =
                    wait.until(
                            ExpectedConditions
                                    .presenceOfElementLocated(
                                            locator));

            HealingLogger.debug(
                    "LOCATOR APPEARED AFTER WAIT | "
                            + locator);

        } catch (TimeoutException timeoutException) {

            /*
             * Original locator genuinely did not
             * appear within the configured timeout.
             *
             * Continue into the existing healing flow.
             */
            HealingLogger.debug(
                    "LOCATOR STILL NOT FOUND AFTER "
                            + config.getWaitTimeoutSeconds()
                            + " SECONDS | "
                            + locator);

            throw firstFailure;
        }
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
        "DEFERRING HEALING UNTIL ELEMENT ACTION | "
                + locator);

return HealingWebElement.deferred(
        locator,
        this);


}catch (Exception exception) {

        throw new RuntimeException(
                "Element lookup failed for locator : "
                        + locator,
                exception);
    }
}
/*
 * =========================================================
 * ACTION-AWARE ELEMENT RESOLUTION
 * =========================================================
 *
 * Called by HealingWebElement after the caller's action
 * is known.
 *
 * Example:
 *
 * findElement(brokenLocator).sendKeys(...)
 *
 * At this point SEND_KEYS has already been recorded.
 */
WebElement resolveForAction(
        By locator,
        ExecutionAction action) {

    HealingLogger.debug(
            "ACTION-AWARE RESOLUTION | locator="
                    + locator
                    + " | action="
                    + action);

    /*
     * First try the original locator again.
     *
     * The element may have appeared after the original
     * lookup failed.
     */
    try {

        WebElement element =
                driver.findElement(locator);

        HealingLogger.debug(
                "ACTION-AWARE ORIGINAL LOCATOR SUCCESS | "
                        + locator
                        + " | tag="
                        + element.getTagName());

        return element;

    } catch (Exception originalFailure) {

        HealingLogger.debug(
                "ACTION-AWARE ORIGINAL LOCATOR FAILED | "
                        + locator
                        + " | action="
                        + action);
    }

    /*
     * The action has already been recorded by
     * HealingWebElement.executeAction().
     *
     * Therefore FailureContextBuilder can now see:
     *
     * SEND_KEYS
     * CLEAR
     * CLICK
     * etc.
     */
        try {

       WebElement healedElement =
        healingEngine.heal(
                driver,
                locator,
                action);

        if (healedElement == null) {

            throw new NoSuchElementException(
                    "Unable to heal locator : "
                            + locator
                            + " | action="
                            + action);
        }

        HealingLogger.debug(
                "ACTION-AWARE HEAL SUCCESS | "
                        + "locator="
                        + locator
                        + " | action="
                        + action
                        + " | actualTag="
                        + healedElement.getTagName());

        return healedElement;

    } catch (NoSuchElementException e) {

        throw e;

    } catch (Exception e) {

        throw new RuntimeException(
                "Action-aware healing failed | "
                        + "locator="
                        + locator
                        + " | action="
                        + action,
                e);
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

@Override
public Object executeScript(
        String script,
        Object... args) {

    return ((JavascriptExecutor) driver)
            .executeScript(script, args);
}

@Override
public Object executeAsyncScript(
        String script,
        Object... args) {

    return ((JavascriptExecutor) driver)
            .executeAsyncScript(script, args);
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
