package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IInventoryTransactionService {
    // Phương thức từ nhánh HEAD
    Page<InventoryTransaction> getImportTransactions(Pageable pageable);
    Page<InventoryTransaction> searchImportTransactions(
            String productName, String supplierName, LocalDate startDate, LocalDate endDate, Pageable pageable);
    void deleteImportTransactions(List<Integer> ids);
    InventoryTransaction getByProductIdAndSupplierId(Integer productId, Integer supplierId);

    // Phương thức từ nhánh khoa-code
    InventoryTransaction findByQRCode(String qrCode);
    List<Supplier> getAllSuppliers();
    void addTransaction(InventoryTransaction transaction);
    void importProduct(Integer productId, Integer quantity, Integer supplierId, String purchasePrice);
    List<Product> getProductsBySupplierId(Integer supplierId);
}