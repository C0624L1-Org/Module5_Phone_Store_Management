package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.repository.ISupplierRepository;
import com.example.md5_phone_store_management.service.ISupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService implements ISupplierService {

    @Autowired
    private ISupplierRepository supplierRepository;

    @Override
    public List<Supplier> getSupplierList() {
        return supplierRepository.findAll();
    }
    public Supplier getSupplier(Integer id) {
        return supplierRepository.findById(id).orElse(null);
    }

    @Override
    public void saveSupplier(Supplier supplier) {
        supplierRepository.insert(supplier.getName(),supplier.getAddress(),
                supplier.getPhone(),supplier.getEmail());
    }
}
