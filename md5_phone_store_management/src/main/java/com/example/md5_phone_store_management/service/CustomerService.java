//package com.example.md5_phone_store_management.service;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//import com.example.md5_phone_store_management.repository.IInvoiceDetailRepository;
//import com.example.md5_phone_store_management.repository.InvoiceRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.example.md5_phone_store_management.model.Customer;
//import com.example.md5_phone_store_management.repository.CustomerRepository;
//
//@Service
//public class CustomerService {
//
//    @Autowired
//    private CustomerRepository customerRepository;
//
//    @Autowired
//    private InvoiceRepository invoiceRepository;
//
//    public List<Customer> findAllCustomers() {
//        return customerRepository.findAll();
//    }
//
//
//    public void deleteCustomer(List<Integer> customerID) {
//        customerRepository.deleteCustomer(customerID);
//    }
//
//
//
//    public boolean updateCustomer(Customer customer) {
//        try {
//            Optional<Customer> existingCustomer = Optional.ofNullable(customerRepository.findById(customer.getCustomerID()));
//            if (existingCustomer.isPresent()) {
//                customerRepository.save(customer); // Cập nhật khách hàng
//                return true; // Cập nhật thành công
//            } else {
//                return false; // Không tìm thấy khách hàng
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false; // Cập nhật thất bại
//        }
//    }
//
//    public Customer getCustomerByID(Integer customerID) {
//        return customerRepository.findById(Math.toIntExact(customerID));
//    }
//
//
//
//    public void addNewCustomer(Customer customer) {
//        customerRepository.save(customer);
//    }
//
//    public Customer addNewCustomerAjax(Customer customer) {
//        return customerRepository.saveAjax(customer);
//    }
//
//    public List<Customer> searchCustomers(String name, String phone, String gender) {
//        return customerRepository.searchCustomers( name,  phone,  gender);
//    }
//
//    // Lấy danh sách khách hàng có lịch sử mua hàng (purchaseCount > 0)
//    public List<Customer> getCustomersWithPurchases() {
//        return customerRepository.findCustomersWithPurchases();
//    }
//
//}
package com.example.md5_phone_store_management.service;

import java.util.*;
import java.util.stream.Collectors;

