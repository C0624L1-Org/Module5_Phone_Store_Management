package com.example.md5_phone_store_management.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vnp_TxnRef;
    private Long amount;
    private String orderInfo;
    private String bankCode;
    private String payDate;
    private String transactionNo;
    private String cardType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceDetail> invoiceDetailList;

    public Invoice() {
    }

    public Invoice(Long id, String vnp_TxnRef, Long amount, String orderInfo, String bankCode, String payDate, String transactionNo, String cardType, Customer customer, List<InvoiceDetail> invoiceDetailList) {
        this.id = id;
        this.vnp_TxnRef = vnp_TxnRef;
        this.amount = amount;
        this.orderInfo = orderInfo;
        this.bankCode = bankCode;
        this.payDate = payDate;
        this.transactionNo = transactionNo;
        this.cardType = cardType;
        this.customer = customer;
        this.invoiceDetailList = invoiceDetailList;
    }

    public Invoice(Long id, String vnp_TxnRef, Long amount, String orderInfo, String bankCode, String payDate, String transactionNo, String cardType, Customer customer) {
        this.id = id;
        this.vnp_TxnRef = vnp_TxnRef;
        this.amount = amount;
        this.orderInfo = orderInfo;
        this.bankCode = bankCode;
        this.payDate = payDate;
        this.transactionNo = transactionNo;
        this.cardType = cardType;
        this.customer = customer;
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

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
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

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<InvoiceDetail> getInvoiceDetailList() {
        return invoiceDetailList;
    }

    public void setInvoiceDetailList(List<InvoiceDetail> invoiceDetailList) {
        this.invoiceDetailList = invoiceDetailList;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", vnp_TxnRef='" + vnp_TxnRef + '\'' +
                ", amount=" + amount +
                ", orderInfo='" + orderInfo + '\'' +
                ", bankCode='" + bankCode + '\'' +
                ", payDate='" + payDate + '\'' +
                ", transactionNo='" + transactionNo + '\'' +
                ", cardType='" + cardType + '\'' +
                ", customer=" + customer +
                ", invoiceDetailList=" + invoiceDetailList +
                '}';
    }
}
