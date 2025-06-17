package com.dataox.scraper;

import com.dataox.config.WebDriverFactory;
import com.dataox.dto.JobPostingDTO;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

//import javax.lang.model.element.Element;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;

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


            findTableWithBlocks(driver);
            findBlocksInTable(driver);


            // Step 5: Return the HTML source
            return driver.getPageSource();

        } catch (Exception e) {
            log.error("Failed to fetch HTML: {}", e.getMessage(), e);
            return "Error occurred while fetching HTML: " + e.getMessage();
        } finally {
//            driver.quit();
        }
    }





    private List<WebElement> findBlocksInTable(WebDriver driver) {

        // found container with page
        WebElement tableBlock = driver.findElement(By.cssSelector("div.infinite-scroll-component.sc-beqWaB.biNQIL"));

        // fetch items with offers
        List<WebElement> jobBlocks = tableBlock.findElements(By.cssSelector("div[itemtype='https://schema.org/JobPosting']"));

        log.info("Found blocks: " + jobBlocks.size());


        //method fetch info from blocks
        if (true) {
//            List<WebElement> blocks = findBlocksInTable(driver);
            List<JobPostingDTO> jobPostings = new ArrayList<>();

            for (WebElement block : jobBlocks) {
//                System.out.println(block.getAttribute("outerHTML"));
                System.out.println(" ");
                JobPostingDTO dto = new JobPostingDTO();
                //method get tags and past into dto
                //todo get tags

                String outerHtml = block.getAttribute("outerHTML");
                Element jsoupElement = Jsoup.parse(outerHtml).body().child(0);
                JobPostingDTO jobPostingDTO = parseJobPosting(jsoupElement);


                //todo get other fields in block
//                JobPostingDTO jobPostingDTO = parseJobBlock(block);

                //todo add to list
                jobPostings.add(dto);
                log.info(dto);
            }
        }

        return jobBlocks;
    }


    public JobPostingDTO parseJobPosting(Element jobElement) {
        Elements outerHTML = jobElement.getElementsByAttribute("outerHTML");
        JobPostingDTO dto = new JobPostingDTO();

        // Название позиции
        Element titleElement = jobElement.selectFirst("[itemprop=title]");
        if (titleElement != null) {
            dto.setPositionName(titleElement.text());
        }

        // Ссылка на вакансию
        Element jobLink = jobElement.selectFirst("[data-testid=job-title-link]");
        if (jobLink != null) {
            dto.setJobPageUrl("https://jobs.techstars.com" + jobLink.attr("href"));
        }

        // Название организации
        Element orgNameMeta = jobElement.selectFirst("meta[itemprop=name]");
        if (orgNameMeta != null) {
            dto.setOrganizationTitle(orgNameMeta.attr("content"));
        }

        // Ссылка на организацию
        Element orgLink = jobElement.selectFirst("a[data-testid=company-logo-link]");
        if (orgLink != null) {
            dto.setOrganizationUrl("https://jobs.techstars.com" + orgLink.attr("href"));
        }

        // Логотип
        Element logoMeta = jobElement.selectFirst("meta[itemprop=logo]");
        if (logoMeta != null) {
            dto.setLogoUrl(logoMeta.attr("content"));
        }

        // Дата публикации
//        Element postedDate = jobElement.selectFirst("meta[itemprop=datePosted]");
//        if (postedDate != null) {
//            dto.setPostedDate(postedDate.attr("content"));
//        }

        // Теги
        Elements tagElements = jobElement.select("[data-testid=tag] > div");
        List<String> tags = tagElements.stream().map(Element::text).toList();
        dto.setTags(tags);
        System.out.println(dto);
        return dto;
    }



    private void findTableWithBlocks(WebDriver driver) {
        WebElement tableBlock = null;
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            tableBlock = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div.infinite-scroll-component.sc-beqWaB.biNQIL")
            ));
            log.info("Table with blocks found!");

        } catch (TimeoutException e) {
            log.error("Table with blocks not found");
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
