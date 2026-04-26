package com.qashqade.extconn.controller;

import com.qashqade.extconn.model.ExtConn;
import com.qashqade.extconn.service.ExtConnService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ext-conn")
@CrossOrigin(origins = "http://localhost:4200")
public class ExtConnController {

    private final ExtConnService service;

    public ExtConnController(ExtConnService service) {
        this.service = service;
    }

    /**
     * GET /api/ext-conn?page=0&size=25&search=bloom&sort=name
     *
     * Spring Data translates these into SQL:
     *   SELECT * FROM qashqade."ExtConn"
     *   WHERE lower("Name") LIKE '%bloom%'
     *   ORDER BY "Name" ASC
     *   LIMIT 25 OFFSET 0        <-- SQL-level pagination
     *
     * Only the requested rows are ever fetched from the database.
     */
    @GetMapping
    public Page<ExtConn> getAll(
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "25")   int    size,
            @RequestParam(defaultValue = "")     String search,
            @RequestParam(defaultValue = "name") String sort) {

        // Hard cap: never allow more than 100 rows per request
        int safeSize = Math.min(size, 100);
        return service.findAll(page, safeSize, search, sort);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExtConn> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ExtConn create(@RequestBody ExtConn extConn) {
        return service.save(extConn);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExtConn> update(
            @PathVariable Long id,
            @RequestBody ExtConn extConn) {
        try {
            return ResponseEntity.ok(service.update(id, extConn));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
