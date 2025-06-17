package com.dataox.controller;

import com.dataox.scraper.SeleniumHtmlFetcher;
import com.dataox.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final SeleniumHtmlFetcher seleniumHtmlFetcher;
    private final JobPostingService jobPostingService;

    @GetMapping("/s")
    public String test_selenium(){
        seleniumHtmlFetcher.fetch();
        return "test_selenium";
    }

}
