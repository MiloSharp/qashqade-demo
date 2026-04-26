package com.qashqade.extconn.repository;

import com.qashqade.extconn.model.Source;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
    @Query("SELECT s FROM Source s WHERE LOWER(s.sourceName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Source> findBySourceNameContainingIgnoreCase(@Param("search") String search, Pageable pageable);
}
