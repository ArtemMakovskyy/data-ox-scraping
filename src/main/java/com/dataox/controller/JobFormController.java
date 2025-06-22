package com.dataox.controller;

import com.dataox.service.JobPostingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class JobFormController {

    @Value("#{'${labor.functions}'.split(',')}")
    private List<String> laborFunctions;

    private final JobPostingService jobPostingService;

    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("functions", laborFunctions);
        return "labor-function-form";
    }

    @PostMapping("/parse")
    public String parse(@RequestParam String laborFunction, Model model) {
        jobPostingService.scrapeAndSaveJobs(laborFunction);
        model.addAttribute("message", "Parsing started for: " + laborFunction);
        return "result";
    }
}
