package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ITransactionInService {
    Page<InventoryTransaction> getImportTransactions(Pageable pageable);
    Page<InventoryTransaction> searchImportTransactions(String productName, String supplierName, LocalDate startDate, LocalDate endDate, Pageable pageable);
    void deleteImportTransactions(List<Integer> ids);
    InventoryTransaction getByProductIdAndSupplierId(Integer productId, Integer supplierId);
    InventoryTransaction findByQRCode(String qrCode);
    List<Supplier> getAllSuppliers();
    void addTransaction(InventoryTransaction transaction);
    InventoryTransaction importProduct(Integer productId, Integer quantity, Integer supplierId, String purchasePrice);
    List<Product> getProductsBySupplierId(Integer supplierId);


    InventoryTransaction save(InventoryTransaction transaction);

    void addInTransaction(InventoryTransaction transaction);
    void deleteOutTransaction(int id);
    InventoryTransaction findById(Integer id);

    void editTransactionById(int inventoryTransactionId, InventoryTransaction transaction);
    InventoryTransaction findByInventoryTransactionId(Integer id);
}
