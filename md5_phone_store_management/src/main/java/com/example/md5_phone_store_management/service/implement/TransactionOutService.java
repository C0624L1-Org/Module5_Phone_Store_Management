package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.TransactionType;
import com.example.md5_phone_store_management.repository.TransactionOutRepository;
import com.example.md5_phone_store_management.service.ITransactionOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionOutService implements ITransactionOutService {

    @Autowired
    private TransactionOutRepository transactionOutRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Override
    public Integer countExportProducts(){
        return transactionOutRepository.countExportProducts();
    }



    @Override
    public Page<InventoryTransaction> searchExportTransactions(String productName, String supplierName, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        if (startDateTime != null && endDateTime == null) {
            endDateTime = LocalDateTime.now();
        }

        return transactionOutRepository.searchTransaction(
                productName, supplierName, startDateTime, endDateTime, TransactionType.OUT, pageable);
    }

    @Override
    public void deleteInventoryTransactionByEmployeeID(Integer employeeID) {
        try {
            transactionOutRepository.deleteInventoryTransactionByEmployeeID(employeeID);
        } catch (Exception e) {
            System.out.println("Lỗi không thể xóa transaction với employeeID: " + e.getMessage());
        }
    }


    @Override
    public List<InventoryTransaction> getAllOutTransactions() {
        return transactionOutRepository.findAllOutTransactions();
    }

    public Page<InventoryTransaction> getAllOutTransactionsPage(Pageable pageable) {
        // Fetch transactions where transactionType is "OUT" (export transactions)
        return transactionOutRepository.getByTransactionType(TransactionType.OUT, pageable);
    }


    @Override
    public List<InventoryTransaction> getAllInTransactions() {
        return transactionOutRepository.findAllInTransactions();
    }

    @Override
    public Optional<InventoryTransaction> getOutTransactionById(Long id) {
        return Optional.ofNullable(transactionOutRepository.findOutTransactionById(id));
    }


    @Override
    public void addOutTransaction(InventoryTransaction transaction) {
        try {
            System.out.println("Transaction Type: " + transaction.getTransactionType());
            if (transaction.getTransactionType() == TransactionType.OUT) {
                transactionOutRepository.saveOutTransaction(
                        transaction.getProduct().getProductID(),
                        transaction.getQuantity(),
                        transaction.getPurchasePrice().doubleValue(),
                        transaction.getTransactionDate().format(formatter),
                        transaction.getSupplier() != null ? transaction.getSupplier().getSupplierID() : null,
                        transaction.getEmployee() != null ? transaction.getEmployee().getEmployeeID() : null,
                        transaction.getTotalPrice().doubleValue()
                );

            }

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu giao dịch: " + e.getMessage());
        }
    }


    @Override
    public void updateOutTransaction(int id, InventoryTransaction transaction) {
        if (transaction.getTransactionType() == TransactionType.OUT) {
            transactionOutRepository.updateOutTransaction(
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
        InventoryTransaction transaction = transactionOutRepository.findOutTransactionById((long) id);
        if (transaction != null && transaction.getTransactionType() == TransactionType.OUT) {
            transactionOutRepository.deleteOutTransaction(id);
        }
    }

//    public List<InventoryTransaction> searchTransaction(String productName, String supplierName, String startDate, String endDate) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        LocalDateTime start = (startDate != null && !startDate.isEmpty())
//                ? LocalDate.parse(startDate, formatter).atStartOfDay()
//                : null;
//        LocalDateTime end = (endDate != null && !endDate.isEmpty())
//                ? LocalDate.parse(endDate, formatter).atTime(23, 59, 59)
//                : null;
//        return transactionOutRepository.searchTransaction(productName, supplierName, start, end);
//    }

}
