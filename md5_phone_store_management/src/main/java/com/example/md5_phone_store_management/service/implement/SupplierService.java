package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.repository.ISupplierRepository;
import com.example.md5_phone_store_management.service.ISupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Override
    public Supplier getSupplier(Integer id) {
        return supplierRepository.findBySupplierID(id);
    }

    @Override
    public void saveSupplier(Supplier supplier) {
        supplierRepository.save(supplier);
    }

    @Override
    public void updateSupplier(Supplier supplier) {
        supplierRepository.save(supplier);
    }

    @Override
    public void deleteSupplier(List<Integer> ids) {
        supplierRepository.deleteByIdIn(ids);
    }

    @Override
    public Page<Supplier> getAllSuppliers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return supplierRepository.findAll(pageable);
    }

    @Override
    public Page<Supplier> searchSuppliers(String name, String address, String phone, String email, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return supplierRepository.searchSuppliersDynamic(name, address, phone, email, pageable);
    }

    @Override
    public boolean existsByEmail(String email) {

        return supplierRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return supplierRepository.existsByPhone(phone);
    }

    @Override
    public long countSuppliers() {
        return supplierRepository.countSuppliers();
    }


}