import com.example.md5_phone_store_management.event.EntityChangeEvent;
import com.example.md5_phone_store_management.repository.IInvoiceDetailRepository;
import com.example.md5_phone_store_management.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.repository.CustomerRepository;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteCustomer(List<Integer> customerIDs) {
        try {
            if (customerIDs == null || customerIDs.isEmpty()) {
                throw new IllegalArgumentException("Customer IDs list cannot be null or empty");
            }

            // Fetch customers to publish events
            List<Customer> customersToDelete = customerRepository.findAllById(customerIDs)
                    .stream()
                    .filter(Objects::nonNull)
                    .toList();

            if (customersToDelete.isEmpty()) {
                System.err.println("No customers found for IDs: " + customerIDs);
                return;
            }

            // Debug fullName của từng khách hàng
            customersToDelete.forEach(customer ->
                    System.out.println("Customer ID: " + customer.getCustomerID() + ", FullName: " + customer.getFullName())
            );

            // Tạo newValue cho sự kiện
            System.out.println("Creating newValue from customersToDelete: " + customersToDelete.size() + " customers");
            String newValue = customersToDelete.stream()
                    .map(customer -> {
                        String name = customer.getFullName() != null ? customer.getFullName() : "ID_" + customer.getCustomerID();
                        System.out.println("Mapping customer ID: " + customer.getCustomerID() + " to name: " + name);
                        return name;
                    })
                    .collect(Collectors.joining(", "));
            System.out.println("Raw newValue after joining: " + newValue);
            if (newValue.isEmpty()) {
                newValue = "xóa không có tên khách hàng";
                System.out.println("newValue is empty, set to default: " + newValue);
            } else {
                System.out.println("Final newValue: " + newValue);
            }

            // Publish DELETE_CUSTOMER event cho từng khách hàng
            String eventId = UUID.randomUUID().toString();
            for (Customer customer : customersToDelete) {
                System.out.println("Publishing DELETE_CUSTOMER event for customer ID: " + customer.getCustomerID() + ", eventId: " + eventId);
                EntityChangeEvent event = new EntityChangeEvent(this, customer, "DELETE_CUSTOMER", null);
                event.addMetadata("newValue", newValue); // Truyền newValue qua metadata
                event.addMetadata("customerId", customer.getCustomerID()); // Thêm customerId vào metadata để dự phòng
                System.out.println("Event metadata added: newValue=" + event.getMetadata().get("newValue") + ", customerId=" + event.getMetadata().get("customerId"));
                eventPublisher.publishEvent(event);
            }

            // Perform batch deletion
            customerRepository.deleteCustomer(customerIDs);
            System.out.println("Successfully deleted customers with IDs: " + customerIDs);

        } catch (Exception e) {
            System.err.println("Error deleting customers: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public boolean updateCustomer(Customer customer) {
        try {
            Optional<Customer> existingCustomer = Optional.ofNullable(customerRepository.findById(customer.getCustomerID()));
            if (existingCustomer.isPresent()) {
                customerRepository.save(customer); // Cập nhật khách hàng
                eventPublisher.publishEvent(new EntityChangeEvent(this, customer, "UPDATE_CUSTOMER", existingCustomer.get()));
                Customer updatedCustomer = customerRepository.save(customer);
                String newValue = customer.getFullName() != null ? customer.getFullName() : String.valueOf(customer.getCustomerID());
                saveChangeLog(customer, "UPDATE", "customer", existingCustomer.get(), newValue, null);
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



    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void addNewCustomer(Customer customer) {
        try {
            if (customer == null || customer.getFullName() == null) {
                throw new IllegalArgumentException("Customer or customer name cannot be null");
            }
            Customer savedCustomer = customerRepository.save(customer);
            String eventId = UUID.randomUUID().toString();
            System.out.println("Publishing INSERT_CUSTOMER event for customer ID: " + savedCustomer.getCustomerID() + ", eventId: " + eventId);
            eventPublisher.publishEvent(new EntityChangeEvent(this, savedCustomer, "INSERT_CUSTOMER", null));

            String newValue = savedCustomer.getFullName() != null ? savedCustomer.getFullName() : String.valueOf(savedCustomer.getCustomerID());
            saveChangeLog(savedCustomer, "INSERT", "customer", null, newValue, null);
        } catch (Exception e) {
            System.err.println("Error adding new customer: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Customer addNewCustomerAjax(Customer customer) {
        try {
            if (customer == null || customer.getFullName() == null) {
                throw new IllegalArgumentException("Customer or customer name cannot be null");
            }
            Customer savedCustomer = customerRepository.saveAjax(customer);
            String eventId = UUID.randomUUID().toString();
            System.out.println("Publishing INSERT_CUSTOMER event for customer ID: " + savedCustomer.getCustomerID() + ", eventId: " + eventId);
            eventPublisher.publishEvent(new EntityChangeEvent(this, savedCustomer, "INSERT_CUSTOMER", null));

            String newValue = savedCustomer.getFullName() != null ? savedCustomer.getFullName() : String.valueOf(savedCustomer.getCustomerID());
            saveChangeLog(savedCustomer, "INSERT", "customer", null, newValue, null);
            return savedCustomer;
        } catch (Exception e) {
            System.err.println("Error adding new customer via AJAX: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<Customer> searchCustomers(String name, String phone, String gender) {
        return customerRepository.searchCustomers(name, phone, gender);
    }

    public List<Customer> getCustomersWithPurchases() {
        return customerRepository.findCustomersWithPurchases();
    }

    // Placeholder for saveChangeLog method (should be implemented based on your application's needs)
    private void saveChangeLog(Object entity, String action, String entityType, Object oldValue, String newValue, Integer employeeId) {
        System.out.println("Saving change log: Action=" + action + ", Entity=" + entityType + ", NewValue=" + newValue);
        // Implement actual change log saving logic here
    }
}