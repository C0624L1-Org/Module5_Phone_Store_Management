package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IEmployeeRepository extends JpaRepository<Employee, Integer> {
    @Query(value = "SELECT * FROM employee", nativeQuery = true)
    Page<Employee> getAllEmployees(Pageable pageable);
    @Query(value = "SELECT * FROM employee WHERE fullName LIKE %?1%", nativeQuery = true)
    Page<Employee> findAllEmployeesByName(String name, Pageable pageable);
    @Query(value = "SELECT * FROM employee WHERE phone LIKE %?1%", nativeQuery = true)
    Page<Employee> findAllEmployeesByPhoneNumber(String phoneNumber, Pageable pageable);
    @Query(value = "SELECT * FROM employee WHERE role = ?1", nativeQuery = true)
    Page<Employee> findAllEmployeesByRole(String role, Pageable pageable);
    @Query(value = "SELECT * FROM employee WHERE fullName LIKE %?1% AND phone LIKE %?2%", nativeQuery = true)
    Page<Employee> findAllEmployeesByNameAndPhoneNumber(String name, String phoneNumber, Pageable pageable);
    @Query(value = "SELECT * FROM employee WHERE phone LIKE %?1% AND role = ?2", nativeQuery = true)
    Page<Employee> findAllEmployeesByPhoneNumberAndRole(String phoneNumber, String role, Pageable pageable);
    @Query(value = "SELECT * FROM employee WHERE fullName LIKE %?1% AND role = ?2", nativeQuery = true)
    Page<Employee> findAllEmployeesByNameAndRole(String name, String role, Pageable pageable);
    @Query(value = "SELECT * FROM employee WHERE fullName LIKE %?1% AND phone LIKE %?2% AND role = ?3", nativeQuery = true)
    Page<Employee> findAllEmployeesByNameAndPhoneNumberAndRole(String name, String phoneNumber, String role, Pageable pageable);
}
