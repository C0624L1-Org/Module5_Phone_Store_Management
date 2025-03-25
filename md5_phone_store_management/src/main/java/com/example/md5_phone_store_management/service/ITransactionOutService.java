package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.InventoryTransaction;

import java.util.List;
import java.util.Optional;

public interface ITransactionOutService {
    List<InventoryTransaction> getAllOutTransactions();
    List<InventoryTransaction> getAllInTransactions();
    Optional<InventoryTransaction> getOutTransactionById(Long id);
    void addOutTransaction(InventoryTransaction transaction);
    void updateOutTransaction(int id, InventoryTransaction transaction);
    void deleteOutTransaction(int id);
    public List<InventoryTransaction> searchTransaction(String productName, String supplierName, String startDate, String endDate);
}