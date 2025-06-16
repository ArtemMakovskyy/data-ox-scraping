package com.dataox.controller;

import com.dataox.scraper.JsoupHtmlFetcher;
import com.dataox.scraper.SeleniumHtmlFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final JsoupHtmlFetcher jsoupHtmlFetcher;
    private final SeleniumHtmlFetcher seleniumHtmlFetcher;

    @GetMapping("/j")
    public String test_jsoup(){
        jsoupHtmlFetcher.fetch();
        return "test_jsoup";
    }

    @GetMapping("/s")
    public String test_selenium(){
        seleniumHtmlFetcher.fetch();
        return "test_selenium";
    }
}
