//package com.example.md5_phone_store_management.service.implement;
//
//import com.example.md5_phone_store_management.model.InventoryTransaction;
//import com.example.md5_phone_store_management.model.Product;
//import com.example.md5_phone_store_management.model.Supplier;
//import com.example.md5_phone_store_management.model.TransactionType;
//import com.example.md5_phone_store_management.repository.IProductRepository;
//import com.example.md5_phone_store_management.repository.ISupplierRepository;
//import com.example.md5_phone_store_management.repository.TransactionOutRepository;
//import com.example.md5_phone_store_management.service.ITransactionOutService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class TransactionOutService implements ITransactionOutService {
//
//    @Autowired
//    private IProductRepository productRepository;
//
//    @Autowired
//    private ISupplierRepository supplierRepository;
//
//    @Autowired
//    private TransactionOutRepository transactionOutRepository;
//
//    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
////    transactionout
//
//    @Override
//    public Integer countExportQuantity() {
//        return transactionOutRepository.countExportQuantity();
//    }
//
//    @Override
//    public Integer countThisMonthExportQuantityProducts() {
//        return transactionOutRepository.countThisMonthExportQuantityProducts();
//    }
//
//    @Override
//    public String getRecentExportSupplierName() {
//        Integer SupplierID = transactionOutRepository.getLastestExportSupplierId();
//        if (SupplierID == null || SupplierID == 0) {
//            throw new IllegalArgumentException("Không có nhà cung cấp nào được tìm thấy (ID không hợp lệ).");
//        }
//        Supplier supplier = supplierRepository.findById(SupplierID)
//                .orElseThrow(() -> new IllegalArgumentException("Nhà cung cấp với ID " + SupplierID + " không tồn tại."));
//        return supplier.getName();
//    }
//
//    @Override
//    public String getRecentExportProductName() {
//                Integer productID = transactionOutRepository.getLastestExportProductId();
//        if (productID == null || productID == 0) {
//            throw new IllegalArgumentException("Không có sản phẩm nào được tìm thấy (ID không hợp lệ).");
//        }
//        Product product = productRepository.findByProductID(productID);
//        return product.getName();
//    }
//
//
//
//
//
//
//
//
//    @Override
//    public Integer countExportProducts(){
//        return transactionOutRepository.countExportProducts();
//    }
//
//
//
//    @Override
//    public Page<InventoryTransaction> searchExportTransactions(String productName, String supplierName, LocalDate startDate, LocalDate endDate, Pageable pageable) {
//        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
//        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(23, 59, 59) : null;
//
//        if (startDateTime != null && endDateTime == null) {
//            endDateTime = LocalDateTime.now();
//        }
//
//        return transactionOutRepository.searchTransaction(
//                productName, supplierName, startDateTime, endDateTime, TransactionType.OUT, pageable);
//    }
//
//    @Override
//    public void deleteInventoryTransactionByEmployeeID(Integer employeeID) {
//        try {
//            transactionOutRepository.deleteInventoryTransactionByEmployeeID(employeeID);
//        } catch (Exception e) {
//            System.out.println("Lỗi không thể xóa transaction với employeeID: " + e.getMessage());
//        }
//    }
//
//
//    @Override
//    public List<InventoryTransaction> getAllOutTransactions() {
//        return transactionOutRepository.findAllOutTransactions();
//    }
//
//    public Page<InventoryTransaction> getAllOutTransactionsPage(Pageable pageable) {
//        // Fetch transactions where transactionType is "OUT" (export transactions)
//        return transactionOutRepository.getByTransactionType(TransactionType.OUT, pageable);
//    }
//
//
//    @Override
//    public List<InventoryTransaction> getAllInTransactions() {
//        return transactionOutRepository.findAllInTransactions();
//    }
//
//    @Override
//    public Optional<InventoryTransaction> getOutTransactionById(Long id) {
//        return Optional.ofNullable(transactionOutRepository.findOutTransactionById(id));
//    }
//
//
//    @Override
//    public void addOutTransaction(InventoryTransaction transaction) {
//        try {
//            System.out.println("Transaction Type: " + transaction.getTransactionType());
//            if (transaction.getTransactionType() == TransactionType.OUT) {
//                transactionOutRepository.saveOutTransaction(
//                        transaction.getProduct().getProductID(),
//                        transaction.getQuantity(),
//                        transaction.getPurchasePrice().doubleValue(),
//                        transaction.getTransactionDate().format(formatter),
//                        transaction.getSupplier() != null ? transaction.getSupplier().getSupplierID() : null,
//                        transaction.getEmployee() != null ? transaction.getEmployee().getEmployeeID() : null,
//                        transaction.getTotalPrice().doubleValue()
//                );
//
//            }
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//            throw new RuntimeException("Lỗi khi lưu giao dịch: " + e.getMessage());
//        }
//    }
//
//
//    @Override
//    public void updateOutTransaction(int id, InventoryTransaction transaction) {
//        if (transaction.getTransactionType() == TransactionType.OUT) {
//            transactionOutRepository.updateOutTransaction(
//                    id,
//                    transaction.getProduct().getProductID(),
//                    transaction.getQuantity(),
//                    transaction.getPurchasePrice().doubleValue(),
//                    transaction.getTransactionDate().format(formatter),
//                    transaction.getSupplier() != null ? transaction.getSupplier().getSupplierID() : null,
//                    transaction.getEmployee() != null ? transaction.getEmployee().getEmployeeID() : null,
//                    transaction.getTotalPrice().doubleValue()
//            );
//        }
//    }
//
//    @Override
//    public void deleteOutTransaction(int id) {
//        InventoryTransaction transaction = transactionOutRepository.findOutTransactionById((long) id);
//        if (transaction != null && transaction.getTransactionType() == TransactionType.OUT) {
//            transactionOutRepository.deleteOutTransaction(id);
//        }
//    }
//
//}

