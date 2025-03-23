package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.InventoryTransaction;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IInventoryTransactionService {
    List<InventoryTransaction> getImportTransactions(Pageable pageable);
    List<InventoryTransaction> searchImportTransactions(String productName, String supplierName, LocalDate transactionDate, Pageable pageable);
    void deleteImportTransactions(List<Integer> ids);
}
