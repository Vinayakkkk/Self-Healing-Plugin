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
