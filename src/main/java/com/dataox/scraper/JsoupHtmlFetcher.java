package com.dataox.scraper;

import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class JsoupHtmlFetcher {

    public void fetchTables() {
        log.info("Fetching tables...");
        String url1 = "https://jobs.techstars.com/jobs?filter=eyJqb2JfZnVuY3Rpb25zIjpbIklUIl19";
        fetchTableFronPage(url1);
    }

    public void fetchTableFronPage(String html) {
        log.info("Fetching tables...");

//        List<JobPosting> results = new ArrayList<>();
        Document doc = Jsoup.parse(html);

//        Elements jobs = doc.select("[itemtype='https://schema.org/JobPosting']");
        Elements tableBlock = doc.select("div.infinite-scroll-component.sc-beqWaB.biNQIL");

        System.out.println(tableBlock.size());


        if (!isEmpty(tableBlock)) {
            for (Element job : tableBlock) {
                String title = job.select("[itemprop=title]").text();
                String company = job.select("[itemprop=name]").text();
                String location = job.select("[itemprop=address]").attr("content");
                String datePosted = job.select("[itemprop=datePosted]").attr("content");
                String link = job.select("a[data-testid=job-title-link]").attr("href");

                System.out.println(title);
                System.out.println(company);
                System.out.println(location);
                System.out.println(datePosted);
                System.out.println(link);
            }


//            JobPosting posting = new JobPosting(
//                    title,
//                    company,
//                    location,
//                    datePosted,
//                    "https://jobs.techstars.com" + link
//            );
//
//            results.add(posting);
        }

//        return results;
    }


    public boolean isEmpty(Elements elements) {
        if (!elements.isEmpty()) {
            for (Element el : elements) {
                if (!el.text().trim().isEmpty()) {
                    log.info("block not empty");
                    return false;
                } else {
                    log.info("block is empty");
                    return false;
                }
            }
        } else {
            log.info("Block not found");

        }
        return true;
    }


    public String fetchPage() {
        String url = "https://jobs.techstars.com/jobs";
        String url1 = "https://jobs.techstars.com/jobs?filter=eyJqb2JfZnVuY3Rpb25zIjpbIklUIl19";
        String html = null;

        try {
            Document document = Jsoup.connect(url1)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            html = document.html();

            log.info(html);
        } catch (Exception e) {
            System.err.println("Error connection or page scrapping: " + e.getMessage());
        }
        return html;
    }


}
