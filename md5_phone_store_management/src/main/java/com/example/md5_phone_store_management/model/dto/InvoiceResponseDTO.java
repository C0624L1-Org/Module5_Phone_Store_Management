package com.example.md5_phone_store_management.model.dto;

public class InvoiceResponseDTO {

    private String transactionId;
    private Long amount;
    private String orderInfo;
    private String payDate;

    public InvoiceResponseDTO() {
    }

    public InvoiceResponseDTO(String transactionId, Long amount, String orderInfo, String payDate) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.orderInfo = orderInfo;
        this.payDate = payDate;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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
}
