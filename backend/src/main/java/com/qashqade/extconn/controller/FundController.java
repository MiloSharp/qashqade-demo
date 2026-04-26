package com.qashqade.extconn.controller;

import com.qashqade.extconn.model.*;
import com.qashqade.extconn.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fund")
@CrossOrigin(origins = "http://localhost:4200")
public class FundController {

    private final FundRepository        fundRepo;
    private final FundAliasRepository   aliasRepo;
    private final PlannerRepository     plannerRepo;
    private final PlannerFundRepository plannerFundRepo;

    public FundController(FundRepository fundRepo,
                          FundAliasRepository aliasRepo,
                          PlannerRepository plannerRepo,
                          PlannerFundRepository plannerFundRepo) {
        this.fundRepo        = fundRepo;
        this.aliasRepo       = aliasRepo;
        this.plannerRepo     = plannerRepo;
        this.plannerFundRepo = plannerFundRepo;
    }

    // ── Fund search (typeahead) ───────────────────────────────

    @GetMapping
    public Page<Fund> getAll(
            @RequestParam(defaultValue = "")   String search,
            @RequestParam(defaultValue = "10") int    size) {
        PageRequest pr = PageRequest.of(0, Math.min(size, 50),
            Sort.by(Sort.Direction.ASC, "fundName"));
        if (search != null && !search.isBlank()) {
            return fundRepo.findByFundNameContainingIgnoreCase(search.trim(), pr);
        }
        return fundRepo.findAll(pr);
    }

    // ── Fund create (+ Create option) ─────────────────────────

    @PostMapping
    public ResponseEntity<Fund> create(@RequestBody Map<String, Object> body) {
        String fundName = body.get("fundName") != null
            ? body.get("fundName").toString().trim() : "";
        if (fundName.isEmpty()) return ResponseEntity.badRequest().build();
        return fundRepo.findByFundNameIgnoreCase(fundName)
            .map(ResponseEntity::ok)
            .orElseGet(() -> {
                Fund f = new Fund();
                f.setFundName(fundName);
                return ResponseEntity.ok(fundRepo.save(f));
            });
    }

    // ── Alias management ──────────────────────────────────────

    @GetMapping("/{fundId}/aliases")
    public List<FundAlias> getAliases(@PathVariable Long fundId) {
        return aliasRepo.findByFundIdOrderByAliasNameAsc(fundId);
    }

    @PostMapping("/{fundId}/aliases")
    public ResponseEntity<FundAlias> addAlias(
            @PathVariable Long fundId,
            @RequestBody Map<String, Object> body) {
        String aliasName = body.get("aliasName") != null
            ? body.get("aliasName").toString().trim() : "";
        if (aliasName.isEmpty()) return ResponseEntity.badRequest().build();
        return fundRepo.findById(fundId).map(fund ->
            aliasRepo.findByFundIdAndAliasNameIgnoreCase(fundId, aliasName)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    FundAlias fa = new FundAlias();
                    fa.setFund(fund);
                    fa.setAliasName(aliasName);
                    return ResponseEntity.ok(aliasRepo.save(fa));
                })
        ).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{fundId}/aliases/{aliasId}")
    public ResponseEntity<Void> removeAlias(
            @PathVariable Long fundId,
            @PathVariable Long aliasId) {
        aliasRepo.deleteById(aliasId);
        return ResponseEntity.noContent().build();
    }

    // ── PlannerFund association endpoints ─────────────────────

    @GetMapping("/planner/{plannerId}")
    public List<PlannerFund> getPlannerFunds(@PathVariable Long plannerId) {
        return plannerFundRepo.findByPlannerIdOrderByFundFundNameAsc(plannerId);
    }

    @PostMapping("/planner/{plannerId}")
    public ResponseEntity<PlannerFund> addPlannerFund(
            @PathVariable Long plannerId,
            @RequestBody Map<String, Object> body) {
        Long fundId  = body.get("fundId")  != null ? Long.valueOf(body.get("fundId").toString())  : null;
        Long aliasId = body.get("aliasId") != null ? Long.valueOf(body.get("aliasId").toString()) : null;
        if (fundId == null) return ResponseEntity.badRequest().build();

        return plannerRepo.findById(plannerId).map(planner ->
            fundRepo.findById(fundId).map(fund -> {
                PlannerFund pf = new PlannerFund();
                pf.setPlanner(planner);
                pf.setFund(fund);
                if (aliasId != null) {
                    aliasRepo.findById(aliasId).ifPresent(pf::setAlias);
                }
                return ResponseEntity.ok(plannerFundRepo.save(pf));
            }).orElse(ResponseEntity.<PlannerFund>notFound().build())
        ).orElse(ResponseEntity.<PlannerFund>notFound().build());
    }

    @PutMapping("/planner/{plannerId}/row/{plannerFundId}")
    public ResponseEntity<PlannerFund> updatePlannerFundAlias(
            @PathVariable Long plannerId,
            @PathVariable Long plannerFundId,
            @RequestBody Map<String, Object> body) {
        Long aliasId = body.get("aliasId") != null
            ? Long.valueOf(body.get("aliasId").toString()) : null;
        return plannerFundRepo.findById(plannerFundId).map(pf -> {
            if (aliasId != null) {
                aliasRepo.findById(aliasId).ifPresent(pf::setAlias);
            } else {
                pf.setAlias(null);
            }
            return ResponseEntity.ok(plannerFundRepo.save(pf));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/planner/{plannerId}/row/{plannerFundId}")
    public ResponseEntity<Void> removePlannerFund(
            @PathVariable Long plannerId,
            @PathVariable Long plannerFundId) {
        plannerFundRepo.deleteById(plannerFundId);
        return ResponseEntity.noContent().build();
    }
}
