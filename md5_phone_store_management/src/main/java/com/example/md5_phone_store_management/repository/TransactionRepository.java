package com.example.md5_phone_store_management.repository;


import com.example.md5_phone_store_management.model.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<InventoryTransaction, Integer> {


    @Query("SELECT t FROM InventoryTransaction t " +
            "WHERE t.transactionType = 'OUT'")
    List<InventoryTransaction> findAllOutTransactions();

    @Query("SELECT t FROM InventoryTransaction t " +
            "WHERE t.transactionType = 'IN'")
    List<InventoryTransaction> findAllInTransactions();


    @Query(value = "SELECT * FROM inventoryTransaction WHERE transactionID = ?1 AND transactionType = 'OUT'", nativeQuery = true)
    InventoryTransaction findOutTransactionById(int id);


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
