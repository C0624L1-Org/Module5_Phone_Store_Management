package com.example.md5_phone_store_management.service.implement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

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
import com.example.md5_phone_store_management.model.InvoiceStatus;
import com.example.md5_phone_store_management.repository.InvoiceRepository;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.service.IInvoiceService;

@Service
public class InvoiceServiceImpl implements IInvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private IEmployeeService employeeService;

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
            
            // Đảm bảo invoice có thời gian tạo
            if (invoice.getCreatedAt() == null) {
                invoice.setCreatedAt(LocalDateTime.now());
            }
            
            // Đảm bảo invoice có trạng thái
            if (invoice.getStatus() == null) {
                invoice.setStatus(InvoiceStatus.PROCESSING);
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
        
        if (invoice.getPaymentMethod() == null) {
            System.err.println("Warning: Payment method is null");
        }

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
    public List<Invoice> findAll() {
        try {
            return invoiceRepository.findAll();
        } catch (Exception e) {
            System.err.println("Error finding all invoices: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Invoice> findAll(Pageable pageable) {
        try {
            return invoiceRepository.findAll(pageable);
        } catch (Exception e) {
            System.err.println("Error finding all invoices with pagination: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteInvoice(Long id) {
        try {
            invoiceRepository.deleteById(id);
        } catch (Exception e) {
            System.err.println("Error deleting invoice: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    //sap xep
    @Override
    public Page<Invoice> findAllSuccessInvoicesWithTimeAsc(Pageable pageable) {
        return invoiceRepository.findAllSuccessInvoicesWithTimeAsc(pageable);
    }

    @Override
    public Page<Invoice> findAllSuccessInvoicesWithTimeDesc(Pageable pageable) {
        return invoiceRepository.findAllSuccessInvoicesWithTimeDesc(pageable);
    }

    @Override
    public Page<Invoice> findAllSuccessInvoicesWithCustomerNameAsc(Pageable pageable) {
        return invoiceRepository.findAllSuccessInvoicesWithCustomerNameAsc(pageable);
    }

    @Override
    public Page<Invoice> findAllSuccessInvoicesWithCustomerNameDesc(Pageable pageable) {
        return invoiceRepository.findAllSuccessInvoicesWithCustomerNameDesc(pageable);
    }

    @Override
    public Page<Invoice> findAllSuccessInvoicesWithProductNameAsc(Pageable pageable) {
        return invoiceRepository.findAllSuccessInvoicesWithProductNameAsc(pageable);
    }

    @Override
    public Page<Invoice> findAllSuccessInvoicesWithProductNameDesc(Pageable pageable) {
        return invoiceRepository.findAllSuccessInvoicesWithProductNameDesc(pageable);
    }

    @Override
    public Page<Invoice> findAllSuccessInvoicesWithAmountAsc(Pageable pageable) {
        return invoiceRepository.findAllSuccessInvoicesWithAmountAsc(pageable);
    }

    @Override
    public Page<Invoice> findAllSuccessInvoicesWithAmountDesc(Pageable pageable) {
        return invoiceRepository.findAllSuccessInvoicesWithAmountDesc(pageable);
    }

    @Override
    public Page<Invoice> findAllSuccessInvoicesWithQuantityAsc(Pageable pageable) {
        return invoiceRepository.findAllSuccessInvoicesWithQuantityAsc(pageable);
    }

    @Override
    public Page<Invoice> findAllSuccessInvoicesWithQuantityDesc(Pageable pageable) {
        return invoiceRepository.findAllSuccessInvoicesWithQuantityDesc(pageable);
    }

    @Override
    public Page<Invoice> findAllSuccessInvoices(Pageable pageable) {
        return invoiceRepository.findAllSuccessInvoices(pageable);
    }


    @Override
    public Integer countAllSuccessInvoices() {
        return invoiceRepository.countAllSuccessInvoices();
    }

    @Override
    public Integer countTodaySuccessInvoices() {
        LocalDate today = LocalDate.now(); // Service computes "today"
        return invoiceRepository.countSuccessInvoicesByDate(today);
    }

    @Override
    public Integer countThisMonthSuccessInvoices() {
        LocalDate now = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(now); // Service computes "this month"
        LocalDate startOfMonth = thisMonth.atDay(1);
        LocalDateTime startDateTime = startOfMonth.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = thisMonth.atEndOfMonth().atTime(23, 59, 59, 999999999); // End of last day
        return invoiceRepository.countSuccessInvoicesBetweenDates(startDateTime, endDateTime);
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