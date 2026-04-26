package com.qashqade.extconn.repository;

import com.qashqade.extconn.model.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportTypeRepository extends JpaRepository<ReportType, Long> {
    List<ReportType> findAllByOrderBySortOrderAsc();
}
