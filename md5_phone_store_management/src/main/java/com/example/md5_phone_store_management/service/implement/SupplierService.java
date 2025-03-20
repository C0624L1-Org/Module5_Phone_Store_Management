package com.example.md5_phone_store_management.service.implement;




import org.apache.logging.log4j.util.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        return supplierRepository.findById(id).orElse(null);
    }

    public Supplier saveSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

//    // Khoa test update
//    public Supplier updateSupplier(Integer id, Supplier supplier) {
//        Optional<Supplier> existingSupplier = supplierRepository.findById(id);
//        if (existingSupplier.isPresent()) {
//            supplier.setSupplierID(id); // Đảm bảo ID không bị thay đổi
//            return supplierRepository.save(supplier);
//        }
//        return null;
//    }

//    //Khoa test delete
//    public boolean deleteSupplier(Integer id) {
//        if (supplierRepository.existsById(id)) {
//            supplierRepository.deleteById(id);
//            return true;
//        }
//        return false;
//    }

}
