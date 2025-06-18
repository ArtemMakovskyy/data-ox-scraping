package com.dataox.scraper.impl;

import com.dataox.scraper.PageFetcher;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobPageFetcher implements PageFetcher {

    /**
     * Tries to fetch the HTML document from the given URL.
     * Returns null if an error occurs (e.g., page not available).
     *
     * @param jobPageUrl the job page URL
     * @return Jsoup Document or null
     */
    @Override
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
