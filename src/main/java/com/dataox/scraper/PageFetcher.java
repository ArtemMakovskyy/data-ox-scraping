package com.dataox.scraper;

import org.jsoup.nodes.Document;

public interface PageFetcher {
    Document fetch(String url);
}
