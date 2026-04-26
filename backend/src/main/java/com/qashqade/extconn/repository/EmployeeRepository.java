package com.qashqade.extconn.repository;

import com.qashqade.extconn.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e WHERE LOWER(e.fullName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Employee> findByFullNameContainingIgnoreCase(
            @Param("search") String search, Pageable pageable);

    Optional<Employee> findByFullNameIgnoreCase(String fullName);
}
