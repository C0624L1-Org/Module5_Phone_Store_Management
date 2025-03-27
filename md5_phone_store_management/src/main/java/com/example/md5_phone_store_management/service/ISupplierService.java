package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Supplier;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ISupplierService {
    List<Supplier> getSupplierList();
    Supplier getSupplier(Integer id);
    void saveSupplier(Supplier supplier);
    void updateSupplier(Supplier supplier);
    void deleteSupplier(List<Integer> id);
    Page<Supplier> getAllSuppliers(int page, int size);
    Page<Supplier> searchSuppliers(String name, String address, String phone, String email, int page, int size);
    boolean existsByEmail(String email);
    long countSuppliers();

}