package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.event.EntityChangeEvent;
import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.model.TransactionType;
import com.example.md5_phone_store_management.repository.IProductRepository;
import com.example.md5_phone_store_management.repository.ISupplierRepository;
import com.example.md5_phone_store_management.repository.TransactionOutRepository;
import com.example.md5_phone_store_management.service.ITransactionOutService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionOutService implements ITransactionOutService {

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private ISupplierRepository supplierRepository;

    @Autowired
    private TransactionOutRepository transactionOutRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Integer countExportQuantity() {
        return transactionOutRepository.countExportQuantity();
    }

    @Override
    public Integer countThisMonthExportQuantityProducts() {
        return transactionOutRepository.countThisMonthExportQuantityProducts();
    }

    @Override
    public String getRecentExportSupplierName() {
        Integer SupplierID = transactionOutRepository.getLastestExportSupplierId();
        if (SupplierID == null || SupplierID == 0) {
            return "Không xác định";
        }
        return supplierRepository.findById(SupplierID)
                .map(Supplier::getName)
                .orElse("Không xác định");
    }

    @Override
    public String getRecentExportProductName() {
        Integer productID = transactionOutRepository.getLastestExportProductId();
        if (productID == null || productID == 0) {
            return "Không xác định";
        }
        Product product = productRepository.findByProductID(productID);
        return product != null && product.getName() != null ? product.getName() : "Không xác định";
    }

    @Override
    public Integer countExportProducts() {
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

    @Override
    public Page<InventoryTransaction> getAllOutTransactionsPage(Pageable pageable) {
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
    @Transactional
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
                // Publish event with input transaction since saveOutTransaction doesn't return InventoryTransaction
                eventPublisher.publishEvent(new EntityChangeEvent(this, transaction, "INSERT_TRANSACTIONOUT", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu giao dịch: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateOutTransaction(int id, InventoryTransaction transaction) {
        InventoryTransaction existingTransaction = transactionOutRepository.findOutTransactionById((long) id);
        if (existingTransaction != null && transaction.getTransactionType() == TransactionType.OUT) {
            InventoryTransaction oldTransaction = new InventoryTransaction();
            BeanUtils.copyProperties(existingTransaction, oldTransaction); // Copy old state
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
            // Publish event with input transaction since updateOutTransaction doesn't return InventoryTransaction
            eventPublisher.publishEvent(new EntityChangeEvent(this, transaction, "UPDATE_TRANSACTIONOUT", oldTransaction));
        }
    }

    @Override
    @Transactional
    public void deleteOutTransaction(int id) {
        InventoryTransaction transaction = transactionOutRepository.findOutTransactionById((long) id);
        if (transaction != null && transaction.getTransactionType() == TransactionType.OUT) {
            eventPublisher.publishEvent(new EntityChangeEvent(this, transaction, "DELETE_TRANSACTIONOUT", transaction));
            transactionOutRepository.deleteOutTransaction(id);
        }
    }
}