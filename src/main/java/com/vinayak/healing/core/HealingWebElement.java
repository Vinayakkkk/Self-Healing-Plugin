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

private final WebElement delegate;
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

                delegate.click();

                return delegate;
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
                    + originalLocator
                    + " | actualTag="
                    + delegate.getTagName()
                    + " | data-test="
                    + delegate.getAttribute("data-test")
                    + " | id="
                    + delegate.getAttribute("id"));

    executeAction(
            ExecutionAction.SEND_KEYS,
            () -> {

                ElementCapability capability =
                        actionCapabilityResolver.resolve(
                                ExecutionAction.SEND_KEYS);

                /*
                 * IMPORTANT:
                 *
                 * If the element already supports
                 * SEND_KEYS, use it directly.
                 *
                 * Do NOT unnecessarily recover it.
                 *
                 * Recovery is only required when the
                 * located element cannot perform the
                 * requested action.
                 */
                if (capability != null
                        && capabilityValidator.supports(
                                delegate,
                                capability)) {

                    HealingLogger.debug(
                            "SEND_KEYS DIRECT | "
                                    + "element already supports TYPE"
                                    + " | tag="
                                    + delegate.getTagName());

                    delegate.sendKeys(keysToSend);

                    return delegate;
                }

                /*
                 * Current element cannot perform the
                 * requested action.
                 *
                 * Now try action recovery.
                 */
                WebElement target =
                        actionRecoveryEngine.recover(
                                delegate,
                                capability);

                HealingLogger.debug(
                        "SEND_KEYS RECOVERED | tag="
                                + target.getTagName());

                target.sendKeys(keysToSend);

                return target;
            });
}

@Override
public void clear() {

    HealingLogger.debug(
            "CLEAR CALLED | originalLocator="
                    + originalLocator
                    + " | actualTag="
                    + delegate.getTagName()
                    + " | data-test="
                    + delegate.getAttribute("data-test")
                    + " | id="
                    + delegate.getAttribute("id"));

    executeAction(
            ExecutionAction.CLEAR,
            () -> {

                ElementCapability capability =
                        actionCapabilityResolver.resolve(
                                ExecutionAction.CLEAR);

                if (capability != null
                        && capabilityValidator.supports(
                                delegate,
                                capability)) {

                    HealingLogger.debug(
                            "CLEAR DIRECT | "
                                    + "element already supports CLEAR"
                                    + " | tag="
                                    + delegate.getTagName());

                    delegate.clear();

                    return delegate;
                }

                WebElement target =
                        actionRecoveryEngine.recover(
                                delegate,
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
        return delegate.getText();
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


}