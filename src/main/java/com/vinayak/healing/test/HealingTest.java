package com.vinayak.healing.test;

import com.vinayak.healing.config.HealingConfig;
import com.vinayak.healing.core.HealingWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class HealingTest {

    public static void main(String[] args) {

        WebDriver driver =
                new ChromeDriver();

        driver.get(
            "https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");

        HealingConfig config =
                new HealingConfig();

        HealingWebDriver healingDriver =
                new HealingWebDriver(
                        driver,
                        config);

        try {

            Thread.sleep(3000);

            // Username
            healingDriver.findElement(
                    By.id("wrong-username"))
                    .sendKeys("Admin");

            // Password
            healingDriver.findElement(
                    By.id("wrong-password"))
                    .sendKeys("admin123");

            System.out.println(
                    "Both fields healed successfully");

            Thread.sleep(5000);

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            driver.quit();
        }
    }
}