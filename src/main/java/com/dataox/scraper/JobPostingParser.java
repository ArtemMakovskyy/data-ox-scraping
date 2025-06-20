package com.dataox.scraper;

import com.dataox.dto.JobPostingDto;
import org.jsoup.nodes.Element;

public interface JobPostingParser {
    JobPostingDto parse(Element jobElement, String laborFunction);
}
