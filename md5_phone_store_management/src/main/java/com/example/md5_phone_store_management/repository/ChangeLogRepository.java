package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.ChangeLog;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long> {

    List<ChangeLog> findTopByEntityNameAndFieldNameOrderByTimestampDesc(String entityName, String fieldName);

    @Query("SELECT c FROM ChangeLog c WHERE LOWER(c.entityName) IN ( 'inventorytransaction', 'supplier')")
    List<ChangeLog> getAllChangeLogForWarehouse(Sort sort);

    List<ChangeLog> findTopByEntityNameOrderByTimestampDesc(String entityName);

    // Tìm các thay đổi theo entityName và entityId
    List<ChangeLog> findByEntityNameAndEntityId(String entityName, Long entityId);

    // Tìm các thay đổi theo employeeId
    List<ChangeLog> findByEmployeeId(Long employeeId);

    // Tìm các thay đổi theo action và entityName
    List<ChangeLog> findByActionAndEntityName(String action, String entityName);
}