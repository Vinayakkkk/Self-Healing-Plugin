package com.vinayak.healing.core;
import com.vinayak.healing.capability.ActionCapabilityResolver;
import com.vinayak.healing.capability.CapabilityValidator;
import com.vinayak.healing.capability.ElementCapability;
import com.vinayak.healing.execution.ExecutionAction;
import com.vinayak.healing.execution.ExecutionRecorder;
import com.vinayak.healing.execution.ExecutionTracker;
import com.vinayak.healing.logging.HealingLogger;
import com.vinayak.healing.recovery.ActionRecoveryEngine;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;

public class HealingWebElement implements WebElement {

@FunctionalInterface
private interface ElementAction {

    WebElement execute();
}

private WebElement delegate;
private final By originalLocator;
private final HealingWebDriver driver;

private final ActionRecoveryEngine actionRecoveryEngine =
        new ActionRecoveryEngine();

private final ActionCapabilityResolver actionCapabilityResolver =
        new ActionCapabilityResolver();

        private final CapabilityValidator capabilityValidator =
        new CapabilityValidator();

public HealingWebElement(
        WebElement delegate,
        By originalLocator,
        HealingWebDriver driver) {

    this.delegate = delegate;
    this.originalLocator = originalLocator;
    this.driver = driver;
}

public static HealingWebElement deferred(
        By locator,
        HealingWebDriver driver) {

    return new HealingWebElement(
            null,
            locator,
            driver);
}

private void executeAction(
        ExecutionAction action,
        ElementAction elementAction)
        throws RuntimeException {

    ExecutionTracker.recordAction(action.name());

    try {

        WebElement actualElement =
                elementAction.execute();

        ExecutionRecorder.record(
                actualElement,
                originalLocator,
                driver);

        /*
         * Action completed successfully.
         * It is safe to clear the action now.
         */
        ExecutionTracker.clearAction();

    } catch (RuntimeException e) {

        /*
         * IMPORTANT:
         *
         * Do NOT clear the action here.
         *
         * The failure-healing pipeline still needs
         * the failed action to determine expected
         * intent.
         *
         * Example:
         *
         * SEND_KEYS
         *     -> failed
         *     -> FailureContextBuilder
         *     -> failedAction = SEND_KEYS
         *     -> ExpectedIntent = INPUT
         */
        HealingLogger.debug(
                "ACTION FAILED | preserving action for healing = "
                        + action);

        throw e;
    }
}

@Override
public void click() {

    executeAction(
            ExecutionAction.CLICK,
            () -> {

                WebElement actual =
                        resolveForAction(
                                ExecutionAction.CLICK);

                actual.click();

                return actual;
            });
}

@Override
public void submit() {

    executeAction(
            ExecutionAction.UNKNOWN,
            () -> {

                delegate.submit();

                return delegate;
            });
}

@Override
public void sendKeys(
        CharSequence... keysToSend) {

    HealingLogger.debug(
            "SEND_KEYS CALLED | originalLocator="
                    + originalLocator);

    /*
     * IMPORTANT:
     *
     * Record the real user action BEFORE resolving
     * the deferred element.
     *
     * The healing pipeline must know that this locator
     * is being requested for SEND_KEYS.
     */
    ExecutionTracker.recordAction(
            ExecutionAction.SEND_KEYS.name());

    try {

        WebElement actual =
                resolveForAction(
                        ExecutionAction.SEND_KEYS);

        HealingLogger.debug(
                "SEND_KEYS TARGET | tag="
                        + actual.getTagName()
                        + " | id="
                        + actual.getAttribute("id")
                        + " | class="
                        + actual.getAttribute("class"));

        ElementCapability capability =
                actionCapabilityResolver.resolve(
                        ExecutionAction.SEND_KEYS);

        if (capability != null
                && capabilityValidator.supports(
                        actual,
                        capability)) {

            HealingLogger.debug(
                    "SEND_KEYS DIRECT | tag="
                            + actual.getTagName());

            actual.sendKeys(keysToSend);

            ExecutionTracker.clearAction();

            ExecutionRecorder.record(
                    actual,
                    originalLocator,
                    driver);

            return;
        }

        WebElement target =
                actionRecoveryEngine.recover(
                        actual,
                        capability);

        HealingLogger.debug(
                "SEND_KEYS RECOVERED | tag="
                        + target.getTagName());

        target.sendKeys(keysToSend);

        ExecutionTracker.clearAction();

        ExecutionRecorder.record(
                target,
                originalLocator,
                driver);

    } catch (RuntimeException e) {

        /*
         * DO NOT clear the action.
         *
         * FailureContextBuilder still needs SEND_KEYS.
         */
        HealingLogger.debug(
                "ACTION FAILED | preserving action for healing = SEND_KEYS");

        throw e;
    }
}

@Override
public void clear() {

    HealingLogger.debug(
            "CLEAR CALLED | originalLocator="
                    + originalLocator);

    executeAction(
            ExecutionAction.CLEAR,
            () -> {

                WebElement actual =
                        resolveForAction(
                                ExecutionAction.CLEAR);

                HealingLogger.debug(
                        "CLEAR TARGET | tag="
                                + actual.getTagName()
                                + " | id="
                                + actual.getAttribute("id")
                                + " | class="
                                + actual.getAttribute("class"));

                ElementCapability capability =
                        actionCapabilityResolver.resolve(
                                ExecutionAction.CLEAR);

                if (capability != null
                        && capabilityValidator.supports(
                                actual,
                                capability)) {

                    HealingLogger.debug(
                            "CLEAR DIRECT | "
                                    + "tag="
                                    + actual.getTagName());

                    actual.clear();

                    return actual;
                }

                WebElement target =
                        actionRecoveryEngine.recover(
                                actual,
                                capability);

                HealingLogger.debug(
                        "CLEAR RECOVERED | tag="
                                + target.getTagName());

                target.clear();

                return target;
            });
}
    @Override
    public String getTagName() {
        return delegate.getTagName();
    }

