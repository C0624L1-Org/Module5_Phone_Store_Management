package com.example.md5_phone_store_management.service.implement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceDetail;
import com.example.md5_phone_store_management.model.InvoiceStatus;
import com.example.md5_phone_store_management.model.PaymentMethod;
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
    
    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findByStatus(InvoiceStatus status) {
        try {
            return invoiceRepository.findByStatus(status);
        } catch (Exception e) {
            System.err.println("Error finding invoices by status: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable) {
        try {
            return invoiceRepository.findByStatus(status, pageable);
        } catch (Exception e) {
            System.err.println("Error finding invoices by status with pagination: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Invoice updateStatus(Long invoiceId, InvoiceStatus status) {
        try {
            Invoice invoice = findById(invoiceId);
            if (invoice == null) {
                throw new IllegalArgumentException("Invoice not found with ID: " + invoiceId);
            }
            
            invoice.setStatus(status);
            return invoiceRepository.save(invoice);
        } catch (Exception e) {
            System.err.println("Error updating invoice status: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findByPaymentMethod(PaymentMethod paymentMethod) {
        try {
            return invoiceRepository.findByPaymentMethod(paymentMethod);
        } catch (Exception e) {
            System.err.println("Error finding invoices by payment method: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Invoice> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable) {
        try {
            return invoiceRepository.findByPaymentMethod(paymentMethod, pageable);
        } catch (Exception e) {
            System.err.println("Error finding invoices by payment method with pagination: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findByEmployee(Employee employee) {
        try {
            return invoiceRepository.findByEmployee(employee);
        } catch (Exception e) {
            System.err.println("Error finding invoices by employee: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Invoice> findByEmployee(Employee employee, Pageable pageable) {
        try {
            return invoiceRepository.findByEmployee(employee, pageable);
        } catch (Exception e) {
            System.err.println("Error finding invoices by employee with pagination: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findByEmployeeId(Integer employeeID) {
        try {
            return invoiceRepository.findByEmployee_EmployeeID(employeeID);
        } catch (Exception e) {
            System.err.println("Error finding invoices by employee ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Invoice> findByEmployeeId(Integer employeeID, Pageable pageable) {
        try {
            return invoiceRepository.findByEmployee_EmployeeID(employeeID, pageable);
        } catch (Exception e) {
            System.err.println("Error finding invoices by employee ID with pagination: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Invoice assignEmployee(Long invoiceId, Integer employeeId) {
        try {
            Invoice invoice = findById(invoiceId);
            if (invoice == null) {
                throw new IllegalArgumentException("Invoice not found with ID: " + invoiceId);
            }
            
            Employee employee = employeeService.getEmployeeById(employeeId);
            if (employee == null) {
                throw new IllegalArgumentException("Employee not found with ID: " + employeeId);
            }
            
            invoice.setEmployee(employee);
            return invoiceRepository.save(invoice);
        } catch (Exception e) {
            System.err.println("Error assigning employee to invoice: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return invoiceRepository.findByCreatedAtBetween(startDate, endDate);
        } catch (Exception e) {
            System.err.println("Error finding invoices between dates: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Invoice> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        try {
            return invoiceRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        } catch (Exception e) {
            System.err.println("Error finding invoices between dates with pagination: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findByStatusAndPaymentMethod(InvoiceStatus status, PaymentMethod paymentMethod) {
        try {
            return invoiceRepository.findByStatusAndPaymentMethod(status, paymentMethod);
        } catch (Exception e) {
            System.err.println("Error finding invoices by status and payment method: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Invoice> findByStatusAndPaymentMethod(InvoiceStatus status, PaymentMethod paymentMethod, Pageable pageable) {
        try {
            return invoiceRepository.findByStatusAndPaymentMethod(status, paymentMethod, pageable);
        } catch (Exception e) {
            System.err.println("Error finding invoices by status and payment method with pagination: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countByStatus(InvoiceStatus status) {
        try {
            return invoiceRepository.countByStatus(status);
        } catch (Exception e) {
            System.err.println("Error counting invoices by status: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countByPaymentMethod(PaymentMethod paymentMethod) {
        try {
            return invoiceRepository.countByPaymentMethod(paymentMethod);
        } catch (Exception e) {
            System.err.println("Error counting invoices by payment method: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getTotalRevenue() {
        try {
            Long revenue = invoiceRepository.getTotalRevenue();
            return revenue != null ? revenue : 0L;
        } catch (Exception e) {
            System.err.println("Error getting total revenue: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getTotalRevenueBetween(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            Long revenue = invoiceRepository.getTotalRevenueBetween(startDate, endDate);
            return revenue != null ? revenue : 0L;
        } catch (Exception e) {
            System.err.println("Error getting total revenue between dates: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Invoice processPayment(Long invoiceId, PaymentMethod paymentMethod) {
        try {
            Invoice invoice = findById(invoiceId);
            if (invoice == null) {
                throw new IllegalArgumentException("Invoice not found with ID: " + invoiceId);
            }
            
            invoice.setPaymentMethod(paymentMethod);
            invoice.setStatus(InvoiceStatus.PROCESSING);
            return invoiceRepository.save(invoice);
        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Invoice completeInvoice(Long invoiceId, Integer employeeId) {
        try {
            Invoice invoice = findById(invoiceId);
            if (invoice == null) {
                throw new IllegalArgumentException("Invoice not found with ID: " + invoiceId);
            }
            
            Employee employee = employeeService.getEmployeeById(employeeId);
            if (employee == null) {
                throw new IllegalArgumentException("Employee not found with ID: " + employeeId);
            }
            
            invoice.setEmployee(employee);
            invoice.setStatus(InvoiceStatus.SUCCESS);
            return invoiceRepository.save(invoice);
        } catch (Exception e) {
            System.err.println("Error completing invoice: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}