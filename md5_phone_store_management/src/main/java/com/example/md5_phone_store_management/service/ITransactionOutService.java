package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ITransactionOutService {
    List<InventoryTransaction> getAllOutTransactions();

    List<InventoryTransaction> getAllInTransactions();

    Optional<InventoryTransaction> getOutTransactionById(Long id);

    void addOutTransaction(InventoryTransaction transaction);

    void updateOutTransaction(int id, InventoryTransaction transaction);

    void deleteOutTransaction(int id);

    Page<InventoryTransaction> getAllOutTransactionsPage(Pageable pageable);


    Page<InventoryTransaction> searchExportTransactions(String productName, String supplierName, LocalDate startDate, LocalDate endDate, Pageable pageable);

}
