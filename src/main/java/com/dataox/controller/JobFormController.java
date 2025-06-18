package com.dataox.controller;

import com.dataox.service.JobPostingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class JobFormController {

    private static final List<String> VALID_LABOR_FUNCTIONS = List.of(
            "Accounting & Finance",
            "Administration",
            "Compliance / Regulatory",
            "Customer Service",
            "Data Science",
            "Design",
            "IT",
            "Legal",
            "Marketing & Communications",
            "Operations",
            "Other Engineering",
            "People & HR",
            "Product",
            "Quality Assurance",
            "Sales & Business Development",
            "Software Engineering"
    );

    private final JobPostingService jobPostingService;

    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("functions", VALID_LABOR_FUNCTIONS);
        return "labor-function-form";
    }

    @PostMapping("/parse")
    public String parse(@RequestParam String laborFunction, Model model) {
        jobPostingService.scrapeAndSaveJobs(laborFunction);
        model.addAttribute("message", "Parsing started for: " + laborFunction);
        return "result";
    }
}
