package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Repository
public interface IEmployeeRepository extends JpaRepository<Employee, Integer> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE employee SET fullName = :fullName, dob = :dob, address = :address, " +
            "phone = :phone, role = :role, email = :email " +
            "WHERE employeeID = :employeeID", nativeQuery = true)
    int updateEmployee(@Param("employeeID") Integer employeeID,
                       @Param("fullName") String fullName,
                       @Param("dob") LocalDate dob,
                       @Param("address") String address,
                       @Param("phone") String phone,
                       @Param("role") String role,
                       @Param("email") String email);
}
