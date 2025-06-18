package com.dataox.scraper.impl;

import com.dataox.scraper.PageFetcher;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobPageFetcher implements PageFetcher {

    @Value("${page.fetch.max-retries}")
    private int maxRetryAttempts;

    @Value("${page.fetch.timeout-ms}")
    private int requestTimeoutMs;

    @Value("${page.fetch.sleep-between-retries-ms}")
    private int sleepBetweenRetriesMs;

    /**
     * Tries to fetch the HTML document from the given URL.
     * Returns null if all retry attempts fail.
     *
     * @param jobPageUrl the job page URL
     * @return Jsoup Document or null
     */
    @Override
    public Document fetch(String jobPageUrl) {
        int currentAttempt = 0;

        while (currentAttempt < maxRetryAttempts) {
            try {
                return Jsoup.connect(jobPageUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(requestTimeoutMs)
                        .get();
            } catch (IOException e) {
                currentAttempt++;
                log.warn("Attempt {}: Failed to load page at "
                        + "URL: {} — {}", currentAttempt, jobPageUrl, e.getMessage());

                try {
                    Thread.sleep(sleepBetweenRetriesMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Retry interrupted");
                    return null;
                }
            }
        }

        log.error("All {} attempts failed for URL: {}", maxRetryAttempts, jobPageUrl);
        return null;
    }
}
