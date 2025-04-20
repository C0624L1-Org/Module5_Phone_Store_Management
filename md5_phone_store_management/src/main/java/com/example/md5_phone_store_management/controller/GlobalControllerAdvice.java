package com.example.md5_phone_store_management.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.repository.IProductRepository;
import com.example.md5_phone_store_management.service.implement.EmployeeService;
import com.example.md5_phone_store_management.service.implement.SupplierService;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private IProductRepository productRepository;

    @ModelAttribute("loggedEmployee")
    public Employee currentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            Optional<Employee> employeeOpt = employeeService.findByUsername(username);
            if (employeeOpt.isPresent()) {
                return employeeOpt.get();
            }
        }
        return null;
    }

    @ModelAttribute("suppliers")
    public List<Supplier> getSuppliers() {
        return supplierService.getSupplierList();
    }

    @ModelAttribute("supplierProducts")
    public Map<Integer, List<Product>> getSupplierProducts() {
        List<Supplier> suppliers = supplierService.getSupplierList();
        Map<Integer, List<Product>> supplierProducts = new HashMap<>();

        for (Supplier supplier : suppliers) {
            List<Product> products = productRepository.findBySupplierId(supplier.getSupplierID());
            supplierProducts.put(supplier.getSupplierID(), products);
        }

        return supplierProducts;
    }
}
