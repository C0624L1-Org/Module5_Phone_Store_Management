package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.model.TransactionType;
import com.example.md5_phone_store_management.repository.IInventoryTransactionInRepo;
import com.example.md5_phone_store_management.repository.ISupplierRepository;
import com.example.md5_phone_store_management.repository.IProductRepository;
import com.example.md5_phone_store_management.service.IInventoryTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryTransactionService implements IInventoryTransactionService {

    @Autowired
    private IInventoryTransactionInRepo inventoryTransactionInRepo;

    @Autowired
    private ISupplierRepository supplierRepository;

    @Autowired
    private IProductRepository productRepository;

    @Override
    public Page<InventoryTransaction> getImportTransactions(Pageable pageable) {
        return inventoryTransactionInRepo.getByTransactionType(TransactionType.IN, pageable);
    }

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
    public void deleteImportTransactions(List<Integer> ids) {
        inventoryTransactionInRepo.deleteByIdsIn(ids);
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
    public void addTransaction(InventoryTransaction transaction) {
        inventoryTransactionInRepo.save(transaction);
    }

    @Override
    @Transactional
    public void importProduct(Integer productId, Integer quantity, Integer supplierId, String purchasePrice) {
        Product product = productRepository.findByProductID(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }
        if (!product.getSupplier().getSupplierID().equals(supplierId)) {
            throw new IllegalArgumentException("Selected product does not belong to the selected supplier");
        }

        BigDecimal price = new BigDecimal(purchasePrice);
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setTransactionType(TransactionType.IN);
        transaction.setQuantity(quantity);
        transaction.setPurchasePrice(price);
        transaction.setTotalPrice(price.multiply(BigDecimal.valueOf(quantity)));
        transaction.setSupplier(new Supplier() {{ setSupplierID(supplierId); }});

        inventoryTransactionInRepo.save(transaction);
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);
    }

    @Override
    public List<Product> getProductsBySupplierId(Integer supplierId) {
        return productRepository.findBySupplierId(supplierId);
    }
}