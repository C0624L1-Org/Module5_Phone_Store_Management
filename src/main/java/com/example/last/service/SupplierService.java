package com.example.last.service;

import com.example.last.model.Supplier;
import com.example.last.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier getSupplierById(Integer supplierID) {
        return supplierRepository.findById(supplierID);
    }

    public void createSupplier(Supplier supplier) {
         supplierRepository.save(supplier);
    }

    public void updateSupplier(Integer supplierID, Supplier updatedSupplier) {
         supplierRepository.update(supplierID, updatedSupplier);
    }

    public void deleteSupplier(Integer supplierID) {
        supplierRepository.deleteById(supplierID);
    }
}
