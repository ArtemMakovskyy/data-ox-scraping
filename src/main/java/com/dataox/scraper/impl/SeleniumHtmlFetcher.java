package com.dataox.scraper.impl;

import com.dataox.config.WebDriverFactory;
import com.dataox.dto.JobPostingDto;
import com.dataox.exception.JobFetchingException;
import com.dataox.scraper.JobFetcher;
import com.dataox.scraper.JobPostingParser;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class SeleniumHtmlFetcher implements JobFetcher {

    private static final String URL_JOB_FUNCTION_DROPDOWN = "#filter-0 > div > div";
    private static final String XPATH_JOB_FUNCTION_OPTION
            = "//div[contains(@class,'sc-beqWaB') and text()='%s']";
    private static final String XPATH_SHOWING_ITEMS = "//div[contains(text(),'Showing')]/b";
    private static final String XPATH_LOAD_MORE_BUTTON = "//div[text()='Load more']";
    private static final String CSS_TABLE_BLOCK = "div.infinite-scroll-component.sc-beqWaB.biNQIL";
    private static final String CSS_JOB_BLOCKS = "div[itemtype='https://schema.org/JobPosting']";
    private static final String COOKIE_ACCEPT_BUTTON_ID = "onetrust-accept-btn-handler";
    private static final String REGEX_NON_DIGITS = "\\D+";
    private static final String JS_CLICK = "arguments[0].click();";
    private static final String JS_SCROLL_TO_BOTTOM
            = "window.scrollTo(0, document.body.scrollHeight);";
    private static final long SLEEP_BEFORE_FILTER_MS = 3000;
    private static final long LOAD_MORE_PAUSE_MS = 1500;

    @Value("${jobs.techstars.url}")
    private String jobUrl;

    private final WebDriverFactory webDriverFactory;
    private final JobPostingParser jobPostingParser;

    @Override
    public List<JobPostingDto> fetch(String laborFunction) {
        WebDriver driver = webDriverFactory.createDriver();
        WebDriverWait wait = webDriverFactory.createWait(driver, Duration.ofSeconds(10));
        try {
            openJobPage(driver);
            acceptCookiesIfPresent(driver);
            selectJobFunction(wait, laborFunction);
            waitForFilterToApply();
            logItemsCount(driver);
            return findBlocksInTable(driver, laborFunction);
        } catch (TimeoutException e) {
            throw new JobFetchingException("Timeout during fetching", e);
        } catch (NoSuchElementException e) {
            throw new JobFetchingException("Element not found during fetching", e);
        } catch (WebDriverException e) {
            throw new JobFetchingException("WebDriver error during fetching", e);
        } finally {
            driver.quit();
        }
    }

    private void openJobPage(WebDriver driver) {
        driver.get(jobUrl);
        driver.manage().window().maximize();
    }

    private void acceptCookiesIfPresent(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement acceptButton = wait.until(ExpectedConditions
                    .elementToBeClickable(By.id(COOKIE_ACCEPT_BUTTON_ID)));
            if (acceptButton.isDisplayed()) {
                ((JavascriptExecutor) driver).executeScript(JS_CLICK, acceptButton);
                log.info("Cookie banner accepted using JavaScript click");
            }
        } catch (TimeoutException ignored) {
            //should be clear
        }
    }

    private void selectJobFunction(WebDriverWait wait, String laborFunction) {
        WebElement jobFunctionDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(URL_JOB_FUNCTION_DROPDOWN)));
        jobFunctionDropdown.click();

        String optionXpath = String.format(XPATH_JOB_FUNCTION_OPTION, laborFunction);
        WebElement desiredOption = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(optionXpath)));
        desiredOption.click();
    }

    private void waitForFilterToApply() {
        try {
            Thread.sleep(SLEEP_BEFORE_FILTER_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Thread was interrupted", e);
        }
    }

    private void logItemsCount(WebDriver driver) {
        WebElement element = driver.findElement(By.xpath(XPATH_SHOWING_ITEMS));
        String text = element.getText();
        String digits = text.replaceAll(REGEX_NON_DIGITS, "");
        log.info("Job items found: " + digits);
    }

    private List<JobPostingDto> findBlocksInTable(WebDriver driver, String laborFunction) {
        try {
            loadAllItems(driver);
            WebElement tableBlock = driver.findElement(By.cssSelector(CSS_TABLE_BLOCK));
            List<WebElement> jobBlocks = tableBlock.findElements(By.cssSelector(CSS_JOB_BLOCKS));
            log.info("Found blocks: " + jobBlocks.size());

            List<JobPostingDto> jobPostings = new ArrayList<>();
            for (WebElement block : jobBlocks) {
                String outerHtml = block.getAttribute("outerHTML");
                Element jsoupElement = Jsoup.parse(outerHtml).body().child(0);
                jobPostings.add(jobPostingParser.parse(jsoupElement, laborFunction));
            }
            return jobPostings;
        } catch (Exception e) {
            throw new JobFetchingException("Error parsing job blocks", e);
        }
    }

    private void loadAllItems(WebDriver driver) {
        while (true) {
            ((JavascriptExecutor) driver).executeScript(JS_SCROLL_TO_BOTTOM);

            List<WebElement> buttons = driver.findElements(By.xpath(XPATH_LOAD_MORE_BUTTON));
            if (buttons.isEmpty()) {
                log.info("No more 'Load more' button found. All items are loaded.");
                break;
            }

            try {
                WebElement loadMoreButton = buttons.get(0);
                if (loadMoreButton.isDisplayed() && loadMoreButton.isEnabled()) {
                    ((JavascriptExecutor) driver).executeScript(JS_CLICK, loadMoreButton);
                    Thread.sleep(LOAD_MORE_PAUSE_MS);
                } else {
                    break;
                }
            } catch (Exception e) {
                log.warn("Error while pressing 'Load more': {}", e.getMessage());
                break;
            }
        }
    }
}
