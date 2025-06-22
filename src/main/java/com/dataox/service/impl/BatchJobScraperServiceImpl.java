package com.dataox.service.impl;

import com.dataox.service.BatchJobScraperService;
import com.dataox.service.JobPostingService;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class BatchJobScraperServiceImpl implements BatchJobScraperService {

    private final JobPostingService jobPostingService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(6);

    @Value("#{'${labor.functions}'.split(',')}")
    private List<String> laborFunctions;

    @Override
    public void scrapeAllInParallel() {

        List<Callable<Void>> tasks = laborFunctions.stream()
                .map(function -> (Callable<Void>) () -> {
                    try {
                        log.info("Start parsing: " + function);
                        jobPostingService.scrapeAndSaveJobs(function);
                        log.info("Finished parsing: " + function);
                    } catch (Exception e) {
                        log.error("Error parsing function: " + function, e);
                    }
                    return null;
                })
                .toList();

        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Batch scraping interrupted", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }
}
