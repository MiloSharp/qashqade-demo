package com.qashqade.extconn.repository;

import com.qashqade.extconn.model.PlannerFund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlannerFundRepository extends JpaRepository<PlannerFund, Long> {
    List<PlannerFund> findByPlannerIdOrderByFundFundNameAsc(Long plannerId);
}
