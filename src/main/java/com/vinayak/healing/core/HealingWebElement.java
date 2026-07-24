package com.vinayak.healing.core;

import com.vinayak.healing.execution.ExecutionTracker;
import com.vinayak.healing.logging.HealingLogger;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;

public class HealingWebElement implements WebElement {

    private final WebElement delegate;
    private final By originalLocator;
    private final HealingWebDriver driver;

    public HealingWebElement(
            WebElement delegate,
            By originalLocator,
            HealingWebDriver driver) {

        this.delegate = delegate;
        this.originalLocator = originalLocator;
        this.driver = driver;
    }

  @Override
public void click() {

    ExecutionTracker.recordAction("CLICK");

    try {
        delegate.click();
    } finally {
        ExecutionTracker.clearAction();
    }
}

@Override
public void submit() {

    ExecutionTracker.recordAction("SUBMIT");

    try {
        delegate.submit();
    } finally {
        ExecutionTracker.clearAction();
    }
}

@Override
public void sendKeys(
        CharSequence... keysToSend) {

    ExecutionTracker.recordAction("SEND_KEYS");

    try {

        HealingLogger.debug(
                "SEND_KEYS CALLED | originalLocator="
                        + originalLocator
                        + " | actualTag="
                        + delegate.getTagName()
                        + " | data-test="
                        + delegate.getAttribute("data-test")
                        + " | id="
                        + delegate.getAttribute("id"));

        delegate.sendKeys(keysToSend);

    } finally {

        ExecutionTracker.clearAction();
    }
}

@Override
public void clear() {

    ExecutionTracker.recordAction("CLEAR");

    try {

        HealingLogger.debug(
                "CLEAR CALLED | originalLocator="
                        + originalLocator
                        + " | actualTag="
                        + delegate.getTagName()
                        + " | data-test="
                        + delegate.getAttribute("data-test")
                        + " | id="
                        + delegate.getAttribute("id"));

        delegate.clear();

    } finally {

        ExecutionTracker.clearAction();
    }
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