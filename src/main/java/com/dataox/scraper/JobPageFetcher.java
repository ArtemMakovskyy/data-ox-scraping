package com.dataox.scraper;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JobPageFetcher {

    /**
     * Tries to fetch the HTML document from the given URL.
     * Returns null if an error occurs (e.g., page not available).
     *
     * @param jobPageUrl the job page URL
     * @return Jsoup Document or null
     */
    public Document fetch(String jobPageUrl) {
        try {
            return Jsoup.connect(jobPageUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
        } catch (IOException e) {
            log.warn("Failed to load page at URL: {} — {}", jobPageUrl, e.getMessage());
            return null;
        }
    }
}
