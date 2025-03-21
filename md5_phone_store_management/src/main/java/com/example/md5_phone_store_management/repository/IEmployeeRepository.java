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
    //    getAllEmployeesExceptAdmin hậu
    @Query(value = "SELECT * FROM employee WHERE LOWER(role) <> 'admin'", nativeQuery = true)
    Page<Employee> getAllEmployeesExceptAdmin(Pageable pageable);
    //    search của hậu
    @Query(value = "SELECT * FROM employee WHERE role <> 'Admin'", nativeQuery = true)
    Page<Employee> getAllEmployees(Pageable pageable);
    @Query(value = "SELECT * FROM employee WHERE full_name LIKE %?1% AND role <> 'Admin'", nativeQuery = true)
    Page<Employee> findAllEmployeesByName(String name, Pageable pageable);
    @Query(value = "SELECT * FROM employee WHERE phone LIKE %?1% AND role <> 'Admin'", nativeQuery = true)
    Page<Employee> findAllEmployeesByPhoneNumber(String phoneNumber, Pageable pageable);
    @Query(value = "SELECT * FROM employee WHERE role = ?1 AND role <> 'Admin'", nativeQuery = true)
    Page<Employee> findAllEmployeesByRole(String role, Pageable pageable);
    @Query(value = "SELECT * FROM employee WHERE full_name LIKE %?1% AND phone LIKE %?2% AND role <> 'Admin'", nativeQuery = true)
    Page<Employee> findAllEmployeesByNameAndPhoneNumber(String name, String phoneNumber, Pageable pageable);
    @Query(value = "SELECT * FROM employee WHERE phone LIKE %?1% AND role = ?2 AND role <> 'Admin'", nativeQuery = true)
    Page<Employee> findAllEmployeesByPhoneNumberAndRole(String phoneNumber, String role, Pageable pageable);
    @Query(value = "SELECT * FROM employee WHERE full_name LIKE %?1% AND role = ?2 AND role <> 'Admin'", nativeQuery = true)
    Page<Employee> findAllEmployeesByNameAndRole(String name, String role, Pageable pageable);
    @Query(value = "SELECT * FROM employee WHERE full_name LIKE %?1% AND phone LIKE %?2% AND role = ?3 AND role <> 'Admin'", nativeQuery = true)
    Page<Employee> findAllEmployeesByNameAndPhoneNumberAndRole(String name, String phoneNumber, String role, Pageable pageable);

    //Read and search (a Đình Anh)
//    @Query(value = "SELECT * FROM employee", nativeQuery = true)
//    Page<Employee> getAllEmployees(Pageable pageable);
//    @Query(value = "SELECT * FROM employee WHERE full_name LIKE %?1%", nativeQuery = true)
//    Page<Employee> findAllEmployeesByName(String name, Pageable pageable);
//    @Query(value = "SELECT * FROM employee WHERE phone LIKE %?1%", nativeQuery = true)
//    Page<Employee> findAllEmployeesByPhoneNumber(String phoneNumber, Pageable pageable);
//    @Query(value = "SELECT * FROM employee WHERE role = ?1", nativeQuery = true)
//    Page<Employee> findAllEmployeesByRole(String role, Pageable pageable);
//    @Query(value = "SELECT * FROM employee WHERE full_name  LIKE %?1% AND phone LIKE %?2%", nativeQuery = true)
//    Page<Employee> findAllEmployeesByNameAndPhoneNumber(String name, String phoneNumber, Pageable pageable);
//    @Query(value = "SELECT * FROM employee WHERE phone LIKE %?1% AND role = ?2", nativeQuery = true)
//    Page<Employee> findAllEmployeesByPhoneNumberAndRole(String phoneNumber, String role, Pageable pageable);
//    @Query(value = "SELECT * FROM employee WHERE full_name  LIKE %?1% AND role = ?2", nativeQuery = true)
//    Page<Employee> findAllEmployeesByNameAndRole(String name, String role, Pageable pageable);
//    @Query(value = "SELECT * FROM employee WHERE full_name  LIKE %?1% AND phone LIKE %?2% AND role = ?3", nativeQuery = true)
//    Page<Employee> findAllEmployeesByNameAndPhoneNumberAndRole(String name, String phoneNumber, String role, Pageable pageable);


    //Update(Tân)
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

    //Delete
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM employee WHERE employeeID = ?1", nativeQuery = true)
    void deleteEmployeeById(Integer id);

    //Đếm
    long count();

    @Query(value = "SELECT COUNT(*) FROM employee WHERE role = 'SalesStaff'", nativeQuery = true)
    long countSalesStaff();

    @Query(value = "SELECT COUNT(*) FROM employee WHERE role = 'SalesPerson'", nativeQuery = true)
    long countSalesPerson();

    @Query(value = "SELECT COUNT(*) FROM employee WHERE role = 'WarehouseStaff'", nativeQuery = true)
    long countWarehouseStaff();

}
