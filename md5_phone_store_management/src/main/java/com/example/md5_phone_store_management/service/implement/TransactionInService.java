//package com.example.md5_phone_store_management.service.implement;
//
//import com.example.md5_phone_store_management.model.*;
//import com.example.md5_phone_store_management.repository.IEmployeeRepository;
//import com.example.md5_phone_store_management.repository.IProductRepository;
//import com.example.md5_phone_store_management.repository.ISupplierRepository;
//import com.example.md5_phone_store_management.repository.TransactionInRepository;
//import com.example.md5_phone_store_management.service.ITransactionInService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
//@Service
//public class TransactionInService implements ITransactionInService {
//    @Autowired
//    private TransactionInRepository inventoryTransactionInRepo;
//
//    @Autowired
//    private ISupplierRepository supplierRepository;
//
//    @Autowired
//    private IProductRepository productRepository;
//
//    @Autowired
//    private IEmployeeRepository employeeRepository;
//
//
//    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
////    transactionin
//
//    @Override
//    public Integer countImportQuantity() {
//        return inventoryTransactionInRepo.countImportQuantity();
//    }
//
//    @Override
//    public Integer countThisMonthImportQuantityProducts() {
//        return inventoryTransactionInRepo.countThisMonthImportQuantityProducts();
//    }
//
//    @Override
//    public String getRecentImportSupplierName() {
//        Integer SupplierID = inventoryTransactionInRepo.getLastestImportSupplierId();
//        if (SupplierID == null || SupplierID == 0) {
//            throw new IllegalArgumentException("Không có nhà cung cấp nào được tìm thấy (ID không hợp lệ).");
//        }
//        Supplier supplier = supplierRepository.findById(SupplierID)
//                .orElseThrow(() -> new IllegalArgumentException("Nhà cung cấp với ID " + SupplierID + " không tồn tại."));
//        return supplier.getName();
//    }
//
//    @Override
//    public String getRecentImportProductName() {
//        Integer productID = inventoryTransactionInRepo.getLastestImportProductId();
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
//    @Override
//    public Integer countRegularSupplier() {
//        return inventoryTransactionInRepo.countRegularSupplier();
//    }
//
//    @Override
//    public String getBestSupplierName() {
//        Integer bestSupplierId = inventoryTransactionInRepo.getBestSupplierId();
//        if (bestSupplierId == null || bestSupplierId == 0) {
//            throw new IllegalArgumentException("Không có nhà cung cấp nào được tìm thấy (ID không hợp lệ).");
//        }
//        Supplier supplier = supplierRepository.findById(bestSupplierId)
//                .orElseThrow(() -> new IllegalArgumentException("Nhà cung cấp với ID " + bestSupplierId + " không tồn tại."));
//        return supplier.getName();
//    }
//
//    @Override
//    public Integer getBestSupplierImportQuantity() {
//        return inventoryTransactionInRepo.getBestSupplierImportQuantity();
//    }
//
//
//
//
//    @Override
//    public Integer countImportProducts(){
//        return inventoryTransactionInRepo.countImportProducts();
//    }
//
//
//    @Override
//    public InventoryTransaction save(InventoryTransaction transaction) {
//        return inventoryTransactionInRepo.save(transaction);
//    }
//
//
//
//
//    @Override
//    public void editTransactionById(int transactionId, InventoryTransaction updatedTransaction) {
//        InventoryTransaction existingTransaction = findById(transactionId);
//
//        // Update only the fields provided by the updatedTransaction
//        if (updatedTransaction.getProduct() != null) {
//            existingTransaction.setProduct(updatedTransaction.getProduct());
//        }
//        if (updatedTransaction.getTransactionType() != null) {
//            existingTransaction.setTransactionType(updatedTransaction.getTransactionType());
//        }
//        if (updatedTransaction.getQuantity() != null) {
//            existingTransaction.setQuantity(updatedTransaction.getQuantity());
//        }
//        if (updatedTransaction.getPurchasePrice() != null) {
//            existingTransaction.setPurchasePrice(updatedTransaction.getPurchasePrice());
//        }
//        if (updatedTransaction.getTotalPrice() != null) {
//            existingTransaction.setTotalPrice(updatedTransaction.getTotalPrice());
//        }
//        if (updatedTransaction.getTransactionDate() != null) {
//            existingTransaction.setTransactionDate(updatedTransaction.getTransactionDate());
//        }
//        if (updatedTransaction.getSupplier() != null) {
//            existingTransaction.setSupplier(updatedTransaction.getSupplier());
//        }
//        if (updatedTransaction.getEmployee() != null) {
//            existingTransaction.setEmployee(updatedTransaction.getEmployee());
//        }
//
//         inventoryTransactionInRepo.save(existingTransaction);
//    }
//
//
//    @Override
//    public void addInTransaction(InventoryTransaction transaction) {
//        try {
//            System.out.println("Transaction Type: " + transaction.getTransactionType());
//            if (transaction.getTransactionType() == TransactionType.IN) {
//                inventoryTransactionInRepo.saveOutTransaction(
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
//    public void deleteOutTransaction(int id) {
//        InventoryTransaction transaction = inventoryTransactionInRepo.findInTransactionById((long) id);
//        if (transaction != null && transaction.getTransactionType() == TransactionType.IN) {
//            inventoryTransactionInRepo.deleteInTransaction(id);
//        }
//    }
//
//    @Override
//    public Page<InventoryTransaction> getImportTransactions(Pageable pageable) {
//        return inventoryTransactionInRepo.getByTransactionType(TransactionType.IN, pageable);
//    }
//
//    @Override
//    public Page<InventoryTransaction> searchImportTransactions(
//            String productName, String supplierName, LocalDate startDate, LocalDate endDate, Pageable pageable) {
//
//        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
//        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(23, 59, 59) : null;
//
//        if (startDateTime != null && endDateTime == null) {
//            endDateTime = LocalDateTime.now();
//        }
//
//        return inventoryTransactionInRepo.searchTransactions(
//                productName, supplierName, startDateTime, endDateTime, TransactionType.IN, pageable);
//    }
//
//    @Override
//    public void deleteImportTransactions(List<Integer> ids) {
//        inventoryTransactionInRepo.deleteByIdsIn(ids);
//    }
//
//    @Override
//    public InventoryTransaction getByProductIdAndSupplierId(Integer productId, Integer supplierId) {
//        return inventoryTransactionInRepo.getByProductIdAndSupplierId(productId, supplierId);
//    }
//
//    @Override
//    public InventoryTransaction findByQRCode(String qrCode) {
//        return inventoryTransactionInRepo.findByQRCode(qrCode);
//    }
//
//    @Override
//    public List<Supplier> getAllSuppliers() {
//        return supplierRepository.findAll();
//    }
//
//    @Override
//    public void addTransaction(InventoryTransaction transaction) {
//        inventoryTransactionInRepo.save(transaction);
//    }
//
//    @Override
//    @Transactional
//    public InventoryTransaction importProduct(Integer productId, Integer quantity, Integer supplierId, String purchasePrice) {
//        if (quantity == null || quantity <= 0) {
//            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
//        }
//
//        Product product = productRepository.findByProductID(productId);
//        if (product == null) {
//            throw new IllegalArgumentException("Sản phẩm không tồn tại");
//        }
//
//        if (!product.getSupplier().getSupplierID().equals(supplierId)) {
//            throw new IllegalArgumentException("Sản phẩm không thuộc nhà cung cấp đã chọn");
//        }
//
//        BigDecimal price;
//        try {
//            price = new BigDecimal(purchasePrice);
//            if (price.compareTo(BigDecimal.ZERO) <= 0) {
//                throw new IllegalArgumentException("Giá nhập phải lớn hơn 0");
//            }
//            BigDecimal maxPrice = new BigDecimal("99999999.99");
//            if (price.compareTo(maxPrice) > 0) {
//                throw new IllegalArgumentException("Giá nhập vượt quá giới hạn tối đa (99999999.99)");
//            }
//        } catch (NumberFormatException e) {
//            throw new IllegalArgumentException("Giá nhập không hợp lệ: " + purchasePrice);
//        }
//
//        InventoryTransaction transaction = new InventoryTransaction();
//        transaction.setProduct(product);
//        transaction.setTransactionType(TransactionType.IN);
//        transaction.setQuantity(quantity);
//        transaction.setPurchasePrice(price);
//        transaction.setTotalPrice(price.multiply(BigDecimal.valueOf(quantity)));
//        transaction.setSupplier(supplierRepository.findById(supplierId)
//                .orElseThrow(() -> new IllegalArgumentException("Nhà cung cấp không tồn tại")));
//        transaction.setTransactionDate(LocalDateTime.now()); // Tự động gán thời gian hiện tại
//
//        Integer employeeId = 1; // Giá trị mặc định
//        Employee employee = employeeRepository.findByEmployeeID(employeeId);
//        if (employee == null) {
//            throw new IllegalArgumentException("Nhân viên không tồn tại trong database");
//        }
//        transaction.setEmployee(employee);
//
//        // Lưu và trả về đối tượng đã lưu - Sử dụng inventoryTransactionInRepo thay vì inventoryTransactionRepository
//        InventoryTransaction savedTransaction = inventoryTransactionInRepo.save(transaction);
//
//        product.setStockQuantity(product.getStockQuantity() + quantity);
//        productRepository.save(product);
//
//        return savedTransaction;
//    }
//
//    @Override
//    public List<Product> getProductsBySupplierId(Integer supplierId) {
//        return productRepository.findBySupplierId(supplierId);
//    }
//
//    @Override
//    public InventoryTransaction findById(Integer id) {
//        return inventoryTransactionInRepo.findById(id).get();
//    }
//    public InventoryTransaction findByInventoryTransactionId(Integer id){
//        return inventoryTransactionInRepo.getByTransactionID(id,TransactionType.IN);
//    }
//
//
//
//
//}
//
//
package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.event.EntityChangeEvent;
import com.example.md5_phone_store_management.model.*;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import com.example.md5_phone_store_management.repository.IProductRepository;
import com.example.md5_phone_store_management.repository.ISupplierRepository;
import com.example.md5_phone_store_management.repository.TransactionInRepository;
import com.example.md5_phone_store_management.service.ITransactionInService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TransactionInService implements ITransactionInService {

    @Autowired
    private TransactionInRepository inventoryTransactionInRepo;

    @Autowired
    private ISupplierRepository supplierRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private IEmployeeRepository employeeRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Integer countImportQuantity() {
        return inventoryTransactionInRepo.countImportQuantity();
    }

    @Override
    public Integer countThisMonthImportQuantityProducts() {
        return inventoryTransactionInRepo.countThisMonthImportQuantityProducts();
    }

    @Override
    public String getRecentImportSupplierName() {
        Integer SupplierID = inventoryTransactionInRepo.getLastestImportSupplierId();
        if (SupplierID == null || SupplierID == 0) {
            throw new IllegalArgumentException("Không có nhà cung cấp nào được tìm thấy (ID không hợp lệ).");
        }
        Supplier supplier = supplierRepository.findById(SupplierID)
                .orElseThrow(() -> new IllegalArgumentException("Nhà cung cấp với ID " + SupplierID + " không tồn tại."));
        return supplier.getName();
    }

    @Override
    public String getRecentImportProductName() {
        Integer productID = inventoryTransactionInRepo.getLastestImportProductId();
        if (productID == null || productID == 0) {
            throw new IllegalArgumentException("Không có sản phẩm nào được tìm thấy (ID không hợp lệ).");
        }
        Product product = productRepository.findByProductID(productID);
        return product.getName();
    }

    @Override
    public Integer countRegularSupplier() {
        return inventoryTransactionInRepo.countRegularSupplier();
    }

    @Override
    public String getBestSupplierName() {
        Integer bestSupplierId = inventoryTransactionInRepo.getBestSupplierId();
        if (bestSupplierId == null || bestSupplierId == 0) {
            throw new IllegalArgumentException("Không có nhà cung cấp nào được tìm thấy (ID không hợp lệ).");
        }
        Supplier supplier = supplierRepository.findById(bestSupplierId)
                .orElseThrow(() -> new IllegalArgumentException("Nhà cung cấp với ID " + bestSupplierId + " không tồn tại."));
        return supplier.getName();
    }

    @Override
    public Integer getBestSupplierImportQuantity() {
        return inventoryTransactionInRepo.getBestSupplierImportQuantity();
    }

    @Override
    public Integer countImportProducts() {
        return inventoryTransactionInRepo.countImportProducts();
    }

    @Override
    @Transactional
    public InventoryTransaction save(InventoryTransaction transaction) {
        InventoryTransaction savedTransaction = inventoryTransactionInRepo.save(transaction);
        eventPublisher.publishEvent(new EntityChangeEvent(this, savedTransaction, "INSERT_TRANSACTIONIN", null));
        return savedTransaction;
    }

    @Override
    @Transactional
    public void editTransactionById(int transactionId, InventoryTransaction updatedTransaction) {
        InventoryTransaction existingTransaction = findById(transactionId);
        InventoryTransaction oldTransaction = new InventoryTransaction();
        BeanUtils.copyProperties(existingTransaction, oldTransaction); // Copy old state

        // Update only the fields provided by the updatedTransaction
        if (updatedTransaction.getProduct() != null) {
            existingTransaction.setProduct(updatedTransaction.getProduct());
        }
        if (updatedTransaction.getTransactionType() != null) {
            existingTransaction.setTransactionType(updatedTransaction.getTransactionType());
        }
        if (updatedTransaction.getQuantity() != null) {
            existingTransaction.setQuantity(updatedTransaction.getQuantity());
        }
        if (updatedTransaction.getPurchasePrice() != null) {
            existingTransaction.setPurchasePrice(updatedTransaction.getPurchasePrice());
        }
        if (updatedTransaction.getTotalPrice() != null) {
            existingTransaction.setTotalPrice(updatedTransaction.getTotalPrice());
        }
        if (updatedTransaction.getTransactionDate() != null) {
            existingTransaction.setTransactionDate(updatedTransaction.getTransactionDate());
        }
        if (updatedTransaction.getSupplier() != null) {
            existingTransaction.setSupplier(updatedTransaction.getSupplier());
        }
        if (updatedTransaction.getEmployee() != null) {
            existingTransaction.setEmployee(updatedTransaction.getEmployee());
        }

        InventoryTransaction updated = inventoryTransactionInRepo.save(existingTransaction);
        eventPublisher.publishEvent(new EntityChangeEvent(this, updated, "UPDATE_TRANSACTIONIN", oldTransaction));
    }

    @Override
    @Transactional
    public void addInTransaction(InventoryTransaction transaction) {
        try {
            System.out.println("Transaction Type: " + transaction.getTransactionType());
            if (transaction.getTransactionType() == TransactionType.IN) {
                inventoryTransactionInRepo.saveOutTransaction(
                        transaction.getProduct().getProductID(),
                        transaction.getQuantity(),
                        transaction.getPurchasePrice().doubleValue(),
                        transaction.getTransactionDate().format(formatter),
                        transaction.getSupplier() != null ? transaction.getSupplier().getSupplierID() : null,
                        transaction.getEmployee() != null ? transaction.getEmployee().getEmployeeID() : null,
                        transaction.getTotalPrice().doubleValue()
                );
                // Since saveOutTransaction doesn't return InventoryTransaction, publish event with input transaction
                eventPublisher.publishEvent(new EntityChangeEvent(this, transaction, "INSERT_TRANSACTIONIN", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu giao dịch: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteOutTransaction(int id) {
        InventoryTransaction transaction = inventoryTransactionInRepo.findInTransactionById((long) id);
        if (transaction != null && transaction.getTransactionType() == TransactionType.IN) {
            eventPublisher.publishEvent(new EntityChangeEvent(this, transaction, "DELETE_TRANSACTIONIN", transaction));
            inventoryTransactionInRepo.deleteInTransaction(id);
        }
    }

    @Override
    @Transactional
    public void deleteImportTransactions(List<Integer> ids) {
        List<InventoryTransaction> transactionsToDelete = inventoryTransactionInRepo.findAllById(ids);
        for (InventoryTransaction transaction : transactionsToDelete) {
            if (transaction.getTransactionType() == TransactionType.IN) {
                eventPublisher.publishEvent(new EntityChangeEvent(this, transaction, "DELETE_TRANSACTIONIN", transaction));
            }
        }
        inventoryTransactionInRepo.deleteByIdsIn(ids);
    }

    @Override
    @Transactional
    public void addTransaction(InventoryTransaction transaction) {
        InventoryTransaction savedTransaction = inventoryTransactionInRepo.save(transaction);
        eventPublisher.publishEvent(new EntityChangeEvent(this, savedTransaction, "INSERT_TRANSACTIONIN", null));
    }

    @Override
    @Transactional
    public InventoryTransaction importProduct(Integer productId, Integer quantity, Integer supplierId, String purchasePrice) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        Product product = productRepository.findByProductID(productId);
        if (product == null) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại");
        }

        if (!product.getSupplier().getSupplierID().equals(supplierId)) {
            throw new IllegalArgumentException("Sản phẩm không thuộc nhà cung cấp đã chọn");
        }

        BigDecimal price;
        try {
            price = new BigDecimal(purchasePrice);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Giá nhập phải lớn hơn 0");
            }
            BigDecimal maxPrice = new BigDecimal("99999999.99");
            if (price.compareTo(maxPrice) > 0) {
                throw new IllegalArgumentException("Giá nhập vượt quá giới hạn tối đa (99999999.99)");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá nhập không hợp lệ: " + purchasePrice);
        }

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setTransactionType(TransactionType.IN);
        transaction.setQuantity(quantity);
        transaction.setPurchasePrice(price);
        transaction.setTotalPrice(price.multiply(BigDecimal.valueOf(quantity)));
        transaction.setSupplier(supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Nhà cung cấp không tồn tại")));
        transaction.setTransactionDate(LocalDateTime.now());

        Integer employeeId = 1; // Giá trị mặc định
        Employee employee = employeeRepository.findByEmployeeID(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("Nhân viên không tồn tại trong database");
        }
        transaction.setEmployee(employee);

        InventoryTransaction savedTransaction = inventoryTransactionInRepo.save(transaction);
        eventPublisher.publishEvent(new EntityChangeEvent(this, savedTransaction, "INSERT_TRANSACTIONIN", null));

        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);

        return savedTransaction;
    }

    @Override
    public List<Product> getProductsBySupplierId(Integer supplierId) {
        return productRepository.findBySupplierId(supplierId);
    }

    @Override
    public Page<InventoryTransaction> getImportTransactions(Pageable pageable) {
        return inventoryTransactionInRepo.getByTransactionType(TransactionType.IN, pageable);
    }

    @Override
    public Page<InventoryTransaction> searchImportTransactions(
            String productName, String supplierName, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        if (startDateTime != null && endDateTime == null) {
            endDateTime = LocalDateTime.now();
        }

        return inventoryTransactionInRepo.searchTransactions(
                productName, supplierName, startDateTime, endDateTime, TransactionType.IN, pageable);
    }

    @Override
    public InventoryTransaction getByProductIdAndSupplierId(Integer productId, Integer supplierId) {
        return inventoryTransactionInRepo.getByProductIdAndSupplierId(productId, supplierId);
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
    public InventoryTransaction findById(Integer id) {
        return inventoryTransactionInRepo.findById(id).get();
    }

    @Override
    public InventoryTransaction findByInventoryTransactionId(Integer id) {
        return inventoryTransactionInRepo.getByTransactionID(id, TransactionType.IN);
    }
}
