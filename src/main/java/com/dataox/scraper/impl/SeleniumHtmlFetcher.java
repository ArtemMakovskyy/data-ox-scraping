package com.dataox.scraper.impl;

import com.dataox.config.WebDriverFactory;
import com.dataox.dto.JobPostingDto;
import com.dataox.exception.JobFetchingException;
import com.dataox.exception.JobParsingException;
import com.dataox.scraper.JobFetcher;
import com.dataox.scraper.PageFetcher;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
    private static final String CSS_JOB_LINK = "[data-testid=job-title-link]";
    private static final String CSS_POSITION_NAME = "[itemprop=title]";
    private static final String CSS_ORGANIZATION_LINK = "a[data-testid=company-logo-link]";
    private static final String CSS_ORGANIZATION_TITLE_META = "meta[itemprop=name]";
    private static final String CSS_LOGO_META = "meta[itemprop=logo]";
    private static final String CSS_POSTED_DATE_META = "meta[itemprop=datePosted]";
    private static final String CSS_LOCATIONS_META
            = "[itemprop=jobLocation] meta[itemprop=address]";
    private static final String CSS_TAGS = "[data-testid=tag]";
    private static final String CSS_DESCRIPTION_BLOCK = "#content > div.sc-beqWaB.eFnOti > "
            + "div.sc-dmqHEX.fPtgCq > div > div > div.sc-beqWaB.fmCCHr > div";
    private static final String COOKIE_ACCEPT_BUTTON_ID = "onetrust-accept-btn-handler";

    @Value("${jobs.techstars.url}")
    private String jobUrl;

    private final WebDriverFactory webDriverFactory;
    private final PageFetcher pageFetcher;

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
            loadAllItems(driver);
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

    private void selectJobFunction(WebDriverWait wait, String laborFunction) {
        WebElement jobFunctionDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(URL_JOB_FUNCTION_DROPDOWN)));
        jobFunctionDropdown.click();

        String optionXpath = String.format(XPATH_JOB_FUNCTION_OPTION, laborFunction);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(optionXpath)));

        WebElement desiredOption = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(optionXpath)));
        desiredOption.click();
    }

    private void waitForFilterToApply() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Thread was interrupted", e);
        }
    }

    private void logItemsCount(WebDriver driver) {
        WebElement element = driver.findElement(By.xpath(XPATH_SHOWING_ITEMS));
        String text = element.getText();
        String digits = text.replaceAll("\\D+", "");
        log.info("Job items found: " + digits);
    }

    private void loadAllItems(WebDriver driver) {
        while (true) {
            ((JavascriptExecutor) driver).executeScript(
                    "window.scrollTo(0, document.body.scrollHeight);");

            List<WebElement> buttons = driver.findElements(By.xpath(XPATH_LOAD_MORE_BUTTON));
            if (buttons.isEmpty()) {
                log.info("No more 'Load more' button found. All items are loaded.");
                break;
            }

            try {
                WebElement loadMoreButton = buttons.get(0);
                if (loadMoreButton.isDisplayed() && loadMoreButton.isEnabled()) {
                    log.info("Clicking 'Load more' button with JavaScript...");
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].click();", loadMoreButton);
                    Thread.sleep(1500);
                } else {
                    log.info("'Load more' button is not clickable. Stopping.");
                    break;
                }
            } catch (Exception e) {
                log.warn("Error while pressing 'Load more': {}", e.getMessage());
                break;
            }
        }
    }

    private List<JobPostingDto> findBlocksInTable(WebDriver driver, String laborFunction) {
        try {
            WebElement tableBlock = driver.findElement(By.cssSelector(CSS_TABLE_BLOCK));
            List<WebElement> jobBlocks = tableBlock.findElements(By.cssSelector(CSS_JOB_BLOCKS));

            log.info("Found blocks: " + jobBlocks.size());

            List<JobPostingDto> jobPostings = new ArrayList<>();
            for (WebElement block : jobBlocks) {
                String outerHtml = block.getAttribute("outerHTML");
                Element jsoupElement = Jsoup.parse(outerHtml).body().child(0);
                jobPostings.add(parseJobPosting(jsoupElement, laborFunction));
            }
            return jobPostings;
        } catch (NoSuchElementException e) {
            throw new JobFetchingException("Error finding job blocks in the page", e);
        } catch (WebDriverException e) {
            throw new JobFetchingException("WebDriver error", e);
        }
    }

    public JobPostingDto parseJobPosting(Element jobElement, String laborFunction) {
        try {
            JobPostingDto dto = new JobPostingDto();

            dto.setJobPageUrl(parseJobPageUrl(jobElement));
            dto.setPositionName(parsePositionName(jobElement));
            dto.setOrganizationUrl(parseOrganizationUrl(jobElement));
            dto.setOrganizationTitle(parseOrganizationTitle(jobElement));
            dto.setLogoUrl(parseLogoUrl(jobElement));
            dto.setPostedDateUnix(parsePostedDateUnix(jobElement));
            dto.setLaborFunction(laborFunction);
            dto.setLocations(parseLocations(jobElement));
            dto.setDescriptionHtml(fetchJobDescription(dto.getJobPageUrl()));
            dto.setTags(parseTags(jobElement));

            return dto;
        } catch (Exception e) {
            throw new JobParsingException("Failed to parse job posting element", e);
        }
    }

    private String parseJobPageUrl(Element jobElement) {
        Element jobLink = jobElement.selectFirst(CSS_JOB_LINK);
        if (jobLink != null) {
            return "https://jobs.techstars.com" + jobLink.attr("href");
        }
        return null;
    }

    private String parsePositionName(Element jobElement) {
        Element titleElement = jobElement.selectFirst(CSS_POSITION_NAME);
        return titleElement != null ? titleElement.text() : null;
    }

    private String parseOrganizationUrl(Element jobElement) {
        Element orgLink = jobElement.selectFirst(CSS_ORGANIZATION_LINK);
        if (orgLink != null) {
            return "https://jobs.techstars.com" + orgLink.attr("href");
        }
        return null;
    }

    private String parseOrganizationTitle(Element jobElement) {
        Element orgNameMeta = jobElement.selectFirst(CSS_ORGANIZATION_TITLE_META);
        return orgNameMeta != null ? orgNameMeta.attr("content") : null;
    }

    private String parseLogoUrl(Element jobElement) {
        Element logoMeta = jobElement.selectFirst(CSS_LOGO_META);
        return logoMeta != null ? logoMeta.attr("content") : null;
    }

    private long parsePostedDateUnix(Element jobElement) {
        Element postedDate = jobElement.selectFirst(CSS_POSTED_DATE_META);
        if (postedDate != null) {
            String postedDateContent = postedDate.attr("content");
            try {
                LocalDate localDate = LocalDate.parse(
                        postedDateContent, DateTimeFormatter.ISO_LOCAL_DATE);
                return localDate.atStartOfDay(ZoneId.of("UTC")).toEpochSecond();
            } catch (Exception e) {
                return 0L;
            }
        }
        return 0L;
    }

    private Set<String> parseLocations(Element jobElement) {
        Set<String> locations = new HashSet<>();
        Elements locationMetaElements = jobElement.select(CSS_LOCATIONS_META);
        for (Element locMeta : locationMetaElements) {
            String address = locMeta.attr("content");
            if (address != null && !address.isBlank()) {
                locations.add(address);
            }
        }
        return locations.isEmpty() ? null : locations;
    }

    private String fetchJobDescription(String jobPageUrl) {
        if (jobPageUrl == null) {
            return null;
        }
        Document jobDetailsPage = pageFetcher.fetch(jobPageUrl);
        if (jobDetailsPage != null) {
            Element descBlock = jobDetailsPage.selectFirst(CSS_DESCRIPTION_BLOCK);
            return descBlock != null ? descBlock.html() : null;
        }
        return null;
    }

    private Set<String> parseTags(Element jobElement) {
        Set<String> tags = new HashSet<>();
        Elements tagElements = jobElement.select(CSS_TAGS);
        for (Element tag : tagElements) {
            String tagText = tag.text();
            if (tagText != null && !tagText.isBlank()) {
                tags.add(tagText.trim());
            }
        }
        return tags.isEmpty() ? null : tags;
    }

    private void acceptCookiesIfPresent(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement acceptButton = wait.until(ExpectedConditions
                    .elementToBeClickable(By.id(COOKIE_ACCEPT_BUTTON_ID)));
            if (acceptButton.isDisplayed()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", acceptButton);
                log.info("Cookie banner accepted using JavaScript click");
            }
        } catch (TimeoutException ignored) {
            // Cookie banner not present
        }
    }

}
