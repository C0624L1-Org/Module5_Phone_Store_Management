package com.example.last.service;

import com.example.last.model.Supplier;

import java.util.List;

public interface ISupplierService {
    List<Supplier> getSupplierList();
    Supplier getSupplier(Integer id);
}
