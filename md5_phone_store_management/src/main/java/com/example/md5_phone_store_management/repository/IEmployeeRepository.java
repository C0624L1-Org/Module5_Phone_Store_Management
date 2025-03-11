package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IEmployeeRepository extends JpaRepository<Employee, Integer> {

    // Read and search
    @Query(value = "SELECT * FROM employee", nativeQuery = true)
    Page<Employee> getAllEmployees(Pageable pageable);

    @Query(value = "SELECT * FROM employee WHERE fullName LIKE %:name%", nativeQuery = true)
    Page<Employee> findAllEmployeesByName(@Param("name") String name, Pageable pageable);

    @Query(value = "SELECT * FROM employee WHERE phone LIKE %:phoneNumber%", nativeQuery = true)
    Page<Employee> findAllEmployeesByPhoneNumber(@Param("phoneNumber") String phoneNumber, Pageable pageable);

    @Query(value = "SELECT * FROM employee WHERE role = :role", nativeQuery = true)
    Page<Employee> findAllEmployeesByRole(@Param("role") String role, Pageable pageable);

    @Query(value = "SELECT * FROM employee WHERE fullName LIKE %:name% AND phone LIKE %:phoneNumber%", nativeQuery = true)
    Page<Employee> findAllEmployeesByNameAndPhoneNumber(@Param("name") String name, @Param("phoneNumber") String phoneNumber, Pageable pageable);

    @Query(value = "SELECT * FROM employee WHERE phone LIKE %:phoneNumber% AND role = :role", nativeQuery = true)
    Page<Employee> findAllEmployeesByPhoneNumberAndRole(@Param("phoneNumber") String phoneNumber, @Param("role") String role, Pageable pageable);

    @Query(value = "SELECT * FROM employee WHERE fullName LIKE %:name% AND role = :role", nativeQuery = true)
    Page<Employee> findAllEmployeesByNameAndRole(@Param("name") String name, @Param("role") String role, Pageable pageable);

    @Query(value = "SELECT * FROM employee WHERE fullName LIKE %:name% AND phone LIKE %:phoneNumber% AND role = :role", nativeQuery = true)
    Page<Employee> findAllEmployeesByNameAndPhoneNumberAndRole(@Param("name") String name, @Param("phoneNumber") String phoneNumber, @Param("role") String role, Pageable pageable);

    // Update
    @Modifying
    @Transactional
    @Query("UPDATE Employee e SET e.fullName = :fullName, e.dob = :dob, e.address = :address, " +
            "e.phone = :phone, e.role = :role, e.email = :email " +
            "WHERE e.employeeID = :employeeID")
    int updateEmployee(@Param("employeeID") Integer employeeID,
                       @Param("fullName") String fullName,
                       @Param("dob") LocalDate dob,
                       @Param("address") String address,
                       @Param("phone") String phone,
                       @Param("role") Role role,
                       @Param("email") String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<Employee> findByUsername(String username);

    // Delete
    @Query(value = "SELECT * FROM employee WHERE employeeID = :id", nativeQuery = true)
    Employee findOneEmployeeById(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM employee WHERE employeeID = :id", nativeQuery = true)
    void deleteEmployeeById(@Param("id") Integer id);
}