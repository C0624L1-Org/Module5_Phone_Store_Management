package com.example.last.repository;

import com.example.last.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISupplierRepository extends JpaRepository<Supplier,Integer> {
    @Query(value = "SELECT * FROM  Supplier ",nativeQuery = true )
    List<Supplier> findAll();
}
