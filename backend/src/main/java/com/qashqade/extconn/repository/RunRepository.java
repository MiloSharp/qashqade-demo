package com.qashqade.extconn.repository;

import com.qashqade.extconn.model.Run;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RunRepository extends JpaRepository<Run, Long> {
    @Query("SELECT r FROM Run r WHERE LOWER(r.runName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Run> findByRunNameContainingIgnoreCase(@Param("search") String search, Pageable pageable);
}
