package com.example.last.service.implement;

import com.example.last.model.Supplier;
import com.example.last.repository.ISupplierRepository;
import com.example.last.service.ISupplierService;
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
}
