package com.example.md5_phone_store_management.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Invoice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IInvoiceService {
    Invoice saveInvoice(Invoice invoice);
    Invoice findById(Long id);
    List<Invoice> findAll();
    Page<Invoice> findAll(Pageable pageable);
    void deleteInvoice(Long id);

    // Sắp xếp
    //Theo thoi gian
    Page<Invoice> findAllSuccessInvoicesWithTimeAsc(Pageable pageable);
    Page<Invoice> findAllSuccessInvoicesWithTimeDesc(Pageable pageable);

    //Theo ten khach hang
    Page<Invoice> findAllSuccessInvoicesWithCustomerNameAsc(Pageable pageable);
    Page<Invoice> findAllSuccessInvoicesWithCustomerNameDesc(Pageable pageable);

    //Theo tên sản phẩm
    Page<Invoice> findAllSuccessInvoicesWithProductNameAsc(Pageable pageable);
    Page<Invoice> findAllSuccessInvoicesWithProductNameDesc(Pageable pageable);

    //Theo so tien
    Page<Invoice> findAllSuccessInvoicesWithAmountAsc(Pageable pageable);
    Page<Invoice> findAllSuccessInvoicesWithAmountDesc(Pageable pageable);

    //Theo so luong
    Page<Invoice> findAllSuccessInvoicesWithQuantityAsc(Pageable pageable);
    Page<Invoice> findAllSuccessInvoicesWithQuantityDesc(Pageable pageable);

    //lay cac hoa don thanh cong
    Page<Invoice> findAllSuccessInvoices(Pageable pageable);

    // Lấy tất cả hóa đơn của khách hàng
    List<Invoice> findByCustomer(Customer customer);

    // Lấy tất cả hóa đơn của khách hàng với phân trang
    Page<Invoice> findByCustomer(Customer customer, Pageable pageable);

    // Lấy tất cả hóa đơn của khách hàng theo ID
    List<Invoice> findByCustomerId(Integer customerID);

    // Lấy tất cả hóa đơn của khách hàng theo ID với phân trang
    Page<Invoice> findByCustomerId(Integer customerID, Pageable pageable);

    //Tính tổng số tien mua bán theo năm
    int getTotalInvoicesYear(@Param("year") int year);

    // Tính tổng số tiền mua bán theo năm
    Long getTotalMoneyInvoicesYear(@Param("year") int year);
    // Đình Anh
    public List<Object[]> getMonthlyRevenueByYear(int year);
    public List<Object[]> findAllSuccessInvoices();
}