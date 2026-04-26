package com.qashqade.extconn.controller;

import com.qashqade.extconn.model.Source;
import com.qashqade.extconn.repository.SourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/source")
@CrossOrigin(origins = "http://localhost:4200")
public class SourceController {

    private final SourceRepository repo;
    public SourceController(SourceRepository repo) { this.repo = repo; }

    @GetMapping
    public Page<Source> getAll(
            @RequestParam(defaultValue = "")   String search,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pr = PageRequest.of(0, Math.min(size, 50),
            Sort.by(Sort.Direction.ASC, "sourceName"));
        if (search != null && !search.isBlank()) {
            return repo.findBySourceNameContainingIgnoreCase(search.trim(), pr);
        }
        return repo.findAll(pr);
    }
}
