package com.example.last.repository;

import com.example.md5_phone_store_management.model1.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISupplierRepository extends JpaRepository<Supplier,Integer> {
    @Query(value = "SELECT * FROM  Supplier ",nativeQuery = true )
    List<Supplier> findAll();
}
