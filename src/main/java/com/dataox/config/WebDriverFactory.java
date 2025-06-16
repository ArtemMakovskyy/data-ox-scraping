package com.dataox.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class WebDriverFactory {

    public WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");

        return new ChromeDriver(options);
    }

    public WebDriverWait createWait(WebDriver driver, Duration timeout) {
        return new WebDriverWait(driver, timeout);
    }
}
