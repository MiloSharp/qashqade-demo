package com.qashqade.extconn.repository;

import com.qashqade.extconn.model.Planner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlannerRepository extends JpaRepository<Planner, Long> {

    @Query("SELECT p FROM Planner p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.owner) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Planner> findByNameOrOwnerContainingIgnoreCase(
            @Param("search") String search, Pageable pageable);
}
