package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.InventoryTransaction;

import java.util.List;
import java.util.Optional;

public interface ITransactionOutService {
    List<InventoryTransaction> getAllOutTransactions();
    Optional<InventoryTransaction> getOutTransactionById(int id);
    void addOutTransaction(InventoryTransaction transaction);
    void updateOutTransaction(int id, InventoryTransaction transaction);
    void deleteOutTransaction(int id);
}