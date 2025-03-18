
package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Supplier;

import java.util.List;

public interface ISupplierService {
    List<Supplier> getSupplierList();
    Supplier getSupplier(Integer id);
    void saveSupplier(Supplier supplier);
}

