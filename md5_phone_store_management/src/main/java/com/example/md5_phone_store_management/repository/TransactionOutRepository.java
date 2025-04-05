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
public interface TransactionOutRepository extends JpaRepository<InventoryTransaction, Integer> {

    @Query("SELECT i FROM InventoryTransaction i WHERE i.transactionType = :type")
    Page<InventoryTransaction> getByTransactionType(@Param("type") TransactionType type, Pageable pageable);




    @Query("SELECT i FROM InventoryTransaction i " +
            "WHERE i.transactionType = :transactionType " +
            "AND (:productName IS NULL OR LOWER(i.product.name) LIKE LOWER(CONCAT('%', :productName, '%'))) " +
            "AND (:supplierName IS NULL OR LOWER(i.supplier.name) LIKE LOWER(CONCAT('%', :supplierName, '%'))) " +
            "AND (:startDate IS NULL OR i.transactionDate >= :startDate) " +
            "AND (:endDate IS NULL OR i.transactionDate <= :endDate OR (:startDate IS NOT NULL AND :endDate IS NULL AND i.transactionDate <= CURRENT_TIMESTAMP))")
    Page<InventoryTransaction> searchTransaction(
            @Param("productName") String productName,
            @Param("supplierName") String supplierName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("transactionType") TransactionType transactionType,
            Pageable pageable
    );



    @Query("SELECT t FROM InventoryTransaction t " +
                "JOIN t.product p " +
                "JOIN t.supplier s " +
                "WHERE (:productName IS NULL OR p.name LIKE %:productName%) " +
                "AND (:supplierName IS NULL OR s.name LIKE %:supplierName%) " +
                "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
                "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
                "ORDER BY t.transactionDate ASC")
        List<InventoryTransaction> searchTransactionNoPage(
                @Param("productName") String productName,
                @Param("supplierName") String supplierName,
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate);



    @Query("SELECT t FROM InventoryTransaction t " +
            "WHERE t.transactionType = 'OUT'")
    List<InventoryTransaction> findAllOutTransactions();

    @Query("SELECT t FROM InventoryTransaction t " +
            "WHERE t.transactionType = 'IN'")
    List<InventoryTransaction> findAllInTransactions();


    @Query(value = "SELECT * FROM inventoryTransaction WHERE transactionID = ?1 AND transactionType = 'OUT'", nativeQuery = true)
    InventoryTransaction findOutTransactionById(Long id);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO inventoryTransaction (productID, transactionType, quantity, purchasePrice, transactionDate, supplierID, employeeID, totalPrice) " +
            "VALUES (?1, 'OUT', ?2, ?3, ?4, ?5, ?6, ?7)", nativeQuery = true)
    void saveOutTransaction(int productID, int quantity, double purchasePrice, String transactionDate, Integer supplierID, Integer employeeID, double totalPrice);

    @Modifying
    @Transactional
    @Query(value = "UPDATE inventoryTransaction SET productID = ?2, quantity = ?3, purchasePrice = ?4, transactionDate = ?5, supplierID = ?6, employeeID = ?7, totalPrice = ?8 " +
            "WHERE transactionID = ?1 AND transactionType = 'OUT'", nativeQuery = true)
    void updateOutTransaction(int id, int productID, int quantity, double purchasePrice, String transactionDate, Integer supplierID, Integer employeeID, double totalPrice);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM inventoryTransaction WHERE transactionID = ?1 AND transactionType = 'OUT'", nativeQuery = true)
    void deleteOutTransaction(int id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM inventoryTransaction WHERE employeeID = ?1", nativeQuery = true)
    void deleteInventoryTransactionByEmployeeID(Integer employeeID);

}
