package com.vinayak.healing.factory;

import com.vinayak.healing.config.HealingConfig;
import com.vinayak.healing.core.HealingWebDriver;
import org.openqa.selenium.WebDriver;

public class SelfHealingDriverFactory {

    public static HealingWebDriver create(
            WebDriver driver,
            HealingConfig config) {

        System.out.println(
                "Starting Self-Healing Driver");

        System.out.println(
                config);

        return new HealingWebDriver(
                driver,
                config);
    }
}