package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Role;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import com.example.md5_phone_store_management.service.IEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmployeeService implements IEmployeeService {
    @Autowired
    private IEmployeeRepository iEmployeeRepository;

    @Override
    public void addEmployee(Employee employee) {
        iEmployeeRepository.save(employee);
    }

    //Read and search (a Đình Anh)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return iEmployeeRepository.getAllEmployees(pageable);
    }
    public Page<Employee> searchEmployees(String name, String phone, String role, Pageable pageable) {
        boolean hasName = name != null && !name.isEmpty();
        boolean hasPhone = phone != null && !phone.isEmpty();
        boolean hasRole = role != null && !role.isEmpty();

        if (hasName && hasPhone && hasRole) {
            return iEmployeeRepository.findAllEmployeesByNameAndPhoneNumberAndRole(name, phone, role, pageable);
        } else if (hasName && hasPhone) {
            return iEmployeeRepository.findAllEmployeesByNameAndPhoneNumber(name, phone, pageable);
        } else if (hasPhone && hasRole) {
            return iEmployeeRepository.findAllEmployeesByPhoneNumberAndRole(phone, role, pageable);
        } else if (hasName && hasRole) {
            return iEmployeeRepository.findAllEmployeesByNameAndRole(name, role, pageable);
        } else if (hasName) {
            return iEmployeeRepository.findAllEmployeesByName(name, pageable);
        } else if (hasPhone) {
            return iEmployeeRepository.findAllEmployeesByPhoneNumber(phone, pageable);
        } else if (hasRole) {
            return iEmployeeRepository.findAllEmployeesByRole(role, pageable);
        } else {
            return iEmployeeRepository.getAllEmployees(pageable);
        }
    }

    //Update(Tân)
    @Override
    public Employee getEmployeeById(Integer employeeID) {
        return iEmployeeRepository.getById(employeeID);
    }

    @Override
    public int updateEmployee(Integer employeeID, String fullName, LocalDate dob, String address, String phone, Role role, String email) {
        return iEmployeeRepository.updateEmployee(employeeID, fullName, dob, address, phone, role, email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return iEmployeeRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return iEmployeeRepository.existsByEmail(email);
    }
}
