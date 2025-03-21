package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.repository.ICustomerRepository;
import com.example.md5_phone_store_management.service.ICustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements ICustomerService {

    @Autowired
    private ICustomerRepository customerRepository;

    @Override
    public Page<Customer> findAllCustomers(Pageable pageable) {
        return customerRepository.getAllCustomerPageable(pageable);
    }

    @Override
    public Integer countTotalCustomers() {
        return customerRepository.countTotalCustomers();
    }

    @Override
    public Page<Customer> searchCustomers(String name, String phone, String gender, Pageable pageable) {
        if (name != null && !name.isEmpty() && phone != null && !phone.isEmpty() && gender != null && !gender.isEmpty()) {
            return customerRepository.findByNameAndPhoneAndGender(name, phone, gender, pageable);
        } else if (name != null && !name.isEmpty() && phone != null && !phone.isEmpty()) {
            return customerRepository.findByNameAndPhone(name, phone, pageable);
        } else if (name != null && !name.isEmpty() && gender != null && !gender.isEmpty()) {
            return customerRepository.findByNameAndGender(name, gender, pageable);
        } else if (phone != null && !phone.isEmpty() && gender != null && !gender.isEmpty()) {
            return customerRepository.findByPhoneAndGender(phone, gender, pageable);
        } else if (name != null && !name.isEmpty()) {
            return customerRepository.findByName(name, pageable);
        } else if (phone != null && !phone.isEmpty()) {
            return customerRepository.findByPhone(phone, pageable);
        } else if (gender != null && !gender.isEmpty()) {
            return customerRepository.findByGender(gender, pageable);
        } else {
            // Nếu không có tham số nào được cung cấp, trả về tất cả khách hàng
            return customerRepository.findAll(pageable);
        }
    }


//public Page<Employee> searchEmployees(String name, String phone, String role, Pageable pageable) {
//    boolean hasName = name != null && !name.isEmpty();
//    boolean hasPhone = phone != null && !phone.isEmpty();
//    boolean hasRole = role != null && !role.isEmpty();
//
//    if (hasName && hasPhone && hasRole) {
//        return iEmployeeRepository.findAllEmployeesByNameAndPhoneNumberAndRole(name, phone, role, pageable);
//    } else if (hasName && hasPhone) {
//        return iEmployeeRepository.findAllEmployeesByNameAndPhoneNumber(name, phone, pageable);
//    } else if (hasPhone && hasRole) {
//        return iEmployeeRepository.findAllEmployeesByPhoneNumberAndRole(phone, role, pageable);
//    } else if (hasName && hasRole) {
//        return iEmployeeRepository.findAllEmployeesByNameAndRole(name, role, pageable);
//    } else if (hasName) {
//        return iEmployeeRepository.findAllEmployeesByName(name, pageable);
//    } else if (hasPhone) {
//        return iEmployeeRepository.findAllEmployeesByPhoneNumber(phone, pageable);
//    } else if (hasRole) {
//        return iEmployeeRepository.findAllEmployeesByRole(role, pageable);
//    } else {
//        return iEmployeeRepository.getAllEmployees(pageable);
//    }
//}

}
