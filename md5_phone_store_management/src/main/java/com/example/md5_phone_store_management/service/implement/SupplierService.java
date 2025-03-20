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

    // Lưu hoặc cập nhật nhà cung cấp (Sử dụng save)
    @Override
    public void saveSupplier(Supplier supplier) {
        supplierRepository.save(supplier);
    }

    // Cập nhật thông tin nhà cung cấp (Sử dụng save)
    @Override
    public void updateSupplier(Supplier supplier) {
        supplierRepository.save(supplier);
    }

    // Tìm tất cả nhà cung cấp với phân trang
    @Override
    public Page<Supplier> getAllSuppliers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return supplierRepository.findAll(pageable);
    }

    @Override
    public Page<Supplier> searchSuppliers(String name, String address, String phone, String email, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Điều kiện mặc định là "%" nếu tham số tìm kiếm là null hoặc trống
        if (name == null || name.isEmpty()) name = "%";
        if (address == null || address.isEmpty()) address = "%";
        if (phone == null || phone.isEmpty()) phone = "%";
        if (email == null || email.isEmpty()) email = "%";

        return supplierRepository.searchSuppliersDynamic(name, address, phone, email, pageable);
    }
}
