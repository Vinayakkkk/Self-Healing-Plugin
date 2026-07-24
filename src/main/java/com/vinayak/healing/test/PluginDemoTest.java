package com.vinayak.healing.test;

import com.vinayak.healing.config.HealingConfig;
import com.vinayak.healing.core.HealingWebDriver;
import com.vinayak.healing.factory.SelfHealingDriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;

public class PluginDemoTest {

    public static void main(String[] args) {

        ChromeDriver chrome =
                new ChromeDriver();

        HealingConfig config =
                new HealingConfig();

        config.setAiEnabled(true);

        config.setCacheEnabled(true);

        config.setReportEnabled(true);

        HealingWebDriver driver =
                SelfHealingDriverFactory.create(
                        chrome,
                        config);

        try {

            chrome.get(
                    "https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");

            Thread.sleep(3000);

            driver.findElement(
                    By.id("wrong-username"))
                    .sendKeys("Admin");

            System.out.println(
                    "Plugin test successful");

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            chrome.quit();
        }
    }
}