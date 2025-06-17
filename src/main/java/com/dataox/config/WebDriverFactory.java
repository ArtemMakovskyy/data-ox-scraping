package com.dataox.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class WebDriverFactory {

    @Value("${webdriver.headless:false}")
    private boolean headless;

    public WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        if (headless) {
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
        }
        return new ChromeDriver(options);
    }

    public WebDriverWait createWait(WebDriver driver, Duration timeout) {
        return new WebDriverWait(driver, timeout);
    }
}
