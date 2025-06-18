package com.dataox.scraper;

import com.dataox.dto.JobPostingDto;
import java.util.List;

public interface JobFetcher {
    List<JobPostingDto> fetch(String laborFunction);
}
