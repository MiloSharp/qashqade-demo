package com.qashqade.extconn.controller;

import com.qashqade.extconn.model.*;
import com.qashqade.extconn.repository.*;
import com.qashqade.extconn.service.PlannerService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/planner")
@CrossOrigin(origins = "http://localhost:4200")
public class PlannerController {

    private final PlannerService    service;
    private final PlannerRepository plannerRepo;
    private final SourceRepository  sourceRepo;
    private final RunRepository     runRepo;
    private final ReportRepository  reportRepo;

    public PlannerController(PlannerService service,
                             PlannerRepository plannerRepo,
                             SourceRepository sourceRepo,
                             RunRepository runRepo,
                             ReportRepository reportRepo) {
        this.service     = service;
        this.plannerRepo = plannerRepo;
        this.sourceRepo  = sourceRepo;
        this.runRepo     = runRepo;
        this.reportRepo  = reportRepo;
    }

    @GetMapping
    public Page<Planner> getAll(
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "25")   int    size,
            @RequestParam(defaultValue = "")     String search,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc")  String dir) {
        return service.findAll(page, Math.min(size, 100), search, sort, dir);
    }

    @GetMapping("/planner-types")
    public List<PlannerType> getPlannerTypes() { return service.findAllPlannerTypes(); }

    @GetMapping("/report-types")
    public List<ReportType> getReportTypes() { return service.findAllReportTypes(); }

    @GetMapping("/{id}")
    public ResponseEntity<Planner> getById(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Planner create(@RequestBody Map<String, Object> body) {
        return service.save(mapBodyToPlanner(new Planner(), body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Planner> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Planner incoming        = mapBodyToPlanner(new Planner(), body);
            Long extConnId          = getLong(body, "extConnId");
            Long plannerTypeId      = getLong(body, "plannerTypeId");
            Long outputFormatId     = getLong(body, "outputFormatId");
            Long ownerEmployeeId    = getLong(body, "ownerEmployeeId");
            String status           = body.get("status") != null ? body.get("status").toString() : "";
            return ResponseEntity.ok(
                service.update(id, incoming, extConnId, plannerTypeId,
                               outputFormatId, ownerEmployeeId, status)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<Planner> run(@PathVariable Long id) {
        try {
            String log = "Run started at " + java.time.OffsetDateTime.now() + "\nCompleted successfully.";
            return ResponseEntity.ok(service.updateStatus(id, "Finished", log));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Source associations ───────────────────────────────────

    @PostMapping("/{id}/sources/{sourceId}")
    public ResponseEntity<Planner> addSource(@PathVariable Long id, @PathVariable Long sourceId) {
        return plannerRepo.findById(id).map(planner ->
            sourceRepo.findById(sourceId).map(source -> {
                if (!planner.getSources().contains(source)) {
                    planner.getSources().add(source);
                    plannerRepo.save(planner);
                }
                return ResponseEntity.ok(planner);
            }).orElse(ResponseEntity.<Planner>notFound().build())
        ).orElse(ResponseEntity.<Planner>notFound().build());
    }

    @DeleteMapping("/{id}/sources/{sourceId}")
    public ResponseEntity<Planner> removeSource(@PathVariable Long id, @PathVariable Long sourceId) {
        return plannerRepo.findById(id).map(planner -> {
            planner.getSources().removeIf(s -> s.getId().equals(sourceId));
            plannerRepo.save(planner);
            return ResponseEntity.ok(planner);
        }).orElse(ResponseEntity.<Planner>notFound().build());
    }

    // ── Run associations ──────────────────────────────────────

    @PostMapping("/{id}/runs/{runId}")
    public ResponseEntity<Planner> addRun(@PathVariable Long id, @PathVariable Long runId) {
        return plannerRepo.findById(id).map(planner ->
            runRepo.findById(runId).map(run -> {
                if (!planner.getRuns().contains(run)) {
                    planner.getRuns().add(run);
                    plannerRepo.save(planner);
                }
                return ResponseEntity.ok(planner);
            }).orElse(ResponseEntity.<Planner>notFound().build())
        ).orElse(ResponseEntity.<Planner>notFound().build());
    }

    @DeleteMapping("/{id}/runs/{runId}")
    public ResponseEntity<Planner> removeRun(@PathVariable Long id, @PathVariable Long runId) {
        return plannerRepo.findById(id).map(planner -> {
            planner.getRuns().removeIf(r -> r.getId().equals(runId));
            plannerRepo.save(planner);
            return ResponseEntity.ok(planner);
        }).orElse(ResponseEntity.<Planner>notFound().build());
    }

    // ── Report associations ───────────────────────────────────

    @PostMapping("/{id}/reports/{reportId}")
    public ResponseEntity<Planner> addReport(@PathVariable Long id, @PathVariable Long reportId) {
        return plannerRepo.findById(id).map(planner ->
            reportRepo.findById(reportId).map(report -> {
                if (!planner.getReports().contains(report)) {
                    planner.getReports().add(report);
                    plannerRepo.save(planner);
                }
                return ResponseEntity.ok(planner);
            }).orElse(ResponseEntity.<Planner>notFound().build())
        ).orElse(ResponseEntity.<Planner>notFound().build());
    }

    @DeleteMapping("/{id}/reports/{reportId}")
    public ResponseEntity<Planner> removeReport(@PathVariable Long id, @PathVariable Long reportId) {
        return plannerRepo.findById(id).map(planner -> {
            planner.getReports().removeIf(r -> r.getId().equals(reportId));
            plannerRepo.save(planner);
            return ResponseEntity.ok(planner);
        }).orElse(ResponseEntity.<Planner>notFound().build());
    }

    private Long getLong(Map<String, Object> body, String key) {
        return body.get(key) != null ? Long.valueOf(body.get(key).toString()) : null;
    }

    private Boolean getBool(Map<String, Object> body, String key) {
        Object val = body.get(key);
        if (val == null) return false;
        if (val instanceof Boolean) return (Boolean) val;
        return Boolean.parseBoolean(val.toString());
    }

    private Planner mapBodyToPlanner(Planner p, Map<String, Object> body) {
        if (body.get("name") != null)        p.setName(body.get("name").toString());
        if (body.get("description") != null) p.setDescription(body.get("description").toString());
        if (body.get("fundName") != null)    p.setFundName(body.get("fundName").toString());
        if (body.get("fundAlias") != null)   p.setFundAlias(body.get("fundAlias").toString());
        p.setTriggerSources(getBool(body, "triggerSources"));
        p.setTriggerRuns(getBool(body, "triggerRuns"));
        p.setTriggerReports(getBool(body, "triggerReports"));
        if (body.get("reportName") != null)  p.setReportName(body.get("reportName").toString());
        return p;
    }
}
