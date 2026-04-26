package com.qashqade.extconn.repository;

import com.qashqade.extconn.model.PlannerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlannerTypeRepository extends JpaRepository<PlannerType, Long> {
    List<PlannerType> findAllByOrderBySortOrderAsc();
}
