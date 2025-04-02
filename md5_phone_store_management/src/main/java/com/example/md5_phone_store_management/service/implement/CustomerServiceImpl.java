package com.example.md5_phone_store_management.service.implement;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.repository.ICustomerRepository;
import com.example.md5_phone_store_management.service.ICustomerService;

@Service
@Transactional
public class CustomerServiceImpl implements ICustomerService {

    @Autowired
    private ICustomerRepository customerRepository;
    

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Page<Customer> findAllCustomers(Pageable pageable) {
        System.out.println("Tìm tất cả khách hàng với pageable: " + pageable);
        
        // Kiểm tra trực tiếp trong database với JDBC template
        try {
            // Sử dụng JDBC Template để thực hiện truy vấn trực tiếp, không qua JPA
            String sql = "SELECT * FROM customer ORDER BY customerID";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            
            System.out.println("Truy vấn JDBC trực tiếp tìm thấy " + rows.size() + " khách hàng:");
            for (Map<String, Object> row : rows) {
                System.out.println("  - ID: " + row.get("customerID") + 
                                   ", Tên: " + row.get("full_Name") + 
                                   ", PurchaseCount: " + row.get("purchaseCount"));
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi truy vấn trực tiếp với JDBC: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Tiếp tục với truy vấn phân trang
        Page<Customer> result = customerRepository.getAllCustomerPageable(pageable);
        System.out.println("Kết quả tất cả: số phần tử = " + result.getTotalElements() + ", số trang = " + result.getTotalPages());
        return result;
    }

    @Override
    public Integer countTotalCustomers() {
        return customerRepository.countTotalCustomers();
    }

    @Override
    public Page<Customer> searchCustomers(String name, String phone, String gender, Pageable pageable) {
        System.out.println("DEBUG: searchCustomers được gọi với name=" + name + ", phone=" + phone + ", gender=" + gender);
        
        // Nếu name và phone có cùng giá trị (được gọi từ controller với cùng một keyword)
        if (name != null && !name.isEmpty() && name.equals(phone)) {
            System.out.println("DEBUG: Thực hiện tìm kiếm theo name: " + name);
            return customerRepository.findByName(name, pageable);
        }
        // Nếu gender chứa @ thì đó là email
        else if (gender != null && gender.contains("@")) {
            System.out.println("DEBUG: Thực hiện tìm kiếm theo email: " + gender);
            // Giả sử đã có phương thức tìm kiếm bằng email, nếu không thì cần thêm vào repository
            return customerRepository.findByEmail(gender, pageable);
        }
        // Giữ logic tìm kiếm chi tiết cho các trường hợp cụ thể
        else if (name != null && !name.isEmpty() && phone != null && !phone.isEmpty() && gender != null && !gender.isEmpty()) {
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

    @Override
    public Customer findCustomerById(Integer id) {
        return customerRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void updatePurchaseCount(Integer customerId, int newCount) {
        customerRepository.updatePurchaseCountById(customerId, newCount);
    }

    @Override
    public Page<Customer> findCustomersWithPurchases(Pageable pageable) {
        System.out.println("Tìm khách hàng đã mua hàng (purchaseCount > 0) với pageable: " + pageable);
        
        // Kiểm tra trực tiếp trong database với JDBC template
        try {
            // Sử dụng JDBC Template để thực hiện truy vấn trực tiếp, không qua JPA
            String sql = "SELECT * FROM customer WHERE purchaseCount > 0";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            
            System.out.println("Truy vấn JDBC trực tiếp tìm thấy " + rows.size() + " khách hàng có purchaseCount > 0:");
            for (int i = 0; i < Math.min(rows.size(), 5); i++) { // Chỉ hiển thị 5 khách hàng đầu tiên để rõ ràng
                Map<String, Object> row = rows.get(i);
                System.out.println("  - ID: " + row.get("customerID") + 
                                   ", Tên: " + row.get("full_Name") + 
                                   ", PurchaseCount: " + row.get("purchaseCount"));
            }
            if (rows.size() > 5) {
                System.out.println("  - ... và " + (rows.size() - 5) + " khách hàng khác");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi truy vấn trực tiếp với JDBC: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Tiếp tục với truy vấn phân trang
        try {
            Page<Customer> result = customerRepository.findCustomersWithPurchases(pageable);
            System.out.println("Kết quả phân trang JPA: số phần tử = " + result.getTotalElements() + 
                          ", số trang = " + result.getTotalPages() + 
                          ", phần tử trong trang = " + result.getContent().size());
            
            // Log chi tiết hơn về kết quả trả về
            List<Customer> customers = result.getContent();
            if (customers.isEmpty()) {
                System.out.println("Không tìm thấy khách hàng nào có purchaseCount > 0 từ JPA!");
                
                // Lấy một số khách hàng mẫu để hiển thị khi không có kết quả
                System.out.println("Sử dụng dữ liệu tạm thời để hiển thị");
                
                // Tự động cập nhật purchaseCount nếu thấy các đơn hàng
                String updateSql = "UPDATE customer c SET c.purchaseCount = (SELECT COUNT(*) FROM sales s WHERE s.customer_id = c.customerID) WHERE EXISTS (SELECT 1 FROM sales s WHERE s.customer_id = c.customerID) AND c.purchaseCount = 0;";
                try {
                    int updatedRows = jdbcTemplate.update(updateSql);
                    System.out.println("Đã cập nhật purchaseCount cho " + updatedRows + " khách hàng");
                    
                    // Thử lấy dữ liệu lại một lần nữa
                    result = customerRepository.findCustomersWithPurchases(pageable);
                    if (!result.isEmpty()) {
                        System.out.println("Đã tìm thấy dữ liệu sau khi cập nhật purchaseCount!");
                        return result;
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi cập nhật purchaseCount: " + e.getMessage());
                }
                
                // Nếu vẫn không có dữ liệu, trả về dữ liệu tạm thời
                return Page.empty(pageable);
            } else {
                System.out.println("Danh sách khách hàng có purchaseCount > 0 từ JPA (tối đa 5 khách hàng đầu tiên):");
                for (int i = 0; i < Math.min(customers.size(), 5); i++) {
                    Customer customer = customers.get(i);
                    System.out.println("  - ID: " + customer.getCustomerID() + 
                                     ", Tên: " + customer.getFullName() + 
                                     ", Phone: " + customer.getPhone() +
                                     ", Email: " + customer.getEmail() +
                                     ", PurchaseCount: " + customer.getPurchaseCount());
                }
                if (customers.size() > 5) {
                    System.out.println("  - ... và " + (customers.size() - 5) + " khách hàng khác");
                }
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("Lỗi khi thực hiện truy vấn JPA findCustomersWithPurchases: " + e.getMessage());
            e.printStackTrace();
            // Trả về trang trống nếu có lỗi
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<Customer> searchCustomerWithPurchases(String name, String phone, String email, Pageable pageable) {
        System.out.println("DEBUG: searchCustomerWithPurchases được gọi với name=" + name + ", phone=" + phone + ", email=" + email);
        
        // Nếu cả ba tham số đều có giá trị (tức là trường hợp gọi từ controller với cùng một keyword)
        if (name != null && !name.isEmpty() && name.equals(phone) && name.equals(email)) {
            // Chỉ tìm theo tên để tăng khả năng tìm thấy kết quả
            System.out.println("DEBUG: Thực hiện tìm kiếm theo name: " + name);
            return customerRepository.findCustomersWithPurchasesByName(name, pageable);
        }
        // Giữ logic tìm kiếm chi tiết cho các trường hợp cụ thể
        else if (name != null && !name.isEmpty() && phone != null && !phone.isEmpty() && email != null && !email.isEmpty()) {
            return customerRepository.findCustomersWithPurchasesByNameAndPhoneAndEmail(name, phone, email, pageable);
        } else if (name != null && !name.isEmpty() && phone != null && !phone.isEmpty()) {
            return customerRepository.findCustomersWithPurchasesByNameAndPhone(name, phone, pageable);
        } else if (name != null && !name.isEmpty() && email != null && !email.isEmpty()) {
            return customerRepository.findCustomersWithPurchasesByNameAndEmail(name, email, pageable);
        } else if (phone != null && !phone.isEmpty() && email != null && !email.isEmpty()) {
            return customerRepository.findCustomersWithPurchasesByPhoneAndEmail(phone, email, pageable);
        } else if (name != null && !name.isEmpty()) {
            return customerRepository.findCustomersWithPurchasesByName(name, pageable);
        } else if (phone != null && !phone.isEmpty()) {
            return customerRepository.findCustomersWithPurchasesByPhone(phone, pageable);
        } else if (email != null && !email.isEmpty()) {
            return customerRepository.findCustomersWithPurchasesByEmail(email, pageable);
        } else {
            return customerRepository.findCustomersWithPurchases(pageable);
        }
    }

    @Override
    public Integer countCustomersWithPurchases() {
        // Sử dụng customerRepository để đếm số khách hàng có purchaseCount > 0
        return customerRepository.countCustomersWithPurchases();
    }

    public javax.sql.DataSource getDataSource() {
        return jdbcTemplate.getDataSource();
    }

}
