package com.example.md5_phone_store_management.repository;


import com.example.md5_phone_store_management.model.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<InventoryTransaction, Integer> {

//    List<InventoryTransaction> searchTransaction(String productName, String supplierName, String startDate, String endDate);



        @Query("SELECT t FROM InventoryTransaction t " +
                "JOIN t.product p " +
                "JOIN t.supplier s " +
                "WHERE (:productName IS NULL OR p.name LIKE %:productName%) " +
                "AND (:supplierName IS NULL OR s.name LIKE %:supplierName%) " +
                "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
                "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
                "ORDER BY t.transactionDate ASC")
        List<InventoryTransaction> searchTransaction(
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


}
