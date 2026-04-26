package com.qashqade.extconn.repository;

import com.qashqade.extconn.model.Fund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FundRepository extends JpaRepository<Fund, Long> {

    @Query("SELECT f FROM Fund f WHERE LOWER(f.fundName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Fund> findByFundNameContainingIgnoreCase(
            @Param("search") String search, Pageable pageable);

    Optional<Fund> findByFundNameIgnoreCase(String fundName);
}
