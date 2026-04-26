package com.qashqade.extconn.controller;

import com.qashqade.extconn.model.Run;
import com.qashqade.extconn.repository.RunRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/run")
@CrossOrigin(origins = "http://localhost:4200")
public class RunController {

    private final RunRepository repo;
    public RunController(RunRepository repo) { this.repo = repo; }

    @GetMapping
    public Page<Run> getAll(
            @RequestParam(defaultValue = "")   String search,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pr = PageRequest.of(0, Math.min(size, 50),
            Sort.by(Sort.Direction.ASC, "runName"));
        if (search != null && !search.isBlank()) {
            return repo.findByRunNameContainingIgnoreCase(search.trim(), pr);
        }
        return repo.findAll(pr);
    }
}
