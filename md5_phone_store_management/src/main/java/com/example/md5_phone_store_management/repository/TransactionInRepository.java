package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionInRepository extends JpaRepository<InventoryTransaction, Integer> {


    // Count total number of IN transactions
    @Query("SELECT COUNT(t) FROM InventoryTransaction t WHERE t.transactionType = 'IN'")
    Integer countImportQuantity();

    // Count IN transactions in the current month
    @Query("SELECT COUNT(t) FROM InventoryTransaction t WHERE t.transactionType = 'IN' " +
            "AND YEAR(t.transactionDate) = YEAR(CURRENT_DATE) " +
            "AND MONTH(t.transactionDate) = MONTH(CURRENT_DATE)")
    Integer countThisMonthImportQuantityProducts();

    // Get supplierID from the most recent IN transaction
    @Query("SELECT t.supplier.supplierID FROM InventoryTransaction t WHERE t.transactionType = 'IN' " +
            "ORDER BY t.transactionDate DESC LIMIT 1")
    Integer getLastestImportSupplierId();

    // Get productID from the most recent IN transaction
    @Query("SELECT t.product.productID FROM InventoryTransaction t WHERE t.transactionType = 'IN' " +
            "ORDER BY t.transactionDate DESC LIMIT 1")
    Integer getLastestImportProductId();










    // Đếm số lượng nhà cung cấp không trùng lặp (các giao dịch nhập kho)
    @Query("SELECT COALESCE(COUNT(DISTINCT it.supplier.supplierID), 0) " +
            "FROM InventoryTransaction it " +
            "WHERE it.transactionType = 'IN'")
    Integer countRegularSupplier();

    // Tìm supplierID của nhà cung cấp có tổng quantity lớn nhất (các giao dịch nhập kho)
    @Query("SELECT COALESCE((SELECT it.supplier.supplierID " +
            "FROM InventoryTransaction it " +
            "WHERE it.transactionType = 'IN' " +
            "GROUP BY it.supplier.supplierID " +
            "ORDER BY SUM(it.quantity) DESC " +
            "LIMIT 1), 0)")
    Integer getBestSupplierId();

    // Tìm tổng quantity lớn nhất của một nhà cung cấp (các giao dịch nhập kho)
    @Query("SELECT COALESCE((SELECT SUM(it.quantity) " +
            "FROM InventoryTransaction it " +
            "WHERE it.transactionType = 'IN' " +
            "GROUP BY it.supplier.supplierID " +
            "ORDER BY SUM(it.quantity) DESC " +
            "LIMIT 1), 0)")
    Integer getBestSupplierImportQuantity();




    @Query("SELECT COALESCE(SUM(it.quantity), 0) FROM InventoryTransaction it WHERE it.transactionType = 'IN'")
    Integer countImportProducts();

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO inventoryTransaction (productID, transactionType, quantity, purchasePrice, transactionDate, supplierID, employeeID, totalPrice) " +
            "VALUES (?1, 'IN', ?2, ?3, ?4, ?5, ?6, ?7)", nativeQuery = true)
    void saveOutTransaction(int productID, int quantity, double purchasePrice, String transactionDate, Integer supplierID, Integer employeeID, double totalPrice);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM inventoryTransaction WHERE transactionID = ?1 AND transactionType = 'IN'", nativeQuery = true)
    void deleteInTransaction(int id);

    @Query(value = "SELECT * FROM inventoryTransaction WHERE transactionID = ?1 AND transactionType = 'IN'", nativeQuery = true)
    InventoryTransaction findInTransactionById(Long id);

    @Query("SELECT i FROM InventoryTransaction i WHERE i.transactionType = :type")
    Page<InventoryTransaction> getByTransactionType(@Param("type") TransactionType type, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM InventoryTransaction i WHERE i.transactionID IN :ids", nativeQuery = true)
    void deleteByIdsIn(@Param("ids") List<Integer> ids);

    @Query("SELECT i FROM InventoryTransaction i " +
            "WHERE i.transactionType = :transactionType " +
            "AND (:productName IS NULL OR LOWER(i.product.name) LIKE LOWER(CONCAT('%', :productName, '%'))) " +
            "AND (:supplierName IS NULL OR LOWER(i.supplier.name) LIKE LOWER(CONCAT('%', :supplierName, '%'))) " +
            "AND (:startDate IS NULL OR i.transactionDate >= :startDate) " +
            "AND (:endDate IS NULL OR i.transactionDate <= :endDate OR (:startDate IS NOT NULL AND :endDate IS NULL AND i.transactionDate <= CURRENT_TIMESTAMP))")
    Page<InventoryTransaction> searchTransactions(
            @Param("productName") String productName,
            @Param("supplierName") String supplierName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("transactionType") TransactionType transactionType,
            Pageable pageable
    );

    @Query("SELECT i FROM InventoryTransaction i WHERE i.product.productID = :productId AND i.supplier.supplierID = :supplierId")
    InventoryTransaction getByProductIdAndSupplierId(@Param("productId") Integer productId, @Param("supplierId") Integer supplierId);

    @Query("SELECT i FROM InventoryTransaction i WHERE i.product.qrCode = :qrCode")
    InventoryTransaction findByQRCode(@Param("qrCode") String qrCode);
    @Query("SELECT i FROM InventoryTransaction i WHERE i.transactionID = :id AND i.transactionType = :transactionType")
    InventoryTransaction getByTransactionID(@Param("id") Integer id, @Param("transactionType") TransactionType transactionType);
}