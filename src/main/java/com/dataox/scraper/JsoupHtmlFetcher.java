package com.dataox.scraper;

import org.hibernate.annotations.Comment;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class JsoupHtmlFetcher {

    public void fetch() {
        String url = "https://jobs.techstars.com/jobs";

        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            String html = document.html();

            System.out.println(html);

        } catch (Exception e) {
            System.err.println("Error connection or page scrapping: " + e.getMessage());
        }
    }
}
