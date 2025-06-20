package com.dataox.scraper.impl;

import com.dataox.dto.JobPostingDto;
import com.dataox.exception.JobParsingException;
import com.dataox.scraper.JobPostingParser;
import com.dataox.scraper.PageFetcher;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobPostingParserImpl implements JobPostingParser {

    private static final String CSS_JOB_LINK = "[data-testid=job-title-link]";
    private static final String CSS_POSITION_NAME = "[itemprop=title]";
    private static final String CSS_ORGANIZATION_LINK = "a[data-testid=company-logo-link]";
    private static final String CSS_ORGANIZATION_TITLE_META = "meta[itemprop=name]";
    private static final String CSS_LOGO_META = "meta[itemprop=logo]";
    private static final String CSS_POSTED_DATE_META = "meta[itemprop=datePosted]";
    private static final String CSS_LOCATIONS_META
            = "[itemprop=jobLocation] meta[itemprop=address]";
    private static final String CSS_DESCRIPTION_BLOCK =
            "#content > div.sc-beqWaB.eFnOti > div.sc-dmqHEX.fPtgCq > div > div > "
                    + "div.sc-beqWaB.fmCCHr > div";
    private static final String CSS_TAGS = "[data-testid=tag]";
    private static final String ATTR_CONTENT = "content";
    private static final String ATTR_HREF = "href";
    private static final String BASE_URL = "https://jobs.techstars.com";

    private final PageFetcher pageFetcher;

    @Override
    public JobPostingDto parse(Element jobElement, String laborFunction) {
        try {
            JobPostingDto dto = new JobPostingDto();
            dto.setJobPageUrl(parseHref(jobElement.selectFirst(CSS_JOB_LINK)));
            dto.setPositionName(parseText(
                    jobElement.selectFirst(CSS_POSITION_NAME)));
            dto.setOrganizationUrl(parseHref(
                    jobElement.selectFirst(CSS_ORGANIZATION_LINK)));
            dto.setOrganizationTitle(parseContent(
                    jobElement.selectFirst(CSS_ORGANIZATION_TITLE_META)));
            dto.setLogoUrl(parseContent(
                    jobElement.selectFirst(CSS_LOGO_META)));
            dto.setPostedDateUnix(parsePostedDateUnix(
                    jobElement.selectFirst(CSS_POSTED_DATE_META)));
            dto.setLaborFunction(laborFunction);
            dto.setLocations(parseLocations(jobElement));
            dto.setDescriptionHtml(fetchJobDescription(dto.getJobPageUrl()));
            dto.setTags(parseTags(jobElement));
            return dto;
        } catch (Exception e) {
            throw new JobParsingException("Failed to parse job posting element", e);
        }
    }

    private String parseHref(Element element) {
        if (element == null) {
            return null;
        }
        String href = element.attr(ATTR_HREF);
        if (href == null || href.isBlank()) {
            return null;
        }
        return BASE_URL + href;
    }

    private String parseContent(Element element) {
        if (element == null) {
            return null;
        }
        String content = element.attr(ATTR_CONTENT);
        if (content == null || content.isBlank()) {
            return null;
        }
        return content;
    }

    private String parseText(Element element) {
        if (element == null) {
            return null;
        }
        String text = element.text();
        if (text == null || text.isBlank()) {
            return null;
        }
        return text;
    }

    private long parsePostedDateUnix(Element postedDate) {
        if (postedDate == null) {
            return 0L;
        }
        String postedDateContent = postedDate.attr(ATTR_CONTENT);
        if (postedDateContent == null || postedDateContent.isBlank()) {
            return 0L;
        }
        try {
            LocalDate localDate = LocalDate.parse(
                    postedDateContent, DateTimeFormatter.ISO_LOCAL_DATE);
            return localDate.atStartOfDay(ZoneId.of("UTC")).toEpochSecond();
        } catch (Exception e) {
            return 0L;
        }
    }

    private Set<String> parseLocations(Element jobElement) {
        Set<String> locations = new HashSet<>();
        Elements locationMetaElements = jobElement.select(CSS_LOCATIONS_META);
        for (Element locMeta : locationMetaElements) {
            String address = locMeta.attr(ATTR_CONTENT);
            if (address != null && !address.isBlank()) {
                locations.add(address);
            }
        }
        return locations.isEmpty() ? Set.of() : locations;
    }

    private String fetchJobDescription(String jobPageUrl) {
        if (jobPageUrl == null) {
            return null;
        }
        Document jobDetailsPage = pageFetcher.fetch(jobPageUrl);
        if (jobDetailsPage != null) {
            Element descBlock = jobDetailsPage.selectFirst(CSS_DESCRIPTION_BLOCK);
            if (descBlock != null) {
                return descBlock.html();
            }
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
        return tags.isEmpty() ? Set.of() : tags;
    }
}
