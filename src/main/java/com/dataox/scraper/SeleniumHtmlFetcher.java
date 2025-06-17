package com.dataox.scraper;

import com.dataox.config.WebDriverFactory;
import com.dataox.dto.JobPostingDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Log4j2
@RequiredArgsConstructor
public class SeleniumHtmlFetcher {
    @Value("${jobs.techstars.url}")
    private String jobUrl;

    private final WebDriverFactory webDriverFactory;
    private final JobPageFetcher jobPageFetcher;

    public List<JobPostingDTO> fetch(String laborFunction) {
        WebDriver driver = webDriverFactory.createDriver();
        WebDriverWait wait = webDriverFactory.createWait(driver, Duration.ofSeconds(10));

        try {
            driver.get(jobUrl);
            driver.manage().window().maximize();

            acceptCookiesIfPresent(driver);

            // Step 1: Click on the "Job function" dropdown
            WebElement jobFunctionDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("#filter-0 > div > div")
            ));
            jobFunctionDropdown.click();

            // Step 2: Wait for the dropdown options to become visible
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'sc-beqWaB') and text()='"
                            + laborFunction + "']")
            ));

            // Step 3: Click on the desired option (e.g., "Design", or another passed value)
            WebElement desiredOption = driver.findElement(
                    By.xpath("//div[contains(@class,'sc-beqWaB') and text()='"
                            + laborFunction + "']")
            );
            desiredOption.click();


            // Step 4: Wait for the page to apply the filter (this can be replaced with a more robust wait)
            Thread.sleep(3000);

            List<JobPostingDTO> blocksInTable = findBlocksInTable(driver, laborFunction);

            // Step 5: Return the HTML source
            return blocksInTable;

        } catch (Exception e) {
            log.error("Failed to fetch HTML: {}", e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            driver.quit();
        }
    }


    private List<JobPostingDTO> findBlocksInTable(WebDriver driver, String laborFunction) {

        // found container with page
        WebElement tableBlock = driver.findElement(By.cssSelector("div.infinite-scroll-component.sc-beqWaB.biNQIL"));

        // fetch items with offers
        List<WebElement> jobBlocks = tableBlock.findElements(By.cssSelector("div[itemtype='https://schema.org/JobPosting']"));

        log.info("Found blocks: " + jobBlocks.size());

        List<JobPostingDTO> jobPostings = new ArrayList<>();

        //method fetch info from blocks
        for (WebElement block : jobBlocks) {

            String outerHtml = block.getAttribute("outerHTML");
            Element jsoupElement = Jsoup.parse(outerHtml).body().child(0);
            JobPostingDTO jobPostingDTO = parseJobPosting(jsoupElement, laborFunction);

            jobPostings.add(jobPostingDTO);
            log.info(jobPostingDTO);
        }

        return jobPostings;
    }


    public JobPostingDTO parseJobPosting(Element jobElement, String laborFunction) {
        JobPostingDTO dto = new JobPostingDTO();

        // jobPageUrl
        Element jobLink = jobElement.selectFirst("[data-testid=job-title-link]");
        if (jobLink != null) {
            dto.setJobPageUrl("https://jobs.techstars.com" + jobLink.attr("href"));
        }

        // positionName
        Element titleElement = jobElement.selectFirst("[itemprop=title]");
        if (titleElement != null) {
            dto.setPositionName(titleElement.text());
        }

        // organizationUrl
        Element orgLink = jobElement.selectFirst("a[data-testid=company-logo-link]");
        if (orgLink != null) {
            dto.setOrganizationUrl("https://jobs.techstars.com" + orgLink.attr("href"));
        }

        // organizationTitle
        Element orgNameMeta = jobElement.selectFirst("meta[itemprop=name]");
        if (orgNameMeta != null) {
            dto.setOrganizationTitle(orgNameMeta.attr("content"));
        }

        // logoUrl
        Element logoMeta = jobElement.selectFirst("meta[itemprop=logo]");
        if (logoMeta != null) {
            dto.setLogoUrl(logoMeta.attr("content"));
        }

        // postedDateUnix
        Element postedDate = jobElement.selectFirst("meta[itemprop=datePosted]");
        if (postedDate != null) {
            String postedDateContent = postedDate.attr("content");
            try {
                LocalDate localDate = LocalDate.parse(postedDateContent, DateTimeFormatter.ISO_LOCAL_DATE);
                long postedDateUnix = localDate.atStartOfDay(ZoneId.of("UTC")).toEpochSecond();
                dto.setPostedDateUnix(postedDateUnix);
            } catch (Exception e) {
                dto.setPostedDateUnix(0L);
            }
        }

        // laborFunction
        dto.setLaborFunction(laborFunction);

        // locations
        List<String> locations = new ArrayList<>();
        Elements locationMetaElements = jobElement.select("[itemprop=jobLocation] meta[itemprop=address]");
        for (Element locMeta : locationMetaElements) {
            String address = locMeta.attr("content");
            if (address != null && !address.isBlank()) {
                locations.add(address);
            }
        }
        dto.setLocations(locations.isEmpty() ? null : locations);

        // descriptionHtml
        if (dto.getJobPageUrl() != null) {
            Document jobDetailsPage = jobPageFetcher.fetch(dto.getJobPageUrl());

            if (jobDetailsPage != null) {
                Element descBlock = jobDetailsPage.selectFirst(
                        "#content > div.sc-beqWaB.eFnOti > div.sc-dmqHEX.fPtgCq > div > div > div.sc-beqWaB.fmCCHr > div"
                );

                if (descBlock != null) {
                    dto.setDescriptionHtml(descBlock.html());
                } else {
                    dto.setDescriptionHtml(null);
                }
            } else {
                dto.setDescriptionHtml(null);
            }
        }

        // tags
        List<String> tags = new ArrayList<>();
        Elements tagElements = jobElement.select("[data-testid=tag]");
        for (Element tag : tagElements) {
            String tagText = tag.text();
            if (tagText != null && !tagText.isBlank()) {
                tags.add(tagText.trim());
            }
        }
        dto.setTags(tags.isEmpty() ? null : tags);

        return dto;
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

}
