package com.example.md5_phone_store_management.model;

import jakarta.persistence.*;

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

    public Invoice() {
    }

    public Invoice(Long id, String vnp_TxnRef, Long amount, String orderInfo, String bankCode, String payDate, String transactionNo, String cardType) {
        this.id = id;
        this.vnp_TxnRef = vnp_TxnRef;
        this.amount = amount;
        this.orderInfo = orderInfo;
        this.bankCode = bankCode;
        this.payDate = payDate;
        this.transactionNo = transactionNo;
        this.cardType = cardType;
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
}
