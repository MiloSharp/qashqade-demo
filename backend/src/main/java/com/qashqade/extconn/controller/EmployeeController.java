package com.qashqade.extconn.controller;

import com.qashqade.extconn.model.Employee;
import com.qashqade.extconn.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin(origins = "http://localhost:4200")
public class EmployeeController {

    private final EmployeeRepository repo;

    public EmployeeController(EmployeeRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public Page<Employee> getAll(
            @RequestParam(defaultValue = "")   String search,
            @RequestParam(defaultValue = "10") int    size) {
        PageRequest pr = PageRequest.of(0, Math.min(size, 50),
            Sort.by(Sort.Direction.ASC, "fullName"));
        if (search != null && !search.isBlank()) {
            return repo.findByFullNameContainingIgnoreCase(search.trim(), pr);
        }
        return repo.findAll(pr);
    }

    // Create a new employee on the fly (called when user types a name not in the list)
    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Map<String, Object> body) {
        String fullName = body.get("fullName") != null ? body.get("fullName").toString().trim() : "";
        if (fullName.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        // Return existing if name already matches exactly
        return repo.findByFullNameIgnoreCase(fullName).map(ResponseEntity::ok).orElseGet(() -> {
            Employee e = new Employee();
            e.setFullName(fullName);
            return ResponseEntity.ok(repo.save(e));
        });
    }
}
