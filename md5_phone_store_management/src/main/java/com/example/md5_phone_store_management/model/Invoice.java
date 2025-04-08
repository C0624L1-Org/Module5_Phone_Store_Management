package com.example.md5_phone_store_management.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "invoices")
public class Invoice extends AuditableEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vnp_TxnRef;
    private Long amount;
    private String orderInfo;
    private String payDate;
    private String transactionNo;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceDetail> invoiceDetailList;

    public Invoice() {
        this.status = InvoiceStatus.PROCESSING;
        this.createdAt = LocalDateTime.now();
    }

    public Invoice(Long id, String vnp_TxnRef, Long amount, String orderInfo, 
                   String payDate, String transactionNo, 
                   PaymentMethod paymentMethod, InvoiceStatus status,
                   Customer customer, Employee employee, List<InvoiceDetail> invoiceDetailList) {
        this.id = id;
        this.vnp_TxnRef = vnp_TxnRef;
        this.amount = amount;
        this.orderInfo = orderInfo;
        this.payDate = payDate;
        this.transactionNo = transactionNo;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.customer = customer;
        this.employee = employee;
        this.invoiceDetailList = invoiceDetailList;
        this.createdAt = LocalDateTime.now();
    }

    public Invoice(Long id, String vnp_TxnRef, Long amount, String orderInfo, 
                   String payDate, String transactionNo,
                   PaymentMethod paymentMethod, InvoiceStatus status,
                   Customer customer, Employee employee) {
        this.id = id;
        this.vnp_TxnRef = vnp_TxnRef;
        this.amount = amount;
        this.orderInfo = orderInfo;
        this.payDate = payDate;
        this.transactionNo = transactionNo;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.customer = customer;
        this.employee = employee;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVnp_TxnRef() {
        return vnp_TxnRef;
    }

    public void setVnp_TxnRef(String vnp_TxnRef) {
        this.vnp_TxnRef = vnp_TxnRef;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }

    public String getPayDate() {
        return payDate;
    }

    public void setPayDate(String payDate) {
        this.payDate = payDate;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public List<InvoiceDetail> getInvoiceDetailList() {
        return invoiceDetailList;
    }

    public void setInvoiceDetailList(List<InvoiceDetail> invoiceDetailList) {
        if (this.invoiceDetailList != null) {
            this.invoiceDetailList.forEach(detail -> detail.setInvoice(null));
        }

        this.invoiceDetailList = invoiceDetailList;

        if (invoiceDetailList != null) {
            invoiceDetailList.forEach(detail -> detail.setInvoice(this));
        }
    }

    /**
     * Helper method to add an invoice detail and maintain the bidirectional relationship
     */
    public void addInvoiceDetail(InvoiceDetail detail) {
        if (invoiceDetailList == null) {
            invoiceDetailList = new ArrayList<>();
        }
        invoiceDetailList.add(detail);
        detail.setInvoice(this);
    }

    /**
     * Helper method to remove an invoice detail and maintain the bidirectional relationship
     */
    public void removeInvoiceDetail(InvoiceDetail detail) {
        if (invoiceDetailList != null) {
            invoiceDetailList.remove(detail);
            detail.setInvoice(null);
        }
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", vnp_TxnRef='" + vnp_TxnRef + '\'' +
                ", amount=" + amount +
                ", orderInfo='" + orderInfo + '\'' +
                ", payDate='" + payDate + '\'' +
                ", transactionNo='" + transactionNo + '\'' +
                ", paymentMethod=" + paymentMethod +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", customer=" + (customer != null ? customer.getCustomerID() : null) +
                ", employee=" + (employee != null ? employee.getEmployeeID() : null) +
                ", invoiceDetailCount=" + (invoiceDetailList != null ? invoiceDetailList.size() : 0) +
                '}';
    }
}
