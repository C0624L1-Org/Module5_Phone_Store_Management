package com.example.md5_phone_store_management.service.implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceDetail;
import com.example.md5_phone_store_management.repository.InvoiceRepository;
import com.example.md5_phone_store_management.service.IInvoiceService;

import java.util.List;

@Service
public class InvoiceServiceImpl implements IInvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Invoice saveInvoice(Invoice invoice) {
        try {
            if (invoice.getCustomer() == null) {
                throw new IllegalArgumentException("Customer cannot be null");
            }

            // Log thông tin chi tiết trước khi lưu
            System.out.println("Saving invoice: " + invoice);
            if (invoice.getInvoiceDetailList() != null) {
                System.out.println("Invoice has " + invoice.getInvoiceDetailList().size() + " details");

                // In chi tiết của từng sản phẩm để debug
                for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                    System.out.println("  - Detail: " + detail);
                }
            }

            // Đảm bảo quan hệ hai chiều được thiết lập đúng
            if (invoice.getInvoiceDetailList() != null) {
                invoice.getInvoiceDetailList().forEach(detail -> {
                    if (detail.getInvoice() == null) {
                        detail.setInvoice(invoice);
                    }
                });
            }

            // Kiểm tra trước khi lưu
            validateInvoice(invoice);

            Invoice savedInvoice = invoiceRepository.save(invoice);
            System.out.println("Invoice saved successfully, ID: " + savedInvoice.getId());
            return savedInvoice;
        } catch (DataIntegrityViolationException e) {
            System.err.println("Data integrity violation when saving invoice: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("Error saving invoice: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Kiểm tra hóa đơn trước khi lưu để đảm bảo tính toàn vẹn
     */
    private void validateInvoice(Invoice invoice) {
        if (invoice.getCustomer() == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        if (invoice.getAmount() == null) {
            System.err.println("Warning: Invoice amount is null");
        }

        // Kiểm tra các trường khác nếu cần
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice findById(Long id) {
        try {
            return invoiceRepository.findById(id).orElse(null);
        } catch (Exception e) {
            System.err.println("Error finding invoice by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findByCustomer(Customer customer) {
        try {
            return invoiceRepository.findByCustomer(customer);
        } catch (Exception e) {
            System.err.println("Error finding invoices by customer: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Invoice> findByCustomer(Customer customer, Pageable pageable) {
        try {
            return invoiceRepository.findByCustomer(customer, pageable);
        } catch (Exception e) {
            System.err.println("Error finding invoices by customer with pagination: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findByCustomerId(Integer customerID) {
        try {
            return invoiceRepository.findByCustomer_CustomerID(customerID);
        } catch (Exception e) {
            System.err.println("Error finding invoices by customer ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Invoice> findByCustomerId(Integer customerID, Pageable pageable) {
        try {
            return invoiceRepository.findByCustomer_CustomerID(customerID, pageable);
        } catch (Exception e) {
            System.err.println("Error finding invoices by customer ID with pagination: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}