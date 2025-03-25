package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.model.TransactionType;
import com.example.md5_phone_store_management.repository.ISupplierRepository;
import com.example.md5_phone_store_management.repository.InventoryTransactionInRepo;
import com.example.md5_phone_store_management.service.IInventoryTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InventoryTransactionService implements IInventoryTransactionService {
    @Autowired
    private InventoryTransactionInRepo inventoryTransactionInRepo;

    @Autowired
    private ISupplierRepository supplierRepository;

    @Override
    public List<InventoryTransaction> getImportTransactions(Pageable pageable) {
        return inventoryTransactionInRepo.getByTransactionType(TransactionType.IN, pageable);
    }
    @Override
    public List<InventoryTransaction> searchImportTransactions(String productName, String supplierName, LocalDate transactionDate, Pageable pageable) {
        return inventoryTransactionInRepo.searchTransactions(productName, supplierName, transactionDate, TransactionType.IN, pageable);
    }
    @Override
    public void deleteImportTransactions(List<Integer> ids) {
        inventoryTransactionInRepo.deleteByIdsIn(ids);
    }

    @Override
    public InventoryTransaction findByQRCode(String qrCode) {
        return inventoryTransactionInRepo.findByQRCode(qrCode);
    }

    @Override
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @Override
    public void addTransaction(InventoryTransaction transaction) {
        inventoryTransactionInRepo.save(transaction);
    }

}
