package com.qashqade.extconn.service;

import com.qashqade.extconn.model.Planner;
import com.qashqade.extconn.model.PlannerType;
import com.qashqade.extconn.model.ReportType;
import com.qashqade.extconn.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PlannerService {

    private final PlannerRepository     plannerRepo;
    private final ExtConnRepository     extConnRepo;
    private final PlannerTypeRepository plannerTypeRepo;
    private final ReportTypeRepository  reportTypeRepo;
    private final EmployeeRepository    employeeRepo;

    public PlannerService(
            PlannerRepository plannerRepo,
            ExtConnRepository extConnRepo,
            PlannerTypeRepository plannerTypeRepo,
            ReportTypeRepository reportTypeRepo,
            EmployeeRepository employeeRepo) {
        this.plannerRepo     = plannerRepo;
        this.extConnRepo     = extConnRepo;
        this.plannerTypeRepo = plannerTypeRepo;
        this.reportTypeRepo  = reportTypeRepo;
        this.employeeRepo    = employeeRepo;
    }

    public Page<Planner> findAll(int page, int size, String search,
                                  String sortBy, String sortDir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
            ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort sort = switch (sortBy) {
            case "owner"    -> Sort.by(direction, "owner");
            case "status"   -> Sort.by(direction, "status");
            case "statusAt" -> Sort.by(direction, "statusAt");
            default         -> Sort.by(direction, "name");
        };

        PageRequest pr = PageRequest.of(page, size, sort);
        if (search != null && !search.isBlank()) {
            return plannerRepo.findByNameOrOwnerContainingIgnoreCase(search.trim(), pr);
        }
        return plannerRepo.findAll(pr);
    }

    public Optional<Planner> findById(Long id) {
        return plannerRepo.findById(id);
    }

    public List<PlannerType> findAllPlannerTypes() {
        return plannerTypeRepo.findAllByOrderBySortOrderAsc();
    }

    public List<ReportType> findAllReportTypes() {
        return reportTypeRepo.findAllByOrderBySortOrderAsc();
    }

    public Planner save(Planner planner) {
        return plannerRepo.save(planner);
    }

    public Planner update(Long id, Planner incoming,
                          Long extConnId, Long plannerTypeId,
                          Long outputFormatId, Long ownerEmployeeId,
                          String newStatus) {
        return plannerRepo.findById(id).map(existing -> {
            existing.setName(incoming.getName());
            existing.setDescription(incoming.getDescription());
            existing.setFundName(incoming.getFundName());
            existing.setFundAlias(incoming.getFundAlias());
            existing.setTriggerSources(incoming.getTriggerSources());
            existing.setTriggerRuns(incoming.getTriggerRuns());
            existing.setTriggerReports(incoming.getTriggerReports());
            existing.setReportName(incoming.getReportName());

            // Sync legacy owner text from employee name
            if (ownerEmployeeId != null) {
                employeeRepo.findById(ownerEmployeeId).ifPresent(emp -> {
                    existing.setOwnerEmployee(emp);
                    existing.setOwner(emp.getFullName());
                });
            } else {
                existing.setOwnerEmployee(null);
                existing.setOwner(null);
            }

            String oldStatus     = existing.getStatus() == null ? "" : existing.getStatus();
            String updatedStatus = newStatus == null ? "" : newStatus;
            if (!updatedStatus.equals(oldStatus)) {
                existing.setStatus(updatedStatus);
                existing.setStatusAt(!updatedStatus.isBlank() ? OffsetDateTime.now() : null);
            }

            if (extConnId != null) {
                extConnRepo.findById(extConnId).ifPresent(existing::setExtConn);
            } else {
                existing.setExtConn(null);
            }
            if (plannerTypeId != null) {
                plannerTypeRepo.findById(plannerTypeId).ifPresent(existing::setPlannerType);
            } else {
                existing.setPlannerType(null);
            }
            if (outputFormatId != null) {
                reportTypeRepo.findById(outputFormatId).ifPresent(existing::setOutputFormat);
            } else {
                existing.setOutputFormat(null);
            }

            return plannerRepo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Planner not found: " + id));
    }

    public void delete(Long id) {
        plannerRepo.deleteById(id);
    }

    public Planner updateStatus(Long id, String status, String logFile) {
        return plannerRepo.findById(id).map(existing -> {
            existing.setStatus(status);
            existing.setLogFile(logFile);
            existing.setStatusAt(OffsetDateTime.now());
            return plannerRepo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Planner not found: " + id));
    }
}
