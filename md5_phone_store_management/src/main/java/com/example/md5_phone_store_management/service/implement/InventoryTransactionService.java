package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.TransactionType;
import com.example.md5_phone_store_management.repository.IInventoryTransactionInRepo;
import com.example.md5_phone_store_management.service.IInventoryTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InventoryTransactionService implements IInventoryTransactionService {
    @Autowired
    private IInventoryTransactionInRepo IInventoryTransactionInRepo;

    @Override
    public Page<InventoryTransaction> getImportTransactions(Pageable pageable) {
        return IInventoryTransactionInRepo.getByTransactionType(TransactionType.IN, pageable);
    }
    @Override
    public Page<InventoryTransaction> searchImportTransactions(String productName, String supplierName, LocalDate transactionDate, Pageable pageable) {
        return IInventoryTransactionInRepo.searchTransactions(productName, supplierName, transactionDate, TransactionType.IN, pageable);
    }
    @Override
    public void deleteImportTransactions(List<Integer> ids) {
        IInventoryTransactionInRepo.deleteByIdsIn(ids);
    }

    @Override
    public InventoryTransaction getByProductIdAndSupplierId(Integer productId, Integer supplierId) {
        return IInventoryTransactionInRepo.getByProductIdAndSupplierId(productId, supplierId);
    }
}