    @Override
    public String getAttribute(String name) {
        return delegate.getAttribute(name);
    }

    @Override
    public boolean isSelected() {
        return delegate.isSelected();
    }

    @Override
    public boolean isEnabled() {
        return delegate.isEnabled();
    }

   @Override
public String getText() {

    WebElement actual =
            resolveForAction(
                    ExecutionAction.VERIFY);

    return actual.getText();
}

    @Override
    public List<WebElement> findElements(By by) {
        return delegate.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return delegate.findElement(by);
    }

@Override
public boolean isDisplayed() {

    /*
     * ExpectedConditions.elementToBeClickable()
     * calls isDisplayed() BEFORE click().
     *
     * Therefore the real user action is CLICK,
     * even though Selenium is currently performing
     * a visibility check.
     *
     * If this deferred element has not been resolved yet,
     * preserve CLICK as the action context so that
     * healing understands what the element is supposed
     * to support.
     */
    if (delegate == null) {

        ExecutionTracker.recordAction(
                ExecutionAction.CLICK.name());

        HealingLogger.debug(
                "CLICK CONTEXT DETECTED FROM "
                        + "elementToBeClickable | locator="
                        + originalLocator);

        delegate =
                driver.resolveForAction(
                        originalLocator,
                        ExecutionAction.CLICK);
    }

    return delegate.isDisplayed();
}

    @Override
    public Point getLocation() {
        return delegate.getLocation();
    }

    @Override
    public Dimension getSize() {
        return delegate.getSize();
    }

    @Override
    public Rectangle getRect() {
        return delegate.getRect();
    }

    @Override
    public String getCssValue(String propertyName) {
        return delegate.getCssValue(propertyName);
    }

    @Override
    public <X> X getScreenshotAs(
            org.openqa.selenium.OutputType<X> target) {

        return delegate.getScreenshotAs(target);
    }

    @Override
    public String getDomProperty(String name) {
        return delegate.getDomProperty(name);
    }

    @Override
    public String getDomAttribute(String name) {
        return delegate.getDomAttribute(name);
    }

    @Override
    public String getAriaRole() {
        return delegate.getAriaRole();
    }

    @Override
    public String getAccessibleName() {
        return delegate.getAccessibleName();
    }

    @Override
    public SearchContext getShadowRoot() {
        return delegate.getShadowRoot();
    }
private WebElement resolveForAction(
        ExecutionAction action) {

    if (delegate != null) {
        return delegate;
    }

    HealingLogger.debug(
            "DEFERRED ELEMENT RESOLUTION | "
                    + "locator="
                    + originalLocator
                    + " | action="
                    + action);

    delegate =
            driver.resolveForAction(
                    originalLocator,
                    action);

    return delegate;
}

}