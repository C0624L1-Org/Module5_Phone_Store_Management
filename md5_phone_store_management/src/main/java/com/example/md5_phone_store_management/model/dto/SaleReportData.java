package com.example.md5_phone_store_management.model.dto;

import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.Product;

import java.util.List;

public class SaleReportData {
    private long totalOrders;
    private Long totalRevenue;
    private long totalCustomers;
    private long totalProductsSold;
    private List<Product> products;
    private List<Object[]> revenueByProduct;
    private List<Object[]> revenueByTime;
    private List<Invoice> filteredInvoices;

    // Getters and Setters
    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public List<Object[]> getRevenueByProduct() {
        return revenueByProduct;
    }

    public void setRevenueByProduct(List<Object[]> revenueByProduct) {
        this.revenueByProduct = revenueByProduct;
    }

    public List<Object[]> getRevenueByTime() {
        return revenueByTime;
    }

    public void setRevenueByTime(List<Object[]> revenueByTime) {
        this.revenueByTime = revenueByTime;
    }

    public List<Invoice> getFilteredInvoices() {
        return filteredInvoices;
    }

    public void setFilteredInvoices(List<Invoice> filteredInvoices) {
        this.filteredInvoices = filteredInvoices;
    }
    public long getTotalProductsSold() {
        return totalProductsSold;
    }

    public void setTotalProductsSold(long totalProductsSold) {
        this.totalProductsSold = totalProductsSold;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
