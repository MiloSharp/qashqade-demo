package com.qashqade.extconn.repository;

import com.qashqade.extconn.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    @Query("SELECT r FROM Report r WHERE LOWER(r.reportType) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.reportName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Report> findByTypeOrNameContainingIgnoreCase(@Param("search") String search, Pageable pageable);
}
