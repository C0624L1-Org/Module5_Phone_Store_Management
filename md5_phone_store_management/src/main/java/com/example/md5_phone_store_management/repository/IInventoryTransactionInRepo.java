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

import java.time.LocalDate;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface IInventoryTransactionInRepo extends JpaRepository<InventoryTransaction, Integer> {

    @Query("SELECT i FROM InventoryTransaction i WHERE i.transactionType = :type")
    Page<InventoryTransaction> getByTransactionType(@Param("type") TransactionType type, Pageable pageable);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM InventoryTransaction i where i.transactionID in : ids",nativeQuery = true)
    void deleteByIdsIn(List<Integer> ids);
    @Query("SELECT i FROM InventoryTransaction i " +
            "WHERE i.transactionType = :transactionType " +  // Mặc định chỉ tìm giao dịch nhập kho
            "AND (:productName IS NULL OR LOWER(i.product.name) LIKE LOWER(CONCAT('%', :productName, '%'))) " +
            "AND (:supplierName IS NULL OR LOWER(i.product.supplier.name) LIKE LOWER(CONCAT('%', :supplierName, '%'))) " +
            "AND (:transactionDate IS NULL OR i.transactionDate = :transactionDate)")
    Page<InventoryTransaction> searchTransactions(
            @Param("productName") String productName,
            @Param("supplierName") String supplierName,
            @Param("transactionDate") LocalDate transactionDate,
            @Param("transactionType") TransactionType transactionType,
            Pageable pageable
    );


}
