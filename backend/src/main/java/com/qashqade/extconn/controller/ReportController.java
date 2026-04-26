package com.qashqade.extconn.controller;

import com.qashqade.extconn.model.Report;
import com.qashqade.extconn.repository.ReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@CrossOrigin(origins = "http://localhost:4200")
public class ReportController {

    private final ReportRepository repo;
    public ReportController(ReportRepository repo) { this.repo = repo; }

    @GetMapping
    public Page<Report> getAll(
            @RequestParam(defaultValue = "")   String search,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pr = PageRequest.of(0, Math.min(size, 50),
            Sort.by(Sort.Direction.ASC, "reportType")
                .and(Sort.by(Sort.Direction.ASC, "reportName")));
        if (search != null && !search.isBlank()) {
            return repo.findByTypeOrNameContainingIgnoreCase(search.trim(), pr);
        }
        return repo.findAll(pr);
    }
}
