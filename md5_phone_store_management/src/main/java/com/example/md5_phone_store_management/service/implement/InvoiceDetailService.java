package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.InvoiceDetail;
import com.example.md5_phone_store_management.repository.IInvoiceDetailRepository;
import com.example.md5_phone_store_management.repository.InvoiceRepository;
import com.example.md5_phone_store_management.service.IInvoiceDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class InvoiceDetailService implements IInvoiceDetailService {
    @Autowired
    private IInvoiceDetailRepository invoiceDetailRepository;

    @Override
    public Page<InvoiceDetail> findAll(Pageable pageable) {
        return invoiceDetailRepository.findAll(pageable);
    }



    @Override
    public List<InvoiceDetail> findInvoiceDetailById(Long invoiceId) {
        return  invoiceDetailRepository.findInvoiceDetailsByInvoiceId(invoiceId);
    }

    @Override
    public Page<InvoiceDetail> sortByCustomerFullName(Pageable pageable) {
        return invoiceDetailRepository.sortByCustomerFullName(pageable);
    }

    @Override
    public Page<InvoiceDetail> sortByProductName(Pageable pageable) {
        return invoiceDetailRepository.sortByProductName(pageable);
    }

    @Override
    public Page<InvoiceDetail> sortByPrice(Pageable pageable) {
        return invoiceDetailRepository.sortByPrice(pageable);
    }
}
