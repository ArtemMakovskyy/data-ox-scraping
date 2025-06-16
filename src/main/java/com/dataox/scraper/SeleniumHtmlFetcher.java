package com.dataox.scraper;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class SeleniumHtmlFetcher {


    public void fetch() {
        String url = "https://jobs.techstars.com/jobs";
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        try {
            driver.get(url);
            log.info(driver.getPageSource());
        } catch (Exception e) {
            log.error("Ошибка: " + e.getMessage());
        } finally {
            driver.quit();
        }

    }

}
