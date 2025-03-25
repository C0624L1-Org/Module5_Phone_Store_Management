package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.TransactionType;
import com.example.md5_phone_store_management.repository.TransactionRepository;
import com.example.md5_phone_store_management.service.ITransactionOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionOutService implements ITransactionOutService {

    @Autowired
    private TransactionRepository transactionRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<InventoryTransaction> getAllOutTransactions() {
        return transactionRepository.findAllOutTransactions();
    }

    @Override
    public List<InventoryTransaction> getAllInTransactions() {
        return transactionRepository.findAllInTransactions();
    }

    @Override
    public Optional<InventoryTransaction> getOutTransactionById(Long id) {
        return Optional.ofNullable(transactionRepository.findOutTransactionById(id));
    }


    @Override
    public void addOutTransaction(InventoryTransaction transaction) {
        try {
            System.out.println("Transaction Type: " + transaction.getTransactionType());
            if (transaction.getTransactionType() == TransactionType.OUT) {
                transactionRepository.saveOutTransaction(
                        transaction.getProduct().getProductID(),
                        transaction.getQuantity(),
                        transaction.getPurchasePrice().doubleValue(),
                        transaction.getTransactionDate().format(formatter),
                        transaction.getSupplier() != null ? transaction.getSupplier().getSupplierID() : null,
                        transaction.getEmployee() != null ? transaction.getEmployee().getEmployeeID() : null,
                        transaction.getTotalPrice().doubleValue()
                );
                System.out.println("ngu");
            }
            System.out.println("ngu1");
        } catch (Exception e) {
            System.out.println("ngu2");
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu giao dịch: " + e.getMessage());
        }
    }


    @Override
    public void updateOutTransaction(int id, InventoryTransaction transaction) {
        if (transaction.getTransactionType() == TransactionType.OUT) {
            transactionRepository.updateOutTransaction(
                    id,
                    transaction.getProduct().getProductID(),
                    transaction.getQuantity(),
                    transaction.getPurchasePrice().doubleValue(),
                    transaction.getTransactionDate().format(formatter),
                    transaction.getSupplier() != null ? transaction.getSupplier().getSupplierID() : null,
                    transaction.getEmployee() != null ? transaction.getEmployee().getEmployeeID() : null,
                    transaction.getTotalPrice().doubleValue()
            );
        }
    }

    @Override
    public void deleteOutTransaction(int id) {
        InventoryTransaction transaction = transactionRepository.findOutTransactionById((long) id);
        if (transaction != null && transaction.getTransactionType() == TransactionType.OUT) {
            transactionRepository.deleteOutTransaction(id);
        }
    }

    public List<InventoryTransaction> searchTransaction(String productName, String supplierName, String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime start = (startDate != null && !startDate.isEmpty())
                ? LocalDate.parse(startDate, formatter).atStartOfDay()
                : null;
        LocalDateTime end = (endDate != null && !endDate.isEmpty())
                ? LocalDate.parse(endDate, formatter).atTime(23, 59, 59)
                : null;
        return transactionRepository.searchTransaction(productName, supplierName, start, end);
    }

}
