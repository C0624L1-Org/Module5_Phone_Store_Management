package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;


    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }


    public boolean updateCustomer(Customer customer) {
        try {
            Optional<Customer> existingCustomer = Optional.ofNullable(customerRepository.findById(customer.getCustomerID()));
            if (existingCustomer.isPresent()) {
                customerRepository.save(customer); // Cập nhật khách hàng
                return true; // Cập nhật thành công
            } else {
                return false; // Không tìm thấy khách hàng
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Cập nhật thất bại
        }
    }

    public Customer getCustomerByID(Integer customerID) {
        return customerRepository.findById(Math.toIntExact(customerID));
    }


    public void deleteCustomer(List<Integer> customerID) {
        customerRepository.deleteCustomer(customerID);
    }

    public void addNewCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    public Customer addNewCustomerAjax(Customer customer) {
        return customerRepository.saveAjax(customer);
    }


    public List<Customer> searchCustomers(String searchType, String keyWord) {
        return customerRepository.searchCustomers(searchType, keyWord);
    }


}
