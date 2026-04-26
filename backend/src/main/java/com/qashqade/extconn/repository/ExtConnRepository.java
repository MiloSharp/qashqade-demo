package com.qashqade.extconn.repository;

import com.qashqade.extconn.model.ExtConn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtConnRepository extends JpaRepository<ExtConn, Long> {

    /**
     * Spring Data translates this into two SQL queries:
     *
     * Data:  SELECT * FROM qashqade."ExtConn"
     *        WHERE lower("Name") LIKE lower('%:search%')
     *        ORDER BY "Name" ASC
     *        LIMIT :size OFFSET :page * :size
     *
     * Count: SELECT COUNT(*) FROM qashqade."ExtConn"
     *        WHERE lower("Name") LIKE lower('%:search%')
     *
     * The count query is needed to calculate totalPages for the UI.
     * Both queries use the idx_extconn_name index for performance.
     */
    @Query("SELECT e FROM ExtConn e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<ExtConn> findByNameContainingIgnoreCase(@Param("search") String search, Pageable pageable);
}
