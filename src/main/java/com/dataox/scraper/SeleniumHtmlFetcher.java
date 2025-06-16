package com.dataox.scraper;

import com.dataox.config.WebDriverFactory;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Log4j2
@RequiredArgsConstructor
public class SeleniumHtmlFetcher {

    private final WebDriverFactory webDriverFactory;

    public String fetch() {
        WebDriver driver = webDriverFactory.createDriver();
        WebDriverWait wait = webDriverFactory.createWait(driver, Duration.ofSeconds(10));

        try {
            driver.get("https://jobs.techstars.com/jobs");
            driver.manage().window().maximize();

            acceptCookiesIfPresent(driver);

            // Step 1: Click on the "Job function" dropdown
            WebElement jobFunctionDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("#filter-0 > div > div")
            ));
            jobFunctionDropdown.click();

            // Step 2: Wait for the dropdown options to become visible
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'sc-beqWaB') and text()='Design']")
            ));

            // Step 3: Click on the desired option (e.g., "Design")
            WebElement desiredOption = driver.findElement(
                    By.xpath("//div[contains(@class,'sc-beqWaB') and text()='Design']")
            );
            desiredOption.click();

            // Step 4: Wait for the page to apply the filter (this can be replaced with a more robust wait)
            Thread.sleep(3000);

            // Step 5: Return the HTML source
            return driver.getPageSource();

        } catch (Exception e) {
            log.error("Failed to fetch HTML: {}", e.getMessage(), e);
            return "Error occurred while fetching HTML: " + e.getMessage();
        } finally {
            driver.quit();
        }
    }

    private void acceptCookiesIfPresent(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement acceptButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("onetrust-accept-btn-handler"))
            );
            if (acceptButton.isDisplayed()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", acceptButton);
                log.info("Cookie banner accepted using JavaScript click");
            }
        } catch (TimeoutException ignored) {
            // Cookie banner not present
        }
    }

    public void fetch2() {
        String url = "https://jobs.techstars.com/jobs";
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        try {
            driver.get(url);
            log.info(driver.getPageSource());
        } catch (Exception e) {
            log.error("Failed to load page: {}", e.getMessage(), e);
        } finally {
            driver.quit();
        }
    }
}
